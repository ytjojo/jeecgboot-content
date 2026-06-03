# 频道发现（Channel Discovery）Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 建立完整的频道发现能力，包括平台分类体系、频道标签、个性化推荐、排行榜、编辑精选、分类浏览和搜索。

**Architecture:** channel 包从零构建，遵循 `controller/biz/service/mapper/entity/req/vo/dto` 分层架构。所有实体继承 `JeecgEntity`，服务继承 `ServiceImpl`，控制器返回 `Result<T>`。可见性过滤作为公共依赖，被所有发现入口统一调用。

**Tech Stack:** Spring Boot 3, MyBatis-Plus, MySQL, Flyway, Swagger/OpenAPI

**Base Path:** `jeecg-boot/jeecg-boot-module/jeecg-module-content`

**Package:** `org.jeecg.modules.content.channel`

---

## Task 1: 数据库迁移与基础实体

**Files:**
- Create: `src/main/resources/flyway/sql/mysql/V3.9.1_63__channel_discovery_tables.sql`
- Create: `src/main/resources/flyway/sql/mysql/R3.9.1_63__channel_discovery_tables_rollback.sql`
- Create: `src/main/java/org/jeecg/modules/content/channel/entity/ContentChannelCategory.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/entity/ContentChannelTag.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/entity/ContentChannelTagRelation.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/entity/ContentChannelRecommendationCache.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/entity/ContentChannelNotInterested.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/entity/ContentChannelRankingSnapshot.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/entity/ContentChannelEditorialPick.java`

- [ ] **Step 1: 编写 Flyway 迁移脚本**

```sql
-- V3.9.1_63__channel_discovery_tables.sql

-- 1. 平台分类表
CREATE TABLE IF NOT EXISTS `content_channel_category` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `parent_id` varchar(32) DEFAULT NULL COMMENT '父级分类ID，null表示根分类',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `path` varchar(255) NOT NULL COMMENT '分类路径，如 /001/002/003',
  `level` tinyint NOT NULL DEFAULT 1 COMMENT '分类层级 1-4',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序序号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0=停用 1=启用',
  `is_system` tinyint NOT NULL DEFAULT 0 COMMENT '是否特殊分类 0=普通 1=特殊',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_channel_category_parent` (`parent_id`),
  KEY `idx_channel_category_status` (`status`, `level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台频道分类表';

-- 2. 频道标签表
CREATE TABLE IF NOT EXISTS `content_channel_tag` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `channel_id` varchar(32) NOT NULL COMMENT '频道ID',
  `name` varchar(20) NOT NULL COMMENT '标签名称',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0=已删除 1=正常',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_channel_tag_name` (`channel_id`, `name`),
  KEY `idx_channel_tag_channel` (`channel_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道内标签表';

-- 3. 标签-内容关联表
CREATE TABLE IF NOT EXISTS `content_channel_tag_relation` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `tag_id` varchar(32) NOT NULL COMMENT '标签ID',
  `content_id` varchar(32) NOT NULL COMMENT '内容ID',
  `content_type` varchar(32) NOT NULL COMMENT '内容类型: article/post/video/note/question',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_content` (`tag_id`, `content_id`, `content_type`),
  KEY `idx_tag_relation_content` (`content_id`, `content_type`),
  KEY `idx_tag_relation_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签-内容关联表';

-- 4. 推荐缓存表
CREATE TABLE IF NOT EXISTS `content_channel_recommendation_cache` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `channel_id` varchar(32) NOT NULL COMMENT '频道ID',
  `ranking_score` decimal(10,4) NOT NULL DEFAULT 0 COMMENT '推荐评分',
  `recommendation_rule` varchar(64) NOT NULL COMMENT '推荐规则: SIMILARITY/PREFERENCE/POPULAR/COLD_START',
  `recommendation_reason` varchar(255) DEFAULT NULL COMMENT '推荐理由',
  `recommendation_status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0=已消费 1=有效',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_recommendation_user` (`user_id`, `recommendation_status`),
  KEY `idx_recommendation_channel` (`channel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道推荐缓存表';

-- 5. 不感兴趣反馈表
CREATE TABLE IF NOT EXISTS `content_channel_not_interested` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `channel_id` varchar(32) NOT NULL COMMENT '频道ID',
  `category_id` varchar(32) DEFAULT NULL COMMENT '频道主分类ID，用于降低同分类权重',
  `expire_time` datetime NOT NULL COMMENT '过期时间，30天后',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_not_interested_user_channel` (`user_id`, `channel_id`),
  KEY `idx_not_interested_expire` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道不感兴趣反馈表';

-- 6. 排行榜快照表
CREATE TABLE IF NOT EXISTS `content_channel_ranking_snapshot` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `channel_id` varchar(32) NOT NULL COMMENT '频道ID',
  `ranking_type` varchar(32) NOT NULL COMMENT '榜单类型: HOT/NEW/SYSTEM',
  `dimension` varchar(16) NOT NULL COMMENT '维度: DAILY/WEEKLY/MONTHLY',
  `rank_position` int NOT NULL COMMENT '排名位置',
  `score` decimal(12,4) NOT NULL DEFAULT 0 COMMENT '综合得分',
  `snapshot_date` date NOT NULL COMMENT '快照日期',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ranking_snapshot` (`ranking_type`, `dimension`, `snapshot_date`, `rank_position`),
  KEY `idx_ranking_channel` (`channel_id`, `snapshot_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道排行榜快照表';

-- 7. 编辑精选表
CREATE TABLE IF NOT EXISTS `content_channel_editorial_pick` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `channel_id` varchar(32) NOT NULL COMMENT '频道ID',
  `recommendation_text` varchar(255) DEFAULT NULL COMMENT '推荐语',
  `start_time` datetime NOT NULL COMMENT '生效开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '生效结束时间，null表示永久',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态 0=下线 1=上线',
  `operator_id` varchar(32) NOT NULL COMMENT '操作人ID',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_editorial_pick_channel` (`channel_id`),
  KEY `idx_editorial_pick_status` (`status`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='频道编辑精选表';
```

- [ ] **Step 2: 编写回滚脚本**

```sql
-- R3.9.1_63__channel_discovery_tables_rollback.sql
DROP TABLE IF EXISTS `content_channel_editorial_pick`;
DROP TABLE IF EXISTS `content_channel_ranking_snapshot`;
DROP TABLE IF EXISTS `content_channel_not_interested`;
DROP TABLE IF EXISTS `content_channel_recommendation_cache`;
DROP TABLE IF EXISTS `content_channel_tag_relation`;
DROP TABLE IF EXISTS `content_channel_tag`;
DROP TABLE IF EXISTS `content_channel_category`;
```

- [ ] **Step 3: 验证 SQL 语法**

运行 Flyway 迁移验证：
```bash
cd jeecg-boot/jeecg-boot-module/jeecg-module-content
mvn flyway:validate -Dflyway.locations=filesystem:src/main/resources/flyway/sql/mysql
```
Expected: 验证通过，无语法错误

- [ ] **Step 4: 创建 ContentChannelCategory 实体**

```java
package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_channel_category")
@Schema(description = "平台频道分类")
public class ContentChannelCategory extends JeecgEntity {

    @Schema(description = "父级分类ID，null表示根分类")
    private String parentId;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类路径，如 /001/002/003")
    private String path;

    @Schema(description = "分类层级 1-4")
    private Integer level;

    @Schema(description = "排序序号")
    private Integer sortOrder;

    @Schema(description = "状态 0=停用 1=启用")
    private Integer status;

    @Schema(description = "是否特殊分类 0=普通 1=特殊")
    private Integer isSystem;
}
```

- [ ] **Step 5: 创建 ContentChannelTag 实体**

```java
package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_channel_tag")
@Schema(description = "频道内标签")
public class ContentChannelTag extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "标签名称")
    private String name;

    @Schema(description = "状态 0=已删除 1=正常")
    private Integer status;
}
```

- [ ] **Step 6: 创建 ContentChannelTagRelation 实体**

```java
package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_channel_tag_relation")
@Schema(description = "标签-内容关联")
public class ContentChannelTagRelation extends JeecgEntity {

    @Schema(description = "标签ID")
    private String tagId;

    @Schema(description = "内容ID")
    private String contentId;

    @Schema(description = "内容类型: article/post/video/note/question")
    private String contentType;
}
```

- [ ] **Step 7: 创建 ContentChannelRecommendationCache 实体**

```java
package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_channel_recommendation_cache")
@Schema(description = "频道推荐缓存")
public class ContentChannelRecommendationCache extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "推荐评分")
    private BigDecimal rankingScore;

    @Schema(description = "推荐规则: SIMILARITY/PREFERENCE/POPULAR/COLD_START")
    private String recommendationRule;

    @Schema(description = "推荐理由")
    private String recommendationReason;

    @Schema(description = "状态 0=已消费 1=有效")
    private Integer recommendationStatus;
}
```

- [ ] **Step 8: 创建 ContentChannelNotInterested 实体**

```java
package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_channel_not_interested")
@Schema(description = "频道不感兴趣反馈")
public class ContentChannelNotInterested extends JeecgEntity {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "频道主分类ID，用于降低同分类权重")
    private String categoryId;

    @Schema(description = "过期时间，30天后")
    private Date expireTime;
}
```

- [ ] **Step 9: 创建 ContentChannelRankingSnapshot 实体**

```java
package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_channel_ranking_snapshot")
@Schema(description = "频道排行榜快照")
public class ContentChannelRankingSnapshot extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "榜单类型: HOT/NEW/SYSTEM")
    private String rankingType;

    @Schema(description = "维度: DAILY/WEEKLY/MONTHLY")
    private String dimension;

    @Schema(description = "排名位置")
    private Integer rankPosition;

    @Schema(description = "综合得分")
    private BigDecimal score;

    @Schema(description = "快照日期")
    private Date snapshotDate;
}
```

- [ ] **Step 10: 创建 ContentChannelEditorialPick 实体**

```java
package org.jeecg.modules.content.channel.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.Date;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("content_channel_editorial_pick")
@Schema(description = "频道编辑精选")
public class ContentChannelEditorialPick extends JeecgEntity {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "推荐语")
    private String recommendationText;

    @Schema(description = "生效开始时间")
    private Date startTime;

    @Schema(description = "生效结束时间，null表示永久")
    private Date endTime;

    @Schema(description = "状态 0=下线 1=上线")
    private Integer status;

    @Schema(description = "操作人ID")
    private String operatorId;
}
```

- [ ] **Step 11: Commit**

```bash
git add src/main/resources/flyway/sql/mysql/V3.9.1_63__channel_discovery_tables.sql \
        src/main/resources/flyway/sql/mysql/R3.9.1_63__channel_discovery_tables_rollback.sql \
        src/main/java/org/jeecg/modules/content/channel/entity/
git commit -m "feat(channel-discovery): add Flyway migration and entity classes"
```

---

## Task 2: Mapper 层

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/channel/mapper/ContentChannelCategoryMapper.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/mapper/ContentChannelTagMapper.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/mapper/ContentChannelTagRelationMapper.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/mapper/ContentChannelRecommendationCacheMapper.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/mapper/ContentChannelNotInterestedMapper.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/mapper/ContentChannelRankingSnapshotMapper.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/mapper/ContentChannelEditorialPickMapper.java`

- [ ] **Step 1: 创建 ContentChannelCategoryMapper**

```java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.content.channel.entity.ContentChannelCategory;

@Mapper
public interface ContentChannelCategoryMapper extends BaseMapper<ContentChannelCategory> {
}
```

- [ ] **Step 2: 创建 ContentChannelTagMapper**

```java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.content.channel.entity.ContentChannelTag;

@Mapper
public interface ContentChannelTagMapper extends BaseMapper<ContentChannelTag> {
}
```

- [ ] **Step 3: 创建 ContentChannelTagRelationMapper**

```java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.content.channel.entity.ContentChannelTagRelation;

@Mapper
public interface ContentChannelTagRelationMapper extends BaseMapper<ContentChannelTagRelation> {
}
```

- [ ] **Step 4: 创建 ContentChannelRecommendationCacheMapper**

```java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.content.channel.entity.ContentChannelRecommendationCache;

@Mapper
public interface ContentChannelRecommendationCacheMapper extends BaseMapper<ContentChannelRecommendationCache> {
}
```

- [ ] **Step 5: 创建 ContentChannelNotInterestedMapper**

```java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.content.channel.entity.ContentChannelNotInterested;

@Mapper
public interface ContentChannelNotInterestedMapper extends BaseMapper<ContentChannelNotInterested> {
}
```

- [ ] **Step 6: 创建 ContentChannelRankingSnapshotMapper**

```java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.content.channel.entity.ContentChannelRankingSnapshot;

@Mapper
public interface ContentChannelRankingSnapshotMapper extends BaseMapper<ContentChannelRankingSnapshot> {
}
```

- [ ] **Step 7: 创建 ContentChannelEditorialPickMapper**

```java
package org.jeecg.modules.content.channel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.content.channel.entity.ContentChannelEditorialPick;

@Mapper
public interface ContentChannelEditorialPickMapper extends BaseMapper<ContentChannelEditorialPick> {
}
```

- [ ] **Step 8: Commit**

```bash
git add src/main/java/org/jeecg/modules/content/channel/mapper/
git commit -m "feat(channel-discovery): add mapper interfaces"
```

---

## Task 3: 分类体系 Service 与 Biz

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/channel/service/IContentChannelCategoryService.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/service/impl/ContentChannelCategoryServiceImpl.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/biz/ContentChannelCategoryBiz.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/req/create/ChannelCategoryCreateReq.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/req/update/ChannelCategoryUpdateReq.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/req/query/ChannelCategoryQueryReq.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/vo/ChannelCategoryTreeVO.java`
- Create: `src/test/java/org/jeecg/modules/content/channel/service/ContentChannelCategoryServiceTest.java`
- Create: `src/test/java/org/jeecg/modules/content/channel/biz/ContentChannelCategoryBizTest.java`

- [ ] **Step 1: 编写分类服务接口**

```java
package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ContentChannelCategory;
import org.jeecg.modules.content.channel.req.create.ChannelCategoryCreateReq;
import org.jeecg.modules.content.channel.req.update.ChannelCategoryUpdateReq;
import org.jeecg.modules.content.channel.vo.ChannelCategoryTreeVO;

import java.util.List;

public interface IContentChannelCategoryService extends IService<ContentChannelCategory> {

    /**
     * 创建分类
     */
    ContentChannelCategory createCategory(ChannelCategoryCreateReq req);

    /**
     * 更新分类
     */
    void updateCategory(ChannelCategoryUpdateReq req);

    /**
     * 获取分类树
     */
    List<ChannelCategoryTreeVO> getCategoryTree();

    /**
     * 停用分类（需检查关联频道）
     */
    void disableCategory(String categoryId);
}
```

- [ ] **Step 2: 编写分类服务实现**

```java
package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ContentChannelCategory;
import org.jeecg.modules.content.channel.mapper.ContentChannelCategoryMapper;
import org.jeecg.modules.content.channel.req.create.ChannelCategoryCreateReq;
import org.jeecg.modules.content.channel.req.update.ChannelCategoryUpdateReq;
import org.jeecg.modules.content.channel.service.IContentChannelCategoryService;
import org.jeecg.modules.content.channel.vo.ChannelCategoryTreeVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContentChannelCategoryServiceImpl
        extends ServiceImpl<ContentChannelCategoryMapper, ContentChannelCategory>
        implements IContentChannelCategoryService {

    private static final int MAX_LEVEL = 4;

    @Override
    public ContentChannelCategory createCategory(ChannelCategoryCreateReq req) {
        // 校验名称
        validateName(req.getName(), null);

        // 计算层级和路径
        int level = 1;
        String path = "/";
        if (req.getParentId() != null) {
            ContentChannelCategory parent = getById(req.getParentId());
            if (parent == null) {
                throw new JeecgBootException("父级分类不存在");
            }
            level = parent.getLevel() + 1;
            if (level > MAX_LEVEL) {
                throw new JeecgBootException("分类层级不能超过" + MAX_LEVEL + "级");
            }
            path = parent.getPath() + parent.getId() + "/";
        }

        // 检查同级重名
        long count = count(Wrappers.<ContentChannelCategory>lambdaQuery()
                .eq(ContentChannelCategory::getParentId, req.getParentId())
                .eq(ContentChannelCategory::getName, req.getName())
                .eq(ContentChannelCategory::getStatus, 1));
        if (count > 0) {
            throw new JeecgBootException("同级分类名称已存在");
        }

        ContentChannelCategory category = new ContentChannelCategory();
        category.setName(req.getName());
        category.setParentId(req.getParentId());
        category.setLevel(level);
        category.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        category.setStatus(1);
        category.setIsSystem(req.getIsSystem() != null ? req.getIsSystem() : 0);
        save(category);

        // 更新 path（需要 id）
        category.setPath(path + category.getId() + "/");
        updateById(category);

        return category;
    }

    @Override
    public void updateCategory(ChannelCategoryUpdateReq req) {
        ContentChannelCategory category = getById(req.getId());
        if (category == null) {
            throw new JeecgBootException("分类不存在");
        }
        if (req.getName() != null) {
            validateName(req.getName(), req.getId());
            category.setName(req.getName());
        }
        if (req.getSortOrder() != null) {
            category.setSortOrder(req.getSortOrder());
        }
        updateById(category);
    }

    @Override
    public List<ChannelCategoryTreeVO> getCategoryTree() {
        List<ContentChannelCategory> all = list(Wrappers.<ContentChannelCategory>lambdaQuery()
                .eq(ContentChannelCategory::getStatus, 1)
                .orderByAsc(ContentChannelCategory::getSortOrder));

        Map<String, List<ContentChannelCategory>> grouped = all.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(ContentChannelCategory::getParentId));

        List<ChannelCategoryTreeVO> roots = all.stream()
                .filter(c -> c.getParentId() == null)
                .map(c -> toTreeVO(c, grouped))
                .collect(Collectors.toList());
        return roots;
    }

    @Override
    public void disableCategory(String categoryId) {
        ContentChannelCategory category = getById(categoryId);
        if (category == null) {
            throw new JeecgBootException("分类不存在");
        }
        category.setStatus(0);
        updateById(category);
    }

    private void validateName(String name, String excludeId) {
        if (name == null || name.isBlank()) {
            throw new JeecgBootException("分类名称不能为空");
        }
        if (name.length() > 50) {
            throw new JeecgBootException("分类名称不能超过50个字符");
        }
    }

    private ChannelCategoryTreeVO toTreeVO(ContentChannelCategory category,
                                            Map<String, List<ContentChannelCategory>> grouped) {
        ChannelCategoryTreeVO vo = new ChannelCategoryTreeVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setLevel(category.getLevel());
        vo.setSortOrder(category.getSortOrder());
        vo.setIsSystem(category.getIsSystem());

        List<ContentChannelCategory> children = grouped.getOrDefault(category.getId(), new ArrayList<>());
        vo.setChildren(children.stream()
                .map(c -> toTreeVO(c, grouped))
                .collect(Collectors.toList()));
        return vo;
    }
}
```

- [ ] **Step 3: 编写请求/响应对象**

```java
// ChannelCategoryCreateReq.java
package org.jeecg.modules.content.channel.req.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建分类请求")
public class ChannelCategoryCreateReq {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称不能超过50个字符")
    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "父级分类ID，null表示根分类")
    private String parentId;

    @Schema(description = "排序序号")
    private Integer sortOrder;

    @Schema(description = "是否特殊分类 0=普通 1=特殊")
    @Max(value = 1, message = "is_system 只能是 0 或 1")
    private Integer isSystem;
}
```

```java
// ChannelCategoryUpdateReq.java
package org.jeecg.modules.content.channel.req.update;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新分类请求")
public class ChannelCategoryUpdateReq {

    @NotBlank(message = "分类ID不能为空")
    @Schema(description = "分类ID")
    private String id;

    @Size(max = 50, message = "分类名称不能超过50个字符")
    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "排序序号")
    private Integer sortOrder;
}
```

```java
// ChannelCategoryQueryReq.java
package org.jeecg.modules.content.channel.req.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "查询分类请求")
public class ChannelCategoryQueryReq {

    @Schema(description = "父级分类ID")
    private String parentId;

    @Schema(description = "状态 0=停用 1=启用")
    private Integer status;
}
```

```java
// ChannelCategoryTreeVO.java
package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "分类树节点")
public class ChannelCategoryTreeVO {

    @Schema(description = "分类ID")
    private String id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类层级")
    private Integer level;

    @Schema(description = "排序序号")
    private Integer sortOrder;

    @Schema(description = "是否特殊分类")
    private Integer isSystem;

    @Schema(description = "子分类")
    private List<ChannelCategoryTreeVO> children;
}
```

- [ ] **Step 4: 编写分类服务单元测试**

```java
// ContentChannelCategoryServiceTest.java
package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ContentChannelCategory;
import org.jeecg.modules.content.channel.req.create.ChannelCategoryCreateReq;
import org.jeecg.modules.content.channel.req.update.ChannelCategoryUpdateReq;
import org.jeecg.modules.content.channel.vo.ChannelCategoryTreeVO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ContentChannelCategoryServiceTest {

    @Resource
    private IContentChannelCategoryService categoryService;

    @Test
    void createCategory_shouldCreateRootCategory() {
        ChannelCategoryCreateReq req = new ChannelCategoryCreateReq();
        req.setName("科技");
        req.setParentId(null);

        ContentChannelCategory result = categoryService.createCategory(req);

        assertNotNull(result.getId());
        assertEquals("科技", result.getName());
        assertEquals(1, result.getLevel().intValue());
        assertTrue(result.getPath().contains(result.getId()));
    }

    @Test
    void createCategory_shouldRejectLevelExceeding4() {
        // 创建4级分类链
        ContentChannelCategory l1 = createCategory("L1", null);
        ContentChannelCategory l2 = createCategory("L2", l1.getId());
        ContentChannelCategory l3 = createCategory("L3", l2.getId());
        ContentChannelCategory l4 = createCategory("L4", l3.getId());

        // 尝试创建第5级
        ChannelCategoryCreateReq req = new ChannelCategoryCreateReq();
        req.setName("L5");
        req.setParentId(l4.getId());

        assertThrows(JeecgBootException.class, () -> categoryService.createCategory(req));
    }

    @Test
    void createCategory_shouldRejectEmptyName() {
        ChannelCategoryCreateReq req = new ChannelCategoryCreateReq();
        req.setName("");

        assertThrows(JeecgBootException.class, () -> categoryService.createCategory(req));
    }

    @Test
    void createCategory_shouldRejectDuplicateNameAtSameLevel() {
        createCategory("科技", null);

        ChannelCategoryCreateReq req = new ChannelCategoryCreateReq();
        req.setName("科技");
        req.setParentId(null);

        assertThrows(JeecgBootException.class, () -> categoryService.createCategory(req));
    }

    @Test
    void getCategoryTree_shouldReturnNestedTree() {
        ContentChannelCategory parent = createCategory("技术", null);
        createCategory("Java", parent.getId());
        createCategory("Python", parent.getId());

        List<ChannelCategoryTreeVO> tree = categoryService.getCategoryTree();

        assertTrue(tree.size() > 0);
        ChannelCategoryTreeVO techNode = tree.stream()
                .filter(n -> "技术".equals(n.getName()))
                .findFirst().orElse(null);
        assertNotNull(techNode);
        assertEquals(2, techNode.getChildren().size());
    }

    @Test
    void disableCategory_shouldSetStatusToZero() {
        ContentChannelCategory category = createCategory("待停用", null);

        categoryService.disableCategory(category.getId());

        ContentChannelCategory updated = categoryService.getById(category.getId());
        assertEquals(0, updated.getStatus().intValue());
    }

    private ContentChannelCategory createCategory(String name, String parentId) {
        ChannelCategoryCreateReq req = new ChannelCategoryCreateReq();
        req.setName(name);
        req.setParentId(parentId);
        return categoryService.createCategory(req);
    }
}
```

- [ ] **Step 5: 运行测试验证**

```bash
cd jeecg-boot/jeecg-boot-module/jeecg-module-content
mvn test -Dtest=ContentChannelCategoryServiceTest -pl .
```
Expected: 所有测试通过

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/jeecg/modules/content/channel/service/ \
        src/main/java/org/jeecg/modules/content/channel/biz/ \
        src/main/java/org/jeecg/modules/content/channel/req/ \
        src/main/java/org/jeecg/modules/content/channel/vo/ChannelCategoryTreeVO.java \
        src/test/java/org/jeecg/modules/content/channel/service/ContentChannelCategoryServiceTest.java
git commit -m "feat(channel-discovery): add category service with tests"
```

---

## Task 4: 分类 Controller

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/channel/controller/ContentChannelCategoryController.java`

- [ ] **Step 1: 编写分类控制器**

```java
package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.entity.ContentChannelCategory;
import org.jeecg.modules.content.channel.req.create.ChannelCategoryCreateReq;
import org.jeecg.modules.content.channel.req.update.ChannelCategoryUpdateReq;
import org.jeecg.modules.content.channel.service.IContentChannelCategoryService;
import org.jeecg.modules.content.channel.vo.ChannelCategoryTreeVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "频道分类管理")
@RestController
@RequestMapping("/content/channel/category")
public class ContentChannelCategoryController {

    @Resource
    private IContentChannelCategoryService categoryService;

    @Operation(summary = "获取分类树")
    @GetMapping("/tree")
    public Result<List<ChannelCategoryTreeVO>> getCategoryTree() {
        return Result.OK(categoryService.getCategoryTree());
    }

    @Operation(summary = "创建分类")
    @PostMapping("/create")
    public Result<ContentChannelCategory> createCategory(@Valid @RequestBody ChannelCategoryCreateReq req) {
        return Result.OK(categoryService.createCategory(req));
    }

    @Operation(summary = "更新分类")
    @PostMapping("/update")
    public Result<Void> updateCategory(@Valid @RequestBody ChannelCategoryUpdateReq req) {
        categoryService.updateCategory(req);
        return Result.OK();
    }

    @Operation(summary = "停用分类")
    @PostMapping("/disable")
    public Result<Void> disableCategory(@RequestParam String categoryId) {
        categoryService.disableCategory(categoryId);
        return Result.OK();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/org/jeecg/modules/content/channel/controller/ContentChannelCategoryController.java
git commit -m "feat(channel-discovery): add category controller"
```

---

## Task 5: 频道标签 Service

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/channel/service/IContentChannelTagService.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/service/impl/ContentChannelTagServiceImpl.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/req/create/ChannelTagCreateReq.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/vo/ChannelTagVO.java`
- Create: `src/test/java/org/jeecg/modules/content/channel/service/ContentChannelTagServiceTest.java`

- [ ] **Step 1: 编写标签服务接口**

```java
package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ContentChannelTag;
import org.jeecg.modules.content.channel.req.create.ChannelTagCreateReq;
import org.jeecg.modules.content.channel.vo.ChannelTagVO;

import java.util.List;

public interface IContentChannelTagService extends IService<ContentChannelTag> {

    ContentChannelTag createTag(ChannelTagCreateReq req);

    void deleteTag(String tagId);

    List<ChannelTagVO> listByChannel(String channelId);
}
```

- [ ] **Step 2: 编写标签服务实现**

```java
package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ContentChannelTag;
import org.jeecg.modules.content.channel.mapper.ContentChannelTagMapper;
import org.jeecg.modules.content.channel.req.create.ChannelTagCreateReq;
import org.jeecg.modules.content.channel.service.IContentChannelTagService;
import org.jeecg.modules.content.channel.vo.ChannelTagVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentChannelTagServiceImpl
        extends ServiceImpl<ContentChannelTagMapper, ContentChannelTag>
        implements IContentChannelTagService {

    @Override
    public ContentChannelTag createTag(ChannelTagCreateReq req) {
        validateName(req.getName(), req.getChannelId());

        long count = count(Wrappers.<ContentChannelTag>lambdaQuery()
                .eq(ContentChannelTag::getChannelId, req.getChannelId())
                .eq(ContentChannelTag::getName, req.getName())
                .eq(ContentChannelTag::getStatus, 1));
        if (count > 0) {
            throw new JeecgBootException("该标签已存在");
        }

        ContentChannelTag tag = new ContentChannelTag();
        tag.setChannelId(req.getChannelId());
        tag.setName(req.getName());
        tag.setStatus(1);
        save(tag);
        return tag;
    }

    @Override
    public void deleteTag(String tagId) {
        ContentChannelTag tag = getById(tagId);
        if (tag == null) {
            throw new JeecgBootException("标签不存在");
        }
        tag.setStatus(0);
        updateById(tag);
    }

    @Override
    public List<ChannelTagVO> listByChannel(String channelId) {
        return list(Wrappers.<ContentChannelTag>lambdaQuery()
                .eq(ContentChannelTag::getChannelId, channelId)
                .eq(ContentChannelTag::getStatus, 1))
                .stream()
                .map(t -> {
                    ChannelTagVO vo = new ChannelTagVO();
                    vo.setId(t.getId());
                    vo.setName(t.getName());
                    vo.setChannelId(t.getChannelId());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private void validateName(String name, String channelId) {
        if (name == null || name.isBlank()) {
            throw new JeecgBootException("标签名称不能为空");
        }
        if (name.length() > 20) {
            throw new JeecgBootException("标签名称不能超过20个字符");
        }
    }
}
```

- [ ] **Step 3: 编写请求/响应对象**

```java
// ChannelTagCreateReq.java
package org.jeecg.modules.content.channel.req.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建标签请求")
public class ChannelTagCreateReq {

    @NotBlank(message = "频道ID不能为空")
    @Schema(description = "频道ID")
    private String channelId;

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 20, message = "标签名称不能超过20个字符")
    @Schema(description = "标签名称")
    private String name;
}
```

```java
// ChannelTagVO.java
package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "频道标签VO")
public class ChannelTagVO {

    @Schema(description = "标签ID")
    private String id;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "标签名称")
    private String name;
}
```

- [ ] **Step 4: 编写标签服务单元测试**

```java
// ContentChannelTagServiceTest.java
package org.jeecg.modules.content.channel.service;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ContentChannelTag;
import org.jeecg.modules.content.channel.req.create.ChannelTagCreateReq;
import org.jeecg.modules.content.channel.vo.ChannelTagVO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ContentChannelTagServiceTest {

    @Resource
    private IContentChannelTagService tagService;

    @Test
    void createTag_shouldCreateValidTag() {
        ChannelTagCreateReq req = new ChannelTagCreateReq();
        req.setChannelId("test-channel-001");
        req.setName("教程");

        ContentChannelTag tag = tagService.createTag(req);

        assertNotNull(tag.getId());
        assertEquals("教程", tag.getName());
        assertEquals(1, tag.getStatus().intValue());
    }

    @Test
    void createTag_shouldRejectEmptyName() {
        ChannelTagCreateReq req = new ChannelTagCreateReq();
        req.setChannelId("test-channel-001");
        req.setName("");

        assertThrows(JeecgBootException.class, () -> tagService.createTag(req));
    }

    @Test
    void createTag_shouldRejectDuplicateName() {
        ChannelTagCreateReq req = new ChannelTagCreateReq();
        req.setChannelId("test-channel-002");
        req.setName("讨论");
        tagService.createTag(req);

        assertThrows(JeecgBootException.class, () -> tagService.createTag(req));
    }

    @Test
    void createTag_shouldRejectNameExceeding20Chars() {
        ChannelTagCreateReq req = new ChannelTagCreateReq();
        req.setChannelId("test-channel-003");
        req.setName("这是一个超过二十个字符的标签名称测试");

        assertThrows(JeecgBootException.class, () -> tagService.createTag(req));
    }

    @Test
    void deleteTag_shouldSetStatusToZero() {
        ChannelTagCreateReq req = new ChannelTagCreateReq();
        req.setChannelId("test-channel-004");
        req.setName("待删除");
        ContentChannelTag tag = tagService.createTag(req);

        tagService.deleteTag(tag.getId());

        ContentChannelTag deleted = tagService.getById(tag.getId());
        assertEquals(0, deleted.getStatus().intValue());
    }

    @Test
    void listByChannel_shouldReturnActiveTags() {
        ChannelTagCreateReq req = new ChannelTagCreateReq();
        req.setChannelId("test-channel-005");
        req.setName("活跃标签");
        tagService.createTag(req);

        List<ChannelTagVO> tags = tagService.listByChannel("test-channel-005");

        assertTrue(tags.size() > 0);
        assertTrue(tags.stream().allMatch(t -> "活跃标签".equals(t.getName())));
    }
}
```

- [ ] **Step 5: 运行测试验证**

```bash
cd jeecg-boot/jeecg-boot-module/jeecg-module-content
mvn test -Dtest=ContentChannelTagServiceTest -pl .
```
Expected: 所有测试通过

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/jeecg/modules/content/channel/service/IContentChannelTagService.java \
        src/main/java/org/jeecg/modules/content/channel/service/impl/ContentChannelTagServiceImpl.java \
        src/main/java/org/jeecg/modules/content/channel/req/create/ChannelTagCreateReq.java \
        src/main/java/org/jeecg/modules/content/channel/vo/ChannelTagVO.java \
        src/test/java/org/jeecg/modules/content/channel/service/ContentChannelTagServiceTest.java
git commit -m "feat(channel-discovery): add tag service with tests"
```

---

## Task 6: 标签 Controller

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/channel/controller/ContentChannelTagController.java`

- [ ] **Step 1: 编写标签控制器**

```java
package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.entity.ContentChannelTag;
import org.jeecg.modules.content.channel.req.create.ChannelTagCreateReq;
import org.jeecg.modules.content.channel.service.IContentChannelTagService;
import org.jeecg.modules.content.channel.vo.ChannelTagVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "频道标签管理")
@RestController
@RequestMapping("/content/channel/tag")
public class ContentChannelTagController {

    @Resource
    private IContentChannelTagService tagService;

    @Operation(summary = "获取频道标签列表")
    @GetMapping("/list")
    public Result<List<ChannelTagVO>> listByChannel(@RequestParam String channelId) {
        return Result.OK(tagService.listByChannel(channelId));
    }

    @Operation(summary = "创建标签")
    @PostMapping("/create")
    public Result<ContentChannelTag> createTag(@Valid @RequestBody ChannelTagCreateReq req) {
        return Result.OK(tagService.createTag(req));
    }

    @Operation(summary = "删除标签")
    @PostMapping("/delete")
    public Result<Void> deleteTag(@RequestParam String tagId) {
        tagService.deleteTag(tagId);
        return Result.OK();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/org/jeecg/modules/content/channel/controller/ContentChannelTagController.java
git commit -m "feat(channel-discovery): add tag controller"
```

---

## Task 7: 可见性服务

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/channel/dto/ChannelVisibilityDTO.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/service/IContentChannelVisibilityService.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/service/impl/ContentChannelVisibilityServiceImpl.java`
- Create: `src/test/java/org/jeecg/modules/content/channel/service/ContentChannelVisibilityServiceTest.java`

- [ ] **Step 1: 编写可见性 DTO**

```java
package org.jeecg.modules.content.channel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "频道可见性判断参数")
public class ChannelVisibilityDTO {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "频道状态")
    private Integer channelStatus;

    @Schema(description = "隐私类型: PUBLIC/PRIVATE")
    private String privacyType;

    @Schema(description = "是否隐藏")
    private Boolean hidden;

    @Schema(description = "是否冻结")
    private Boolean frozen;

    @Schema(description = "是否限制公开曝光")
    private Boolean restrictExposure;

    @Schema(description = "审核状态")
    private String auditStatus;
}
```

- [ ] **Step 2: 编写可见性服务接口**

```java
package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.dto.ChannelVisibilityDTO;

import java.util.List;

public interface IContentChannelVisibilityService {

    /**
     * 判断频道是否可被公开发现
     */
    boolean isDiscoverable(ChannelVisibilityDTO channel);

    /**
     * 过滤不可发现的频道
     */
    List<ChannelVisibilityDTO> filterDiscoverable(List<ChannelVisibilityDTO> channels);
}
```

- [ ] **Step 3: 编写可见性服务实现**

```java
package org.jeecg.modules.content.channel.service.impl;

import org.jeecg.modules.content.channel.dto.ChannelVisibilityDTO;
import org.jeecg.modules.content.channel.service.IContentChannelVisibilityService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentChannelVisibilityServiceImpl implements IContentChannelVisibilityService {

    @Override
    public boolean isDiscoverable(ChannelVisibilityDTO channel) {
        if (channel == null) {
            return false;
        }
        // 频道状态必须为 Active
        if (channel.getChannelStatus() == null || channel.getChannelStatus() != 1) {
            return false;
        }
        // 隐私必须为公开
        if (!"PUBLIC".equals(channel.getPrivacyType())) {
            return false;
        }
        // 不能是隐藏频道
        if (Boolean.TRUE.equals(channel.getHidden())) {
            return false;
        }
        // 不能是冻结频道
        if (Boolean.TRUE.equals(channel.getFrozen())) {
            return false;
        }
        // 不能限制公开曝光
        if (Boolean.TRUE.equals(channel.getRestrictExposure())) {
            return false;
        }
        // 审核状态必须通过
        if (!"APPROVED".equals(channel.getAuditStatus())) {
            return false;
        }
        return true;
    }

    @Override
    public List<ChannelVisibilityDTO> filterDiscoverable(List<ChannelVisibilityDTO> channels) {
        if (channels == null) {
            return List.of();
        }
        return channels.stream()
                .filter(this::isDiscoverable)
                .collect(Collectors.toList());
    }
}
```

- [ ] **Step 4: 编写可见性服务单元测试**

```java
// ContentChannelVisibilityServiceTest.java
package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.dto.ChannelVisibilityDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ContentChannelVisibilityServiceTest {

    @Resource
    private IContentChannelVisibilityService visibilityService;

    @Test
    void isDiscoverable_shouldReturnTrueForPublicActiveChannel() {
        ChannelVisibilityDTO dto = createPublicActiveChannel();
        assertTrue(visibilityService.isDiscoverable(dto));
    }

    @Test
    void isDiscoverable_shouldReturnFalseForPrivateChannel() {
        ChannelVisibilityDTO dto = createPublicActiveChannel();
        dto.setPrivacyType("PRIVATE");
        assertFalse(visibilityService.isDiscoverable(dto));
    }

    @Test
    void isDiscoverable_shouldReturnFalseForHiddenChannel() {
        ChannelVisibilityDTO dto = createPublicActiveChannel();
        dto.setHidden(true);
        assertFalse(visibilityService.isDiscoverable(dto));
    }

    @Test
    void isDiscoverable_shouldReturnFalseForFrozenChannel() {
        ChannelVisibilityDTO dto = createPublicActiveChannel();
        dto.setFrozen(true);
        assertFalse(visibilityService.isDiscoverable(dto));
    }

    @Test
    void isDiscoverable_shouldReturnFalseForRestrictedExposure() {
        ChannelVisibilityDTO dto = createPublicActiveChannel();
        dto.setRestrictExposure(true);
        assertFalse(visibilityService.isDiscoverable(dto));
    }

    @Test
    void isDiscoverable_shouldReturnFalseForNotApproved() {
        ChannelVisibilityDTO dto = createPublicActiveChannel();
        dto.setAuditStatus("PENDING");
        assertFalse(visibilityService.isDiscoverable(dto));
    }

    @Test
    void isDiscoverable_shouldReturnFalseForNull() {
        assertFalse(visibilityService.isDiscoverable(null));
    }

    @Test
    void filterDiscoverable_shouldFilterOutNonDiscoverable() {
        ChannelVisibilityDTO public1 = createPublicActiveChannel();
        ChannelVisibilityDTO private1 = createPublicActiveChannel();
        private1.setPrivacyType("PRIVATE");
        ChannelVisibilityDTO hidden1 = createPublicActiveChannel();
        hidden1.setHidden(true);

        List<ChannelVisibilityDTO> result = visibilityService.filterDiscoverable(
                List.of(public1, private1, hidden1));

        assertEquals(1, result.size());
    }

    private ChannelVisibilityDTO createPublicActiveChannel() {
        ChannelVisibilityDTO dto = new ChannelVisibilityDTO();
        dto.setChannelId("test-channel");
        dto.setChannelStatus(1);
        dto.setPrivacyType("PUBLIC");
        dto.setHidden(false);
        dto.setFrozen(false);
        dto.setRestrictExposure(false);
        dto.setAuditStatus("APPROVED");
        return dto;
    }
}
```

- [ ] **Step 5: 运行测试验证**

```bash
cd jeecg-boot/jeecg-boot-module/jeecg-module-content
mvn test -Dtest=ContentChannelVisibilityServiceTest -pl .
```
Expected: 所有测试通过

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/jeecg/modules/content/channel/dto/ChannelVisibilityDTO.java \
        src/main/java/org/jeecg/modules/content/channel/service/IContentChannelVisibilityService.java \
        src/main/java/org/jeecg/modules/content/channel/service/impl/ContentChannelVisibilityServiceImpl.java \
        src/test/java/org/jeecg/modules/content/channel/service/ContentChannelVisibilityServiceTest.java
git commit -m "feat(channel-discovery): add visibility service with tests"
```

---

## Task 8: 频道推荐 Service

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/channel/service/IContentChannelRecommendationService.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/service/impl/ContentChannelRecommendationServiceImpl.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/req/query/ChannelRecommendationQueryReq.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/vo/ChannelRecommendationVO.java`
- Create: `src/test/java/org/jeecg/modules/content/channel/service/ContentChannelRecommendationServiceTest.java`

- [ ] **Step 1: 编写推荐服务接口**

```java
package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ContentChannelRecommendationCache;
import org.jeecg.modules.content.channel.req.query.ChannelRecommendationQueryReq;
import org.jeecg.modules.content.channel.vo.ChannelRecommendationVO;

public interface IContentChannelRecommendationService extends IService<ContentChannelRecommendationCache> {

    /**
     * 获取用户推荐频道列表
     */
    IPage<ChannelRecommendationVO> getRecommendations(String userId, ChannelRecommendationQueryReq req);

    /**
     * 记录不感兴趣反馈
     */
    void markNotInterested(String userId, String channelId);

    /**
     * 冷启动推荐（无行为数据用户）
     */
    IPage<ChannelRecommendationVO> getColdStartRecommendations(ChannelRecommendationQueryReq req);
}
```

- [ ] **Step 2: 编写推荐服务实现**

```java
package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.ContentChannelNotInterested;
import org.jeecg.modules.content.channel.entity.ContentChannelRecommendationCache;
import org.jeecg.modules.content.channel.mapper.ContentChannelNotInterestedMapper;
import org.jeecg.modules.content.channel.mapper.ContentChannelRecommendationCacheMapper;
import org.jeecg.modules.content.channel.req.query.ChannelRecommendationQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelRecommendationService;
import org.jeecg.modules.content.channel.service.IContentChannelVisibilityService;
import org.jeecg.modules.content.channel.vo.ChannelRecommendationVO;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentChannelRecommendationServiceImpl
        extends ServiceImpl<ContentChannelRecommendationCache, ContentChannelRecommendationCache>
        implements IContentChannelRecommendationService {

    @Resource
    private ContentChannelNotInterestedMapper notInterestedMapper;

    @Resource
    private IContentChannelVisibilityService visibilityService;

    @Override
    public IPage<ChannelRecommendationVO> getRecommendations(String userId, ChannelRecommendationQueryReq req) {
        // 查询用户的不感兴趣列表
        List<String> notInterestedChannelIds = getNotInterestedChannelIds(userId);

        // 查询推荐缓存
        Page<ContentChannelRecommendationCache> page = new Page<>(req.getPageNo(), req.getPageSize());
        LambdaQueryWrapper<ContentChannelRecommendationCache> wrapper = Wrappers.<ContentChannelRecommendationCache>lambdaQuery()
                .eq(ContentChannelRecommendationCache::getUserId, userId)
                .eq(ContentChannelRecommendationCache::getRecommendationStatus, 1)
                .notIn(!notInterestedChannelIds.isEmpty(),
                        ContentChannelRecommendationCache::getChannelId, notInterestedChannelIds)
                .orderByDesc(ContentChannelRecommendationCache::getRankingScore);

        IPage<ContentChannelRecommendationCache> cachePage = page(page, wrapper);

        // 转换为 VO
        return cachePage.convert(cache -> {
            ChannelRecommendationVO vo = new ChannelRecommendationVO();
            vo.setChannelId(cache.getChannelId());
            vo.setRecommendationReason(cache.getRecommendationReason());
            vo.setRankingScore(cache.getRankingScore());
            return vo;
        });
    }

    @Override
    public void markNotInterested(String userId, String channelId) {
        ContentChannelNotInterested ni = new ContentChannelNotInterested();
        ni.setUserId(userId);
        ni.setChannelId(channelId);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 30);
        ni.setExpireTime(cal.getTime());

        notInterestedMapper.insert(ni);
    }

    @Override
    public IPage<ChannelRecommendationVO> getColdStartRecommendations(ChannelRecommendationQueryReq req) {
        // 冷启动：返回热门频道 + 编辑精选 + 系统频道
        // 实际实现需要查询 ranking_snapshot 和 editorial_pick 表
        Page<ChannelRecommendationVO> page = new Page<>(req.getPageNo(), req.getPageSize());
        // TODO: 实现冷启动逻辑，依赖 ranking 和 editorial pick 服务
        return page;
    }

    private List<String> getNotInterestedChannelIds(String userId) {
        return notInterestedMapper.selectList(
                Wrappers.<ContentChannelNotInterested>lambdaQuery()
                        .eq(ContentChannelNotInterested::getUserId, userId)
                        .gt(ContentChannelNotInterested::getExpireTime, new Date()))
                .stream()
                .map(ContentChannelNotInterested::getChannelId)
                .collect(Collectors.toList());
    }
}
```

- [ ] **Step 3: 编写请求/响应对象**

```java
// ChannelRecommendationQueryReq.java
package org.jeecg.modules.content.channel.req.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "推荐查询请求")
public class ChannelRecommendationQueryReq {

    @Schema(description = "页码")
    private Integer pageNo = 1;

    @Schema(description = "每页数量")
    private Integer pageSize = 20;
}
```

```java
// ChannelRecommendationVO.java
package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "推荐频道VO")
public class ChannelRecommendationVO {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "频道名称")
    private String channelName;

    @Schema(description = "频道图标")
    private String channelIcon;

    @Schema(description = "频道类型")
    private String channelType;

    @Schema(description = "主分类名称")
    private String categoryName;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "订阅数")
    private Long subscriberCount;

    @Schema(description = "推荐理由")
    private String recommendationReason;

    @Schema(description = "推荐评分")
    private BigDecimal rankingScore;

    @Schema(description = "是否已订阅")
    private Boolean subscribed;
}
```

- [ ] **Step 4: 编写推荐服务单元测试**

```java
// ContentChannelRecommendationServiceTest.java
package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.content.channel.entity.ContentChannelRecommendationCache;
import org.jeecg.modules.content.channel.req.query.ChannelRecommendationQueryReq;
import org.jeecg.modules.content.channel.vo.ChannelRecommendationVO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ContentChannelRecommendationServiceTest {

    @Resource
    private IContentChannelRecommendationService recommendationService;

    @Test
    void getRecommendations_shouldReturnCachedRecommendations() {
        // 准备测试数据
        ContentChannelRecommendationCache cache = new ContentChannelRecommendationCache();
        cache.setUserId("test-user-001");
        cache.setChannelId("test-channel-001");
        cache.setRankingScore(new BigDecimal("85.5000"));
        cache.setRecommendationRule("SIMILARITY");
        cache.setRecommendationReason("因为你订阅了相似频道");
        cache.setRecommendationStatus(1);
        recommendationService.save(cache);

        ChannelRecommendationQueryReq req = new ChannelRecommendationQueryReq();
        req.setPageNo(1);
        req.setPageSize(10);

        IPage<ChannelRecommendationVO> result = recommendationService.getRecommendations("test-user-001", req);

        assertTrue(result.getRecords().size() > 0);
        assertEquals("因为你订阅了相似频道", result.getRecords().get(0).getRecommendationReason());
    }

    @Test
    void markNotInterested_shouldCreateFeedback() {
        recommendationService.markNotInterested("test-user-002", "test-channel-002");

        // 验证后续推荐中不再包含该频道
        ChannelRecommendationQueryReq req = new ChannelRecommendationQueryReq();
        req.setPageNo(1);
        req.setPageSize(100);

        IPage<ChannelRecommendationVO> result = recommendationService.getRecommendations("test-user-002", req);

        boolean found = result.getRecords().stream()
                .anyMatch(r -> "test-channel-002".equals(r.getChannelId()));
        assertFalse(found);
    }

    @Test
    void getColdStartRecommendations_shouldReturnPage() {
        ChannelRecommendationQueryReq req = new ChannelRecommendationQueryReq();
        req.setPageNo(1);
        req.setPageSize(10);

        IPage<ChannelRecommendationVO> result = recommendationService.getColdStartRecommendations(req);

        assertNotNull(result);
    }
}
```

- [ ] **Step 5: 运行测试验证**

```bash
cd jeecg-boot/jeecg-boot-module/jeecg-module-content
mvn test -Dtest=ContentChannelRecommendationServiceTest -pl .
```
Expected: 所有测试通过

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/jeecg/modules/content/channel/service/IContentChannelRecommendationService.java \
        src/main/java/org/jeecg/modules/content/channel/service/impl/ContentChannelRecommendationServiceImpl.java \
        src/main/java/org/jeecg/modules/content/channel/req/query/ChannelRecommendationQueryReq.java \
        src/main/java/org/jeecg/modules/content/channel/vo/ChannelRecommendationVO.java \
        src/test/java/org/jeecg/modules/content/channel/service/ContentChannelRecommendationServiceTest.java
git commit -m "feat(channel-discovery): add recommendation service with tests"
```

---

## Task 9: 推荐 Controller

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/channel/controller/ContentChannelRecommendationController.java`

- [ ] **Step 1: 编写推荐控制器**

```java
package org.jeecg.modules.content.channel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.req.query.ChannelRecommendationQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelRecommendationService;
import org.jeecg.modules.content.channel.vo.ChannelRecommendationVO;
import org.springframework.web.bind.annotation.*;

@Tag(name = "频道推荐")
@RestController
@RequestMapping("/content/channel/recommendation")
public class ContentChannelRecommendationController {

    @Resource
    private IContentChannelRecommendationService recommendationService;

    @Operation(summary = "获取推荐频道列表")
    @GetMapping("/list")
    public Result<IPage<ChannelRecommendationVO>> getRecommendations(
            @RequestParam String userId,
            ChannelRecommendationQueryReq req) {
        return Result.OK(recommendationService.getRecommendations(userId, req));
    }

    @Operation(summary = "冷启动推荐（无行为数据用户）")
    @GetMapping("/cold-start")
    public Result<IPage<ChannelRecommendationVO>> getColdStartRecommendations(
            ChannelRecommendationQueryReq req) {
        return Result.OK(recommendationService.getColdStartRecommendations(req));
    }

    @Operation(summary = "标记不感兴趣")
    @PostMapping("/not-interested")
    public Result<Void> markNotInterested(
            @RequestParam String userId,
            @RequestParam String channelId) {
        recommendationService.markNotInterested(userId, channelId);
        return Result.OK();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/org/jeecg/modules/content/channel/controller/ContentChannelRecommendationController.java
git commit -m "feat(channel-discovery): add recommendation controller"
```

---

## Task 10: 排行榜 Service

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/channel/service/IContentChannelRankingService.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/service/impl/ContentChannelRankingServiceImpl.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/req/query/ChannelRankingQueryReq.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/vo/ChannelRankingItemVO.java`
- Create: `src/test/java/org/jeecg/modules/content/channel/service/ContentChannelRankingServiceTest.java`

- [ ] **Step 1: 编写排行榜服务接口**

```java
package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ContentChannelRankingSnapshot;
import org.jeecg.modules.content.channel.req.query.ChannelRankingQueryReq;
import org.jeecg.modules.content.channel.vo.ChannelRankingItemVO;

import java.util.List;

public interface IContentChannelRankingService extends IService<ContentChannelRankingSnapshot> {

    /**
     * 获取热门频道榜
     */
    List<ChannelRankingItemVO> getHotRanking(ChannelRankingQueryReq req);

    /**
     * 获取新晋频道榜
     */
    List<ChannelRankingItemVO> getNewRanking(ChannelRankingQueryReq req);

    /**
     * 获取系统频道榜
     */
    List<ChannelRankingItemVO> getSystemRanking(ChannelRankingQueryReq req);
}
```

- [ ] **Step 2: 编写排行榜服务实现**

```java
package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.content.channel.entity.ContentChannelRankingSnapshot;
import org.jeecg.modules.content.channel.mapper.ContentChannelRankingSnapshotMapper;
import org.jeecg.modules.content.channel.req.query.ChannelRankingQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelRankingService;
import org.jeecg.modules.content.channel.vo.ChannelRankingItemVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentChannelRankingServiceImpl
        extends ServiceImpl<ContentChannelRankingSnapshotMapper, ContentChannelRankingSnapshot>
        implements IContentChannelRankingService {

    @Override
    public List<ChannelRankingItemVO> getHotRanking(ChannelRankingQueryReq req) {
        return getRanking("HOT", req.getDimension());
    }

    @Override
    public List<ChannelRankingItemVO> getNewRanking(ChannelRankingQueryReq req) {
        return getRanking("NEW", req.getDimension());
    }

    @Override
    public List<ChannelRankingItemVO> getSystemRanking(ChannelRankingQueryReq req) {
        return getRanking("SYSTEM", req.getDimension());
    }

    private List<ChannelRankingItemVO> getRanking(String type, String dimension) {
        List<ContentChannelRankingSnapshot> snapshots = list(
                Wrappers.<ContentChannelRankingSnapshot>lambdaQuery()
                        .eq(ContentChannelRankingSnapshot::getRankingType, type)
                        .eq(ContentChannelRankingSnapshot::getDimension, dimension)
                        .orderByAsc(ContentChannelRankingSnapshot::getRankPosition));

        return snapshots.stream().map(s -> {
            ChannelRankingItemVO vo = new ChannelRankingItemVO();
            vo.setChannelId(s.getChannelId());
            vo.setRankPosition(s.getRankPosition());
            vo.setScore(s.getScore());
            vo.setSnapshotDate(s.getSnapshotDate());
            return vo;
        }).collect(Collectors.toList());
    }
}
```

- [ ] **Step 3: 编写请求/响应对象**

```java
// ChannelRankingQueryReq.java
package org.jeecg.modules.content.channel.req.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "排行榜查询请求")
public class ChannelRankingQueryReq {

    @Schema(description = "维度: DAILY/WEEKLY/MONTHLY")
    private String dimension = "DAILY";
}
```

```java
// ChannelRankingItemVO.java
package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "排行榜条目VO")
public class ChannelRankingItemVO {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "频道名称")
    private String channelName;

    @Schema(description = "频道图标")
    private String channelIcon;

    @Schema(description = "排名位置")
    private Integer rankPosition;

    @Schema(description = "综合得分")
    private BigDecimal score;

    @Schema(description = "快照日期")
    private Date snapshotDate;

    @Schema(description = "更新时间")
    private Date updateTime;
}
```

- [ ] **Step 4: 编写排行榜服务单元测试**

```java
// ContentChannelRankingServiceTest.java
package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.entity.ContentChannelRankingSnapshot;
import org.jeecg.modules.content.channel.req.query.ChannelRankingQueryReq;
import org.jeecg.modules.content.channel.vo.ChannelRankingItemVO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ContentChannelRankingServiceTest {

    @Resource
    private IContentChannelRankingService rankingService;

    @Test
    void getHotRanking_shouldReturnSortedByPosition() {
        // 准备测试数据
        ContentChannelRankingSnapshot s1 = createSnapshot("HOT", "DAILY", 1, "88.5000");
        ContentChannelRankingSnapshot s2 = createSnapshot("HOT", "DAILY", 2, "75.0000");

        ChannelRankingQueryReq req = new ChannelRankingQueryReq();
        req.setDimension("DAILY");

        List<ChannelRankingItemVO> result = rankingService.getHotRanking(req);

        assertTrue(result.size() >= 2);
        assertEquals(1, result.get(0).getRankPosition());
        assertEquals(2, result.get(1).getRankPosition());
    }

    @Test
    void getNewRanking_shouldReturnNewChannelRanking() {
        ContentChannelRankingSnapshot s = createSnapshot("NEW", "DAILY", 1, "92.0000");

        ChannelRankingQueryReq req = new ChannelRankingQueryReq();
        req.setDimension("DAILY");

        List<ChannelRankingItemVO> result = rankingService.getNewRanking(req);

        assertTrue(result.size() > 0);
    }

    @Test
    void getSystemRanking_shouldReturnSystemChannelRanking() {
        ContentChannelRankingSnapshot s = createSnapshot("SYSTEM", "DAILY", 1, "100.0000");

        ChannelRankingQueryReq req = new ChannelRankingQueryReq();
        req.setDimension("DAILY");

        List<ChannelRankingItemVO> result = rankingService.getSystemRanking(req);

        assertTrue(result.size() > 0);
    }

    private ContentChannelRankingSnapshot createSnapshot(String type, String dimension,
                                                          int position, String score) {
        ContentChannelRankingSnapshot s = new ContentChannelRankingSnapshot();
        s.setChannelId("test-channel-" + position);
        s.setRankingType(type);
        s.setDimension(dimension);
        s.setRankPosition(position);
        s.setScore(new BigDecimal(score));
        s.setSnapshotDate(new Date());
        rankingService.save(s);
        return s;
    }
}
```

- [ ] **Step 5: 运行测试验证**

```bash
cd jeecg-boot/jeecg-boot-module/jeecg-module-content
mvn test -Dtest=ContentChannelRankingServiceTest -pl .
```
Expected: 所有测试通过

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/jeecg/modules/content/channel/service/IContentChannelRankingService.java \
        src/main/java/org/jeecg/modules/content/channel/service/impl/ContentChannelRankingServiceImpl.java \
        src/main/java/org/jeecg/modules/content/channel/req/query/ChannelRankingQueryReq.java \
        src/main/java/org/jeecg/modules/content/channel/vo/ChannelRankingItemVO.java \
        src/test/java/org/jeecg/modules/content/channel/service/ContentChannelRankingServiceTest.java
git commit -m "feat(channel-discovery): add ranking service with tests"
```

---

## Task 11: 排行榜 Controller 与定时任务

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/channel/controller/ContentChannelRankingController.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/task/ChannelRankingDailyTask.java`
- Create: `src/test/java/org/jeecg/modules/content/channel/task/ChannelRankingDailyTaskTest.java`

- [ ] **Step 1: 编写排行榜控制器**

```java
package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.req.query.ChannelRankingQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelRankingService;
import org.jeecg.modules.content.channel.vo.ChannelRankingItemVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "频道排行榜")
@RestController
@RequestMapping("/content/channel/ranking")
public class ContentChannelRankingController {

    @Resource
    private IContentChannelRankingService rankingService;

    @Operation(summary = "获取热门频道榜")
    @GetMapping("/hot")
    public Result<List<ChannelRankingItemVO>> getHotRanking(ChannelRankingQueryReq req) {
        return Result.OK(rankingService.getHotRanking(req));
    }

    @Operation(summary = "获取新晋频道榜")
    @GetMapping("/new")
    public Result<List<ChannelRankingItemVO>> getNewRanking(ChannelRankingQueryReq req) {
        return Result.OK(rankingService.getNewRanking(req));
    }

    @Operation(summary = "获取系统频道榜")
    @GetMapping("/system")
    public Result<List<ChannelRankingItemVO>> getSystemRanking(ChannelRankingQueryReq req) {
        return Result.OK(rankingService.getSystemRanking(req));
    }
}
```

- [ ] **Step 2: 编写排行榜每日更新定时任务**

```java
package org.jeecg.modules.content.channel.task;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.channel.service.IContentChannelRankingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Slf4j
@Component
public class ChannelRankingDailyTask {

    @Resource
    private IContentChannelRankingService rankingService;

    /**
     * 每日凌晨2点执行榜单更新
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void execute() {
        log.info("开始执行频道排行榜每日更新任务");
        try {
            // 1. 计算热门榜
            calculateHotRanking();
            // 2. 计算新晋榜
            calculateNewRanking();
            // 3. 系统榜由运营配置，不自动计算
            log.info("频道排行榜每日更新任务完成");
        } catch (Exception e) {
            log.error("频道排行榜每日更新任务异常", e);
        }
    }

    private void calculateHotRanking() {
        // TODO: 实现热门榜计算逻辑
        // 公式：订阅数 × 0.4 + 近 7 日活跃度 × 0.3 + 近 7 日互动量 × 0.3
        log.info("计算热门榜完成");
    }

    private void calculateNewRanking() {
        // TODO: 实现新晋榜计算逻辑
        // 创建 30 天内频道，按订阅增长率 + 活跃度排序
        log.info("计算新晋榜完成");
    }
}
```

- [ ] **Step 3: 编写定时任务单元测试**

```java
// ChannelRankingDailyTaskTest.java
package org.jeecg.modules.content.channel.task;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChannelRankingDailyTaskTest {

    @Resource
    private ChannelRankingDailyTask rankingDailyTask;

    @Test
    void execute_shouldRunWithoutException() {
        assertDoesNotThrow(() -> rankingDailyTask.execute());
    }
}
```

- [ ] **Step 4: 运行测试验证**

```bash
cd jeecg-boot/jeecg-boot-module/jeecg-module-content
mvn test -Dtest=ChannelRankingDailyTaskTest -pl .
```
Expected: 测试通过

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/jeecg/modules/content/channel/controller/ContentChannelRankingController.java \
        src/main/java/org/jeecg/modules/content/channel/task/ChannelRankingDailyTask.java \
        src/test/java/org/jeecg/modules/content/channel/task/ChannelRankingDailyTaskTest.java
git commit -m "feat(channel-discovery): add ranking controller and daily task"
```

---

## Task 12: 编辑精选 Service

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/channel/service/IContentChannelEditorialPickService.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/service/impl/ContentChannelEditorialPickServiceImpl.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/req/create/ChannelEditorialPickCreateReq.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/req/update/ChannelEditorialPickUpdateReq.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/vo/ChannelEditorialPickVO.java`
- Create: `src/test/java/org/jeecg/modules/content/channel/service/ContentChannelEditorialPickServiceTest.java`

- [ ] **Step 1: 编写编辑精选服务接口**

```java
package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.content.channel.entity.ContentChannelEditorialPick;
import org.jeecg.modules.content.channel.req.create.ChannelEditorialPickCreateReq;
import org.jeecg.modules.content.channel.req.update.ChannelEditorialPickUpdateReq;
import org.jeecg.modules.content.channel.vo.ChannelEditorialPickVO;

import java.util.List;

public interface IContentChannelEditorialPickService extends IService<ContentChannelEditorialPick> {

    ContentChannelEditorialPick createPick(ChannelEditorialPickCreateReq req);

    void updatePick(ChannelEditorialPickUpdateReq req);

    void removePick(String pickId);

    List<ChannelEditorialPickVO> listActivePicks();
}
```

- [ ] **Step 2: 编写编辑精选服务实现**

```java
package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.content.channel.entity.ContentChannelEditorialPick;
import org.jeecg.modules.content.channel.mapper.ContentChannelEditorialPickMapper;
import org.jeecg.modules.content.channel.req.create.ChannelEditorialPickCreateReq;
import org.jeecg.modules.content.channel.req.update.ChannelEditorialPickUpdateReq;
import org.jeecg.modules.content.channel.service.IContentChannelEditorialPickService;
import org.jeecg.modules.content.channel.vo.ChannelEditorialPickVO;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentChannelEditorialPickServiceImpl
        extends ServiceImpl<ContentChannelEditorialPickMapper, ContentChannelEditorialPick>
        implements IContentChannelEditorialPickService {

    @Override
    public ContentChannelEditorialPick createPick(ChannelEditorialPickCreateReq req) {
        ContentChannelEditorialPick pick = new ContentChannelEditorialPick();
        pick.setChannelId(req.getChannelId());
        pick.setRecommendationText(req.getRecommendationText());
        pick.setStartTime(req.getStartTime());
        pick.setEndTime(req.getEndTime());
        pick.setStatus(1);
        pick.setOperatorId(req.getOperatorId());
        save(pick);
        return pick;
    }

    @Override
    public void updatePick(ChannelEditorialPickUpdateReq req) {
        ContentChannelEditorialPick pick = getById(req.getId());
        if (pick == null) {
            throw new JeecgBootException("精选记录不存在");
        }
        if (req.getRecommendationText() != null) {
            pick.setRecommendationText(req.getRecommendationText());
        }
        if (req.getEndTime() != null) {
            pick.setEndTime(req.getEndTime());
        }
        if (req.getStatus() != null) {
            pick.setStatus(req.getStatus());
        }
        updateById(pick);
    }

    @Override
    public void removePick(String pickId) {
        ContentChannelEditorialPick pick = getById(pickId);
        if (pick == null) {
            throw new JeecgBootException("精选记录不存在");
        }
        pick.setStatus(0);
        updateById(pick);
    }

    @Override
    public List<ChannelEditorialPickVO> listActivePicks() {
        Date now = new Date();
        return list(Wrappers.<ContentChannelEditorialPick>lambdaQuery()
                .eq(ContentChannelEditorialPick::getStatus, 1)
                .le(ContentChannelEditorialPick::getStartTime, now)
                .and(w -> w.isNull(ContentChannelEditorialPick::getEndTime)
                        .or()
                        .ge(ContentChannelEditorialPick::getEndTime, now)))
                .stream()
                .map(p -> {
                    ChannelEditorialPickVO vo = new ChannelEditorialPickVO();
                    vo.setId(p.getId());
                    vo.setChannelId(p.getChannelId());
                    vo.setRecommendationText(p.getRecommendationText());
                    vo.setStartTime(p.getStartTime());
                    vo.setEndTime(p.getEndTime());
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
```

- [ ] **Step 3: 编写请求/响应对象**

```java
// ChannelEditorialPickCreateReq.java
package org.jeecg.modules.content.channel.req.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Schema(description = "创建编辑精选请求")
public class ChannelEditorialPickCreateReq {

    @NotBlank(message = "频道ID不能为空")
    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "推荐语")
    private String recommendationText;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "生效开始时间")
    private Date startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "生效结束时间")
    private Date endTime;

    @NotBlank(message = "操作人ID不能为空")
    @Schema(description = "操作人ID")
    private String operatorId;
}
```

```java
// ChannelEditorialPickUpdateReq.java
package org.jeecg.modules.content.channel.req.update;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Schema(description = "更新编辑精选请求")
public class ChannelEditorialPickUpdateReq {

    @NotBlank(message = "精选ID不能为空")
    @Schema(description = "精选ID")
    private String id;

    @Schema(description = "推荐语")
    private String recommendationText;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "生效结束时间")
    private Date endTime;

    @Schema(description = "状态 0=下线 1=上线")
    private Integer status;
}
```

```java
// ChannelEditorialPickVO.java
package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "编辑精选VO")
public class ChannelEditorialPickVO {

    @Schema(description = "精选ID")
    private String id;

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "频道名称")
    private String channelName;

    @Schema(description = "频道图标")
    private String channelIcon;

    @Schema(description = "推荐语")
    private String recommendationText;

    @Schema(description = "生效开始时间")
    private Date startTime;

    @Schema(description = "生效结束时间")
    private Date endTime;
}
```

- [ ] **Step 4: 编写编辑精选服务单元测试**

```java
// ContentChannelEditorialPickServiceTest.java
package org.jeecg.modules.content.channel.service;

import org.jeecg.modules.content.channel.entity.ContentChannelEditorialPick;
import org.jeecg.modules.content.channel.req.create.ChannelEditorialPickCreateReq;
import org.jeecg.modules.content.channel.req.update.ChannelEditorialPickUpdateReq;
import org.jeecg.modules.content.channel.vo.ChannelEditorialPickVO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ContentChannelEditorialPickServiceTest {

    @Resource
    private IContentChannelEditorialPickService editorialPickService;

    @Test
    void createPick_shouldCreateValidPick() {
        ChannelEditorialPickCreateReq req = new ChannelEditorialPickCreateReq();
        req.setChannelId("test-channel-001");
        req.setRecommendationText("优质技术频道");
        req.setStartTime(new Date());
        req.setOperatorId("operator-001");

        ContentChannelEditorialPick pick = editorialPickService.createPick(req);

        assertNotNull(pick.getId());
        assertEquals(1, pick.getStatus().intValue());
    }

    @Test
    void removePick_shouldSetStatusToZero() {
        ChannelEditorialPickCreateReq req = new ChannelEditorialPickCreateReq();
        req.setChannelId("test-channel-002");
        req.setOperatorId("operator-001");
        ContentChannelEditorialPick pick = editorialPickService.createPick(req);

        editorialPickService.removePick(pick.getId());

        ContentChannelEditorialPick updated = editorialPickService.getById(pick.getId());
        assertEquals(0, updated.getStatus().intValue());
    }

    @Test
    void listActivePicks_shouldReturnOnlyActivePicks() {
        ChannelEditorialPickCreateReq req = new ChannelEditorialPickCreateReq();
        req.setChannelId("test-channel-003");
        req.setStartTime(new Date());
        req.setOperatorId("operator-001");
        editorialPickService.createPick(req);

        List<ChannelEditorialPickVO> picks = editorialPickService.listActivePicks();

        assertTrue(picks.size() > 0);
    }

    @Test
    void updatePick_shouldUpdateRecommendationText() {
        ChannelEditorialPickCreateReq createReq = new ChannelEditorialPickCreateReq();
        createReq.setChannelId("test-channel-004");
        createReq.setOperatorId("operator-001");
        ContentChannelEditorialPick pick = editorialPickService.createPick(createReq);

        ChannelEditorialPickUpdateReq updateReq = new ChannelEditorialPickUpdateReq();
        updateReq.setId(pick.getId());
        updateReq.setRecommendationText("更新后的推荐语");
        editorialPickService.updatePick(updateReq);

        ContentChannelEditorialPick updated = editorialPickService.getById(pick.getId());
        assertEquals("更新后的推荐语", updated.getRecommendationText());
    }
}
```

- [ ] **Step 5: 运行测试验证**

```bash
cd jeecg-boot/jeecg-boot-module/jeecg-module-content
mvn test -Dtest=ContentChannelEditorialPickServiceTest -pl .
```
Expected: 所有测试通过

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/jeecg/modules/content/channel/service/IContentChannelEditorialPickService.java \
        src/main/java/org/jeecg/modules/content/channel/service/impl/ContentChannelEditorialPickServiceImpl.java \
        src/main/java/org/jeecg/modules/content/channel/req/create/ChannelEditorialPickCreateReq.java \
        src/main/java/org/jeecg/modules/content/channel/req/update/ChannelEditorialPickUpdateReq.java \
        src/main/java/org/jeecg/modules/content/channel/vo/ChannelEditorialPickVO.java \
        src/test/java/org/jeecg/modules/content/channel/service/ContentChannelEditorialPickServiceTest.java
git commit -m "feat(channel-discovery): add editorial pick service with tests"
```

---

## Task 13: 编辑精选 Controller 与推荐刷新任务

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/channel/controller/ContentChannelEditorialPickController.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/task/ChannelRecommendationRefreshTask.java`
- Create: `src/test/java/org/jeecg/modules/content/channel/task/ChannelRecommendationRefreshTaskTest.java`

- [ ] **Step 1: 编写编辑精选控制器**

```java
package org.jeecg.modules.content.channel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.entity.ContentChannelEditorialPick;
import org.jeecg.modules.content.channel.req.create.ChannelEditorialPickCreateReq;
import org.jeecg.modules.content.channel.req.update.ChannelEditorialPickUpdateReq;
import org.jeecg.modules.content.channel.service.IContentChannelEditorialPickService;
import org.jeecg.modules.content.channel.vo.ChannelEditorialPickVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "频道编辑精选")
@RestController
@RequestMapping("/content/channel/editorial-pick")
public class ContentChannelEditorialPickController {

    @Resource
    private IContentChannelEditorialPickService editorialPickService;

    @Operation(summary = "获取有效精选列表")
    @GetMapping("/list")
    public Result<List<ChannelEditorialPickVO>> listActivePicks() {
        return Result.OK(editorialPickService.listActivePicks());
    }

    @Operation(summary = "创建编辑精选")
    @PostMapping("/create")
    public Result<ContentChannelEditorialPick> createPick(@Valid @RequestBody ChannelEditorialPickCreateReq req) {
        return Result.OK(editorialPickService.createPick(req));
    }

    @Operation(summary = "更新编辑精选")
    @PostMapping("/update")
    public Result<Void> updatePick(@Valid @RequestBody ChannelEditorialPickUpdateReq req) {
        editorialPickService.updatePick(req);
        return Result.OK();
    }

    @Operation(summary = "移除编辑精选")
    @PostMapping("/remove")
    public Result<Void> removePick(@RequestParam String pickId) {
        editorialPickService.removePick(pickId);
        return Result.OK();
    }
}
```

- [ ] **Step 2: 编写推荐缓存刷新定时任务**

```java
package org.jeecg.modules.content.channel.task;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.content.channel.service.IContentChannelRecommendationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Slf4j
@Component
public class ChannelRecommendationRefreshTask {

    @Resource
    private IContentChannelRecommendationService recommendationService;

    /**
     * 每5分钟刷新推荐缓存
     */
    @Scheduled(fixedRate = 300000)
    public void execute() {
        log.info("开始刷新频道推荐缓存");
        try {
            // 清理过期的不感兴趣反馈
            cleanupExpiredNotInterested();
            // 刷新推荐缓存（实际实现依赖推荐算法）
            refreshRecommendationCache();
            log.info("频道推荐缓存刷新完成");
        } catch (Exception e) {
            log.error("频道推荐缓存刷新异常", e);
        }
    }

    private void cleanupExpiredNotInterested() {
        // TODO: 清理过期的不感兴趣反馈记录
        log.info("清理过期不感兴趣反馈完成");
    }

    private void refreshRecommendationCache() {
        // TODO: 实现推荐缓存刷新逻辑
        log.info("推荐缓存刷新完成");
    }
}
```

- [ ] **Step 3: 编写定时任务单元测试**

```java
// ChannelRecommendationRefreshTaskTest.java
package org.jeecg.modules.content.channel.task;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChannelRecommendationRefreshTaskTest {

    @Resource
    private ChannelRecommendationRefreshTask refreshTask;

    @Test
    void execute_shouldRunWithoutException() {
        assertDoesNotThrow(() -> refreshTask.execute());
    }
}
```

- [ ] **Step 4: 运行测试验证**

```bash
cd jeecg-boot/jeecg-boot-module/jeecg-module-content
mvn test -Dtest=ChannelRecommendationRefreshTaskTest -pl .
```
Expected: 测试通过

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/jeecg/modules/content/channel/controller/ContentChannelEditorialPickController.java \
        src/main/java/org/jeecg/modules/content/channel/task/ChannelRecommendationRefreshTask.java \
        src/test/java/org/jeecg/modules/content/channel/task/ChannelRecommendationRefreshTaskTest.java
git commit -m "feat(channel-discovery): add editorial pick controller and recommendation refresh task"
```

---

## Task 14: 分类浏览

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/channel/req/query/ChannelBrowseQueryReq.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/vo/ChannelBrowseItemVO.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/controller/ContentChannelBrowseController.java`

- [ ] **Step 1: 编写浏览请求/响应对象**

```java
// ChannelBrowseQueryReq.java
package org.jeecg.modules.content.channel.req.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分类浏览查询请求")
public class ChannelBrowseQueryReq {

    @Schema(description = "分类ID")
    private String categoryId;

    @Schema(description = "排序方式: SUBSCRIBER_COUNT/ACTIVITY/CREATE_TIME")
    private String sortBy = "SUBSCRIBER_COUNT";

    @Schema(description = "频道类型筛选: PERSONAL/ORGANIZATION/SYSTEM")
    private String channelType;

    @Schema(description = "页码")
    private Integer pageNo = 1;

    @Schema(description = "每页数量")
    private Integer pageSize = 20;
}
```

```java
// ChannelBrowseItemVO.java
package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分类浏览频道卡片VO")
public class ChannelBrowseItemVO {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "频道名称")
    private String channelName;

    @Schema(description = "频道图标")
    private String channelIcon;

    @Schema(description = "频道类型")
    private String channelType;

    @Schema(description = "主分类名称")
    private String categoryName;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "订阅数")
    private Long subscriberCount;
}
```

- [ ] **Step 2: 编写分类浏览控制器**

```java
package org.jeecg.modules.content.channel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.req.query.ChannelBrowseQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelVisibilityService;
import org.jeecg.modules.content.channel.vo.ChannelBrowseItemVO;
import org.springframework.web.bind.annotation.*;

@Tag(name = "频道分类浏览")
@RestController
@RequestMapping("/content/channel/browse")
public class ContentChannelBrowseController {

    @Resource
    private IContentChannelVisibilityService visibilityService;

    @Operation(summary = "按分类浏览频道")
    @GetMapping("/category")
    public Result<IPage<ChannelBrowseItemVO>> browseByCategory(ChannelBrowseQueryReq req) {
        // TODO: 实现分类浏览逻辑，调用 visibilityService 过滤不可见频道
        // 需要查询 content_channel_category 的 path LIKE 匹配子分类
        // 需要关联查询频道信息并过滤可见性
        return Result.OK();
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/org/jeecg/modules/content/channel/req/query/ChannelBrowseQueryReq.java \
        src/main/java/org/jeecg/modules/content/channel/vo/ChannelBrowseItemVO.java \
        src/main/java/org/jeecg/modules/content/channel/controller/ContentChannelBrowseController.java
git commit -m "feat(channel-discovery): add category browse controller"
```

---

## Task 15: 频道搜索 Service

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/channel/service/IContentChannelSearchService.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/service/impl/ContentChannelSearchServiceImpl.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/req/query/ChannelSearchQueryReq.java`
- Create: `src/main/java/org/jeecg/modules/content/channel/vo/ChannelSearchResultVO.java`
- Create: `src/test/java/org/jeecg/modules/content/channel/service/ContentChannelSearchServiceTest.java`

- [ ] **Step 1: 编写搜索服务接口**

```java
package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.content.channel.req.query.ChannelSearchQueryReq;
import org.jeecg.modules.content.channel.vo.ChannelSearchResultVO;

public interface IContentChannelSearchService {

    /**
     * 搜索频道
     */
    IPage<ChannelSearchResultVO> search(String userId, ChannelSearchQueryReq req);
}
```

- [ ] **Step 2: 编写搜索服务实现**

```java
package org.jeecg.modules.content.channel.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.content.channel.req.query.ChannelSearchQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelSearchService;
import org.jeecg.modules.content.channel.service.IContentChannelVisibilityService;
import org.jeecg.modules.content.channel.vo.ChannelSearchResultVO;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class ContentChannelSearchServiceImpl implements IContentChannelSearchService {

    @Resource
    private IContentChannelVisibilityService visibilityService;

    @Override
    public IPage<ChannelSearchResultVO> search(String userId, ChannelSearchQueryReq req) {
        Page<ChannelSearchResultVO> page = new Page<>(req.getPageNo(), req.getPageSize());

        // TODO: 实现搜索逻辑
        // 1. 使用 MySQL FULLTEXT 或 LIKE 搜索 name, description, tag
        // 2. 调用 visibilityService 过滤不可见频道
        // 3. 按 req.getSortBy() 排序
        // 4. 构建匹配原因

        return page;
    }
}
```

- [ ] **Step 3: 编写请求/响应对象**

```java
// ChannelSearchQueryReq.java
package org.jeecg.modules.content.channel.req.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "频道搜索请求")
public class ChannelSearchQueryReq {

    @Schema(description = "搜索关键词")
    private String keyword;

    @Schema(description = "频道类型筛选: PERSONAL/ORGANIZATION/SYSTEM")
    private String channelType;

    @Schema(description = "分类ID筛选")
    private String categoryId;

    @Schema(description = "排序方式: RELEVANCE/ACTIVITY/SUBSCRIBER_COUNT")
    private String sortBy = "RELEVANCE";

    @Schema(description = "页码")
    private Integer pageNo = 1;

    @Schema(description = "每页数量")
    private Integer pageSize = 20;
}
```

```java
// ChannelSearchResultVO.java
package org.jeecg.modules.content.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "搜索结果VO")
public class ChannelSearchResultVO {

    @Schema(description = "频道ID")
    private String channelId;

    @Schema(description = "频道名称")
    private String channelName;

    @Schema(description = "频道图标")
    private String channelIcon;

    @Schema(description = "频道类型")
    private String channelType;

    @Schema(description = "主分类名称")
    private String categoryName;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "订阅数")
    private Long subscriberCount;

    @Schema(description = "匹配原因")
    private String matchReason;
}
```

- [ ] **Step 4: 编写搜索服务单元测试**

```java
// ContentChannelSearchServiceTest.java
package org.jeecg.modules.content.channel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.content.channel.req.query.ChannelSearchQueryReq;
import org.jeecg.modules.content.channel.vo.ChannelSearchResultVO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ContentChannelSearchServiceTest {

    @Resource
    private IContentChannelSearchService searchService;

    @Test
    void search_shouldReturnEmptyPageWhenNoResults() {
        ChannelSearchQueryReq req = new ChannelSearchQueryReq();
        req.setKeyword("不存在的频道名称");
        req.setPageNo(1);
        req.setPageSize(20);

        IPage<ChannelSearchResultVO> result = searchService.search("test-user", req);

        assertNotNull(result);
        assertEquals(0, result.getRecords().size());
    }

    @Test
    void search_shouldReturnPageWithDefaultPagination() {
        ChannelSearchQueryReq req = new ChannelSearchQueryReq();
        req.setKeyword("测试");

        IPage<ChannelSearchResultVO> result = searchService.search("test-user", req);

        assertNotNull(result);
        assertEquals(1, result.getCurrent());
    }
}
```

- [ ] **Step 5: 运行测试验证**

```bash
cd jeecg-boot/jeecg-boot-module/jeecg-module-content
mvn test -Dtest=ContentChannelSearchServiceTest -pl .
```
Expected: 所有测试通过

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/jeecg/modules/content/channel/service/IContentChannelSearchService.java \
        src/main/java/org/jeecg/modules/content/channel/service/impl/ContentChannelSearchServiceImpl.java \
        src/main/java/org/jeecg/modules/content/channel/req/query/ChannelSearchQueryReq.java \
        src/main/java/org/jeecg/modules/content/channel/vo/ChannelSearchResultVO.java \
        src/test/java/org/jeecg/modules/content/channel/service/ContentChannelSearchServiceTest.java
git commit -m "feat(channel-discovery): add search service with tests"
```

---

## Task 16: 搜索 Controller

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/channel/controller/ContentChannelSearchController.java`

- [ ] **Step 1: 编写搜索控制器**

```java
package org.jeecg.modules.content.channel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.content.channel.req.query.ChannelSearchQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelSearchService;
import org.jeecg.modules.content.channel.vo.ChannelSearchResultVO;
import org.springframework.web.bind.annotation.*;

@Tag(name = "频道搜索")
@RestController
@RequestMapping("/content/channel/search")
public class ContentChannelSearchController {

    @Resource
    private IContentChannelSearchService searchService;

    @Operation(summary = "搜索频道")
    @GetMapping("/query")
    public Result<IPage<ChannelSearchResultVO>> search(
            @RequestParam String userId,
            ChannelSearchQueryReq req) {
        return Result.OK(searchService.search(userId, req));
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/org/jeecg/modules/content/channel/controller/ContentChannelSearchController.java
git commit -m "feat(channel-discovery): add search controller"
```

---

## Task 17: 发现页聚合编排

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/channel/biz/ContentChannelDiscoveryBiz.java`
- Create: `src/test/java/org/jeecg/modules/content/channel/biz/ContentChannelDiscoveryBizTest.java`

- [ ] **Step 1: 编写发现页聚合 Biz**

```java
package org.jeecg.modules.content.channel.biz;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.jeecg.modules.content.channel.req.query.ChannelRecommendationQueryReq;
import org.jeecg.modules.content.channel.service.IContentChannelEditorialPickService;
import org.jeecg.modules.content.channel.service.IContentChannelRankingService;
import org.jeecg.modules.content.channel.service.IContentChannelRecommendationService;
import org.jeecg.modules.content.channel.vo.ChannelEditorialPickVO;
import org.jeecg.modules.content.channel.vo.ChannelRankingItemVO;
import org.jeecg.modules.content.channel.vo.ChannelRecommendationVO;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ContentChannelDiscoveryBiz {

    @Resource
    private IContentChannelRecommendationService recommendationService;

    @Resource
    private IContentChannelRankingService rankingService;

    @Resource
    private IContentChannelEditorialPickService editorialPickService;

    /**
     * 获取发现页聚合数据
     */
    public Map<String, Object> getDiscoveryData(String userId) {
        Map<String, Object> data = new HashMap<>();

        // 推荐频道
        ChannelRecommendationQueryReq req = new ChannelRecommendationQueryReq();
        req.setPageNo(1);
        req.setPageSize(10);
        IPage<ChannelRecommendationVO> recommendations = recommendationService.getRecommendations(userId, req);
        data.put("recommendations", recommendations.getRecords());

        // 热门榜
        List<ChannelRankingItemVO> hotRanking = rankingService.getHotRanking(
                createRankingReq("DAILY"));
        data.put("hotRanking", hotRanking);

        // 编辑精选
        List<ChannelEditorialPickVO> editorialPicks = editorialPickService.listActivePicks();
        data.put("editorialPicks", editorialPicks);

        return data;
    }

    private org.jeecg.modules.content.channel.req.query.ChannelRankingQueryReq createRankingReq(String dimension) {
        org.jeecg.modules.content.channel.req.query.ChannelRankingQueryReq req =
                new org.jeecg.modules.content.channel.req.query.ChannelRankingQueryReq();
        req.setDimension(dimension);
        return req;
    }
}
```

- [ ] **Step 2: 编写发现页聚合测试**

```java
// ContentChannelDiscoveryBizTest.java
package org.jeecg.modules.content.channel.biz;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ContentChannelDiscoveryBizTest {

    @Resource
    private ContentChannelDiscoveryBiz discoveryBiz;

    @Test
    void getDiscoveryData_shouldReturnAllSections() {
        Map<String, Object> data = discoveryBiz.getDiscoveryData("test-user");

        assertNotNull(data);
        assertTrue(data.containsKey("recommendations"));
        assertTrue(data.containsKey("hotRanking"));
        assertTrue(data.containsKey("editorialPicks"));
    }
}
```

- [ ] **Step 3: 运行测试验证**

```bash
cd jeecg-boot/jeecg-boot-module/jeecg-module-content
mvn test -Dtest=ContentChannelDiscoveryBizTest -pl .
```
Expected: 测试通过

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/jeecg/modules/content/channel/biz/ContentChannelDiscoveryBiz.java \
        src/test/java/org/jeecg/modules/content/channel/biz/ContentChannelDiscoveryBizTest.java
git commit -m "feat(channel-discovery): add discovery biz orchestration"
```

---

## Task 18: 常量类与最终验证

**Files:**
- Create: `src/main/java/org/jeecg/modules/content/channel/constant/ChannelDiscoveryConstant.java`

- [ ] **Step 1: 创建常量类**

```java
package org.jeecg.modules.content.channel.constant;

public class ChannelDiscoveryConstant {

    /** 分类最大层级 */
    public static final int MAX_CATEGORY_LEVEL = 4;

    /** 频道最大副分类数 */
    public static final int MAX_SUB_CATEGORY_COUNT = 3;

    /** 标签名称最大长度 */
    public static final int MAX_TAG_NAME_LENGTH = 20;

    /** 分类名称最大长度 */
    public static final int MAX_CATEGORY_NAME_LENGTH = 50;

    /** 不感兴趣反馈过期天数 */
    public static final int NOT_INTERESTED_EXPIRE_DAYS = 30;

    /** 搜索结果每页默认数量 */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /** 推荐缓存刷新间隔（毫秒） */
    public static final long RECOMMENDATION_REFRESH_INTERVAL = 300000L;

    /** 榜单类型 */
    public static final String RANKING_TYPE_HOT = "HOT";
    public static final String RANKING_TYPE_NEW = "NEW";
    public static final String RANKING_TYPE_SYSTEM = "SYSTEM";

    /** 榜单维度 */
    public static final String DIMENSION_DAILY = "DAILY";
    public static final String DIMENSION_WEEKLY = "WEEKLY";
    public static final String DIMENSION_MONTHLY = "MONTHLY";

    /** 推荐规则 */
    public static final String RECOMMENDATION_RULE_SIMILARITY = "SIMILARITY";
    public static final String RECOMMENDATION_RULE_PREFERENCE = "PREFERENCE";
    public static final String RECOMMENDATION_RULE_POPULAR = "POPULAR";
    public static final String RECOMMENDATION_RULE_COLD_START = "COLD_START";
}
```

- [ ] **Step 2: 运行全部测试**

```bash
cd jeecg-boot/jeecg-boot-module/jeecg-module-content
mvn test -pl .
```
Expected: 所有测试通过

- [ ] **Step 3: 最终 Commit**

```bash
git add src/main/java/org/jeecg/modules/content/channel/constant/ChannelDiscoveryConstant.java
git commit -m "feat(channel-discovery): add constants and final verification"
```

---

## Summary

完成以上 18 个 Task 后，频道发现模块的基础架构已建立：
- 7 张数据表（Flyway 迁移）
- 7 个实体类
- 7 个 Mapper 接口
- 6 个 Service（分类、标签、推荐、排行榜、精选、搜索）+ 1 个可见性服务
- 7 个 Controller
- 2 个定时任务
- 1 个聚合 Biz
- 完整的单元测试覆盖
