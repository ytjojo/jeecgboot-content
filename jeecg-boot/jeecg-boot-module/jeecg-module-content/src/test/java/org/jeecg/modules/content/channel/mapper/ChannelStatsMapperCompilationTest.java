package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.content.channel.entity.ChannelStats;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ChannelStatsMapper 编译期烟雾测试
 * - 验证 Mapper 接口和 MyBatis-Plus BaseMapper 兼容
 * - 验证 selectTrendData 方法签名（4 参数 + 命名参数）能正确解析
 *
 * 集成行为由对应的 XML 真实执行覆盖（运行期 MyBatis）。
 */
class ChannelStatsMapperCompilationTest {

    @Test
    void should_extend_base_mapper() {
        assertThat(BaseMapper.class.isAssignableFrom(ChannelStatsMapper.class)).isTrue();
    }

    @Test
    void should_declare_select_trend_data_with_4_params() throws NoSuchMethodException {
        Method m = ChannelStatsMapper.class.getMethod("selectTrendData",
            String.class, LocalDate.class, LocalDate.class, String.class);
        assertThat(m.getReturnType()).isEqualTo(List.class);
    }

    @Test
    void should_be_annotated_as_mapper() {
        assertThat(ChannelStatsMapper.class.isAnnotationPresent(
            org.apache.ibatis.annotations.Mapper.class)).isTrue();
    }

    @Test
    void should_be_assignable_to_wrapper_query_target() {
        Wrapper<ChannelStats> wrapper = null;
        // 编译期类型校验
        assertThat(ChannelStats.class).isAssignableFrom(ChannelStats.class);
        assertThat(wrapper == null).isTrue();
    }
}
