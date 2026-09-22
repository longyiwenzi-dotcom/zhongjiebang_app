create database if not exists zhongjiebang_redeem;
use zhongjiebang_redeem;
create table if not exists redeem_codes (
    id bigint primary key auto_increment,
    code_hash char(64) not null unique,
    plan_code varchar(20) not null,
    used_by bigint null,
    used_at datetime(3) null
);
create table if not exists undo_log (
    branch_id bigint not null, xid varchar(128) not null, context varchar(128) not null,
    rollback_info longblob not null, log_status int not null, log_created datetime not null,
    log_modified datetime not null, unique key ux_undo_log (xid, branch_id)
);
