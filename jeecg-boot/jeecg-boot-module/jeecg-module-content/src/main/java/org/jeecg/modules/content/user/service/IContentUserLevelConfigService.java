package org.jeecg.modules.content.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.user.entity.ContentUserLevelConfig;

import java.util.List;

/**
 * 内容社区用户等级配置服务契约。
 */
public interface IContentUserLevelConfigService extends IService<ContentUserLevelConfig> {

    /**
     * 加载已启用且通过校验的等级配置。
     */
    List<ContentUserLevelConfig> listValidEnabledLevels();

    /**
     * 根据成长值计算当前等级。
     */
    int calculateLevel(Integer growthValue);
}
