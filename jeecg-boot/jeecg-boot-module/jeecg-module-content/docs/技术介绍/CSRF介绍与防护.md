# CSRF介绍与防护文档

## 文档概述

**文档目标：** 全面介绍CSRF（跨站请求伪造）攻击原理、危害及防护方案，并结合JeecgBoot内容社区项目提供具体的落地实施指南。

**适用范围：** JeecgBoot框架开发者、内容社区系统开发团队、安全工程师

**技术栈：** Spring Boot、Shiro、Spring Security、Vue3、内容社区系统

---

## 一、CSRF攻击原理与危害

### 1.1 什么是CSRF攻击

CSRF（Cross-Site Request Forgery，跨站请求伪造）是一种网络攻击方式，攻击者诱导受害者在已登录的Web应用程序上执行非预期的操作。攻击者利用受害者在目标网站的已认证会话，通过构造恶意请求来执行未授权的操作。

#### 1.1.1 攻击原理

```
用户 -> 目标网站: 1. 正常登录
目标网站 -> 用户: 2. 返回认证Cookie
用户 -> 恶意网站: 3. 访问恶意网站
恶意网站 -> 用户: 4. 返回包含恶意请求的页面
用户 -> 目标网站: 5. 浏览器自动发送恶意请求（携带Cookie）
目标网站 -> 用户: 6. 执行恶意操作并返回结果
```

#### 1.1.2 攻击条件

CSRF攻击需要满足以下条件：

1. **用户已登录目标网站**：受害者在目标网站有有效的认证会话
2. **用户访问恶意网站**：受害者在同一浏览器中访问了攻击者控制的网站
3. **目标网站存在CSRF漏洞**：目标网站没有有效的CSRF防护机制
4. **浏览器自动发送Cookie**：浏览器会自动在跨域请求中携带目标网站的Cookie

### 1.2 CSRF攻击类型

#### 1.2.1 GET型CSRF攻击

通过GET请求执行恶意操作，通常通过图片标签、链接等方式触发。

**攻击示例：**
```html
<!-- 恶意网站页面 -->
<img src="http://content-community.com/content/delete?id=123" style="display:none;">
<a href="http://content-community.com/user/follow?userId=attacker">点击查看精彩内容</a>
```

#### 1.2.2 POST型CSRF攻击

通过POST请求执行恶意操作，通常通过自动提交的表单实现。

**攻击示例：**
```html
<!-- 恶意网站页面 -->
<form id="maliciousForm" action="http://content-community.com/content/create" method="POST">
    <input type="hidden" name="title" value="恶意内容标题">
    <input type="hidden" name="content" value="恶意内容正文">
    <input type="hidden" name="communityId" value="123">
</form>
<script>
    document.getElementById('maliciousForm').submit();
</script>
```

### 1.3 对内容社区系统的危害

在内容社区系统中，CSRF攻击可能造成以下危害：

**1. 内容管理危害**
- **恶意发布内容**：攻击者可以代替用户发布垃圾内容、广告信息或恶意链接
- **删除重要内容**：攻击者可以删除用户的重要文章、评论等内容
- **篡改内容信息**：修改文章标题、正文、标签等关键信息

**2. 社区管理危害**
- **恶意加入社区**：强制用户加入特定社区或退出重要社区
- **权限滥用**：如果用户是管理员，可能被利用进行社区管理操作
- **破坏社区秩序**：批量操作可能导致社区内容混乱

**3. 用户关系危害**
- **恶意关注/取消关注**：操控用户的关注关系
- **发送垃圾消息**：代替用户发送私信或评论
- **投票操纵**：在投票功能中进行恶意投票

---

## 二、CSRF防护技术方案

### 2.1 防护原理

CSRF防护的核心思想是验证请求的合法性，确保请求确实来自于用户的主观意愿，而不是被恶意网站诱导产生的。

#### 2.1.1 防护策略

1. **验证请求来源**：检查HTTP Referer头部
2. **使用CSRF Token**：在请求中包含随机生成的令牌
3. **验证自定义头部**：要求AJAX请求包含特定头部
4. **双重Cookie验证**：使用Cookie和请求参数的双重验证
5. **SameSite Cookie属性**：限制Cookie的跨站发送

### 2.2 CSRF Token防护方案

#### 2.2.1 Synchronizer Token Pattern

这是最常用和最有效的CSRF防护方案。

**工作原理：**
1. 服务器为每个用户会话生成唯一的CSRF Token
2. Token存储在服务器端（Session或缓存中）
3. 客户端在每个状态改变请求中包含Token
4. 服务器验证Token的有效性

### 2.3 HTTP Referer验证

检查HTTP请求的Referer头部，确保请求来自合法的源站。

**优势：**
- 实现简单，无需修改前端代码
- 对用户透明，不影响用户体验

**局限性：**
- 部分浏览器或代理可能不发送Referer
- 用户可能禁用Referer发送
- HTTPS到HTTP的请求不会发送Referer

### 2.4 SameSite Cookie属性

SameSite是Cookie的一个属性，用于控制Cookie在跨站请求中的发送行为。

**属性值：**
- **Strict**：最严格，完全禁止跨站发送Cookie
- **Lax**：相对宽松，只在安全的跨站请求中发送Cookie（如GET请求的导航）
- **None**：允许跨站发送Cookie（需要配合Secure属性）

---

## 三、JeecgBoot项目CSRF防护实施

### 3.1 现状分析

#### 3.1.1 当前安全架构

基于代码分析，JeecgBoot项目的安全架构如下：

**主要安全框架：**
- **Shiro**：主要的安全框架，负责认证和授权
- **Spring Security**：在微服务监控模块中使用
- **JWT**：用于无状态身份验证

**现有CSRF防护：**
- 微服务监控模块启用了Spring Security的CSRF防护
- 主应用使用Shiro，默认未启用CSRF防护
- 前后端分离架构，主要依赖JWT Token进行身份验证

### 3.2 CSRF防护策略选择

考虑到JeecgBoot项目的特点，推荐采用以下防护策略：

| 防护方案 | 适用场景 | 优先级 | 实施难度 |
|---------|---------|--------|---------|
| CSRF Token | 表单提交、重要操作 | 高 | 中 |
| 自定义头部验证 | AJAX请求 | 高 | 低 |
| Referer验证 | 辅助验证 | 中 | 低 |
| SameSite Cookie | Cookie安全 | 中 | 低 |

**推荐方案：CSRF Token + 自定义头部验证**
- 对于传统表单提交使用CSRF Token
- 对于AJAX请求使用自定义头部验证
- 结合JWT Token的现有认证机制

---

## 四、内容社区项目CSRF防护落地方案

### 4.1 业务场景分析

#### 4.1.1 高风险操作识别

基于内容社区系统的业务特点，以下操作需要重点进行CSRF防护：

**内容管理操作：**
- 发布新内容（POST /content/create）
- 编辑内容（PUT /content/update）
- 删除内容（DELETE /content/delete）
- 内容状态变更（PUT /content/status）

**社区管理操作：**
- 加入社区（POST /community/join）
- 退出社区（POST /community/leave）
- 创建社区（POST /community/create）
- 社区设置修改（PUT /community/settings）

**用户关系操作：**
- 关注用户（POST /user/follow）
- 取消关注（POST /user/unfollow）
- 发送私信（POST /message/send）
- 用户资料修改（PUT /user/profile）

#### 4.1.2 风险等级评估

| 操作类型 | 风险等级 | 防护策略 | 验证方式 |
|---------|---------|---------|---------|
| 内容发布/编辑 | 高 | CSRF Token + 自定义头部 | 双重验证 |
| 用户关系操作 | 高 | CSRF Token + Referer | 双重验证 |
| 社区管理 | 中 | CSRF Token | 单一验证 |
| 个人设置 | 中 | CSRF Token + 二次确认 | 增强验证 |
| 管理员操作 | 极高 | 多重验证 + 操作日志 | 最高级别 |

### 4.2 实施步骤

#### 4.2.1 第一阶段：基础防护实施

1. **创建CSRF Token服务**
2. **实现Shiro CSRF过滤器**
3. **更新前端请求拦截器**
4. **配置高风险接口防护**

#### 4.2.2 第二阶段：增强防护实施

1. **添加自定义头部验证**
2. **实施Referer验证**
3. **配置SameSite Cookie**
4. **添加操作日志记录**

#### 4.2.3 第三阶段：监控与优化

1. **实施安全监控**
2. **性能优化**
3. **用户体验优化**
4. **安全测试验证**

---

## 五、代码实现示例

### 5.1 CSRF Token服务

```java
/**
 * CSRF Token管理服务
 * 负责CSRF Token的生成、存储和验证
 */
@Service
@Slf4j
public class CsrfTokenService {
    
    @Autowired
    private RedisUtil redisUtil;
    
    private static final String CSRF_TOKEN_PREFIX = "csrf_token:";
    private static final int TOKEN_EXPIRE_TIME = 3600; // 1小时过期
    
    /**
     * 为用户生成CSRF Token
     */
    public String generateToken(String userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String cacheKey = CSRF_TOKEN_PREFIX + userId;
        redisUtil.set(cacheKey, token, TOKEN_EXPIRE_TIME);
        log.debug("为用户{}生成CSRF Token：{}", userId, token);
        return token;
    }
    
    /**
     * 验证CSRF Token
     */
    public boolean validateToken(String userId, String token) {
        if (userId == null || token == null) {
            return false;
        }
        String cacheKey = CSRF_TOKEN_PREFIX + userId;
        String expectedToken = (String) redisUtil.get(cacheKey);
        return token.equals(expectedToken);
    }
}
```

### 5.2 Shiro CSRF过滤器

```java
/**
 * Shiro CSRF防护过滤器
 */
@Slf4j
public class ShiroCsrfFilter extends AccessControlFilter {
    
    private static final String CSRF_TOKEN_HEADER = "X-CSRF-Token";
    private static final String CSRF_TOKEN_PARAM = "_csrf";
    
    @Autowired
    private CsrfTokenService csrfTokenService;
    
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // 只对状态改变的请求进行CSRF验证
        if (!isStateChangingRequest(httpRequest)) {
            return true;
        }
        
        Subject subject = SecurityUtils.getSubject();
        if (!subject.isAuthenticated()) {
            return true; // 未认证用户不需要CSRF验证
        }
        
        return validateCsrfToken(httpRequest);
    }
    
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
        httpResponse.setContentType("application/json;charset=UTF-8");
        
        Result<String> result = Result.error("CSRF验证失败");
        httpResponse.getWriter().write(JSON.toJSONString(result));
        return false;
    }
    
    private boolean isStateChangingRequest(HttpServletRequest request) {
        String method = request.getMethod();
        return "POST".equals(method) || "PUT".equals(method) || 
               "DELETE".equals(method) || "PATCH".equals(method);
    }
    
    private boolean validateCsrfToken(HttpServletRequest request) {
        String tokenFromRequest = getTokenFromRequest(request);
        if (tokenFromRequest == null) {
            return false;
        }
        
        String userId = (String) SecurityUtils.getSubject().getPrincipal();
        return csrfTokenService.validateToken(userId, tokenFromRequest);
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(CSRF_TOKEN_HEADER);
        if (token != null) {
            return token;
        }
        return request.getParameter(CSRF_TOKEN_PARAM);
    }
}
```

### 5.3 前端集成

```javascript
// CSRF Token管理
class CsrfTokenManager {
    constructor() {
        this.token = null;
    }
    
    async getToken() {
        if (!this.token) {
            await this.fetchToken();
        }
        return this.token;
    }
    
    async fetchToken() {
        try {
            const response = await axios.get('/api/csrf/token');
            this.token = response.data.result;
        } catch (error) {
            console.error('获取CSRF Token失败：', error);
            throw error;
        }
    }
}

// Axios请求拦截器
axios.interceptors.request.use(
    async (config) => {
        if (['post', 'put', 'delete', 'patch'].includes(config.method?.toLowerCase())) {
            try {
                const csrfManager = new CsrfTokenManager();
                const token = await csrfManager.getToken();
                config.headers['X-CSRF-Token'] = token;
            } catch (error) {
                console.error('添加CSRF Token失败：', error);
            }
        }
        
        config.headers['X-Requested-With'] = 'XMLHttpRequest';
        config.headers['X-Custom-Header'] = 'content-community';
        
        return config;
    }
);
```

---

## 六、安全测试与验证

### 6.1 测试用例

#### 6.1.1 CSRF攻击测试

1. **无Token攻击测试**：验证没有CSRF Token的请求被拒绝
2. **错误Token攻击测试**：验证错误的CSRF Token被拒绝
3. **过期Token攻击测试**：验证过期的CSRF Token被拒绝
4. **跨域攻击测试**：验证跨域恶意请求被拒绝

#### 6.1.2 功能测试

1. **正常操作测试**：验证正常用户操作不受影响
2. **性能测试**：验证CSRF防护不影响系统性能
3. **兼容性测试**：验证不同浏览器的兼容性

### 6.2 监控指标

1. **CSRF攻击次数**：记录被拦截的CSRF攻击
2. **Token生成频率**：监控Token生成的频率
3. **验证失败率**：监控CSRF验证的失败率
4. **响应时间影响**：监控CSRF验证对响应时间的影响

---

## 七、最佳实践建议

### 7.1 开发建议

1. **默认启用CSRF防护**：对所有状态改变操作默认启用CSRF防护
2. **合理设置Token过期时间**：平衡安全性和用户体验
3. **提供友好的错误提示**：当CSRF验证失败时，提供清晰的错误信息
4. **记录安全日志**：记录所有CSRF相关的安全事件

### 7.2 运维建议

1. **定期安全审计**：定期检查CSRF防护的有效性
2. **监控异常请求**：监控和分析异常的CSRF请求
3. **及时更新防护策略**：根据新的攻击手段更新防护策略
4. **用户安全教育**：教育用户识别和防范CSRF攻击

### 7.3 性能优化

1. **使用Redis缓存Token**：提高Token验证的性能
2. **合理设置缓存过期时间**：避免内存泄漏
3. **异步处理日志记录**：避免影响主要业务流程
4. **批量验证优化**：对批量操作进行优化

---

## 八、总结

CSRF攻击是Web应用面临的重要安全威胁，特别是对于内容社区这类用户交互频繁的系统。通过实施综合的CSRF防护方案，包括Token验证、头部验证、来源验证等多层防护措施，可以有效防范CSRF攻击，保护用户和系统的安全。

在JeecgBoot内容社区项目中，我们建议采用CSRF Token作为主要防护手段，结合自定义头部验证和Referer验证作为辅助防护，形成多层次的安全防护体系。同时，要注重用户体验，确保安全防护不会对正常用户操作造成不便。

通过持续的安全监控、测试验证和策略优化，可以建立起一个既安全又易用的内容社区系统，为用户提供安全可靠的服务环境。