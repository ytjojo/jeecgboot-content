package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.content.user.entity.ContentUserHomepageModule;
import org.jeecg.modules.content.user.entity.ContentUserProfileHistory;
import org.jeecg.modules.content.user.entity.ContentUserProfileReview;
import org.jeecg.modules.content.user.entity.ContentUserVerificationBadge;
import org.jeecg.modules.content.user.mapper.ContentUserHomepageModuleMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileHistoryMapper;
import org.jeecg.modules.content.user.mapper.ContentUserProfileReviewMapper;
import org.jeecg.modules.content.user.mapper.ContentUserVerificationBadgeMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserHomepageServiceImpl;
import org.jeecg.modules.content.user.service.impl.ContentUserProfileHistoryServiceImpl;
import org.jeecg.modules.content.user.service.impl.ContentUserProfileReviewServiceImpl;
import org.jeecg.modules.content.user.service.impl.ContentUserVerificationBadgeServiceImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 内容社区资料管理新增表基础 CRUD 类型契约测试。
 */
class ContentUserProfileManagementCrudContractTest {

    private static final Map<Class<?>, String> ENTITY_TABLES = Map.of(
        ContentUserProfileReview.class, "content_user_profile_review",
        ContentUserHomepageModule.class, "content_user_homepage_module",
        ContentUserVerificationBadge.class, "content_user_verification_badge",
        ContentUserProfileHistory.class, "content_user_profile_history"
    );

    @Test
    void shouldMapProfileManagementEntitiesToExpectedTables() {
        ENTITY_TABLES.forEach((entityType, tableName) -> {
            TableName annotation = entityType.getAnnotation(TableName.class);

            assertThat(annotation).as(entityType.getSimpleName()).isNotNull();
            assertThat(annotation.value()).as(entityType.getSimpleName()).isEqualTo(tableName);
        });
    }

    @Test
    void shouldExposeMappersAndServicesForNewTables() {
        assertThat(BaseMapper.class).isAssignableFrom(ContentUserProfileReviewMapper.class);
        assertThat(BaseMapper.class).isAssignableFrom(ContentUserHomepageModuleMapper.class);
        assertThat(BaseMapper.class).isAssignableFrom(ContentUserVerificationBadgeMapper.class);
        assertThat(BaseMapper.class).isAssignableFrom(ContentUserProfileHistoryMapper.class);

        assertThat(IContentUserProfileReviewService.class).isAssignableFrom(ContentUserProfileReviewServiceImpl.class);
        assertThat(IContentUserHomepageService.class).isAssignableFrom(ContentUserHomepageServiceImpl.class);
        assertThat(IContentUserVerificationBadgeService.class).isAssignableFrom(ContentUserVerificationBadgeServiceImpl.class);
        assertThat(IContentUserProfileHistoryService.class).isAssignableFrom(ContentUserProfileHistoryServiceImpl.class);
    }

    @Test
    void shouldExposeFieldsNeededByProfileManagementFlows() {
        assertHasFields(ContentUserProfileReview.class, "userId", "reviewStatus", "riskReason", "originalSnapshotJson", "targetSnapshotJson");
        assertHasFields(ContentUserHomepageModule.class, "userId", "moduleKey", "visible", "sortOrder");
        assertHasFields(ContentUserVerificationBadge.class, "userId", "badgeType", "badgeLabel", "status", "verifiedAt", "expiresAt");
        assertHasFields(ContentUserProfileHistory.class, "userId", "historyType", "historyValue", "expired", "expiresAt");
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
