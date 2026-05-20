package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.user.entity.ContentUserLevelConfig;
import org.jeecg.modules.content.user.mapper.ContentUserLevelConfigMapper;
import org.jeecg.modules.content.user.service.IContentUserLevelConfigService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 内容社区用户等级配置服务实现。
 */
@Service
public class ContentUserLevelConfigServiceImpl
    extends ServiceImpl<ContentUserLevelConfigMapper, ContentUserLevelConfig>
    implements IContentUserLevelConfigService {

    private static final int DEFAULT_LEVEL = 1;
    private static final int DEFAULT_LEVEL_STEP = 100;

    /**
     * 加载并校验运营配置的等级阈值，避免坏配置影响用户等级。
     */
    @Override
    public List<ContentUserLevelConfig> listValidEnabledLevels() {
        List<ContentUserLevelConfig> configs = baseMapper == null ? List.of() : baseMapper.selectList(
            Wrappers.<ContentUserLevelConfig>lambdaQuery()
                .eq(ContentUserLevelConfig::getEnabled, Boolean.TRUE)
                .orderByAsc(ContentUserLevelConfig::getLevel)
        );
        List<ContentUserLevelConfig> sorted = configs == null ? List.of() : configs.stream()
            .filter(item -> Boolean.TRUE.equals(item.getEnabled()))
            .sorted(Comparator.comparing(ContentUserLevelConfig::getLevel,
                Comparator.nullsFirst(Integer::compareTo)))
            .toList();
        validateConfigs(sorted);
        return sorted;
    }

    /**
     * 根据成长值匹配不超过当前成长值的最高等级。
     */
    @Override
    public int calculateLevel(Integer growthValue) {
        int safeGrowth = Math.max(growthValue == null ? 0 : growthValue, 0);
        List<ContentUserLevelConfig> configs = listValidEnabledLevels();
        if (configs.isEmpty()) {
            return Math.max(DEFAULT_LEVEL, safeGrowth / DEFAULT_LEVEL_STEP + DEFAULT_LEVEL);
        }
        int matchedLevel = DEFAULT_LEVEL;
        for (ContentUserLevelConfig config : configs) {
            if (safeGrowth >= config.getGrowthThreshold()) {
                matchedLevel = config.getLevel();
            }
        }
        return Math.max(DEFAULT_LEVEL, matchedLevel);
    }

    private void validateConfigs(List<ContentUserLevelConfig> configs) {
        Set<Integer> levels = new HashSet<>();
        Integer previousThreshold = null;
        for (ContentUserLevelConfig config : configs) {
            if (config.getLevel() == null || config.getLevel() < DEFAULT_LEVEL) {
                throw new JeecgBootException("等级配置等级不合法");
            }
            if (!levels.add(config.getLevel())) {
                throw new JeecgBootException("等级配置等级重复");
            }
            if (!StringUtils.hasText(config.getLevelName()) || config.getLevelName().length() > 64
                || (StringUtils.hasText(config.getBadgeStyleKey()) && config.getBadgeStyleKey().length() > 64)) {
                throw new JeecgBootException("等级配置展示字段不合法");
            }
            if (config.getGrowthThreshold() == null || config.getGrowthThreshold() < 0) {
                throw new JeecgBootException("等级配置成长阈值不合法");
            }
            if (previousThreshold != null && config.getGrowthThreshold() <= previousThreshold) {
                throw new JeecgBootException("等级配置成长阈值必须递增");
            }
            previousThreshold = config.getGrowthThreshold();
        }
    }
}
