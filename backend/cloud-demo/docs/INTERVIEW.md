# 面试讲解提纲

## 为什么保留两种架构

公网机器只有 2 GB 内存，直接部署 Nacos、Sentinel、Seata、Redis、MySQL 和四个 Java 进程会降低稳定性。因此线上使用模块化单体，按领域拆包并共享同一事务；本地演示使用微服务，展示服务治理和分布式系统取舍。这是根据资源和业务规模选择架构，而不是为了技术栈而拆分。

## 一次房源详情请求

1. Gateway 接收 Bearer Token，对令牌做 SHA-256 后查询 Redis。
2. Gateway 删除外部 `X-User-Id`，将 Redis 中的可信用户编号写入内部请求头。
3. House Service 通过 OpenFeign 调用 Membership Service 消耗查看额度。
4. Membership Service 在数据库事务中判断会员有效期和额度。
5. 调用失败时 Sentinel fallback 默认拒绝访问，防止付费数据泄露。
6. Micrometer 将 traceId 贯穿 Gateway、Feign 和下游服务并发送到 Zipkin。

## 兑换码并发安全

兑换语句带有 `used_at is null` 条件。并发请求中只有一个事务能更新一行，其他请求得到更新行数 0，因此一个兑换码不能被重复消费。正式单体还通过行锁将校验、消费和会员延期放在同一事务中。

## Redis 故障策略

登录会话依赖 Redis，Redis 故障时网关拒绝敏感请求，属于 fail-closed。正式单节点后端的接口限流则提供内存降级实现，因为限流暂时退化不会造成身份越权，并能保证低配服务器继续提供服务。

## Seata 的边界

`membership-app` 开启全局事务后，通过 OpenFeign 调用 `redeem-app`。兑换码位于 `zhongjiebang_redeem`，会员位于 `zhongjiebang_cloud`，两边都有 `undo_log`，XID 随调用传播；会员写入失败会回滚兑换码消费。默认关闭 Seata，只有演示跨库 AT 模式时才开启。生产中若业务允许，更倾向事务消息和最终一致性，以避免长事务扩大锁范围。

## 可继续扩展

- 用 Testcontainers 完成 MySQL、Redis 的端到端测试。
- 引入消息队列，把浏览历史和审计日志从主链路异步化。
- 为 Gateway 增加按用户和 IP 的令牌桶限流。
- 将密钥迁移到云端 Secret Manager，并为 Nacos 开启鉴权和网络隔离。
