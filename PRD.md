# Reverse Geocoding API PRD (Product Requirements Document)

## 1. 개요

### 1.1 목적

위도/경도 좌표를 주소 정보로 변환하는 Reverse Geocoding API를 개발하여 위치 기반 서비스의 주소 정보 제공 기능을 구현합니다.

### 1.2 대상 사용자

- 위치 기반 서비스를 개발하는 개발자
- 지도 기반 애플리케이션 개발자
- 위치 데이터를 주소로 변환해야 하는 기업/조직

## 2. 기능 요구사항

### 2.1 핵심 기능

1. 좌표 → 주소 변환

   - 위도/경도 좌표 입력
   - 상세 주소 정보 반환
   - 행정구역 정보 제공

2. 주소 상세도 레벨
   - 국가
   - 시/도
   - 구/군
   - 동/읍/면
   - 도로명/지번
   - 건물번호

### 2.2 API 스펙

```
GET /api/v1/reverse-geocode
```

#### 요청 파라미터

- `lat`: 위도 (필수)
- `lon`: 경도 (필수)
- `coordType`: 좌표 타입 (선택, EPSG3857 | WGS84GEO | KATECH | BESSELGEO | BESSELTM | GRS80GEO | GRS80TM, 기본값: WGS84GEO)

#### 응답 형식

```json
{
  "addressInfo": {
    "fullAddress": "서울시 마포구 상수동",
    "city_do": "서울시",
    "gu_gun": "마포구",
    "ri": "죽전리",
    "roadName": "강남대로",
    "buildingIndex": "11-11",
    "buildingName": "영진그린빌라",
    "mappingDistance": 0,
    "roadCode": "261104175064",
    "bunji": "121-12"
  },
  "roadCoord": {
    "lat": "37.56631111",
    "lon": "126.90161389"
  }
}
```

## 3. 비기능 요구사항

### 3.1 성능

- 응답 시간: 95% 이상의 요청이 200ms 이내 처리
- 동시 처리: 초당 1000건 이상 처리 가능
- 가용성: 99.9% 이상

### 3.2 보안 (일단 신경쓰지 않아도 됨)

- API 키 인증 필수
- HTTPS 통신
- Rate limiting 적용
- IP 기반 접근 제한

### 3.3 확장성

- 수평적 확장 가능한 아키텍처
- 캐싱 시스템 도입
- 부하 분산 지원

## 4. 기술 스택

### 4.1 백엔드

- 언어: Kotlin
- 프레임워크: Spring boot 3
- 데이터베이스: Elasticsearch + Postgresql
- 캐시: Caffein

### 4.2 인프라

- 컨테이너화: Docker
- 오케스트레이션: Kubernetes
- CI/CD: GitHub Actions
- 모니터링: Prometheus + Grafana
