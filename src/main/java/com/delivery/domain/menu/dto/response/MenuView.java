package com.delivery.domain.menu.dto.response;

// 메뉴 조회 응답의 공통 타입 - 조회자 권한에 따라 MenuResponse(소유자/관리자, 전체 필드)
// 또는 PublicMenuResponse(손님, hidden/updatedAt 등 운영 메타데이터 제외)로 갈린다.
public sealed interface MenuView permits MenuResponse, PublicMenuResponse {}
