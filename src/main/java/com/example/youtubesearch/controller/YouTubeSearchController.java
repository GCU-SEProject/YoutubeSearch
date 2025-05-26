package com.example.youtubesearch.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.youtubesearch.dto.SearchResultDto;
import com.example.youtubesearch.service.YouTubeSearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/search/video") //127.0.0.1:8080/search/video?keyword=Battleground PUBG
@RequiredArgsConstructor
public class YouTubeSearchController {
    private final YouTubeSearchService youTubeSearchService;

    @GetMapping
    public Mono<ResponseEntity<List<SearchResultDto>>> search(
            @RequestParam(value = "keyword", required = false) String keyword) {
        if (keyword == null || keyword.isBlank()) {
            log.warn("Missing or empty keyword parameter");
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(List.of()));
        }

        return youTubeSearchService.searchVideos(keyword)
                .map(ResponseEntity::ok)
                .doOnError(e -> log.error("Error in searchVideos", e))
                .onErrorResume(e -> {
                    log.error("Returning 500 due to error", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }
}
