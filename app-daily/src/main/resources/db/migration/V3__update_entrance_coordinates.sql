-- 기존 데이터를 임시 컬럼으로 복사
ALTER TABLE road_name_address_entrances
    ADD COLUMN entrc_point_x_temp DECIMAL(17,7),
    ADD COLUMN entrc_point_y_temp DECIMAL(17,7);

-- 기존 데이터를 DECIMAL 타입으로 변환하여 임시 컬럼에 저장
UPDATE road_name_address_entrances
SET entrc_point_x_temp = CAST(NULLIF(entrc_point_x, '') AS DECIMAL(17,7)),
    entrc_point_y_temp = CAST(NULLIF(entrc_point_y, '') AS DECIMAL(17,7));

-- 기존 컬럼 삭제
ALTER TABLE road_name_address_entrances
    DROP COLUMN entrc_point_x,
    DROP COLUMN entrc_point_y;

-- 임시 컬럼을 원래 이름으로 변경 (각각 실행)
ALTER TABLE road_name_address_entrances
    RENAME COLUMN entrc_point_x_temp TO entrc_point_x;
ALTER TABLE road_name_address_entrances
    RENAME COLUMN entrc_point_y_temp TO entrc_point_y; 