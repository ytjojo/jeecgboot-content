package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.user.entity.ContentUserExchangeGoods;
import org.jeecg.modules.content.user.mapper.ContentUserExchangeGoodsMapper;
import org.jeecg.modules.content.user.service.IContentUserExchangeGoodsService;
import org.springframework.stereotype.Service;

/**
 * 内容社区积分兑换商品服务实现。
 */
@Service
public class ContentUserExchangeGoodsServiceImpl
    extends ServiceImpl<ContentUserExchangeGoodsMapper, ContentUserExchangeGoods>
    implements IContentUserExchangeGoodsService {
}
