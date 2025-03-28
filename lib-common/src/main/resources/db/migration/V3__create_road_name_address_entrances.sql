CREATE TABLE IF NOT EXISTS road_name_address_entrances (
    address_management_no VARCHAR(255) NOT NULL,
    road_name_code VARCHAR(255) NOT NULL,
    emd_code VARCHAR(255) NOT NULL,
    building_main_no INTEGER NOT NULL,
    building_sub_no INTEGER NOT NULL,
    entrance_no VARCHAR(10) NOT NULL,
    entrance_type VARCHAR(2),
    entrance_category VARCHAR(2),
    longitude DOUBLE PRECISION,
    latitude DOUBLE PRECISION,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (address_management_no, road_name_code, emd_code, building_main_no, building_sub_no),
    FOREIGN KEY (address_management_no, road_name_code, emd_code, building_main_no, building_sub_no)
        REFERENCES road_name_addresses (address_management_no, road_name_code, emd_code, building_main_no, building_sub_no)
);

CREATE INDEX IF NOT EXISTS idx_road_name_address_entrances_address_management_no 
    ON road_name_address_entrances(address_management_no);
CREATE INDEX IF NOT EXISTS idx_road_name_address_entrances_entrance_type 
    ON road_name_address_entrances(entrance_type);
CREATE INDEX IF NOT EXISTS idx_road_name_address_entrances_entrance_category 
    ON road_name_address_entrances(entrance_category); 