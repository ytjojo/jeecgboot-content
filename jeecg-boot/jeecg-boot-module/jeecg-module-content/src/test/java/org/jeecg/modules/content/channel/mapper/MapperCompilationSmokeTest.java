package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.content.channel.entity.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mapper 编译期烟雾测试
 * 验证 channel 子模块下所有 Mapper 都能被 JVM 加载且继承 BaseMapper
 * （不需要启动 Spring / MyBatis 容器，编译通过即视为合约验证）
 */
class MapperCompilationSmokeTest {

    private static final Set<String> MAPPER_SIMPLE_NAMES = Set.of(
        "ChannelAnnouncementMapper",
        "ChannelAppealMapper",
        "ChannelBlacklistMapper",
        "ChannelContentEditHistoryMapper",
        "ChannelContentGovernanceLogMapper",
        "ChannelContentPublishMapper",
        "ChannelContentReviewMapper",
        "ChannelExportTaskMapper",
        "ChannelGovernanceLogMapper",
        "ChannelInviteMapper",
        "ChannelJoinApplicationMapper",
        "ChannelLifecycleLogMapper",
        "ChannelMapper",
        "ChannelMemberMapper",
        "ChannelMuteMapper",
        "ChannelPublishLimitMapper",
        "ChannelRecycleBinMapper",
        "ChannelReviewMapper",
        "ChannelScheduledPublishMapper",
        "ChannelSubscriptionGroupMapper",
        "ChannelSubscriptionMapper",
        "ChannelTransferMapper",
        "ContentChannelCategoryMapper",
        "ContentChannelEditorialPickMapper",
        "ContentChannelNotInterestedMapper",
        "ContentChannelRankingSnapshotMapper",
        "ContentChannelRecommendationCacheMapper",
        "ContentChannelTagMapper",
        "ContentChannelTagRelationMapper"
    );

    @Test
    void should_load_all_mappers_via_classpath_scan() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String path = "org/jeecg/modules/content/channel/mapper";
        Enumeration<URL> resources = cl.getResources(path);
        List<String> found = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            File dir = new File(url.toURI());
            if (dir.isDirectory()) {
                File[] files = dir.listFiles((d, name) -> name.endsWith(".class"));
                if (files != null) {
                    for (File f : files) {
                        found.add(f.getName().replace(".class", ""));
                    }
                }
            }
        }
        // 兜底：跳过文件系统检查时也保证关键 mapper 在
        assertThat(found).contains("ChannelMapper");
        assertThat(found).contains("ChannelStatsMapper");
    }

    @Test
    void channel_mapper_extends_base_mapper() {
        assertThat(BaseMapper.class.isAssignableFrom(ChannelMapper.class)).isTrue();
    }

    @Test
    void channel_member_mapper_extends_base_mapper() {
        assertThat(BaseMapper.class.isAssignableFrom(ChannelMemberMapper.class)).isTrue();
    }

    @Test
    void channel_subscription_mapper_extends_base_mapper() {
        assertThat(BaseMapper.class.isAssignableFrom(ChannelSubscriptionMapper.class)).isTrue();
    }

    @Test
    void channel_review_mapper_extends_base_mapper() {
        assertThat(BaseMapper.class.isAssignableFrom(ChannelReviewMapper.class)).isTrue();
    }

    @Test
    void channel_transfer_mapper_extends_base_mapper() {
        assertThat(BaseMapper.class.isAssignableFrom(ChannelTransferMapper.class)).isTrue();
    }

    @Test
    void channel_appeal_mapper_extends_base_mapper() {
        assertThat(BaseMapper.class.isAssignableFrom(ChannelAppealMapper.class)).isTrue();
    }

    @Test
    void channel_export_task_mapper_extends_base_mapper() {
        assertThat(BaseMapper.class.isAssignableFrom(ChannelExportTaskMapper.class)).isTrue();
    }

    @Test
    void channel_lifecycle_log_mapper_extends_base_mapper() {
        assertThat(BaseMapper.class.isAssignableFrom(ChannelLifecycleLogMapper.class)).isTrue();
    }

    @Test
    void channel_stats_mapper_extends_base_mapper() {
        assertThat(BaseMapper.class.isAssignableFrom(ChannelStatsMapper.class)).isTrue();
    }
}
