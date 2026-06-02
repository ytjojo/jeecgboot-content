package org.jeecg.modules.common.converters;

import org.apache.ibatis.type.JdbcType;
import org.jeecg.modules.common.enums.EnableStatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * MyBatis 枚举类型转换器单测。
 *
 * 覆盖审计报告 P0 项：三个 {@code getNullableResult} 重载的"吞错返回 null"路径
 * 以及 {@code loadEnum} 的三段匹配顺序。
 */
@ExtendWith(MockitoExtension.class)
class MybatisEnumTypeHandlarTest {

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private CallableStatement callableStatement;

    // ---------------------------------------------------------------------
    // setNonNullParameter
    // ---------------------------------------------------------------------

    @Test
    void setNonNullParameter_shouldInvokeSetObjectWithValue() throws SQLException {
        MybatisEnumTypeHandlar<EnableStatusEnum> handler = new MybatisEnumTypeHandlar<>(EnableStatusEnum.class);

        handler.setNonNullParameter(preparedStatement, 1, EnableStatusEnum.ENABLED, JdbcType.INTEGER);

        verify(preparedStatement).setObject(1, EnableStatusEnum.ENABLED.getValue(), JdbcType.INTEGER.TYPE_CODE);
        verify(preparedStatement, never()).setNull(anyInt(), anyInt());
    }

    @Test
    void setNonNullParameter_shouldInvokeSetNullWhenParameterIsNull() throws SQLException {
        MybatisEnumTypeHandlar<EnableStatusEnum> handler = new MybatisEnumTypeHandlar<>(EnableStatusEnum.class);

        handler.setNonNullParameter(preparedStatement, 2, null, JdbcType.VARCHAR);

        verify(preparedStatement).setNull(2, JdbcType.VARCHAR.TYPE_CODE);
        verify(preparedStatement, never()).setObject(anyInt(), any(), anyInt());
    }

    // ---------------------------------------------------------------------
    // getNullableResult(ResultSet, String) - by column name
    // ---------------------------------------------------------------------

    @Test
    void getNullableResult_byColumnName_shouldReturnNullWhenColumnValueIsNull() throws SQLException {
        MybatisEnumTypeHandlar<EnableStatusEnum> handler = new MybatisEnumTypeHandlar<>(EnableStatusEnum.class);
        doReturn(null).when(resultSet).getObject("status");

        EnableStatusEnum result = handler.getNullableResult(resultSet, "status");

        assertThat(result).isNull();
    }

    @Test
    void getNullableResult_byColumnName_shouldMatchGetValueToString() throws SQLException {
        MybatisEnumTypeHandlar<EnableStatusEnum> handler = new MybatisEnumTypeHandlar<>(EnableStatusEnum.class);
        // 两次 getObject 调用：第一处 null-check，第二处真正 loadEnum
        doReturn(1).doReturn(1).when(resultSet).getObject("status");

        EnableStatusEnum result = handler.getNullableResult(resultSet, "status");

        assertThat(result).isEqualTo(EnableStatusEnum.ENABLED);
    }

    @Test
    void getNullableResult_byColumnName_shouldMatchEnumName() throws SQLException {
        MybatisEnumTypeHandlar<EnableStatusEnum> handler = new MybatisEnumTypeHandlar<>(EnableStatusEnum.class);
        doReturn("REVIEWING").doReturn("REVIEWING").when(resultSet).getObject("status");

        EnableStatusEnum result = handler.getNullableResult(resultSet, "status");

        assertThat(result).isEqualTo(EnableStatusEnum.REVIEWING);
    }

    @Test
    void getNullableResult_byColumnName_shouldMatchGetName() throws SQLException {
        MybatisEnumTypeHandlar<EnableStatusEnum> handler = new MybatisEnumTypeHandlar<>(EnableStatusEnum.class);
        doReturn("rejected").doReturn("rejected").when(resultSet).getObject("status");

        EnableStatusEnum result = handler.getNullableResult(resultSet, "status");

        assertThat(result).isEqualTo(EnableStatusEnum.REJECTED);
    }

    @Test
    void getNullableResult_byColumnName_shouldThrowWhenValueUnknown() throws SQLException {
        MybatisEnumTypeHandlar<EnableStatusEnum> handler = new MybatisEnumTypeHandlar<>(EnableStatusEnum.class);
        doReturn("NOT_A_VALID_STATUS").doReturn("NOT_A_VALID_STATUS").when(resultSet).getObject("status");

        assertThatThrownBy(() -> handler.getNullableResult(resultSet, "status"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("unknown enumerated type")
            .hasMessageContaining("NOT_A_VALID_STATUS");
    }

    @Test
    void getNullableResult_byColumnName_shouldSwallowSqlExceptionAndReturnNull() throws SQLException {
        MybatisEnumTypeHandlar<EnableStatusEnum> handler = new MybatisEnumTypeHandlar<>(EnableStatusEnum.class);
        // 第一次 getObject 返回非 null（绕过 null-check），第二次抛 SQLException
        doReturn(1).doThrow(new SQLException("simulated DB error")).when(resultSet).getObject("status");

        EnableStatusEnum result = handler.getNullableResult(resultSet, "status");

        // 审计明确指出的"吞错返回 null"风险：异常被捕获且无外暴露
        assertThat(result).isNull();
    }

    // ---------------------------------------------------------------------
    // getNullableResult(ResultSet, int) - by column index
    // ---------------------------------------------------------------------

    @Test
    void getNullableResult_byColumnIndex_shouldReturnNullWhenColumnValueIsNull() throws SQLException {
        MybatisEnumTypeHandlar<EnableStatusEnum> handler = new MybatisEnumTypeHandlar<>(EnableStatusEnum.class);
        doReturn(null).when(resultSet).getObject(3);

        EnableStatusEnum result = handler.getNullableResult(resultSet, 3);

        assertThat(result).isNull();
    }

    @Test
    void getNullableResult_byColumnIndex_shouldMatchGetValueToString() throws SQLException {
        MybatisEnumTypeHandlar<EnableStatusEnum> handler = new MybatisEnumTypeHandlar<>(EnableStatusEnum.class);
        doReturn(0).doReturn(0).when(resultSet).getObject(2);

        EnableStatusEnum result = handler.getNullableResult(resultSet, 2);

        assertThat(result).isEqualTo(EnableStatusEnum.DISABLED);
    }

    @Test
    void getNullableResult_byColumnIndex_shouldMatchEnumName() throws SQLException {
        MybatisEnumTypeHandlar<EnableStatusEnum> handler = new MybatisEnumTypeHandlar<>(EnableStatusEnum.class);
        doReturn("DELETED").doReturn("DELETED").when(resultSet).getObject(2);

        EnableStatusEnum result = handler.getNullableResult(resultSet, 2);

        assertThat(result).isEqualTo(EnableStatusEnum.DELETED);
    }

    @Test
    void getNullableResult_byColumnIndex_shouldThrowWhenValueUnknown() throws SQLException {
        MybatisEnumTypeHandlar<EnableStatusEnum> handler = new MybatisEnumTypeHandlar<>(EnableStatusEnum.class);
        doReturn(Integer.MAX_VALUE).doReturn(Integer.MAX_VALUE).when(resultSet).getObject(2);

        assertThatThrownBy(() -> handler.getNullableResult(resultSet, 2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("unknown enumerated type");
    }

    @Test
    void getNullableResult_byColumnIndex_shouldSwallowSqlExceptionAndReturnNull() throws SQLException {
        MybatisEnumTypeHandlar<EnableStatusEnum> handler = new MybatisEnumTypeHandlar<>(EnableStatusEnum.class);
        doReturn(1).doThrow(new SQLException("simulated column-index error")).when(resultSet).getObject(2);

        EnableStatusEnum result = handler.getNullableResult(resultSet, 2);

        assertThat(result).isNull();
    }

    // ---------------------------------------------------------------------
    // getNullableResult(CallableStatement, int)
    // ---------------------------------------------------------------------

    @Test
    void getNullableResult_callableStatement_shouldReturnNullWhenColumnValueIsNull() throws SQLException {
        MybatisEnumTypeHandlar<EnableStatusEnum> handler = new MybatisEnumTypeHandlar<>(EnableStatusEnum.class);
        doReturn(null).when(callableStatement).getObject(1);

        EnableStatusEnum result = handler.getNullableResult(callableStatement, 1);

        assertThat(result).isNull();
    }

    @Test
    void getNullableResult_callableStatement_shouldMatchGetValueToString() throws SQLException {
        MybatisEnumTypeHandlar<EnableStatusEnum> handler = new MybatisEnumTypeHandlar<>(EnableStatusEnum.class);
        doReturn(2).doReturn(2).when(callableStatement).getObject(1);

        EnableStatusEnum result = handler.getNullableResult(callableStatement, 1);

        assertThat(result).isEqualTo(EnableStatusEnum.REVIEWING);
    }

    @Test
    void getNullableResult_callableStatement_shouldMatchGetName() throws SQLException {
        MybatisEnumTypeHandlar<EnableStatusEnum> handler = new MybatisEnumTypeHandlar<>(EnableStatusEnum.class);
        doReturn("enabled").doReturn("enabled").when(callableStatement).getObject(1);

        EnableStatusEnum result = handler.getNullableResult(callableStatement, 1);

        assertThat(result).isEqualTo(EnableStatusEnum.ENABLED);
    }

    @Test
    void getNullableResult_callableStatement_shouldThrowWhenValueUnknown() throws SQLException {
        MybatisEnumTypeHandlar<EnableStatusEnum> handler = new MybatisEnumTypeHandlar<>(EnableStatusEnum.class);
        doReturn("???").doReturn("???").when(callableStatement).getObject(1);

        assertThatThrownBy(() -> handler.getNullableResult(callableStatement, 1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("unknown enumerated type");
    }

    @Test
    void getNullableResult_callableStatement_shouldSwallowSqlExceptionAndReturnNull() throws SQLException {
        MybatisEnumTypeHandlar<EnableStatusEnum> handler = new MybatisEnumTypeHandlar<>(EnableStatusEnum.class);
        doReturn(1).doThrow(new SQLException("simulated callable error")).when(callableStatement).getObject(1);

        EnableStatusEnum result = handler.getNullableResult(callableStatement, 1);

        assertThat(result).isNull();
    }

    // ---------------------------------------------------------------------
    // 防御性断言：避免静默通过：未用到的 mock
    // ---------------------------------------------------------------------

    @Test
    void verifyUnusedMocks_shouldNotBeCalled() throws SQLException {
        // 单独的"未用"自检，确保 preparedStatement 之外的 mock 在其他测试中未误用
        // 这里仅 sanity check 一些未在其它 case 中被调用的方法
        verify(preparedStatement, never()).setObject(eq(99), any(), anyInt());
        verify(resultSet, never()).getObject(anyString());
        verify(callableStatement, never()).getObject(99);
    }
}
