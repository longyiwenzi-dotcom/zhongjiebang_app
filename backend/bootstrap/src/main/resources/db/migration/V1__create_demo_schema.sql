CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    phone VARCHAR(20) NOT NULL,
    password_hash VARCHAR(100) NOT NULL DEFAULT '',
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_users_phone UNIQUE (phone)
);

CREATE TABLE user_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_sessions_token UNIQUE (token_hash),
    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX idx_sessions_user_expiry ON user_sessions(user_id, expires_at);

CREATE TABLE houses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    is_rent BOOLEAN NOT NULL,
    community VARCHAR(120),
    street VARCHAR(180),
    building_no VARCHAR(40) NOT NULL,
    unit_no VARCHAR(40) NOT NULL,
    floor_no INT NOT NULL,
    rooms INT,
    halls INT,
    bathrooms INT,
    building_area DECIMAL(10,2),
    usable_area DECIMAL(10,2),
    price DECIMAL(14,2) NOT NULL,
    payment_term VARCHAR(40),
    has_elevator BOOLEAN,
    decoration VARCHAR(40),
    orientation VARCHAR(40),
    image_urls VARCHAR(4000) NOT NULL DEFAULT '[]',
    video_link VARCHAR(800),
    specific_address VARCHAR(200),
    landlord_name VARCHAR(40),
    landlord_phone VARCHAR(20),
    uploader_phone VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_houses_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);
CREATE INDEX idx_houses_search ON houses(is_rent, status, created_at);
CREATE INDEX idx_houses_community ON houses(community);
CREATE INDEX idx_houses_street ON houses(street);
CREATE INDEX idx_houses_price ON houses(is_rent, price);

CREATE TABLE redeem_codes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code_hash VARCHAR(64) NOT NULL,
    plan_code VARCHAR(20) NOT NULL,
    batch_no VARCHAR(40) NOT NULL,
    used_by BIGINT,
    used_at TIMESTAMP(6),
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_redeem_code_hash UNIQUE (code_hash),
    CONSTRAINT fk_redeem_user FOREIGN KEY (used_by) REFERENCES users(id)
);

CREATE TABLE memberships (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    plan_code VARCHAR(20) NOT NULL,
    starts_at TIMESTAMP(6) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_membership_user FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX idx_membership_active ON memberships(user_id, expires_at);

CREATE TABLE house_view_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    house_id BIGINT NOT NULL,
    view_date DATE NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_daily_house_view UNIQUE (user_id, house_id, view_date),
    CONSTRAINT fk_view_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_view_house FOREIGN KEY (house_id) REFERENCES houses(id)
);
CREATE INDEX idx_daily_view_count ON house_view_records(user_id, view_date);

CREATE TABLE daily_view_counters (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    view_date DATE NOT NULL,
    view_count INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_daily_view_counter UNIQUE (user_id, view_date),
    CONSTRAINT fk_daily_counter_user FOREIGN KEY (user_id) REFERENCES users(id)
);
