package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.user.entity.ContentUserExchangeGoods;
import org.jeecg.modules.content.user.entity.ContentUserExchangeOrder;
import org.jeecg.modules.content.user.entity.ContentUserFeatureUnlock;
import org.jeecg.modules.content.user.entity.ContentUserGrowthDecayState;
import org.jeecg.modules.content.user.entity.ContentUserLevelBenefitConfig;
import org.jeecg.modules.content.user.entity.ContentUserLevelConfig;
import org.jeecg.modules.content.user.entity.ContentUserRewardEvent;
import org.jeecg.modules.content.user.entity.ContentUserRewardRule;
import org.jeecg.modules.content.user.entity.ContentUserVirtualGiftRecord;
import org.jeecg.modules.content.user.mapper.ContentUserExchangeGoodsMapper;
import org.jeecg.modules.content.user.mapper.ContentUserExchangeOrderMapper;
import org.jeecg.modules.content.user.mapper.ContentUserFeatureUnlockMapper;
import org.jeecg.modules.content.user.mapper.ContentUserGrowthDecayStateMapper;
import org.jeecg.modules.content.user.mapper.ContentUserLevelBenefitConfigMapper;
import org.jeecg.modules.content.user.mapper.ContentUserLevelConfigMapper;
import org.jeecg.modules.content.user.mapper.ContentUserRewardEventMapper;
import org.jeecg.modules.content.user.mapper.ContentUserRewardRuleMapper;
import org.jeecg.modules.content.user.mapper.ContentUserVirtualGiftRecordMapper;
import org.jeecg.modules.content.user.service.impl.ContentUserExchangeGoodsServiceImpl;
import org.jeecg.modules.content.user.service.impl.ContentUserExchangeOrderServiceImpl;
import org.jeecg.modules.content.user.service.impl.ContentUserFeatureUnlockServiceImpl;
import org.jeecg.modules.content.user.service.impl.ContentUserGrowthDecayStateServiceImpl;
import org.jeecg.modules.content.user.service.impl.ContentUserLevelBenefitConfigServiceImpl;
import org.jeecg.modules.content.user.service.impl.ContentUserLevelConfigServiceImpl;
import org.jeecg.modules.content.user.service.impl.ContentUserRewardEventServiceImpl;
import org.jeecg.modules.content.user.service.impl.ContentUserRewardRuleServiceImpl;
import org.jeecg.modules.content.user.service.impl.ContentUserVirtualGiftRecordServiceImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 内容社区勋章积分成长基础 CRUD 类型契约测试。
 */
class ContentUserBadgesPointsGrowthCrudContractTest {

    private static final Map<Class<?>, String> ENTITY_TABLES = Map.of(
        ContentUserRewardRule.class, "content_user_reward_rule",
        ContentUserRewardEvent.class, "content_user_reward_event",
        ContentUserLevelConfig.class, "content_user_level_config",
        ContentUserLevelBenefitConfig.class, "content_user_level_benefit_config",
        ContentUserExchangeGoods.class, "content_user_exchange_goods",
        ContentUserExchangeOrder.class, "content_user_exchange_order",
        ContentUserFeatureUnlock.class, "content_user_feature_unlock",
        ContentUserVirtualGiftRecord.class, "content_user_virtual_gift_record",
        ContentUserGrowthDecayState.class, "content_user_growth_decay_state"
    );

    @Test
    void shouldMapNewEntitiesToExpectedTables() {
        ENTITY_TABLES.forEach((entityType, tableName) -> {
            TableName annotation = entityType.getAnnotation(TableName.class);

            assertThat(annotation).as(entityType.getSimpleName()).isNotNull();
            assertThat(annotation.value()).as(entityType.getSimpleName()).isEqualTo(tableName);
        });
    }

    @Test
    void shouldExposeBaseMapperForNewTables() {
        assertThat(BaseMapper.class).isAssignableFrom(ContentUserRewardRuleMapper.class);
        assertThat(BaseMapper.class).isAssignableFrom(ContentUserRewardEventMapper.class);
        assertThat(BaseMapper.class).isAssignableFrom(ContentUserLevelConfigMapper.class);
        assertThat(BaseMapper.class).isAssignableFrom(ContentUserLevelBenefitConfigMapper.class);
        assertThat(BaseMapper.class).isAssignableFrom(ContentUserExchangeGoodsMapper.class);
        assertThat(BaseMapper.class).isAssignableFrom(ContentUserExchangeOrderMapper.class);
        assertThat(BaseMapper.class).isAssignableFrom(ContentUserFeatureUnlockMapper.class);
        assertThat(BaseMapper.class).isAssignableFrom(ContentUserVirtualGiftRecordMapper.class);
        assertThat(BaseMapper.class).isAssignableFrom(ContentUserGrowthDecayStateMapper.class);
    }

    @Test
    void shouldExposeBaseCrudServicesForNewTables() {
        assertThat(IService.class).isAssignableFrom(IContentUserRewardRuleService.class);
        assertThat(IService.class).isAssignableFrom(IContentUserRewardEventService.class);
        assertThat(IService.class).isAssignableFrom(IContentUserLevelConfigService.class);
        assertThat(IService.class).isAssignableFrom(IContentUserLevelBenefitConfigService.class);
        assertThat(IService.class).isAssignableFrom(IContentUserExchangeGoodsService.class);
        assertThat(IService.class).isAssignableFrom(IContentUserExchangeOrderService.class);
        assertThat(IService.class).isAssignableFrom(IContentUserFeatureUnlockService.class);
        assertThat(IService.class).isAssignableFrom(IContentUserVirtualGiftRecordService.class);
        assertThat(IService.class).isAssignableFrom(IContentUserGrowthDecayStateService.class);

        assertThat(ServiceImpl.class).isAssignableFrom(ContentUserRewardRuleServiceImpl.class);
        assertThat(ServiceImpl.class).isAssignableFrom(ContentUserRewardEventServiceImpl.class);
        assertThat(ServiceImpl.class).isAssignableFrom(ContentUserLevelConfigServiceImpl.class);
        assertThat(ServiceImpl.class).isAssignableFrom(ContentUserLevelBenefitConfigServiceImpl.class);
        assertThat(ServiceImpl.class).isAssignableFrom(ContentUserExchangeGoodsServiceImpl.class);
        assertThat(ServiceImpl.class).isAssignableFrom(ContentUserExchangeOrderServiceImpl.class);
        assertThat(ServiceImpl.class).isAssignableFrom(ContentUserFeatureUnlockServiceImpl.class);
        assertThat(ServiceImpl.class).isAssignableFrom(ContentUserVirtualGiftRecordServiceImpl.class);
        assertThat(ServiceImpl.class).isAssignableFrom(ContentUserGrowthDecayStateServiceImpl.class);
    }

    @Test
    void shouldExposeFieldsNeededByRewardExchangeAndDecayFlows() {
        assertHasFields(ContentUserRewardRule.class, "ruleCode", "sourceType", "pointAmount", "growthAmount", "dailyPointCap", "dailyGrowthCap");
        assertHasFields(ContentUserRewardEvent.class, "eventId", "userId", "sourceType", "ruleCode", "pointDelta", "growthDelta", "dailyBucket");
        assertHasFields(ContentUserExchangeOrder.class, "orderNo", "userId", "goodsId", "goodsCode", "quantity", "pointCost", "benefitStatus");
        assertHasFields(ContentUserGrowthDecayState.class, "userId", "lastActiveTime", "lastDecayTime", "protectionUntil", "status", "ruleSnapshotJson");
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
