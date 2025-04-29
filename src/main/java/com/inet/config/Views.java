package com.inet.config;

/**
 * JSON 직렬화를 위한 View 정의
 */
public class Views {
    // 기본 정보만 포함하는 뷰
    public interface Summary {}
    
    // Summary를 상속하며 상세 정보를 포함하는 뷰
    public interface Detail extends Summary {}
} 