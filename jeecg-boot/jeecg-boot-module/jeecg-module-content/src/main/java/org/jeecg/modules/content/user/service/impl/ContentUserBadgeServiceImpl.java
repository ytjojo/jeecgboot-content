package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.UUIDGenerator;
import org.jeecg.modules.content.user.constant.ContentUserBadgeConstant;
import org.jeecg.modules.content.user.constant.ContentUserCacheConstant;
import org.jeecg.modules.content.user.dto.ContentUserBadgeProgressDTO;
import org.jeecg.modules.content.user.entity.ContentUserAuditLog;
import org.jeecg.modules.content.user.entity.ContentUserBadgeDefinition;
import org.jeecg.modules.content.user.entity.ContentUserBadgeGrant;
import org.jeecg.modules.content.user.mapper.ContentUserAuditLogMapper;
import org.jeecg.modules.content.user.mapper.ContentUserBadgeDefinitionMapper;
import org.jeecg.modules.content.user.mapper.ContentUserBadgeGrantMapper;
import org.jeecg.modules.content.user.service.IContentUserBadgeService;
import org.jeecg.modules.content.user.vo.ContentUserBadgeVO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 内容社区用户勋章服务实现。
 */
@Service
public class ContentUserBadgeServiceImpl implements IContentUserBadgeService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int MAX_DISPLAY_TEXT_LENGTH = 255;
    private static final String RULE_FIELD_METRIC = "metric";
    private static final String RULE_FIELD_TARGET = "target";

    @Resource
    private ContentUserBadgeDefinitionMapper badgeDefinitionMapper;

    @Resource
    private ContentUserBadgeGrantMapper badgeGrantMapper;

    @Resource
    private ContentUserAuditLogMapper auditLogMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 按分类返回可展示勋章目录，并填充用户进度和授予状态。
     */
    @Override
    public Map<String, List<ContentUserBadgeVO>> listBadgeCatalog(String userId) {
        List<ContentUserBadgeDefinition> definitions = loadValidDefinitions();
        Map<String, ContentUserBadgeGrant> grants = loadUserGrants(userId);
        Map<String, List<ContentUserBadgeVO>> result = new LinkedHashMap<>();
        for (String category : ContentUserBadgeConstant.SUPPORTED_CATEGORIES) {
            result.put(category, new ArrayList<>());
        }
        for (ContentUserBadgeDefinition definition : definitions) {
            ContentUserBadgeVO vo = toVO(definition, grants.get(definition.getBadgeCode()), userId);
            result.computeIfAbsent(definition.getCategory(), key -> new ArrayList<>()).add(vo);
        }
        return result;
    }

    /**
     * 返回单个勋章详情，未获得时展示进度，已获得时展示授予元数据。
     */
    @Override
    public ContentUserBadgeVO getBadgeDetail(String userId, String badgeCode) {
        if (!StringUtils.hasText(badgeCode)) {
            throw new JeecgBootException("勋章编码不能为空");
        }
        ContentUserBadgeDefinition definition = loadValidDefinitions().stream()
            .filter(item -> badgeCode.equals(item.getBadgeCode()))
            .findFirst()
            .orElseThrow(() -> new JeecgBootException("勋章不存在或不可用"));
        return toVO(definition, loadUserGrants(userId).get(badgeCode), userId);
    }

    /**
     * 根据规则配置和缓存值计算勋章进度。
     */
    @Override
    public ContentUserBadgeProgressDTO calculateProgress(String userId, String badgeCode) {
        ContentUserBadgeDefinition definition = loadDefinitionByCode(badgeCode);
        BadgeRule rule = parseRule(definition);
        int current = readProgress(userId, badgeCode);
        return toProgress(definition.getBadgeCode(), rule, current);
    }

    /**
     * 记录某类行为进度，达到自动发放规则时幂等授予勋章。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentUserBadgeGrant autoGrant(String userId, String metric, int currentProgress, String grantSource) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(metric) || currentProgress < 0) {
            throw new JeecgBootException("勋章进度参数不合法");
        }
        for (ContentUserBadgeDefinition definition : loadValidDefinitions()) {
            BadgeRule rule = parseRule(definition);
            if (!Boolean.TRUE.equals(definition.getAutoGrant()) || !metric.equals(rule.metric())) {
                continue;
            }
            writeProgress(userId, definition.getBadgeCode(), currentProgress);
            if (currentProgress < rule.target()) {
                continue;
            }
            ContentUserBadgeGrant existing = loadGrant(userId, definition.getBadgeCode());
            if (existing != null) {
                return existing;
            }
            ContentUserBadgeGrant grant = new ContentUserBadgeGrant()
                .setUserId(userId)
                .setBadgeDefinitionId(definition.getId())
                .setBadgeCode(definition.getBadgeCode())
                .setGrantSource(StringUtils.hasText(grantSource) ? grantSource : "AUTO")
                .setGrantReason(definition.getConditionDescription())
                .setDisplaying(Boolean.FALSE)
                .setStatus(ContentUserBadgeConstant.STATUS_ACTIVE)
                .setExpiresAt(resolveExpiresAt(definition));
            grant.setId(UUIDGenerator.generate());
            badgeGrantMapper.insert(grant);
            return grant;
        }
        return null;
    }

    /**
     * 保存用户佩戴勋章，最多允许 5 个有效且归属自己的授予记录。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ContentUserBadgeVO> saveWornBadges(String userId, List<String> grantIds) {
        validateWornRequest(userId, grantIds);
        List<ContentUserBadgeGrant> existing = loadUserGrantList(userId);
        Map<String, ContentUserBadgeGrant> byId = existing.stream()
            .filter(item -> StringUtils.hasText(item.getId()))
            .collect(Collectors.toMap(ContentUserBadgeGrant::getId, Function.identity(), (a, b) -> a));
        for (String grantId : grantIds) {
            ContentUserBadgeGrant grant = byId.get(grantId);
            if (!isWearableGrant(grant, new Date())) {
                throw new JeecgBootException("存在不可佩戴的勋章");
            }
        }
        for (ContentUserBadgeGrant grant : existing) {
            int order = grantIds.indexOf(grant.getId());
            grant.setDisplaying(order >= 0);
            grant.setDisplayOrder(order >= 0 ? order + 1 : null);
            badgeGrantMapper.updateById(grant);
        }
        return listWornBadges(userId);
    }

    /**
     * 返回主页、帖子和评论展示面使用的佩戴勋章。
     */
    @Override
    public List<ContentUserBadgeVO> listWornBadges(String userId) {
        Map<String, ContentUserBadgeDefinition> definitions = loadValidDefinitions().stream()
            .collect(Collectors.toMap(ContentUserBadgeDefinition::getBadgeCode, Function.identity(), (a, b) -> a));
        return loadUserGrantList(userId).stream()
            .filter(item -> Boolean.TRUE.equals(item.getDisplaying()))
            .filter(item -> isWearableGrant(item, new Date()))
            .sorted(Comparator.comparing(item -> item.getDisplayOrder() == null ? Integer.MAX_VALUE : item.getDisplayOrder()))
            .map(item -> toVO(definitions.get(item.getBadgeCode()), item, userId))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * 将到期勋章标记为过期，并同步取消佩戴展示。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int expireBadges(Date now) {
        Date current = now == null ? new Date() : now;
        int expired = 0;
        List<ContentUserBadgeGrant> grants = badgeGrantMapper.selectList(
            Wrappers.<ContentUserBadgeGrant>lambdaQuery()
                .eq(ContentUserBadgeGrant::getStatus, ContentUserBadgeConstant.STATUS_ACTIVE)
        );
        for (ContentUserBadgeGrant grant : emptyIfNull(grants)) {
            if (grant.getExpiresAt() == null || grant.getExpiresAt().after(current)) {
                continue;
            }
            grant.setStatus(ContentUserBadgeConstant.STATUS_EXPIRED);
            grant.setDisplaying(Boolean.FALSE);
            grant.setDisplayOrder(null);
            badgeGrantMapper.updateById(grant);
            expired++;
        }
        return expired;
    }

    /**
     * 管理员违规回收用户勋章，写入审计日志并取消佩戴。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContentUserBadgeGrant recycleBadge(String grantId, String operatorUserId, String reason) {
        if (!StringUtils.hasText(grantId) || !StringUtils.hasText(operatorUserId) || !StringUtils.hasText(reason)) {
            throw new JeecgBootException("勋章回收参数不能为空");
        }
        if (reason.length() > MAX_DISPLAY_TEXT_LENGTH) {
            throw new JeecgBootException("勋章回收原因过长");
        }
        ContentUserBadgeGrant grant = badgeGrantMapper.selectById(grantId);
        if (grant == null) {
            throw new JeecgBootException("勋章授予记录不存在");
        }
        grant.setStatus(ContentUserBadgeConstant.STATUS_RECYCLED);
        grant.setDisplaying(Boolean.FALSE);
        grant.setDisplayOrder(null);
        grant.setRecycledBy(operatorUserId);
        grant.setRecycleReason(reason);
        grant.setRecycledAt(new Date());
        badgeGrantMapper.updateById(grant);
        if (auditLogMapper != null) {
            auditLogMapper.insert(ContentUserAuditLog.badgeRecycled(grant.getUserId(), operatorUserId, grant.getBadgeCode(), reason));
        }
        return grant;
    }

    private List<ContentUserBadgeDefinition> loadValidDefinitions() {
        List<ContentUserBadgeDefinition> definitions = badgeDefinitionMapper.selectList(
            Wrappers.<ContentUserBadgeDefinition>lambdaQuery()
                .eq(ContentUserBadgeDefinition::getEnabled, Boolean.TRUE)
        );
        Map<String, Long> codeCount = emptyIfNull(definitions).stream()
            .filter(item -> StringUtils.hasText(item.getBadgeCode()))
            .collect(Collectors.groupingBy(ContentUserBadgeDefinition::getBadgeCode, Collectors.counting()));
        return emptyIfNull(definitions).stream()
            .filter(item -> isValidDefinition(item, codeCount))
            .sorted(Comparator.comparing((ContentUserBadgeDefinition item) -> item.getSortOrder() == null ? 0 : item.getSortOrder())
                .thenComparing(ContentUserBadgeDefinition::getBadgeCode))
            .collect(Collectors.toList());
    }

    private ContentUserBadgeDefinition loadDefinitionByCode(String badgeCode) {
        if (!StringUtils.hasText(badgeCode)) {
            throw new JeecgBootException("勋章编码不能为空");
        }
        return loadValidDefinitions().stream()
            .filter(item -> badgeCode.equals(item.getBadgeCode()))
            .findFirst()
            .orElseThrow(() -> new JeecgBootException("勋章不存在或不可用"));
    }

    private boolean isValidDefinition(ContentUserBadgeDefinition item, Map<String, Long> codeCount) {
        if (item == null || !Boolean.TRUE.equals(item.getEnabled())) {
            return false;
        }
        if (!StringUtils.hasText(item.getBadgeCode()) || !StringUtils.hasText(item.getBadgeName())) {
            return false;
        }
        if (codeCount.getOrDefault(item.getBadgeCode(), 0L) > 1) {
            return false;
        }
        if (!ContentUserBadgeConstant.SUPPORTED_CATEGORIES.contains(item.getCategory())) {
            return false;
        }
        if (item.getValidDays() != null && item.getValidDays() < 0) {
            return false;
        }
        if (overLength(item.getBadgeName()) || overLength(item.getIconUrl()) || overLength(item.getEffectKey())
            || overLength(item.getConditionDescription())) {
            return false;
        }
        return parseRuleOrNull(item) != null;
    }

    private boolean overLength(String value) {
        return value != null && value.length() > MAX_DISPLAY_TEXT_LENGTH;
    }

    private Map<String, ContentUserBadgeGrant> loadUserGrants(String userId) {
        return loadUserGrantList(userId).stream()
            .filter(item -> StringUtils.hasText(item.getBadgeCode()))
            .collect(Collectors.toMap(ContentUserBadgeGrant::getBadgeCode, Function.identity(), (a, b) -> a));
    }

    private List<ContentUserBadgeGrant> loadUserGrantList(String userId) {
        if (!StringUtils.hasText(userId)) {
            return List.of();
        }
        return emptyIfNull(badgeGrantMapper.selectList(
            Wrappers.<ContentUserBadgeGrant>lambdaQuery()
                .eq(ContentUserBadgeGrant::getUserId, userId)
        ));
    }

    private ContentUserBadgeGrant loadGrant(String userId, String badgeCode) {
        return badgeGrantMapper.selectOne(
            Wrappers.<ContentUserBadgeGrant>lambdaQuery()
                .eq(ContentUserBadgeGrant::getUserId, userId)
                .eq(ContentUserBadgeGrant::getBadgeCode, badgeCode)
        );
    }

    private ContentUserBadgeVO toVO(ContentUserBadgeDefinition definition, ContentUserBadgeGrant grant, String userId) {
        if (definition == null) {
            return null;
        }
        return new ContentUserBadgeVO()
            .setBadgeDefinitionId(definition.getId())
            .setBadgeGrantId(grant == null ? null : grant.getId())
            .setBadgeCode(definition.getBadgeCode())
            .setBadgeName(definition.getBadgeName())
            .setBadgeType(definition.getBadgeType())
            .setCategory(definition.getCategory())
            .setIconUrl(definition.getIconUrl())
            .setEffectKey(definition.getEffectKey())
            .setConditionDescription(definition.getConditionDescription())
            .setGranted(grant != null)
            .setDisplaying(grant != null && Boolean.TRUE.equals(grant.getDisplaying()))
            .setGrantReason(grant == null ? null : grant.getGrantReason())
            .setStatus(grant == null ? null : grant.getStatus())
            .setGrantTime(grant == null ? null : grant.getCreateTime())
            .setExpiresAt(grant == null ? null : grant.getExpiresAt())
            .setProgress(toProgress(definition.getBadgeCode(), parseRule(definition), readProgress(userId, definition.getBadgeCode())));
    }

    private ContentUserBadgeProgressDTO toProgress(String badgeCode, BadgeRule rule, int current) {
        int safeCurrent = Math.max(current, 0);
        int target = Math.max(rule.target(), 0);
        return new ContentUserBadgeProgressDTO()
            .setBadgeCode(badgeCode)
            .setMetric(rule.metric())
            .setCurrentProgress(safeCurrent)
            .setTargetProgress(target)
            .setRemainingRequirement(Math.max(target - safeCurrent, 0));
    }

    private BadgeRule parseRule(ContentUserBadgeDefinition definition) {
        BadgeRule rule = parseRuleOrNull(definition);
        if (rule == null) {
            throw new JeecgBootException("勋章规则配置不合法");
        }
        return rule;
    }

    private BadgeRule parseRuleOrNull(ContentUserBadgeDefinition definition) {
        if (definition == null || !StringUtils.hasText(definition.getRuleConfigJson())) {
            return null;
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(definition.getRuleConfigJson());
            String metric = node.path(RULE_FIELD_METRIC).asText(null);
            int target = node.path(RULE_FIELD_TARGET).asInt(-1);
            if (!StringUtils.hasText(metric) || target < 0) {
                return null;
            }
            return new BadgeRule(metric, target);
        } catch (Exception e) {
            return null;
        }
    }

    private int readProgress(String userId, String badgeCode) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(badgeCode) || redisTemplate == null) {
            return 0;
        }
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        if (operations == null) {
            return 0;
        }
        Object value = operations.get(progressKey(userId, badgeCode));
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && StringUtils.hasText(text)) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private void writeProgress(String userId, String badgeCode, int currentProgress) {
        if (redisTemplate == null) {
            return;
        }
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        if (operations != null) {
            operations.set(progressKey(userId, badgeCode), currentProgress, 7, TimeUnit.DAYS);
        }
    }

    private String progressKey(String userId, String badgeCode) {
        return ContentUserCacheConstant.BADGE_PROGRESS_PREFIX + userId + ":" + badgeCode;
    }

    private Date resolveExpiresAt(ContentUserBadgeDefinition definition) {
        if (definition.getValidDays() == null || definition.getValidDays() <= 0) {
            return null;
        }
        return new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(definition.getValidDays()));
    }

    private void validateWornRequest(String userId, List<String> grantIds) {
        if (!StringUtils.hasText(userId) || grantIds == null || grantIds.isEmpty()) {
            throw new JeecgBootException("佩戴勋章不能为空");
        }
        if (grantIds.size() > ContentUserBadgeConstant.MAX_WORN_BADGE_COUNT) {
            throw new JeecgBootException("最多佩戴5个勋章");
        }
        Set<String> uniqueIds = new LinkedHashSet<>(grantIds);
        if (uniqueIds.size() != grantIds.size() || uniqueIds.stream().anyMatch(id -> !StringUtils.hasText(id))) {
            throw new JeecgBootException("佩戴勋章参数不合法");
        }
    }

    private boolean isWearableGrant(ContentUserBadgeGrant grant, Date now) {
        if (grant == null || !ContentUserBadgeConstant.STATUS_ACTIVE.equals(grant.getStatus())) {
            return false;
        }
        return grant.getExpiresAt() == null || grant.getExpiresAt().after(now);
    }

    private <T> List<T> emptyIfNull(List<T> list) {
        return list == null ? List.of() : list;
    }

    private record BadgeRule(String metric, int target) {
    }
}
