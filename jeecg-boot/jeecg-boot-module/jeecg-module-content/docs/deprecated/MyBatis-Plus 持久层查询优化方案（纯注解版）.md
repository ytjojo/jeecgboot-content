

# MyBatis-Plus 持久层查询优化方案（纯注解版）

## 1. 核心概念

**本质**：在数据持久层使用 MyBatis-Plus 注解方式，在查询时动态选择字段、条件和关联关系，从源头上减少数据传输量和查询次数。

**适用场景**：对查询性能有极高要求的场景，作为DTO方案一（组合模式）的性能补充手段。

## 2. 实现方案

### 方案一：查询字段选择（`@TableField` + 条件构造器）

**适用场景**：简单字段过滤，不需要复杂关联查询

```java
// Entity 类
@Data
@TableName("user")
public class UserEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    @TableField("avatar_url")
    private String avatarUrl;
    
    private String email;
    
    private Integer age;
    
    private String bio;
    
    private String title;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    private Integer status;
    
    private Integer deleted;
}

// Service 实现
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {
    
    // 列表查询 - 只选择必要字段
    public List<UserItemDTO> getUserItemList() {
        List<UserEntity> entities = baseMapper.selectList(
            Wrappers.<UserEntity>lambdaQuery()
                .select(UserEntity::getId, 
                       UserEntity::getUsername,
                       UserEntity::getAvatarUrl,
                       UserEntity::getTitle)
                .eq(UserEntity::getStatus, 1)
                .eq(UserEntity::getDeleted, 0)
                .orderByDesc(UserEntity::getCreateTime)
        );
        return convertToItemDTOList(entities);
    }

    // 详情查询 - 选择更多字段
    public UserDetailDTO getUserDetail(Long id) {
        UserEntity entity = baseMapper.selectOne(
            Wrappers.<UserEntity>lambdaQuery()
                .select(UserEntity::getId,
                       UserEntity::getUsername,
                       UserEntity::getAvatarUrl,
                       UserEntity::getEmail,
                       UserEntity::getBio,
                       UserEntity::getAge,
                       UserEntity::getCreateTime)
                .eq(UserEntity::getId, id)
                .eq(UserEntity::getDeleted, 0)
        );
        return convertToDetailDTO(entity);
    }
}
```

### 方案二：动态条件查询（`@Select` + `<script>`）

**适用场景**：需要复杂动态条件查询

```java
// Mapper 接口
public interface UserMapper extends BaseMapper<UserEntity> {
    
    // 动态条件查询 - 基本版
    @Select({
        "<script>",
        "SELECT id, username, age, avatar_url FROM user",
        "WHERE deleted = 0",
        "<if test='age != null'>AND age &gt; #{age}</if>",
        "<if test='username != null'>AND username LIKE CONCAT('%', #{username}, '%')</if>",
        "<if test='status != null'>AND status = #{status}</if>",
        "ORDER BY create_time DESC",
        "</script>"
    })
    List<UserEntity> searchUsers(@Param("age") Integer age, 
                                @Param("username") String username,
                                @Param("status") Integer status);
    
    // 动态条件查询 - 包含字段选择
    @Select({
        "<script>",
        "SELECT",
        "<if test='fields == \"simple\"'>id, username, avatar_url</if>",
        "<if test='fields == \"detail\"'>id, username, avatar_url, email, age, bio</if>",
        "<if test='fields == null'>*</if>",
        "FROM user WHERE deleted = 0",
        "<if test='minAge != null'>AND age &gt;= #{minAge}</if>",
        "<if test='maxAge != null'>AND age &lt;= #{maxAge}</if>",
        "<if test='username != null'>AND username LIKE CONCAT('%', #{username}, '%')</if>",
        "ORDER BY create_time DESC",
        "</script>"
    })
    List<UserEntity> searchUsersDynamic(@Param("minAge") Integer minAge,
                                       @Param("maxAge") Integer maxAge,
                                       @Param("username") String username,
                                       @Param("fields") String fields);
    
    // 分页动态查询
    @Select({
        "<script>",
        "SELECT id, username, avatar_url, age FROM user",
        "WHERE deleted = 0",
        "<if test='params.age != null'>AND age = #{params.age}</if>",
        "<if test='params.username != null'>AND username LIKE CONCAT('%', #{params.username}, '%')</if>",
        "<if test='params.status != null'>AND status = #{params.status}</if>",
        "ORDER BY create_time DESC",
        "</script>"
    })
    Page<UserEntity> searchUsersPage(Page<?> page, @Param("params") UserQueryParams params);
}

// 查询参数DTO
@Data
public class UserQueryParams {
    private Integer age;
    private String username;
    private Integer status;
    private Integer minAge;
    private Integer maxAge;
}
```

### 方案三：跨表关联查询优化

**适用场景**：需要关联多表查询但要避免N+1问题，一次性获取所有关联数据。

#### 3.1 一对一关联查询

```java
// Mapper 接口
public interface ContentMapper extends BaseMapper<Content> {
    
    @Select("SELECT c.*, a.username as author_name, a.avatar as author_avatar " +
            "FROM contents c LEFT JOIN authors a ON c.author_id = a.id " +
            "WHERE c.id = #{id}")
    @Results({
        @Result(column = "id", property = "id"),
        @Result(column = "title", property = "title"),
        @Result(column = "cover", property = "cover"),
        @Result(column = "author_id", property = "authorId"),
        @Result(column = "author_name", property = "author.username"),
        @Result(column = "author_avatar", property = "author.avatar")
    })
    Content selectContentWithAuthor(Long id);
}
```

#### 3.2 一对多关联查询（嵌套集合）

```java
// Mapper 接口
public interface ContentMapper extends BaseMapper<Content> {
    
    @Select({
        "<script>",
        "SELECT",
        "c.id, c.title, c.cover, c.author_id,",
        "t.id as topic_id, t.name as topic_name, t.description as topic_desc",
        "FROM contents c",
        "LEFT JOIN content_topics ct ON c.id = ct.content_id",
        "LEFT JOIN topics t ON ct.topic_id = t.id",
        "WHERE c.id = #{id}",
        "</script>"
    })
    @Results(id = "contentDetailMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "title", column = "title"),
        @Result(property = "cover", column = "cover"),
        @Result(property = "authorId", column = "author_id"),
        
        // 嵌套集合映射 - 一对多关系
        @Result(property = "topics", column = "id", 
                many = @Many(select = "selectTopicsByContentId"))
    })
    Content selectContentWithTopics(Long id);
    
    // 用于嵌套查询的方法
    @Select("SELECT t.id, t.name, t.description FROM topics t " +
            "INNER JOIN content_topics ct ON t.id = ct.topic_id " +
            "WHERE ct.content_id = #{contentId}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "name", column = "name"),
        @Result(property = "description", column = "description")
    })
    List<Topic> selectTopicsByContentId(Long contentId);
}
```

#### 3.3 多对多关联查询（单次JOIN）

```java
// Mapper 接口
public interface ContentMapper extends BaseMapper<Content> {
    
    @Select({
        "<script>",
        "SELECT",
        "c.id, c.title, c.cover, c.author_id,",
        "t.id as topic_id, t.name as topic_name, t.description as topic_desc,",
        "a.id as author_id, a.username as author_name, a.avatar as author_avatar",
        "FROM contents c",
        "LEFT JOIN authors a ON c.author_id = a.id",
        "LEFT JOIN content_topics ct ON c.id = ct.content_id",
        "LEFT JOIN topics t ON ct.topic_id = t.id",
        "WHERE c.id = #{id}",
        "</script>"
    })
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "title", column = "title"),
        @Result(property = "cover", column = "cover"),
        @Result(property = "authorId", column = "author_id"),
        
        // 一对一关联
        @Result(property = "author.id", column = "author_id"),
        @Result(property = "author.username", column = "author_name"),
        @Result(property = "author.avatar", column = "author_avatar"),
        
        // 一对多关联（使用ResultHandler处理）
        @Result(property = "topics", column = "id", 
                many = @Many(resultMap = "topicResultMap"))
    })
    Content selectContentFullInfo(Long id);
    
    // 可复用的ResultMap
    @ResultMap("topicResultMap")
    @Results(id = "topicResultMap", value = {
        @Result(property = "id", column = "topic_id"),
        @Result(property = "name", column = "topic_name"),
        @Result(property = "description", column = "topic_desc")
    })
    Topic mapTopic(ResultSet rs) throws SQLException;
}
```

#### 3.4 动态关联查询

```java
// Mapper 接口
public interface ContentMapper extends BaseMapper<Content> {
    
    @Select({
        "<script>",
        "SELECT c.id, c.title, c.cover, c.author_id",
        "<if test='includeAuthor != null and includeAuthor'>",
        ", a.username as author_name, a.avatar as author_avatar",
        "</if>",
        "<if test='includeTopics != null and includeTopics'>",
        ", t.id as topic_id, t.name as topic_name",
        "</if>",
        "FROM contents c",
        "<if test='includeAuthor != null and includeAuthor'>",
        "LEFT JOIN authors a ON c.author_id = a.id",
        "</if>",
        "<if test='includeTopics != null and includeTopics'>",
        "LEFT JOIN content_topics ct ON c.id = ct.content_id",
        "LEFT JOIN topics t ON ct.topic_id = t.id",
        "</if>",
        "WHERE c.id = #{id}",
        "</script>"
    })
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "title", column = "title"),
        @Result(property = "cover", column = "cover"),
        @Result(property = "authorId", column = "author_id"),
        @Result(property = "author.username", column = "author_name"),
        @Result(property = "author.avatar", column = "author_avatar"),
        @Result(property = "topics", column = "id", 
                many = @Many(select = "selectTopicsByContentId"))
    })
    Content selectContentDynamic(@Param("id") Long id,
                                @Param("includeAuthor") Boolean includeAuthor,
                                @Param("includeTopics") Boolean includeTopics);
}
```

#### 3.5 IN查询动态条件

```java
// Mapper 接口
public interface UserMapper extends BaseMapper<UserEntity> {
    
    // IN 查询动态条件
    @Select({
        "<script>",
        "SELECT id, username, avatar_url FROM user",
        "WHERE deleted = 0",
        "<if test='statusList != null and statusList.size() > 0'>",
        "AND status IN",
        "<foreach collection='statusList' item='status' open='(' separator=',' close=')'>",
        "#{status}",
        "</foreach>",
        "</if>",
        "<if test='ageList != null and ageList.size() > 0'>",
        "AND age IN",
        "<foreach collection='ageList' item='age' open='(' separator=',' close=')'>",
        "#{age}",
        "</foreach>",
        "</if>",
        "</script>"
    })
    List<UserEntity> findUsersByConditions(@Param("statusList") List<Integer> statusList,
                                         @Param("ageList") List<Integer> ageList);
}
```

### 方案四：选择器模式动态查询

**适用场景**：需要高度灵活的查询条件

```java
// Mapper 接口
public interface UserMapper extends BaseMapper<UserEntity> {
    
    @Select({
        "<script>",
        "SELECT",
        "<choose>",
        "<when test='selectSimple != null and selectSimple'>id, username, avatar_url</when>",
        "<otherwise>id, username, avatar_url, email, age, bio, create_time</otherwise>",
        "</choose>",
        "FROM user WHERE deleted = 0",
        "<if test='username != null'>AND username = #{username}</if>",
        "<if test='minCreateTime != null'>AND create_time &gt;= #{minCreateTime}</if>",
        "<if test='maxCreateTime != null'>AND create_time &lt;= #{maxCreateTime}</if>",
        "<choose>",
        "<when test='orderBy != null'>ORDER BY ${orderBy}</when>",
        "<otherwise>ORDER BY create_time DESC</otherwise>",
        "</choose>",
        "</script>"
    })
    List<UserEntity> findUsersBySelector(@Param("username") String username,
                                       @Param("minCreateTime") LocalDateTime minCreateTime,
                                       @Param("maxCreateTime") LocalDateTime maxCreateTime,
                                       @Param("selectSimple") Boolean selectSimple,
                                       @Param("orderBy") String orderBy);
}
```

## 3. Service层使用示例

### 3.1 基础查询服务

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    // 动态查询服务方法
    public List<UserItemDTO> searchUsers(UserSearchRequest request) {
        List<UserEntity> entities = userMapper.searchUsers(
            request.getAge(),
            request.getUsername(),
            request.getStatus()
        );
        return convertToItemDTOList(entities);
    }

    // 复杂动态查询
    public Page<UserItemDTO> searchUsersAdvanced(UserQueryParams params, Pageable pageable) {
        Page<UserEntity> page = new Page<>(pageable.getPageNumber(), pageable.getPageSize());
        userMapper.searchUsersPage(page, params);
        return page.convert(this::convertToItemDTO);
    }

    // 转换方法
    private UserItemDTO convertToItemDTO(UserEntity entity) {
        UserItemDTO dto = new UserItemDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setAvatarUrl(entity.getAvatarUrl());
        return dto;
    }
}
```

### 3.2 关联查询服务

```java
@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentMapper contentMapper;

    // 获取内容详情（包含作者信息）
    public ContentDetailDTO getContentDetail(Long contentId) {
        Content content = contentMapper.selectContentWithAuthor(contentId);
        return convertToDetailDTO(content);
    }

    // 获取内容完整信息（包含所有关联数据）
    public ContentFullDTO getContentFullInfo(Long contentId) {
        Content content = contentMapper.selectContentFullInfo(contentId);
        return convertToFullDTO(content);
    }

    // 动态选择关联数据
    public ContentDetailDTO getContentDynamic(Long contentId, boolean includeTopics) {
        Content content = contentMapper.selectContentDynamic(contentId, true, includeTopics);
        return convertToDetailDTO(content);
    }

    // 转换方法
    private ContentDetailDTO convertToDetailDTO(Content content) {
        ContentDetailDTO dto = new ContentDetailDTO();
        dto.setId(content.getId());
        dto.setTitle(content.getTitle());
        dto.setCover(content.getCover());
        
        if (content.getAuthor() != null) {
            AuthorSimpleDTO author = new AuthorSimpleDTO();
            author.setId(content.getAuthor().getId());
            author.setUsername(content.getAuthor().getUsername());
            author.setAvatar(content.getAuthor().getAvatar());
            dto.setAuthor(author);
        }
        
        if (content.getTopics() != null) {
            List<TopicItemDTO> topics = content.getTopics().stream()
                .map(topic -> {
                    TopicItemDTO topicDTO = new TopicItemDTO();
                    topicDTO.setId(topic.getId());
                    topicDTO.setName(topic.getName());
                    return topicDTO;
                })
                .collect(Collectors.toList());
            dto.setTopics(topics);
        }
        
        return dto;
    }
}
```

### 3.3 查询请求VO

```java
// 查询请求VO
@Data
public class UserSearchRequest {
    private Integer age;
    private String username;
    private Integer status;
    private Integer minAge;
    private Integer maxAge;
}
```

## 4. 性能优化建议

### 4.1 关联查询优化策略

1. **避免N+1查询**：使用JOIN一次性获取所有数据，而不是多次查询
2. **按需加载关联**：使用动态SQL条件控制是否加载关联数据
3. **字段选择优化**：只选择需要的字段，避免 `SELECT *`
4. **分页优化**：在主查询层面进行分页，而不是在内存中分页

### 4.2 最佳实践建议

1. **优先使用Lambda查询**：简单条件推荐使用条件构造器
2. **复杂逻辑用注解动态SQL**：`<script>` 标签处理复杂动态条件
3. **避免SQL注入**：使用 `#{}` 参数绑定，不要用 `${}` 拼接用户输入
4. **字段选择优化**：在SQL中明确指定需要查询的字段
5. **分页查询**：结合MyBatis-Plus分页插件使用
6. **关联查询优化**：合理使用JOIN，避免过度嵌套查询
7. **缓存策略**：对于频繁查询的关联数据，考虑使用缓存

### 4.3 配置优化

```yaml
# application.yml 配置
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    # 开启驼峰命名转换
    map-underscore-to-camel-case: true
    # 设置超时时间
    default-statement-timeout: 30
  global-config:
    db-config:
      logic-delete-field: deleted # 逻辑删除字段
      logic-delete-value: 1       # 删除值
      logic-not-delete-value: 0   # 未删除值
  # 分页插件配置
  plugins:
    - com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor
```

## 5. 动态SQL标签说明

| 标签 | 用途 | 示例 |
|------|------|------|
| `<if>` | 条件判断 | `<if test='name != null'>AND name = #{name}</if>` |
| `<choose>` | 多路选择 | `<choose><when test='...'><otherwise></otherwise></choose>` |
| `<foreach>` | 循环遍历 | `<foreach collection='list' item='item' index='index'>` |
| `<where>` | 智能WHERE | `<where><if>...</if></where>` |
| `<set>` | 智能SET | `<set><if>...</if></set>` |

这样既保持了纯注解的风格，又充分利用了 MyBatis 的动态 SQL 能力，实现了灵活的查询优化。

### 4.4 索引优化

```sql
-- 为关联字段创建索引
CREATE INDEX idx_content_author ON contents(author_id);
CREATE INDEX idx_content_topic ON content_topics(content_id, topic_id);
CREATE INDEX idx_topic_content ON content_topics(topic_id, content_id);
```

### 4.5 查询性能对比

```java
// ❌ 不推荐：N+1查询（性能差）
Content content = contentMapper.selectById(contentId);
List<Topic> topics = topicMapper.selectByContentId(contentId); // 额外查询
content.setTopics(topics);

// ✅ 推荐：单次JOIN查询（性能优）
Content content = contentMapper.selectContentWithTopics(contentId); // 一次查询所有数据
```

## 5. 最佳实践总结

1. **明确关联需求**：根据业务场景决定需要加载哪些关联数据
2. **使用动态关联**：通过参数控制是否加载关联关系
3. **监控SQL性能**：使用Druid等工具监控关联查询性能
4. **合理使用索引**：为关联字段创建合适的索引
5. **避免过度关联**：不要一次性加载所有可能的关联数据
6. **结果集去重**：一对多关联时可能出现重复数据，需要在业务层处理
7. **内存占用控制**：一次性加载大量关联数据可能占用较多内存
8. **查询复杂度平衡**：复杂的关联查询可能影响执行计划，需要测试性能

## 6. 总结

本文档提供了MyBatis-Plus持久层查询优化的完整解决方案，涵盖了从基础字段选择到复杂关联查询的各种场景。通过合理使用这些技术，可以在保持代码简洁性的同时，显著提升查询性能，为高并发应用提供强有力的数据访问支持。