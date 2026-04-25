
## 1. 核心设计思路

### 1.1 内容与媒体分离设计
业界主流做法是将文章内容和媒体资源分离存储，这样可以：
- 提高查询性能
- 便于媒体资源管理
- 支持多种媒体类型扩展
- 便于缓存和CDN优化

### 1.2 富文本内容存储方案
根据搜索结果，富文本内容主要有三种存储方式 <mcreference link="https://docs.pingcode.com/baike/1915822" index="4">4</mcreference>：

1. **HTML格式存储**：直接存储HTML标签，灵活性高但需要防XSS攻击
2. **Markdown格式存储**：简洁易读，安全性高但功能有限
3. **JSON格式存储**：结构化数据，便于解析但复杂度较高

## 2. 推荐的数据库表设计

### 2.1 文章主表 (articles)
```sql
CREATE TABLE `articles` (
  `id` VARCHAR(32) NOT NULL COMMENT '文章ID',
  `title` VARCHAR(200) NOT NULL COMMENT '文章标题',
  `content` LONGTEXT COMMENT '文章内容(富文本HTML)',
  `content_json` JSON COMMENT '结构化内容(可选)',
  `summary` TEXT COMMENT '文章摘要',
  `author_id` VARCHAR(32) NOT NULL COMMENT '作者ID',
  `category_id` VARCHAR(32) COMMENT '分类ID',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0草稿,1发布,2下线',
  `view_count` INT DEFAULT 0 COMMENT '浏览次数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_author_status` (`author_id`, `status`),
  INDEX `idx_category_time` (`category_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';
```

### 2.2 媒体资源表 (media_resources)
```sql
CREATE TABLE `media_resources` (
  `id` VARCHAR(32) NOT NULL COMMENT '媒体资源ID',
  `type` VARCHAR(20) NOT NULL COMMENT '媒体类型:video,audio,image,document',
  `source_type` VARCHAR(20) NOT NULL COMMENT '来源类型:local,youtube,bilibili,tencent,youku,iqiyi',
  `original_url` TEXT COMMENT '原始URL',
  `embed_url` TEXT COMMENT '嵌入URL(iframe src)',
  `embed_code` TEXT COMMENT '完整嵌入代码',
  `title` VARCHAR(200) COMMENT '媒体标题',
  `description` TEXT COMMENT '媒体描述',
  `thumbnail_url` VARCHAR(500) COMMENT '缩略图URL',
  `duration` INT COMMENT '时长(秒)',
  `file_size` BIGINT COMMENT '文件大小(字节)',
  `width` INT COMMENT '宽度',
  `height` INT COMMENT '高度',
  `metadata` JSON COMMENT '扩展元数据',
  `status` TINYINT DEFAULT 1 COMMENT '状态:0无效,1有效',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_type_source` (`type`, `source_type`),
  INDEX `idx_status_time` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='媒体资源表';
```

### 2.3 文章媒体关联表 (article_media)
```sql
CREATE TABLE `article_media` (
  `id` VARCHAR(32) NOT NULL COMMENT '关联ID',
  `article_id` VARCHAR(32) NOT NULL COMMENT '文章ID',
  `media_id` VARCHAR(32) NOT NULL COMMENT '媒体ID',
  `position` INT DEFAULT 0 COMMENT '在文章中的位置序号',
  `embed_type` VARCHAR(20) DEFAULT 'inline' COMMENT '嵌入类型:inline,popup,gallery',
  `custom_width` INT COMMENT '自定义宽度',
  `custom_height` INT COMMENT '自定义高度',
  `caption` TEXT COMMENT '媒体说明文字',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_media_pos` (`article_id`, `media_id`, `position`),
  INDEX `idx_article_pos` (`article_id`, `position`),
  FOREIGN KEY (`article_id`) REFERENCES `articles`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`media_id`) REFERENCES `media_resources`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章媒体关联表';
```

## 3. 第三方视频处理最佳实践

### 3.1 视频嵌入方式选择
根据搜索结果 <mcreference link="https://blog.csdn.net/WuLex/article/details/105677054" index="4">4</mcreference>，推荐使用 `<iframe>` 标签：

- **iframe标签**：全平台支持，兼容PC和移动端
- **video标签**：仅支持特定格式(mp4/ogg/webm)，移动端兼容性好
- **embed标签**：不支持移动端，已逐渐淘汰

### 3.2 主流视频平台处理
```sql
-- 示例数据：不同平台的视频资源
INSERT INTO `media_resources` VALUES 
('vid_001', 'video', 'youtube', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 'https://www.youtube.com/embed/dQw4w9WgXcQ', '<iframe width="560" height="315" src="https://www.youtube.com/embed/dQw4w9WgXcQ" frameborder="0" allowfullscreen></iframe>', 'Sample Video', 'Description', 'https://img.youtube.com/vi/dQw4w9WgXcQ/maxresdefault.jpg', 212, NULL, 560, 315, NULL, 1, NOW(), NOW()),
('vid_002', 'video', 'bilibili', 'https://www.bilibili.com/video/BV1xx411c7mD', 'https://player.bilibili.com/player.html?bvid=BV1xx411c7mD', '<iframe src="https://player.bilibili.com/player.html?bvid=BV1xx411c7mD" width="560" height="315" frameborder="0" allowfullscreen></iframe>', 'Bilibili Video', 'Description', 'https://i0.hdslb.com/bfs/archive/thumbnail.jpg', 180, NULL, 560, 315, NULL, 1, NOW(), NOW()),
('vid_003', 'video', 'tencent', 'https://v.qq.com/x/page/i0670jbe37a.html', 'https://v.qq.com/iframe/player.html?vid=i0670jbe37a&tiny=0&auto=0', '<iframe frameborder="0" src="https://v.qq.com/iframe/player.html?vid=i0670jbe37a&tiny=0&auto=0" allowfullscreen></iframe>', 'Tencent Video', 'Description', 'https://puui.qpic.cn/vcover_hz_pic/0/i0670jbe37a/0', 300, NULL, 560, 315, NULL, 1, NOW(), NOW());
```

## 4. 内容存储策略

### 4.1 混合存储方案 <mcreference link="https://blog.csdn.net/weixin_39309402/article/details/101215388" index="1">1</mcreference>
```sql
-- 文章内容字段设计
ALTER TABLE `articles` ADD COLUMN `content_type` VARCHAR(20) DEFAULT 'html' COMMENT '内容格式:html,markdown,json';
ALTER TABLE `articles` ADD COLUMN `content_raw` LONGTEXT COMMENT '原始内容(markdown/json)';
ALTER TABLE `articles` ADD COLUMN `content_html` LONGTEXT COMMENT '渲染后HTML内容';
```

### 4.2 安全性考虑
- 对HTML内容进行XSS过滤 <mcreference link="https://blog.csdn.net/weixin_39309402/article/details/101215388" index="1">1</mcreference>
- 使用 `HtmlUtils.htmlEscapeHex()` 转义特殊字符
- iframe添加安全属性：`sandbox="allow-scripts allow-same-origin"`

## 5. 性能优化建议

### 5.1 缓存策略 <mcreference link="https://cloud.tencent.com/developer/ask/136543" index="5">5</mcreference>
- 生成的HTML存储到文件系统
- 数据库存储纯文本用于搜索
- 使用Redis缓存热点文章

### 5.2 索引优化
```sql
-- 添加全文搜索索引
ALTER TABLE `articles` ADD FULLTEXT INDEX `ft_title_content` (`title`, `summary`);

-- 添加复合索引
CREATE INDEX `idx_status_time_author` ON `articles` (`status`, `create_time`, `author_id`);
```

## 6. 扩展性设计

### 6.1 支持更多媒体类型
```sql
-- 媒体类型枚举扩展
-- video: 视频(YouTube, Bilibili, 腾讯视频等)
-- audio: 音频(网易云音乐, QQ音乐等)
-- document: 文档(PDF, PPT等)
-- interactive: 交互内容(地图, 图表等)
```

### 6.2 版本控制
```sql
CREATE TABLE `article_versions` (
  `id` VARCHAR(32) NOT NULL,
  `article_id` VARCHAR(32) NOT NULL,
  `version` INT NOT NULL,
  `content` LONGTEXT,
  `change_log` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_version` (`article_id`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章版本表';
```

这种设计方案既保证了数据的结构化存储，又具备良好的扩展性和性能，是业界处理富文本内容和第三方视频嵌入的最佳实践。
        