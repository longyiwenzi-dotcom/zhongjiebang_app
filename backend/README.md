# 哈市中介帮 Java 面试项目

这是从现有 Android + Spring Boot 项目演进出的独立演示后端。它使用 Java 17、Spring Boot、
Spring Security、JDBC、Flyway 和 MySQL，重点展示真实业务规则、鉴权、事务和并发安全。

## 两种部署形态

- 当前目录的业务模块打包为模块化单体，适合公网 2 GB 服务器：只运行一个 Java 进程，但保留清晰领域边界。
- [`cloud-demo`](cloud-demo/README.md) 是本地 Spring Cloud 面试环境，包含 Nacos、Gateway、OpenFeign、Sentinel、Seata 和 Redis。

正式 API 设置 `REDIS_RATE_LIMIT_ENABLED=true` 后启用多实例共享限流；未启用时使用内存实现，保证单机演示不依赖 Redis 也能启动。

## 模块

- `common`：值对象、业务异常、分页模型。
- `user-service`：账号、BCrypt 密码、摘要化会话令牌。
- `house-service`：房源发布、组合查询、私密联系方式的数据权限。
- `membership-service`：会员期限、每日查看额度、一次性兑换码。
- `bootstrap`：REST API、安全过滤器、数据库迁移和可运行入口。

## 本地运行

```bash
docker compose up -d mysql
DB_PORT=3307 DB_PASSWORD=demo-local-only mvn -pl bootstrap -am spring-boot:run
```

接口文档：`http://localhost:8090/zhongjiebang-demo/docs`

## 安全设计

- 客户端手机号不再作为身份凭据，所有敏感操作从服务端会话读取用户身份。
- 数据库只保存 BCrypt 密码和 SHA-256 会话令牌摘要。
- 房东电话和具体地址只向房源上传人返回。
- 兑换码只保存摘要，并在同一事务内锁定、消费和开通会员。
- 数据库结构全部通过 Flyway 迁移，不在业务代码启动时执行临时建表或改表。
