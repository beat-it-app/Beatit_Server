-- PostgreSQL 기준.
-- 기존 team_parts 행에는 어느 멤버의 파트인지 알 수 있는 정보가 없으므로,
-- 운영 데이터가 있다면 user_id를 먼저 직접 매핑한 뒤 NOT NULL을 적용해야 한다.

ALTER TABLE team_parts
    ADD COLUMN IF NOT EXISTS user_id BIGINT;

-- 예시: 기존 데이터가 개발용 더미이고 유지할 필요가 없을 때만 직접 실행한다.
-- DELETE FROM team_parts WHERE user_id IS NULL;

ALTER TABLE team_parts
    ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE team_parts
    ADD CONSTRAINT fk_team_parts_user
        FOREIGN KEY (user_id) REFERENCES users(user_id);

ALTER TABLE team_parts
    ADD CONSTRAINT uk_team_parts_team_user
        UNIQUE (team_id, user_id);

CREATE INDEX idx_team_parts_team_active_order
    ON team_parts (team_id, is_active, display_order);
