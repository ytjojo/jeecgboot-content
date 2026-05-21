package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.content.user.entity.ContentUserBlock;
import org.jeecg.modules.content.user.entity.ContentUserFilterRule;
import org.jeecg.modules.content.user.entity.ContentUserMute;
import org.jeecg.modules.content.user.entity.ContentUserNotInterested;
import org.jeecg.modules.content.user.mapper.ContentUserBlockMapper;
import org.jeecg.modules.content.user.mapper.ContentUserFilterRuleMapper;
import org.jeecg.modules.content.user.mapper.ContentUserMuteMapper;
import org.jeecg.modules.content.user.mapper.ContentUserNotInterestedMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserBlockServiceImpl;
import org.jeecg.modules.content.user.service.impl.ContentUserFilterRuleServiceImpl;
import org.jeecg.modules.content.user.service.impl.ContentUserMuteServiceImpl;
import org.jeecg.modules.content.user.service.impl.ContentUserNotInterestedServiceImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 内容社区拉黑屏蔽新增表基础 CRUD 类型契约测试。
 */
class ContentBlockingMutingCrudContractTest {

    private static final Map<Class<?>, String> ENTITY_TABLES = Map.of(
        ContentUserBlock.class, "content_user_block",
        ContentUserMute.class, "content_user_mute",
        ContentUserFilterRule.class, "content_user_filter_rule",
        ContentUserNotInterested.class, "content_user_not_interested"
    );

    @Test
    void shouldMapBlockingMutingEntitiesToExpectedTables() {
        ENTITY_TABLES.forEach((entityType, tableName) -> {
            TableName annotation = entityType.getAnnotation(TableName.class);

            assertThat(annotation).as(entityType.getSimpleName()).isNotNull();
            assertThat(annotation.value()).as(entityType.getSimpleName()).isEqualTo(tableName);
        });
    }

    @Test
    void shouldExposeMappersAndServicesForNewTables() {
        assertThat(BaseMapper.class).isAssignableFrom(ContentUserBlockMapper.class);
        assertThat(BaseMapper.class).isAssignableFrom(ContentUserMuteMapper.class);
        assertThat(BaseMapper.class).isAssignableFrom(ContentUserFilterRuleMapper.class);
        assertThat(BaseMapper.class).isAssignableFrom(ContentUserNotInterestedMapper.class);

        assertThat(IContentUserBlockService.class).isAssignableFrom(ContentUserBlockServiceImpl.class);
        assertThat(IContentUserMuteService.class).isAssignableFrom(ContentUserMuteServiceImpl.class);
        assertThat(IContentUserFilterRuleService.class).isAssignableFrom(ContentUserFilterRuleServiceImpl.class);
        assertThat(IContentUserNotInterestedService.class).isAssignableFrom(ContentUserNotInterestedServiceImpl.class);
    }

    @Test
    void shouldExposeFieldsNeededByBlockingMutingFlows() {
        assertHasFields(ContentUserBlock.class, "userId", "blockedUserId", "blockTime", "status", "reason");
        assertHasFields(ContentUserMute.class, "userId", "mutedUserId", "muteTime", "status", "reason");
        assertHasFields(ContentUserFilterRule.class, "userId", "ruleType", "ruleValue", "normalizedValue", "matchScope", "expiresAt", "status");
        assertHasFields(ContentUserNotInterested.class, "userId", "contentId", "contentType", "topic", "reason", "feedbackTime", "status");
    }

    private void assertHasFields(Class<?> entityType, String... fieldNames) {
        for (String fieldName : fieldNames) {
            Field field = findField(entityType, fieldName);
            assertThat(field).as(entityType.getSimpleName() + "." + fieldName).isNotNull();
        }
    }

    private Field findField(Class<?> entityType, String fieldName) {
        try {
            return entityType.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}
