package com.example.youtubesearch.vo;

import lombok.Data;

import java.util.List;

@Data
public class YouTubeVideoDetailsResponse {
    private List<Item> items;

    @Data
    public static class Item {
        private String id;
        private Snippet snippet;
        private Statistics statistics;
    }

    @Data
    public static class Snippet {
        private String publishedAt;
        private List<String> tags;
    }

    @Data
    public static class Statistics {
        private String viewCount;
    }
}