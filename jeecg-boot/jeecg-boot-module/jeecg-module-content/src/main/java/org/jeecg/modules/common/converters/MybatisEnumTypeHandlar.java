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

/**
 * Mybatis 枚举类型转换器
 * @param <T> 枚举类型
 * @author scott
 * @email jeecgos@163.com
 * @date  2019年1月19日
 */
@Slf4j
@NoArgsConstructor
public class MybatisEnumTypeHandlar<T extends BaseEnum> extends BaseTypeHandler<T> {

    /**
     * 枚举类型
     * @param <T> 枚举类型
     */
    private Class<T> enumType;
    /**
     * 枚举值数组
     */
    private T[] enums;

    /**
     * 构造函数
     * @param enumType 枚举类型
     */
    public MybatisEnumTypeHandlar(Class<T> enumType) {
        this.enumType = enumType;
        this.enums = enumType.getEnumConstants();
    }

    /**
     * 加载枚举值
     * @param value 枚举值
     * @return 枚举值
     */
    @SuppressWarnings("unchecked")
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

    /**
     * 设置非空参数
     * @param ps PreparedStatement
     * @param i 参数索引
     * @param parameter 参数值
     * @param jdbcType JDBC 类型
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setNull(i, jdbcType.TYPE_CODE);
        } else {
            ps.setObject(i, parameter.getValue(), jdbcType.TYPE_CODE);
        }
    }

    /**
     * 获取可空结果
     * @param rs ResultSet
     * @param columnName 列名
     * @return 可空结果
     */
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

    /**
     * 获取可空结果
     * @param rs ResultSet
     * @param columnIndex 列索引
     * @return 可空结果
     */
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

    /**
     * 获取可空结果
     * @param cs CallableStatement
     * @param columnIndex 列索引
     * @return 可空结果
     */
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
