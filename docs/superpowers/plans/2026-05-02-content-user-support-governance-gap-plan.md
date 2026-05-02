# Content User Support Governance Gap Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 补齐审计报告中支持治理域第一阶段的 P0 缺口，优先实现 `help-center 分类级客服联动`、`customer-service 按用户分层路由`，并补齐支持域列表接口的分页评估与必要改造。

**Architecture:** 基于现有 `ContentUserSupportController`、`ContentUserSupportServiceImpl`、`ContentHelpCenterVO`、`ContentCustomerServiceVO` 做最小增量改造，不扩展到无关模块。优先复用现有 `profile`、`status` 数据，保持 `Result<T>` 返回约定和当前模块分层方式不变。列表接口按“静态小集合不分页、用户历史与管理列表默认分页”的原则处理：`help-center` 保持非分页，`appeal/list` 升级为分页返回，`report/list` 维持分页。

**Tech Stack:** Spring Boot 3, JeecgBoot, MyBatis-Plus, JUnit 5, Mockito, MockMvc

## 启动条件

- 覆盖审计报告已确认
- 目标缺口已锁定
- 未提交本地改动影响已确认

## 第一阶段范围

- 实现 `GET /content/user/support/help-center?userId=...` 结构化返回与分类级客服推荐
- 实现 `GET /content/user/support/customer-service?userId=...` 按用户分层路由
- 将 `GET /content/user/support/appeal/list` 从 `List` 升级为分页返回
- 保持 `GET /content/user/support/report/progress`、`GET /content/user/support/help-center` 非列表或静态小列表语义不变
- 保持 `GET /content/user/support/admin/report/list` 现有分页模型不变

## 第二阶段范围

- 在 `POST /content/user/support/admin/appeal/handle` 中补齐“申诉通过后恢复可恢复治理状态”的最小闭环
- 新增 `GET /content/user/governance/status/history`，按用户维度分页查询状态历史
- 继续遵守“用户历史/管理列表默认分页、静态引导列表不分页”的接口设计原则

## 最新进展

- 第一阶段已完成：`help-center` 结构化分类推荐、`customer-service` 用户分层路由、`appeal/list` 分页返回
- 第二阶段已完成首批闭环：申诉通过时恢复可恢复治理状态；状态历史提供分页查询能力
- 第二阶段已新增：到期治理状态自动恢复任务
- 第二阶段已新增：成长处罚恢复编排，覆盖积分、成长值、等级、勋章状态以及首批 `PRIORITY_CUSTOMER_SERVICE` 等级权益恢复
- 第二阶段已新增：更多成长处罚来源建模扩展，覆盖治理处罚入口与举报处理入口统一建档
- 第二阶段已新增：成长处罚真实执行引擎，覆盖积分、成长值、等级、勋章和首批等级权益处罚闭环
- 第二阶段已新增：更多等级权益消费方落地，覆盖统一权益判定、成长摘要能力输出与 `TOPIC` 订阅额度消费
- 第二阶段剩余缺口：无
