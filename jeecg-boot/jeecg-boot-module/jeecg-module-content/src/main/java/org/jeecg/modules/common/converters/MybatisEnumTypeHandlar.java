package org.jeecg.modules.common.converters;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.jeecg.modules.common.enums.BaseEnum;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class MybatisEnumTypeHandlar<T extends BaseEnum> extends BaseTypeHandler<T> {

    private Class<T> enumType;
    private T[] enums;

    public MybatisEnumTypeHandlar(Class<T> enumType) {
        this.enumType = enumType;
        this.enums = enumType.getEnumConstants();
    }

    private T loadEnum(Object value) {
        for (T e : enums) {
            log.info("e:{} value:{}", e.getValue(), value);
            if (e.getValue().toString().equals(value.toString())) {
                return e;
            }
            if (e instanceof Enum) {
                Enum<?> enumValue = (Enum<?>) e;
                if (enumValue.name().equals(value.toString())) {
                    return e;
                }
            }
            if (e.getName().equals(value.toString())) {
                return e;
            }
        }
        throw new IllegalArgumentException(enumType.getName() + "  unknown enumerated type  value:" + value);
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setNull(i, jdbcType.TYPE_CODE);
        } else {
            ps.setObject(i, parameter.getValue(), jdbcType.TYPE_CODE);
        }
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        if (rs.getObject(columnName) == null) {
            return null;
        }
        T result = null;
        try {
            Object value = rs.getObject(columnName);
            if (value != null) {
                result = loadEnum(value);
            }
        } catch (SQLException e) {
            log.error("Error while getting nullable result from ResultSet", e);
        }
        return result;
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        if (rs.getObject(columnIndex) == null) {
            return null;
        }
        T result = null;
        try {
            Object value = rs.getObject(columnIndex);
            if (value != null) {
                result = loadEnum(value);
            }
        } catch (SQLException e) {
            log.error("Error while getting nullable result from ResultSet", e);
        }
        return result;
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        if (cs.getObject(columnIndex) == null) {
            return null;
        }
        T result = null;
        try {
            Object value = cs.getObject(columnIndex);
            if (value != null) {
                result = loadEnum(value);
            }
        } catch (SQLException e) {
            log.error("Error while getting nullable result from CallableStatement", e);
        }
        return result;
    }

}
