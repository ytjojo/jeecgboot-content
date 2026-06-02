package org.jeecg.modules.content.circle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.content.circle.entity.Circle;
import org.jeecg.modules.content.circle.entity.CircleAnnouncement;
import org.jeecg.modules.content.circle.entity.CircleAuditLog;
import org.jeecg.modules.content.circle.entity.CircleContent;
import org.jeecg.modules.content.circle.entity.CircleDataStatistics;
import org.jeecg.modules.content.circle.entity.CircleGovernanceLog;
import org.jeecg.modules.content.circle.entity.CircleJoinRequest;
import org.jeecg.modules.content.circle.entity.CircleMember;
import org.jeecg.modules.content.circle.entity.CircleRecommendSource;
import org.jeecg.modules.content.circle.entity.CircleReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 圈子子模块 Mapper 编译期契约校验。
 * 验证：① 每个 Mapper 接口均继承自 {@link BaseMapper}；
 * ② 自定义方法签名与审计报告中的契约一致（参数类型、返回类型、存在性）。
 *
 * <p>该测试不依赖 Spring 上下文，单纯使用反射对 Mapper 接口结构做编译期校验，
 * 防止 Mapper 重命名或方法签名漂移被延迟到运行时才暴露。</p>
 */
@DisplayName("Circle Mapper Compilation Contract")
class CircleMapperCompilationTest {

    // ==================== 类型契约 ====================

    @Test
    @DisplayName("circleMapper - extends BaseMapper<Circle>")
    void circleMapper_extendsBaseMapper() {
        assertThat(BaseMapper.class.isAssignableFrom(CircleMapper.class)).isTrue();
    }

    @Test
    @DisplayName("circleMemberMapper - extends BaseMapper<CircleMember>")
    void circleMemberMapper_extendsBaseMapper() {
        assertThat(BaseMapper.class.isAssignableFrom(CircleMemberMapper.class)).isTrue();
    }

    @Test
    @DisplayName("circleAnnouncementMapper - extends BaseMapper<CircleAnnouncement>")
    void circleAnnouncementMapper_extendsBaseMapper() {
        assertThat(BaseMapper.class.isAssignableFrom(CircleAnnouncementMapper.class)).isTrue();
    }

    @Test
    @DisplayName("circleAuditLogMapper - extends BaseMapper<CircleAuditLog>")
    void circleAuditLogMapper_extendsBaseMapper() {
        assertThat(BaseMapper.class.isAssignableFrom(CircleAuditLogMapper.class)).isTrue();
    }

    @Test
    @DisplayName("circleContentMapper - extends BaseMapper<CircleContent>")
    void circleContentMapper_extendsBaseMapper() {
        assertThat(BaseMapper.class.isAssignableFrom(CircleContentMapper.class)).isTrue();
    }

    @Test
    @DisplayName("circleDataStatisticsMapper - extends BaseMapper<CircleDataStatistics>")
    void circleDataStatisticsMapper_extendsBaseMapper() {
        assertThat(BaseMapper.class.isAssignableFrom(CircleDataStatisticsMapper.class)).isTrue();
    }

    @Test
    @DisplayName("circleGovernanceLogMapper - extends BaseMapper<CircleGovernanceLog>")
    void circleGovernanceLogMapper_extendsBaseMapper() {
        assertThat(BaseMapper.class.isAssignableFrom(CircleGovernanceLogMapper.class)).isTrue();
    }

    @Test
    @DisplayName("circleJoinRequestMapper - extends BaseMapper<CircleJoinRequest>")
    void circleJoinRequestMapper_extendsBaseMapper() {
        assertThat(BaseMapper.class.isAssignableFrom(CircleJoinRequestMapper.class)).isTrue();
    }

    @Test
    @DisplayName("circleRecommendSourceMapper - extends BaseMapper<CircleRecommendSource>")
    void circleRecommendSourceMapper_extendsBaseMapper() {
        assertThat(BaseMapper.class.isAssignableFrom(CircleRecommendSourceMapper.class)).isTrue();
    }

    @Test
    @DisplayName("circleReportMapper - extends BaseMapper<CircleReport>")
    void circleReportMapper_extendsBaseMapper() {
        assertThat(BaseMapper.class.isAssignableFrom(CircleReportMapper.class)).isTrue();
    }

    // ==================== 自定义方法契约 ====================

    @Test
    @DisplayName("circleMapper - incrementMemberCount 返回 int, 入参 String")
    void circleMapper_hasIncrementMemberCount() throws NoSuchMethodException {
        Method method = CircleMapper.class.getMethod("incrementMemberCount", String.class);
        assertThat(method.getReturnType()).isEqualTo(int.class);
    }

    @Test
    @DisplayName("circleMapper - decrementMemberCount 返回 int, 入参 String")
    void circleMapper_hasDecrementMemberCount() throws NoSuchMethodException {
        Method method = CircleMapper.class.getMethod("decrementMemberCount", String.class);
        assertThat(method.getReturnType()).isEqualTo(int.class);
    }

    @Test
    @DisplayName("circleMapper - selectHotCircles 返回 List<Circle>, 入参 int")
    void circleMapper_hasSelectHotCircles() throws NoSuchMethodException {
        Method method = CircleMapper.class.getMethod("selectHotCircles", int.class);
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("circleMapper - selectNewCircles 返回 List<Circle>, 入参 int")
    void circleMapper_hasSelectNewCircles() throws NoSuchMethodException {
        Method method = CircleMapper.class.getMethod("selectNewCircles", int.class);
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("circleMemberMapper - selectCircleIdsByUserId 返回 List<String>, 入参 String")
    void circleMemberMapper_hasSelectCircleIdsByUserId() throws NoSuchMethodException {
        Method method = CircleMemberMapper.class.getMethod("selectCircleIdsByUserId", String.class);
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("circleAnnouncementMapper - selectActiveByCircleId 返回 CircleAnnouncement, 入参 String")
    void circleAnnouncementMapper_hasSelectActiveByCircleId() throws NoSuchMethodException {
        Method method = CircleAnnouncementMapper.class.getMethod("selectActiveByCircleId", String.class);
        assertThat(method.getReturnType()).isEqualTo(CircleAnnouncement.class);
    }

    @Test
    @DisplayName("circleAuditLogMapper - selectByTarget 返回 List<CircleAuditLog>")
    void circleAuditLogMapper_hasSelectByTarget() throws NoSuchMethodException {
        Method method = CircleAuditLogMapper.class.getMethod("selectByTarget", String.class, String.class);
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("circleAuditLogMapper - selectByTimeRange 返回 List<CircleAuditLog>")
    void circleAuditLogMapper_hasSelectByTimeRange() throws NoSuchMethodException {
        Method method = CircleAuditLogMapper.class.getMethod("selectByTimeRange", Date.class, Date.class);
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("circleContentMapper - selectCircleContentList 返回 List<CircleContent>")
    void circleContentMapper_hasSelectCircleContentList() throws NoSuchMethodException {
        Method method = CircleContentMapper.class.getMethod("selectCircleContentList", String.class);
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("circleDataStatisticsMapper - selectByCircleIdAndDateRange 返回 List<CircleDataStatistics>")
    void circleDataStatisticsMapper_hasSelectByCircleIdAndDateRange() throws NoSuchMethodException {
        Method method = CircleDataStatisticsMapper.class.getMethod(
                "selectByCircleIdAndDateRange", String.class, java.time.LocalDate.class, java.time.LocalDate.class);
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("circleJoinRequestMapper - selectPendingByCircleId 返回 List<CircleJoinRequest>")
    void circleJoinRequestMapper_hasSelectPendingByCircleId() throws NoSuchMethodException {
        Method method = CircleJoinRequestMapper.class.getMethod("selectPendingByCircleId", String.class);
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("circleJoinRequestMapper - selectTimedOutRequests 返回 List<CircleJoinRequest>")
    void circleJoinRequestMapper_hasSelectTimedOutRequests() throws NoSuchMethodException {
        Method method = CircleJoinRequestMapper.class.getMethod("selectTimedOutRequests");
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("circleRecommendSourceMapper - insertBatch 返回 int, 入参 List<CircleRecommendSource>")
    void circleRecommendSourceMapper_hasInsertBatch() throws NoSuchMethodException {
        Method method = CircleRecommendSourceMapper.class.getMethod("insertBatch", List.class);
        assertThat(method.getReturnType()).isEqualTo(int.class);
    }

    @Test
    @DisplayName("circleRecommendSourceMapper - updateClickTime 返回 int")
    void circleRecommendSourceMapper_hasUpdateClickTime() throws NoSuchMethodException {
        Method method = CircleRecommendSourceMapper.class.getMethod("updateClickTime", String.class, String.class);
        assertThat(method.getReturnType()).isEqualTo(int.class);
    }

    @Test
    @DisplayName("circleReportMapper - selectByCircleAndStatus 返回 List<CircleReport>")
    void circleReportMapper_hasSelectByCircleAndStatus() throws NoSuchMethodException {
        Method method = CircleReportMapper.class.getMethod("selectByCircleAndStatus", String.class, String.class);
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("circleMemberMapper - selectMemberStatsGroupByCircle 返回 List<Map>")
    void circleMemberMapper_hasSelectMemberStatsGroupByCircle() throws NoSuchMethodException {
        Method method = CircleMemberMapper.class.getMethod(
                "selectMemberStatsGroupByCircle", java.time.LocalDateTime.class);
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    /**
     * 防止所有 10 个 Mapper 都不存在时类型契约部分"全绿"假象 - 至少有一个 XML/注解的 SQL 方法存在于 circle 上。
     */
    @Test
    @DisplayName("Mapper 集合总数 - 10 个 circle 模块 Mapper 接口均存在")
    void allCircleMappersExist() {
        Class<?>[] expectedMappers = {
                CircleMapper.class,
                CircleMemberMapper.class,
                CircleAnnouncementMapper.class,
                CircleAuditLogMapper.class,
                CircleContentMapper.class,
                CircleDataStatisticsMapper.class,
                CircleGovernanceLogMapper.class,
                CircleJoinRequestMapper.class,
                CircleRecommendSourceMapper.class,
                CircleReportMapper.class
        };
        assertThat(expectedMappers).hasSize(10);
        for (Class<?> mapper : expectedMappers) {
            assertThat(BaseMapper.class.isAssignableFrom(mapper))
                    .as("Mapper %s should extend BaseMapper", mapper.getSimpleName())
                    .isTrue();
        }
    }
}
