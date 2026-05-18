package org.jeecg.modules.content.user.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.constant.ContentUserBadgeConstant;
import org.jeecg.modules.content.user.dto.ContentUserBadgeProgressDTO;
import org.jeecg.modules.content.user.entity.ContentUserAuditLog;
import org.jeecg.modules.content.user.entity.ContentUserBadgeDefinition;
import org.jeecg.modules.content.user.entity.ContentUserBadgeGrant;
import org.jeecg.modules.content.user.mapper.ContentUserAuditLogMapper;
import org.jeecg.modules.content.user.mapper.ContentUserBadgeDefinitionMapper;
import org.jeecg.modules.content.user.mapper.ContentUserBadgeGrantMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserBadgeServiceImpl;
import org.jeecg.modules.content.user.vo.ContentUserBadgeVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 内容社区用户勋章业务服务测试。
 */
class ContentUserBadgeServiceTest {

    private ContentUserBadgeDefinitionMapper badgeDefinitionMapper;

    private ContentUserBadgeGrantMapper badgeGrantMapper;

    private ContentUserAuditLogMapper auditLogMapper;

    private FakeRedisTemplate redisTemplate;

    private final List<ContentUserBadgeDefinition> definitions = new ArrayList<>();
    private final List<ContentUserBadgeGrant> grants = new ArrayList<>();
    private final List<ContentUserBadgeGrant> updatedGrants = new ArrayList<>();
    private final List<ContentUserAuditLog> auditLogs = new ArrayList<>();

    private ContentUserBadgeServiceImpl badgeService;

    @BeforeEach
    void setUp() {
        badgeDefinitionMapper = definitionMapper();
        badgeGrantMapper = grantMapper();
        auditLogMapper = auditLogMapper();
        redisTemplate = new FakeRedisTemplate();
        badgeService = new ContentUserBadgeServiceImpl();
        ReflectionTestUtils.setField(badgeService, "badgeDefinitionMapper", badgeDefinitionMapper);
        ReflectionTestUtils.setField(badgeService, "badgeGrantMapper", badgeGrantMapper);
        ReflectionTestUtils.setField(badgeService, "auditLogMapper", auditLogMapper);
        ReflectionTestUtils.setField(badgeService, "redisTemplate", redisTemplate);
    }

    @Test
    void shouldGroupEnabledBadgeCatalogByCategoryAndHideInvalidDefinitions() {
        definitions.addAll(List.of(
            definition("d1", "PUBLISH_3", ContentUserBadgeConstant.CATEGORY_ACHIEVEMENT, "CONTENT_PUBLISH", 3),
            definition("d2", "FOLLOW_10", ContentUserBadgeConstant.CATEGORY_RELATIONSHIP, "FOLLOWER_COUNT", 10),
            definition("d3", "DISABLED", ContentUserBadgeConstant.CATEGORY_ACTIVITY, "ACTIVITY_JOIN", 1).setEnabled(Boolean.FALSE),
            definition("d4", "", ContentUserBadgeConstant.CATEGORY_ACTIVITY, "ACTIVITY_JOIN", 1),
            definition("d5", "BAD_CATEGORY", "BAD", "ACTIVITY_JOIN", 1),
            definition("d6", "BAD_RULE", ContentUserBadgeConstant.CATEGORY_ACTIVITY, "ACTIVITY_JOIN", 1).setRuleConfigJson("{bad")
        ));

        Map<String, List<ContentUserBadgeVO>> result = badgeService.listBadgeCatalog("u1");

        assertThat(result.get(ContentUserBadgeConstant.CATEGORY_ACHIEVEMENT)).extracting(ContentUserBadgeVO::getBadgeCode)
            .containsExactly("PUBLISH_3");
        assertThat(result.get(ContentUserBadgeConstant.CATEGORY_RELATIONSHIP)).extracting(ContentUserBadgeVO::getBadgeCode)
            .containsExactly("FOLLOW_10");
        assertThat(result.values().stream().flatMap(List::stream).map(ContentUserBadgeVO::getBadgeCode))
            .doesNotContain("DISABLED", "BAD_CATEGORY", "BAD_RULE");
    }

    @Test
    void shouldHideDuplicateBadgeCodeBecauseCatalogMustStayDeterministic() {
        definitions.addAll(List.of(
            definition("d1", "DUP", ContentUserBadgeConstant.CATEGORY_ACTIVITY, "ACTIVITY_JOIN", 1),
            definition("d2", "DUP", ContentUserBadgeConstant.CATEGORY_ACTIVITY, "ACTIVITY_JOIN", 1)
        ));

        Map<String, List<ContentUserBadgeVO>> result = badgeService.listBadgeCatalog("u1");

        assertThat(result.values().stream().flatMap(List::stream)).isEmpty();
    }

    @Test
    void shouldShowProgressForUngrantBadgeAndGrantMetadataForGrantedBadge() {
        ContentUserBadgeDefinition publish = definition("d1", "PUBLISH_3", ContentUserBadgeConstant.CATEGORY_ACHIEVEMENT, "CONTENT_PUBLISH", 3);
        ContentUserBadgeGrant grant = grant("g1", "u1", "PUBLISH_3").setGrantReason("发布达人");
        grant.setCreateTime(new Date(1000L));
        definitions.add(publish);
        grants.add(grant);
        redisTemplate.values.put("content:growth:badge_progress:u1:PUBLISH_3", "2");

        ContentUserBadgeVO result = badgeService.getBadgeDetail("u1", "PUBLISH_3");

        assertThat(result.getGranted()).isTrue();
        assertThat(result.getGrantReason()).isEqualTo("发布达人");
        assertThat(result.getGrantTime()).isEqualTo(new Date(1000L));
        assertThat(result.getProgress().getCurrentProgress()).isEqualTo(2);
        assertThat(result.getProgress().getRemainingRequirement()).isEqualTo(1);
    }

    @Test
    void shouldCalculatePublishRelationAndActivityProgressFromCache() {
        definitions.addAll(List.of(
            definition("d1", "PUBLISH_3", ContentUserBadgeConstant.CATEGORY_ACHIEVEMENT, "CONTENT_PUBLISH", 3),
            definition("d2", "FOLLOW_10", ContentUserBadgeConstant.CATEGORY_RELATIONSHIP, "FOLLOWER_COUNT", 10),
            definition("d3", "ACTIVITY_1", ContentUserBadgeConstant.CATEGORY_ACTIVITY, "ACTIVITY_JOIN", 1)
        ));
        redisTemplate.values.put("content:growth:badge_progress:u1:PUBLISH_3", 3);
        redisTemplate.values.put("content:growth:badge_progress:u1:FOLLOW_10", 7);
        redisTemplate.values.put("content:growth:badge_progress:u1:ACTIVITY_1", 0);

        ContentUserBadgeProgressDTO publish = badgeService.calculateProgress("u1", "PUBLISH_3");
        ContentUserBadgeProgressDTO relation = badgeService.calculateProgress("u1", "FOLLOW_10");
        ContentUserBadgeProgressDTO activity = badgeService.calculateProgress("u1", "ACTIVITY_1");

        assertThat(publish.getRemainingRequirement()).isZero();
        assertThat(relation.getRemainingRequirement()).isEqualTo(3);
        assertThat(activity.getTargetProgress()).isEqualTo(1);
    }

    @Test
    void shouldAutoGrantOnceAndSetExpirationWhenProgressReachesRuleTarget() {
        ContentUserBadgeDefinition definition = definition("d1", "PUBLISH_3", ContentUserBadgeConstant.CATEGORY_ACHIEVEMENT, "CONTENT_PUBLISH", 3)
            .setValidDays(7)
            .setAutoGrant(Boolean.TRUE);
        definitions.add(definition);

        ContentUserBadgeGrant first = badgeService.autoGrant("u1", "CONTENT_PUBLISH", 3, "POST");
        ContentUserBadgeGrant second = badgeService.autoGrant("u1", "CONTENT_PUBLISH", 4, "POST");

        assertThat(first.getBadgeCode()).isEqualTo("PUBLISH_3");
        assertThat(first.getExpiresAt()).isAfter(new Date());
        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(grants).hasSize(1);
        assertThat(redisTemplate.values.get("content:growth:badge_progress:u1:PUBLISH_3")).isEqualTo(4);
    }

    @Test
    void shouldSaveUpToFiveWearableBadgesAndRejectInvalidValues() {
        ContentUserBadgeDefinition definition = definition("d1", "PUBLISH_3", ContentUserBadgeConstant.CATEGORY_ACHIEVEMENT, "CONTENT_PUBLISH", 3);
        ContentUserBadgeGrant grant = grant("g1", "u1", "PUBLISH_3");
        definitions.add(definition);
        grants.add(grant);

        List<ContentUserBadgeVO> worn = badgeService.saveWornBadges("u1", List.of("g1"));

        assertThat(worn).extracting(ContentUserBadgeVO::getBadgeCode).containsExactly("PUBLISH_3");
        assertThat(grant.getDisplaying()).isTrue();
        assertThat(grant.getDisplayOrder()).isEqualTo(1);
        assertThatThrownBy(() -> badgeService.saveWornBadges("u1", List.of()))
            .isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> badgeService.saveWornBadges("u1", List.of("g1", "g1")))
            .isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> badgeService.saveWornBadges("u1", List.of("a", "b", "c", "d", "e", "f")))
            .isInstanceOf(JeecgBootException.class);
    }

    @Test
    void shouldRejectExpiredRecycledAndForeignBadgeWhenSavingWornBadges() {
        grants.addAll(List.of(
            grant("g1", "u1", "PUBLISH_3").setStatus(ContentUserBadgeConstant.STATUS_EXPIRED),
            grant("g2", "u1", "FOLLOW_10").setStatus(ContentUserBadgeConstant.STATUS_RECYCLED)
        ));

        assertThatThrownBy(() -> badgeService.saveWornBadges("u1", List.of("g1")))
            .isInstanceOf(JeecgBootException.class);
        assertThatThrownBy(() -> badgeService.saveWornBadges("u1", List.of("unknown")))
            .isInstanceOf(JeecgBootException.class);
    }

    @Test
    void shouldExpireBadgesAndRemoveThemFromWearingList() {
        ContentUserBadgeGrant expired = grant("g1", "u1", "PUBLISH_3")
            .setDisplaying(Boolean.TRUE)
            .setDisplayOrder(1)
            .setExpiresAt(new Date(1000L));
        ContentUserBadgeGrant active = grant("g2", "u1", "FOLLOW_10").setExpiresAt(new Date(3000L));
        grants.addAll(List.of(expired, active));

        int count = badgeService.expireBadges(new Date(2000L));

        assertThat(count).isEqualTo(1);
        assertThat(expired.getStatus()).isEqualTo(ContentUserBadgeConstant.STATUS_EXPIRED);
        assertThat(expired.getDisplaying()).isFalse();
        assertThat(updatedGrants).contains(expired).doesNotContain(active);
    }

    @Test
    void shouldRecycleBadgeWithAuditNotification() {
        ContentUserBadgeGrant grant = grant("g1", "u1", "PUBLISH_3").setDisplaying(Boolean.TRUE);
        grants.add(grant);

        ContentUserBadgeGrant result = badgeService.recycleBadge("g1", "admin", "违规获得");

        assertThat(result.getStatus()).isEqualTo(ContentUserBadgeConstant.STATUS_RECYCLED);
        assertThat(result.getDisplaying()).isFalse();
        assertThat(result.getRecycledBy()).isEqualTo("admin");
        assertThat(auditLogs).anySatisfy(it -> {
            assertThat(it.getEventType()).isEqualTo("USER_BADGE_RECYCLED");
            assertThat(it.getExtraDataJson()).contains("\"notification\":true");
        });
    }

    private ContentUserBadgeDefinitionMapper definitionMapper() {
        return (ContentUserBadgeDefinitionMapper) Proxy.newProxyInstance(
            ContentUserBadgeDefinitionMapper.class.getClassLoader(),
            new Class<?>[]{ContentUserBadgeDefinitionMapper.class},
            (proxy, method, args) -> {
                if ("selectList".equals(method.getName())) {
                    return new ArrayList<>(definitions);
                }
                return defaultValue(method.getReturnType());
            });
    }

    private ContentUserBadgeGrantMapper grantMapper() {
        return (ContentUserBadgeGrantMapper) Proxy.newProxyInstance(
            ContentUserBadgeGrantMapper.class.getClassLoader(),
            new Class<?>[]{ContentUserBadgeGrantMapper.class},
            (proxy, method, args) -> {
                if ("selectList".equals(method.getName())) {
                    return new ArrayList<>(grants);
                }
                if ("selectOne".equals(method.getName())) {
                    return grants.isEmpty() ? null : grants.get(0);
                }
                if ("selectById".equals(method.getName())) {
                    String id = String.valueOf(args[0]);
                    return grants.stream().filter(item -> id.equals(item.getId())).findFirst().orElse(null);
                }
                if ("insert".equals(method.getName())) {
                    grants.add((ContentUserBadgeGrant) args[0]);
                    return 1;
                }
                if ("updateById".equals(method.getName())) {
                    updatedGrants.add((ContentUserBadgeGrant) args[0]);
                    return 1;
                }
                return defaultValue(method.getReturnType());
            });
    }

    private ContentUserAuditLogMapper auditLogMapper() {
        return (ContentUserAuditLogMapper) Proxy.newProxyInstance(
            ContentUserAuditLogMapper.class.getClassLoader(),
            new Class<?>[]{ContentUserAuditLogMapper.class},
            (proxy, method, args) -> {
                if ("insert".equals(method.getName())) {
                    auditLogs.add((ContentUserAuditLog) args[0]);
                    return 1;
                }
                return defaultValue(method.getReturnType());
            });
    }

    private Object defaultValue(Class<?> returnType) {
        if (returnType == Integer.TYPE) {
            return 0;
        }
        if (returnType == Boolean.TYPE) {
            return false;
        }
        return null;
    }

    private ContentUserBadgeDefinition definition(String id, String code, String category, String metric, int target) {
        ContentUserBadgeDefinition definition = new ContentUserBadgeDefinition()
            .setBadgeCode(code)
            .setBadgeName(code + "勋章")
            .setBadgeType("SYSTEM")
            .setCategory(category)
            .setIconUrl("https://example.com/" + code + ".png")
            .setEffectKey("spark")
            .setConditionDescription("达到" + target)
            .setRuleConfigJson("{\"metric\":\"" + metric + "\",\"target\":" + target + "}")
            .setSortOrder(1)
            .setAutoGrant(Boolean.TRUE)
            .setEnabled(Boolean.TRUE);
        definition.setId(id);
        return definition;
    }

    private ContentUserBadgeGrant grant(String id, String userId, String code) {
        ContentUserBadgeGrant grant = new ContentUserBadgeGrant()
            .setUserId(userId)
            .setBadgeDefinitionId("d-" + code)
            .setBadgeCode(code)
            .setGrantSource("AUTO")
            .setGrantReason("自动授予")
            .setDisplaying(Boolean.FALSE)
            .setDisplayOrder(null)
            .setStatus(ContentUserBadgeConstant.STATUS_ACTIVE);
        grant.setId(id);
        return grant;
    }

    private static final class FakeRedisTemplate extends RedisTemplate<String, Object> {

        private final Map<String, Object> values = new HashMap<>();

        @Override
        @SuppressWarnings("unchecked")
        public ValueOperations<String, Object> opsForValue() {
            return (ValueOperations<String, Object>) Proxy.newProxyInstance(
                ValueOperations.class.getClassLoader(),
                new Class<?>[]{ValueOperations.class},
                (proxy, method, args) -> {
                    if ("get".equals(method.getName())) {
                        return values.get(String.valueOf(args[0]));
                    }
                    if ("set".equals(method.getName())) {
                        values.put(String.valueOf(args[0]), args[1]);
                        return null;
                    }
                    return null;
                });
        }
    }
}
