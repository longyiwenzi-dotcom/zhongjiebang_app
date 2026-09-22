# 哈市中介帮

哈市中介帮包含 Android 客户端、原业务服务和用于 Java 面试展示的重构后端。

- `app/`：Android 客户端。
- `server/`：当前线上兼容服务。
- `backend/`：Java 17 多模块重构工程，拥有独立数据库和部署入口。

重构后端覆盖账号鉴权、房源搜索与数据权限、会员额度、一次性兑换码、数据库迁移、
OpenAPI、自动化测试和 CI。详细运行方式见 [backend/README.md](backend/README.md)。

本地微服务演示位于 [`backend/cloud-demo`](backend/cloud-demo/README.md)，覆盖 Gateway、Nacos、OpenFeign、Sentinel、Seata、Redis 与 Zipkin 链路追踪。

在线演示部署后通过 `/zhongjiebang-demo/` 访问，原在线简历 `/resume/` 保持不变。

项目地址：[github.com/longyiwenzi-dotcom/zhongjiebang_app](https://github.com/longyiwenzi-dotcom/zhongjiebang_app)
