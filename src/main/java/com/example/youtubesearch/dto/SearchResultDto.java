package com.example.youtubesearch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResultDto {
    private String videoId;         // 영상 ID
    private String title;           // 영상 제목
    private String description;     // 영상 설명
    private String thumbnailUrl;    // 썸네일 사진
    private String videoUrl;        // 영상 링크
    private Long viewCount;         // 조회수
    private String uploadTime;      // 업로드 시간 (ISO 8601)
    private List<String> tags;      // 태그 목록
}