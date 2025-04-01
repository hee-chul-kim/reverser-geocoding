# 작업 중단
## 과제 수행 목적과 달라서 작업 중단. 현재 cursor 로 초기 작업 후 수정 작업 거의 하지 않은 상태.
- 구현된 기능
  - 월 데이터로 초기화
  - 좌표로 주소 검색
- 미구현 기능
  - 일 데이터로 업데이트
  - 주소로 좌표 검색
- 실행하기 위해 주소 데이터(도로명주소 한글, 도로명주소 출입구 정보), postgresql, elasticsearch 등 필요 (docker-compose.yml 참고)
- cursor chat history 는 history 폴더 참고
  - 작업 시작 전 cursor rule, cursorignore, doc 파일 생성하였음

---

# 역지오코딩 데이터 파이프라인

이 프로젝트는 CSV 형식의 주소 데이터를 관계형 데이터베이스 테이블로 변환하고, 최종적으로 Elasticsearch에 적재하는 데이터 파이프라인을 구현합니다.

## 데이터 처리 프로세스

### Step 1: 기존 데이터 삭제
- 도로명주소 출입구 테이블 데이터 삭제
- 도로명주소 테이블 데이터 삭제

### Step 2: 새로운 데이터 처리
1. **도로명주소 데이터 처리**
   - CSV 파일에서 도로명주소 데이터 읽기
   - 데이터 유효성 검증
   - 변경 사유에 따른 처리:
     - 신규(31) 또는 변경(34): 데이터 생성/업데이트
     - 폐지(63): 논리적 삭제 처리

2. **출입구 좌표 데이터 처리**
   - CSV 파일에서 출입구 좌표 데이터 읽기
   - 좌표계 변환 처리
   - 출입구 정보 생성 및 업데이트

### Step 3: Elasticsearch 인덱스 생성
- 타임스탬프 기반의 새로운 인덱스 생성
- 인덱스 매핑 및 설정 적용

### Step 4: Elasticsearch 데이터 적재
- RDB 데이터를 Elasticsearch 문서 형태로 변환
- 벌크 인덱싱을 통한 대량 데이터 적재
- 처리 상태 모니터링

### Step 5: Elasticsearch 별칭 설정
- 새로운 인덱스에 별칭 설정
- 무중단 인덱스 전환

## 데이터 구조

### CSV 파일 구조

1. **도로명주소 데이터 (도로명주소.txt)**
   ```
   도로명주소관리번호|법정동코드|시도명|시군구명|법정읍면동명|법정리명|산여부|지번본번|지번부번|
   도로명코드|도로명|지하여부|건물본번|건물부번|행정동코드|행정동명|우편번호|이전도로명주소|
   효력발생일|공동주택구분|변경사유코드|건물명|시군구용건물명|비고
   ```

2. **출입구 좌표 데이터 (출입구좌표.txt)**
   ```
   도로명주소관리번호|법정동코드|시도명|시군구명|법정읍면동명|법정리명|도로명코드|도로명|
   지하여부|건물본번|건물부번|우편번호|효력발생일|변경사유코드|출입구일련번호|출입구구분|
   출입구유형|출입구좌표X|출입구좌표Y
   ```

### RDB 테이블 구조

1. **도로명주소 테이블 (road_name_addresses)**
   - 복합 기본키: (address_management_no, road_name_code, is_basement, building_main_no, building_sub_no)
   - 주소 관련 정보 (시도, 시군구, 읍면동, 도로명 등)
   - 건물 관련 정보 (건물명, 건물번호 등)
   - 이력 관리 (생성일시, 수정일시, 삭제일시, 버전)

2. **출입구 정보 테이블 (road_name_address_entrances)**
   - 복합 기본키: (address_management_no, road_name_code, is_basement, building_main_no, building_sub_no)
   - 출입구 정보 (출입구번호, 구분, 유형)
   - 좌표 정보 (위도, 경도)
   - 이력 관리 (생성일시, 수정일시)

### Elasticsearch 인덱스 구조

**address_geo 인덱스**
```json
{
  "mappings": {
    "properties": {
      "fullAddress": {
        "type": "text",
        "fields": {
          "keyword": {
            "type": "keyword"
          }
        }
      },
      "location": {
        "type": "geo_point"
      },
      "timestamp": {
        "type": "date"
      }
    }
  }
}
```

## 실행 방법

프로그램 실행 시 연도와 월을 지정하여 데이터를 처리할 수 있습니다:

```bash
# 기본값 사용 (2025년 3월)
java -jar app.jar

# 특정 연월 지정
java -jar app.jar --year=2024 --month=04
```

## 기술 스택

- Language: Kotlin
- Framework: Spring Boot
- Database: PostgreSQL
- Search Engine: Elasticsearch
- Build Tool: Gradle

## API 문서

API 문서는 Swagger UI를 통해 제공됩니다. 서버 실행 후 아래 URL에서 확인할 수 있습니다:
```
http://localhost:8080/swagger-ui.html
```