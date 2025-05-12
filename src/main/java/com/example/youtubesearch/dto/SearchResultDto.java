package com.example.youtubesearch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResultDto {
    private String videoId;
    private String title;
    private String description;
    private String thumbnailUrl;
}