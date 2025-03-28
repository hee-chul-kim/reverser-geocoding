CREATE TABLE IF NOT EXISTS road_name_addresses (
    address_management_no VARCHAR(255) NOT NULL,
    road_name_code VARCHAR(255) NOT NULL,
    emd_code VARCHAR(255) NOT NULL,
    building_main_no INTEGER NOT NULL,
    building_sub_no INTEGER NOT NULL,
    is_basement BOOLEAN DEFAULT false,
    underground_floor INTEGER DEFAULT 0,
    building_name VARCHAR(255),
    detail_building_name VARCHAR(255),
    sido_name VARCHAR(255),
    sigungu_name VARCHAR(255),
    emd_name VARCHAR(255),
    road_name VARCHAR(255),
    zip_code VARCHAR(255),
    building_year INTEGER,
    building_use_name VARCHAR(255),
    ground_floor INTEGER DEFAULT 0,
    apartment_name VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE,
    version INTEGER DEFAULT 0,
    PRIMARY KEY (address_management_no, road_name_code, emd_code, building_main_no, building_sub_no)
);

CREATE INDEX IF NOT EXISTS idx_road_name_addresses_address_management_no ON road_name_addresses(address_management_no);
CREATE INDEX IF NOT EXISTS idx_road_name_addresses_emd_code ON road_name_addresses(emd_code);
CREATE INDEX IF NOT EXISTS idx_road_name_addresses_road_name_code ON road_name_addresses(road_name_code);
CREATE INDEX IF NOT EXISTS idx_road_name_addresses_building_main_no ON road_name_addresses(building_main_no);
CREATE INDEX IF NOT EXISTS idx_road_name_addresses_building_sub_no ON road_name_addresses(building_sub_no); 