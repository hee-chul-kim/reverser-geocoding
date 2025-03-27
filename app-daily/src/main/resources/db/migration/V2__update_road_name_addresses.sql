-- 기존 컬럼 삭제
ALTER TABLE road_name_addresses
    DROP COLUMN IF EXISTS dong_cd,
    DROP COLUMN IF EXISTS dong_nm,
    DROP COLUMN IF EXISTS floor_cd,
    DROP COLUMN IF EXISTS floor_nm,
    DROP COLUMN IF EXISTS ho_cd,
    DROP COLUMN IF EXISTS ho_nm;

-- 새로운 컬럼 추가
ALTER TABLE road_name_addresses
    ADD COLUMN IF NOT EXISTS und_yn VARCHAR(1),
    ADD COLUMN IF NOT EXISTS road_seq VARCHAR(3),
    ADD COLUMN IF NOT EXISTS road_id VARCHAR(12),
    ADD COLUMN IF NOT EXISTS adm_zone_cd VARCHAR(10),
    ADD COLUMN IF NOT EXISTS adm_zone_nm VARCHAR(40),
    ADD COLUMN IF NOT EXISTS buld_nm_chg_res VARCHAR(200),
    ADD COLUMN IF NOT EXISTS buld_nm_chg_hist VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS detail_buld_nm VARCHAR(200);

-- 컬럼 설명 업데이트
COMMENT ON COLUMN road_name_addresses.und_yn IS '지하여부';
COMMENT ON COLUMN road_name_addresses.road_seq IS '도로명일련번호';
COMMENT ON COLUMN road_name_addresses.road_id IS '도로명ID';
COMMENT ON COLUMN road_name_addresses.adm_zone_cd IS '행정구역코드';
COMMENT ON COLUMN road_name_addresses.adm_zone_nm IS '행정구역명';
COMMENT ON COLUMN road_name_addresses.buld_nm_chg_res IS '건물명변경사유';
COMMENT ON COLUMN road_name_addresses.buld_nm_chg_hist IS '건물명변경이력';
COMMENT ON COLUMN road_name_addresses.detail_buld_nm IS '상세건물명'; 