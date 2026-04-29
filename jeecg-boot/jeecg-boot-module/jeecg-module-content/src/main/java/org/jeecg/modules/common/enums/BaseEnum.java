package org.jeecg.modules.common.enums;

/**
 * Base interface for enums.
 */
public interface BaseEnum<E extends Enum<?>, T> {
 
    /**
     * 获取枚举值
     * @return 枚举值
     */
    T getValue();


    /**
     * 获取枚举值名称
     * @return 枚举值的名称
     */
    String getName();
 
    /**
     * 获取枚举值描述
     * @return 枚举值的描述
     */
    String getDescription();



 
}