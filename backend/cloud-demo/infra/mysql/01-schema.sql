create table if not exists cloud_users (
    id bigint primary key auto_increment,
    phone char(11) not null unique,
    password_hash varchar(100) not null,
    active boolean not null default true,
    created_at datetime(3) not null default current_timestamp(3)
);

create table if not exists cloud_memberships (
    user_id bigint primary key,
    plan_code varchar(20) not null,
    expires_at datetime(3) not null
);
create table if not exists cloud_redeem_codes (
    id bigint primary key auto_increment,
    code_hash char(64) not null unique,
    used_by bigint null,
    used_at datetime(3) null
);
create table if not exists undo_log (
    branch_id bigint not null,
    xid varchar(128) not null,
    context varchar(128) not null,
    rollback_info longblob not null,
    log_status int not null,
    log_created datetime not null,
    log_modified datetime not null,
    unique key ux_undo_log (xid, branch_id)
);
insert ignore into cloud_memberships(user_id, plan_code, expires_at)
values (1, 'WEEK', date_add(now(), interval 7 day));
