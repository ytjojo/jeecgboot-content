
🔑 RBAC + ABAC 权限划分清单

1. RBAC（Shiro）适合管的权限

👉 特点：静态、强约束、与上下文无关，只要没有角色/权限，绝对不能操作。
	•	系统级操作（不允许被属性绕过）
	•	system:shutdown → 停机、重启、清理缓存
	•	user:delete → 删除其他用户
	•	role:assign → 角色授权
	•	安全敏感操作
	•	finance:export → 导出财务报表
	•	admin:config → 修改系统配置
	•	超管独有权限
	•	平台级别的全局配置、数据迁移
	•	只依赖角色/权限，不依赖业务上下文的操作
	•	content:publish（内容是否能发布，跟谁写的无关）

✅ 原则：没有 RBAC 权限 → 必须拒绝，不能靠 ABAC 补救。

⸻

2. ABAC（SAPL）适合管的权限

👉 特点：动态、依赖上下文、业务相关，必须看「人 + 资源 + 环境」。
	•	作者/资源所有权控制
	•	用户只能编辑自己的文章 → resource.author == subject.id
	•	用户只能删除自己上传的文件 → resource.owner == subject.id
	•	范围/领域约束
	•	频道管理员只能管理自己频道 → resource.channelId == subject.channelId
	•	部门经理只能查看本部门报表 → resource.deptId == subject.deptId
	•	临时/条件性访问
	•	VIP 到期前才能访问 → subject.vipExpireTime > now()
	•	赠送 7 天试用 → subject.tempVip == true and subject.expire > now()
	•	环境属性限制
	•	只允许在公司网络访问 → environment.ip in corp.cidr
	•	只允许工作时间下载 → environment.time between 9:00 and 18:00

✅ 原则：ABAC 补充 RBAC，不替代 RBAC，用来细化控制。

⸻

3. RBAC + ABAC 混合控制场景

👉 特点：RBAC 给粗权限，ABAC 做精细化裁决
	•	编辑内容（案例1）
	•	RBAC：content:edit（管理员有）
	•	ABAC：普通用户只能编辑自己的 → resource.author == subject.id
	•	频道管理（案例2）
	•	RBAC：channel:manage（系统管理员、频道管理员都有）
	•	ABAC：频道管理员只能管自己频道 → resource.channelId == subject.channelId
	•	VIP 内容（案例3）
	•	RBAC：vip:view（管理员直接赋予）
	•	ABAC：用户等级/有效期判断 → subject.vipLevel >= 1 or subject.vipExpireTime > now()

✅ 原则：
	1.	RBAC 做入口（大门），先过滤掉完全无关的人。
	2.	ABAC 做精细决策（小门），根据上下文给出裁决。

⸻

4. 最佳实践
	1.	RBAC = 粗粒度、静态、必需
	•	定义系统里 谁可能做什么。
	2.	ABAC = 精细化、动态、灵活
	•	定义 在什么条件下能做。
	3.	避免权限冲突
	•	明确哪些权限是「死权限」（必须 RBAC 控制）。
	•	哪些是「活权限」（ABAC 可以补救/细化）。
	4.	技术实现
	•	Shiro：权限注解/拦截器，输出 hasPermission 结果。
	•	SAPL：把 Shiro 的 hasPermission 结果作为 subject 属性之一，统一交给策略决策。

⸻

要不要我帮你整理一张 对照表（RBAC 管什么，ABAC 管什么），做成项目里的权限设计蓝图？