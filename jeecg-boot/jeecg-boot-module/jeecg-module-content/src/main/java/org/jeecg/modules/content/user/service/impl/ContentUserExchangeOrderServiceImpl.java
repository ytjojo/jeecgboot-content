package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.user.entity.ContentUserExchangeOrder;
import org.jeecg.modules.content.user.mapper.ContentUserExchangeOrderMapper;
import org.jeecg.modules.content.user.service.IContentUserExchangeOrderService;
import org.springframework.stereotype.Service;

/**
 * 内容社区积分兑换订单服务实现。
 */
@Service
public class ContentUserExchangeOrderServiceImpl
    extends ServiceImpl<ContentUserExchangeOrderMapper, ContentUserExchangeOrder>
    implements IContentUserExchangeOrderService {
}
