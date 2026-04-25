## 评论转发功能设计

基于用户更新的 `contents` 表结构，重新设计评论转发功能。用户在发表内容时就可以选择是原创内容还是转发内容，而不需要后续转换。

### 设计理念

用户在评论时就明确选择：
1. **普通评论**：对内容进行评论讨论
2. **转发评论**：转发内容并添加自己的观点，生成新的内容

### 数据库设计（已更新）

#### contents 表结构（用户已更新）
用户已在 `contents` 表中添加了以下关键字段：
- `source_type ENUM('ORIGINAL', 'REPOST') DEFAULT 'ORIGINAL'` - 内容来源类型
- `original_post_id INT NULL` - 原始帖子ID（转发时使用）
- `repost_count INT DEFAULT 0` - 转发数量

相关索引：
- `INDEX idx_original_post_id (original_post_id)` - 原帖索引
- `INDEX idx_source_type (source_type)` - 来源类型索引

### 功能流程设计

#### 1. 用户交互流程

**前端界面设计**：
- 在内容详情页提供两个操作按钮：
  - 「评论」：普通评论功能
  - 「转发」：转发并评论功能

**转发流程**：
1. 用户点击「转发」按钮
2. 弹出转发编辑器，显示原内容预览
3. 用户输入转发评论内容
4. 选择发布频道（可选）
5. 提交后直接创建新的 `contents` 记录

#### 2. 数据处理逻辑

**转发内容创建**：
```sql
-- 创建转发内容
INSERT INTO contents (
    user_id, channel_id, title, content, content_source, content_plain,
    content_type, editor_type, source_type, original_post_id,
    status, visibility, created_at
) VALUES (
    ?, -- 转发用户ID
    ?, -- 频道ID
    CONCAT('转发: ', SUBSTRING(original_title, 1, 50)), -- 转发标题
    ?, -- 转发内容（包含原内容引用和用户评论）
    ?, -- 原始内容源
    ?, -- 纯文本内容
    'POST', -- 内容类型
    'RICH_TEXT', -- 编辑器类型
    'REPOST', -- 来源类型：转发
    ?, -- 原始帖子ID
    'PUBLISHED', -- 状态
    'PUBLIC', -- 可见性
    NOW()
);

-- 更新原内容的转发计数
UPDATE contents 
SET repost_count = repost_count + 1 
WHERE id = ?;
```

#### 3. 内容展示格式

**转发内容结构**：
```html
<!-- 用户转发评论 -->
<div class="repost-comment">
    <p>用户的转发评论内容...</p>
</div>

<!-- 原内容引用 -->
<div class="original-content-quote">
    <div class="quote-header">
        <span class="original-author">@原作者</span>
        <span class="original-time">发布时间</span>
    </div>
    <div class="quote-content">
        <h4>原内容标题</h4>
        <p>原内容摘要...</p>
        <!-- 原内容附件缩略图 -->
    </div>
</div>
```

### API设计

#### 1. 转发内容接口
```java
/**
 * 转发内容
 * @param originalContentId 原内容ID
 * @param repostRequest 转发请求参数
 * @return 转发后的内容ID
 */
@PostMapping("/contents/{originalContentId}/repost")
public Result<Long> repostContent(
    @PathVariable Long originalContentId,
    @RequestBody RepostContentRequest repostRequest
) {
    Long repostContentId = contentService.repostContent(originalContentId, repostRequest);
    return Result.ok(repostContentId);
}

/**
 * 转发请求参数
 */
public class RepostContentRequest {
    private String content; // 转发评论内容
    private Long channelId; // 发布频道ID（可选）
    private String title; // 自定义标题（可选）
    private List<AttachmentInfo> attachments; // 附件信息（可选）
    // getters and setters...
}
```

#### 2. 获取转发列表接口
```java
/**
 * 获取内容的转发列表
 * @param contentId 内容ID
 * @param pageRequest 分页参数
 * @return 转发列表
 */
@GetMapping("/contents/{contentId}/reposts")
public Result<PageResult<ContentVO>> getReposts(
    @PathVariable Long contentId,
    @ModelAttribute PageRequest pageRequest
) {
    PageResult<ContentVO> reposts = contentService.getReposts(contentId, pageRequest);
    return Result.ok(reposts);
}
```

### 业务规则

#### 1. 权限控制
- 所有用户都可以转发公开内容
- 私有内容只有好友可以转发
- 被拉黑用户无法转发对方内容

#### 2. 内容处理
- 转发内容必须包含用户的评论（不能空转发）
- 转发内容自动包含原内容的引用
- 转发链不超过3层（防止无限转发）

#### 3. 统计更新
- 原内容的 `repost_count` 自动增加
- 转发内容独立计算互动数据
- 支持对转发内容进行评论和再次转发

### 查询优化

#### 1. 获取原创内容
```sql
SELECT * FROM contents 
WHERE source_type = 'ORIGINAL' 
AND status = 'PUBLISHED'
ORDER BY created_at DESC;
```

#### 2. 获取转发内容及原内容信息
```sql
SELECT 
    r.*, -- 转发内容
    o.title as original_title,
    o.content as original_content,
    o.user_id as original_user_id,
    o.created_at as original_created_at
FROM contents r
LEFT JOIN contents o ON r.original_post_id = o.id
WHERE r.source_type = 'REPOST'
AND r.status = 'PUBLISHED'
ORDER BY r.created_at DESC;
```

#### 3. 获取热门转发内容
```sql
SELECT 
    original_post_id,
    COUNT(*) as repost_count,
    MAX(created_at) as latest_repost_time
FROM contents 
WHERE source_type = 'REPOST'
AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY original_post_id
ORDER BY repost_count DESC, latest_repost_time DESC
LIMIT 10;
```

### 功能优势

1. **用户体验优化**：用户在发表时就明确意图，避免后续转换的复杂性
2. **内容传播**：转发机制促进优质内容的传播和讨论
3. **数据一致性**：统一的内容表结构保证数据一致性
4. **扩展性强**：支持多层转发和复杂的内容关系
5. **性能优化**：通过索引和合理的查询设计保证查询性能

这种设计更符合现代社交媒体的使用习惯，用户在转发时就能明确表达自己的观点，同时保持了系统架构的简洁性和一致性。