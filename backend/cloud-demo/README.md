# Spring Cloud interview demo

This project is independent from the production modular monolith. It demonstrates a real cloud call path without forcing a 2 GB server to run every middleware component.

## Modules

| Module | Port | Responsibility |
| --- | ---: | --- |
| `gateway` | 9000 | Routing, Redis session authentication and Sentinel integration |
| `user-app` | 9101 | Registration, BCrypt password authentication and session issue |
| `house-app` | 9102 | House detail API and OpenFeign membership check |
| `membership-app` | 9103 | Permission, one-time redemption and Seata transaction boundary |
| `audit-app` | 9104 | RabbitMQ consumer for browsing history and audit events |
| `redeem-app` | 9105 | Independent redemption database and Seata transaction participant |
| `contracts` | - | Stable inter-service DTO contracts |

```mermaid
flowchart LR
    Client --> Gateway[Spring Cloud Gateway]
    Gateway --> User[User service]
    Gateway --> House[House service]
    Gateway --> Member[Membership service]
    Gateway --> Audit[Audit service]
    Member -->|OpenFeign + XID| Redeem[Redeem service]
    House -->|OpenFeign| Member
    Gateway & User --> Redis[(Redis sessions)]
    Member --> MySQL[(MySQL)]
    House -->|RabbitMQ event| Audit
    Gateway & House -.-> Sentinel[Sentinel]
    Gateway & User & House & Member --> Nacos[Nacos]
    Member -. global transaction .-> Seata[Seata]
```

The house service uses a fail-closed fallback: if membership is unavailable, paid house details remain protected.

## Run locally

1. `docker compose up -d mysql redis rabbitmq nacos sentinel zipkin`
2. `mvn clean package`
3. Start `UserApplication`, `MembershipApplication`, `HouseApplication`, then `GatewayApplication` from the IDE.
4. Register and receive an opaque session token:

```bash
curl -X POST http://127.0.0.1:9000/cloud/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"phone":"18100000001","password":"DemoPass123"}'
```

5. Use the returned `accessToken`; the gateway resolves the user and overwrites any client-supplied identity header:

```bash
curl http://127.0.0.1:9000/cloud/api/houses/1001 \
  -H "Authorization: Bearer <accessToken>"
```

Nacos is at `http://127.0.0.1:8848/nacos`, Sentinel at `http://127.0.0.1:8858`, RabbitMQ at `http://127.0.0.1:15672`, and Zipkin at `http://127.0.0.1:9411`.

To run the four applications in containers instead of the IDE, package first and then start the `apps` profile:

```bash
mvn clean package
docker compose --profile apps up -d --build
```

For the cross-service transaction demonstration, enable Seata while starting both profiles:

```bash
SEATA_ENABLED=true docker compose --profile seata --profile apps up -d --build
```

Membership owns `zhongjiebang_cloud.cloud_memberships`; redemption owns `zhongjiebang_redeem.redeem_codes`. The global XID propagates through OpenFeign, so a membership failure rolls back the redemption branch through each database's `undo_log`.

## Interview scenarios

- Stop membership service: the Sentinel/Feign fallback denies the detail request.
- Send a fake `X-User-Id`: the gateway removes it and derives identity only from the Redis session.
- Redeem one code concurrently: the conditional update permits one successful consumer.
- Disable Redis: the production app falls back to in-memory rate limiting for single-node operation.
- Explain deployment: the public 2 GB host runs the modular monolith; this full stack is a local architecture exercise.

Performance methodology and SQL execution-plan checks are documented in [`docs/PERFORMANCE.md`](docs/PERFORMANCE.md); the k6 workload is in [`performance/house-detail.js`](performance/house-detail.js).

## Security decisions

- Passwords use BCrypt with cost 12.
- Access tokens are opaque random values; Redis stores only SHA-256 token digests.
- Sessions expire after 12 hours and identity headers are never trusted from the public client.
- Membership degradation is fail-closed so a dependency outage cannot expose paid data.
- Nacos authentication is disabled only in the local Compose environment and must not be copied to a public server.
