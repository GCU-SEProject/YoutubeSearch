package com.example.youtubesearch.service;

import com.example.youtubesearch.dto.SearchResultDto;
import reactor.core.publisher.Mono;

import java.util.List;

public interface YouTubeSearchService {
    Mono<List<SearchResultDto>> searchVideos(String keyword);
}