create table if not exists cloud_users (
    id bigint primary key auto_increment,
    phone char(11) not null unique,
    password_hash varchar(100) not null,
    active boolean not null default true,
    created_at datetime(3) not null default current_timestamp(3)
);
create table if not exists cloud_houses (
    id bigint primary key auto_increment,
    owner_id bigint not null,
    is_rent boolean not null,
    community varchar(255), street varchar(255), building varchar(50) not null, unit_no varchar(50) not null,
    floor int not null, building_area decimal(10,2), usable_area decimal(10,2), price decimal(12,2) not null,
    payment_term varchar(50), image_urls text, video_link varchar(1000), specific_address varchar(500),
    landlord_phone varchar(20), status varchar(20) not null default 'ACTIVE',
    created_at datetime(3) not null default current_timestamp(3),
    key ix_house_search(is_rent, status, created_at), key ix_house_owner(owner_id)
);

create table if not exists cloud_memberships (
    user_id bigint primary key,
    plan_code varchar(20) not null,
    expires_at datetime(3) not null
);
create table if not exists cloud_redeem_codes (
    id bigint primary key auto_increment,
    code_hash char(64) not null unique,
    plan_code varchar(20) not null,
    used_by bigint null,
    used_at datetime(3) null
);
create table if not exists cloud_house_view_events (
    id bigint primary key auto_increment,
    user_id bigint not null,
    house_id bigint not null,
    view_date date not null,
    created_at datetime(3) not null default current_timestamp(3),
    unique key ux_user_house_day(user_id, house_id, view_date),
    key ix_user_day(user_id, view_date)
);
create table if not exists cloud_house_view_history (
    id bigint primary key auto_increment,
    user_id bigint not null, house_id bigint not null, viewed_at datetime(3) not null, trace_id varchar(64),
    key ix_history_user_time(user_id, viewed_at)
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
