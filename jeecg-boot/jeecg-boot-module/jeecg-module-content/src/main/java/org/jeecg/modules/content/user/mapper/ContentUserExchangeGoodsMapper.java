package org.jeecg.modules.content.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.content.user.entity.ContentUserExchangeGoods;

/**
 * 内容社区积分兑换商品 Mapper。
 */
public interface ContentUserExchangeGoodsMapper extends BaseMapper<ContentUserExchangeGoods> {

    /**
     * 在库存足够时原子扣减库存，空库存表示不限量。
     */
    @Update("update content_user_exchange_goods set stock_quantity = stock_quantity - #{quantity}, update_time = now() "
        + "where id = #{goodsId} and enabled = 1 and stock_quantity is not null and stock_quantity >= #{quantity}")
    int deductStockIfEnough(@Param("goodsId") String goodsId, @Param("quantity") int quantity);
}
