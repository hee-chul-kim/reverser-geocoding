-- 기존 NULL 값을 기본값으로 업데이트
UPDATE road_name_addresses 
SET address_management_no = COALESCE(address_management_no, uuid_generate_v4()::text)
WHERE address_management_no IS NULL;

UPDATE road_name_addresses 
SET road_name_code = COALESCE(road_name_code, '')
WHERE road_name_code IS NULL;

UPDATE road_name_addresses 
SET emd_code = COALESCE(emd_code, '')
WHERE emd_code IS NULL;

UPDATE road_name_addresses 
SET building_main_no = COALESCE(building_main_no, 0)
WHERE building_main_no IS NULL;

UPDATE road_name_addresses 
SET building_sub_no = COALESCE(building_sub_no, 0)
WHERE building_sub_no IS NULL;

-- NOT NULL 제약조건 추가
ALTER TABLE road_name_addresses
    ALTER COLUMN address_management_no SET NOT NULL,
    ALTER COLUMN road_name_code SET NOT NULL,
    ALTER COLUMN emd_code SET NOT NULL,
    ALTER COLUMN building_main_no SET NOT NULL,
    ALTER COLUMN building_sub_no SET NOT NULL; 