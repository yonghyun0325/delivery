package com.delivery.domain.menu.dto.response;

// 메뉴 횡단 검색 응답의 공통 타입 - MenuView와 별개로 storeName을 포함한다.
// 검색은 여러 가게에 걸친 결과라 어느 가게 메뉴인지 이름으로 보여줘야 하지만,
// 단건/가게별 목록 조회는 storeId가 URL에 이미 있어 storeName이 불필요해 분리했다.
public sealed interface MenuSearchView permits MenuSearchResponse, PublicMenuSearchResponse {}
