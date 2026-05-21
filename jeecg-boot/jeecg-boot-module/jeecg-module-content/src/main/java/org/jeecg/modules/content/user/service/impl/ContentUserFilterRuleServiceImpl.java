package org.jeecg.modules.content.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.user.entity.ContentUserFilterRule;
import org.jeecg.modules.content.user.mapper.ContentUserFilterRuleMapper;
import org.jeecg.modules.content.user.service.IContentUserFilterRuleService;
import org.springframework.stereotype.Service;

/**
 * 内容社区用户屏蔽规则服务实现。
 */
@Service
public class ContentUserFilterRuleServiceImpl
    extends ServiceImpl<ContentUserFilterRuleMapper, ContentUserFilterRule>
    implements IContentUserFilterRuleService {
}
