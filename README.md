# MySimpleNavi

---

# 안드로이드 길 안내 앱

기간 2023.03.08 ~

---

- 목적지를 검색해서 길 안내
- 지도에서 목적지를 찾아 길 안내

---

- Android Gradle Plugin Version 7.3.1
- Gradle Version 7.4
- Hilt Version 2.42
- Kotlin Version 1.7.0 

---


# 1. API

## 길 안내 API


BASE URL : https://routing.openstreetmap.de

별도 기한 및 키가 존재 하지 않는다.

Retrofit2 방식 사용




## 검색 API


BASE URL : https://dapi.kakao.com

AppKey 필수

Retrofit2 방식 사용



---

# 2. 라이브러리

## 지도 SDK

카카오 지도 SDK

참조 https://apis.map.kakao.com/android/
