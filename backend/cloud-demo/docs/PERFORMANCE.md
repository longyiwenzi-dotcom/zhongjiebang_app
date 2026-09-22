# 性能验证与 SQL 优化

## 可复现压测

先注册用户、开通会员并取得 `ACCESS_TOKEN`，随后运行：

```bash
k6 run -e ACCESS_TOKEN=<token> -e HOUSE_ID=1 performance/house-detail.js
```

脚本在两分钟内从 20 个虚拟用户升到 50 个，要求错误率低于 1%，P95 低于 500 ms。仓库不填写虚构成绩；每次部署后应保存 k6 输出、服务器规格、数据量和 Git 提交号再做对比。

## 关键索引

| 表 | 索引 | 对应查询 |
| --- | --- | --- |
| `cloud_houses` | `(is_rent, status, created_at)` | 出租/出售列表按时间倒序 |
| `cloud_houses` | `(owner_id)` | 上传人查看自己的房源 |
| `cloud_house_view_events` | `(user_id, view_date)` | 当日已查看数量 |
| `cloud_house_view_events` | 唯一 `(user_id, house_id, view_date)` | 同房源当天只扣一次 |
| `cloud_house_view_history` | `(user_id, viewed_at)` | 浏览历史倒序分页 |
| `redeem_codes` | 唯一 `(code_hash)` | 摘要兑换码定位及防重复 |

## 执行计划检查

在接近生产数据量的库中执行：

```sql
explain analyze
select * from cloud_houses
where status='ACTIVE' and is_rent=true
order by id desc limit 20;

explain analyze
select count(*) from cloud_house_view_events
where user_id=1 and view_date=current_date;
```

检查是否使用预期索引、实际扫描行数是否随总数据量异常增长。`community/street like '%关键字%'` 无法使用普通 B-Tree；数据量增大后应迁移到 Elasticsearch/OpenSearch 或 MySQL FULLTEXT，而不是继续堆组合索引。

## 调优顺序

1. 先确认慢点在 Gateway、Feign、数据库还是消息队列。
2. 用 Zipkin 查看跨服务耗时，用 MySQL 慢查询日志定位 SQL。
3. 优化索引和查询字段，再评估缓存；避免用 Redis 掩盖无界查询。
4. 调整连接池时确保总连接数不超过 MySQL 承载能力。
5. 对会员扣次等强一致写操作不做结果缓存。
