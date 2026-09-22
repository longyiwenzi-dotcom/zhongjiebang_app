#!/usr/bin/env bash
set -euo pipefail

SOURCE_ENV=/etc/zhongjiebang-server.env
DEMO_ENV=/etc/zhongjiebang-demo.env
SITE_CONF=/etc/nginx/conf.d/zhongjiebang.conf
SNIPPET=/etc/nginx/snippets/zhongjiebang-demo.conf

test -f "$SOURCE_ENV"
set -a
source "$SOURCE_ENV"
set +a

mysql --protocol=socket <<SQL
create database if not exists zhongjiebang_demo character set utf8mb4 collate utf8mb4_unicode_ci;
create user if not exists '${DB_USER}'@'127.0.0.1' identified by '${DB_PASSWORD}';
alter user '${DB_USER}'@'127.0.0.1' identified by '${DB_PASSWORD}';
grant all privileges on zhongjiebang_demo.* to '${DB_USER}'@'127.0.0.1';
flush privileges;
SQL

install -d -o admin -g admin -m 0755 /opt/zhongjiebang-demo
install -o admin -g admin -m 0644 /tmp/bootstrap-2.0.0-SNAPSHOT.jar /opt/zhongjiebang-demo/bootstrap.jar
{
    printf 'SERVER_PORT=8090\n'
    printf 'DB_HOST=127.0.0.1\n'
    printf 'DB_PORT=3306\n'
    printf 'DB_NAME=zhongjiebang_demo\n'
    printf 'DB_USER=%s\n' "$DB_USER"
    printf 'DB_PASSWORD=%s\n' "$DB_PASSWORD"
    printf 'DB_POOL_SIZE=6\n'
    printf 'REDIS_RATE_LIMIT_ENABLED=false\n'
} > "$DEMO_ENV"
chmod 0600 "$DEMO_ENV"

install -m 0644 /tmp/zhongjiebang-demo.service /etc/systemd/system/zhongjiebang-demo.service
install -m 0644 /tmp/nginx-location.conf "$SNIPPET"

if ! grep -q 'zhongjiebang-demo.conf' "$SITE_CONF"; then
    cp -a "$SITE_CONF" "${SITE_CONF}.before-demo"
    sed -i '/include \/etc\/nginx\/snippets\/zhaozibo-resume.conf;/a\ include /etc/nginx/snippets/zhongjiebang-demo.conf;' "$SITE_CONF"
fi

nginx -t
systemctl daemon-reload
systemctl enable --now zhongjiebang-demo
systemctl reload nginx

for ignored in {1..30}; do
    if curl --fail --silent http://127.0.0.1:8090/actuator/health >/dev/null; then
        printf 'DEPLOY_OK\n'
        exit 0
    fi
    sleep 2
done

journalctl -u zhongjiebang-demo -n 80 --no-pager
exit 1
