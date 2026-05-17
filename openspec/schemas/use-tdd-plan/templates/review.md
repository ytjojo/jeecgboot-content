功能正确性(Correctness)

* 需求实现正确
* 边界条件完整
* 异常流程正确


架构(Architecture)

* 分层合理
* 无越层依赖
* 模块边界清晰

安全(Security)

* 无敏感日志
* 权限安全
* 输入校验


测试/日志埋点(Testability/Observability)

* 单测存在
* 埋点完整

稳定性(Robustness)

系统是否抗异常。

重点：

* 是否处理异常
* 是否有 fallback
* 是否可恢复
* 是否存在状态错乱

规范一致性(Consistency)

包括：

* 命名规范
* 目录结构
* API 风格
* 注释风格
* 错误处理风格

业务风险（Business Risk）


重点：

* 是否影响核心链路
* 是否兼容旧数据
* 是否会影响线上用户
* 是否支持灰度/回滚