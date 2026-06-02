package org.jeecg.modules.content.channel;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.jeecg.modules.content.channel.entity.Channel;
import org.jeecg.modules.content.channel.entity.ChannelContentPublish;
import org.jeecg.modules.content.channel.entity.ChannelTransfer;

/**
 * MyBatis-Plus lambda cache initializer for unit tests.
 *
 * MyBatis-Plus 内部维护一张实体类到列名映射的缓存（LambdaUtils.COLUMN_CACHE_MAP），
 * 在 Spring 容器启动时由 ServiceImpl 注册。在纯 Mockito 单元测试中，这张缓存为空，
 * 导致 service.lambdaQuery() / lambdaUpdate() 抛
 * "can not find lambda cache for this entity" 异常。
 *
 * 在 @BeforeAll 调用本类提供的方法即可预热缓存。
 */
public final class LambdaCacheInit {

    private LambdaCacheInit() {}

    public static synchronized void initAll() {
        init(Channel.class);
        init(ChannelTransfer.class);
        init(ChannelContentPublish.class);
    }

    public static synchronized void init(Class<?> entityClass) {
        if (LambdaUtils.getColumnMap(entityClass) != null) {
            return;
        }
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfo tableInfo = TableInfoHelper.initTableInfo(assistant, entityClass);
        if (tableInfo != null) {
            LambdaUtils.installCache(tableInfo);
        }
    }
}
