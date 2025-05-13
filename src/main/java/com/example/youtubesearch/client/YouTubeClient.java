package com.example.youtubesearch.client;

import com.example.youtubesearch.vo.YouTubeSearchResponse;
import com.example.youtubesearch.exception.YouTubeApiException;
import com.example.youtubesearch.vo.YouTubeVideoDetailsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class YouTubeClient {
    private final WebClient youtubeWebClient;
    private final String apiKey;
    private final int maxResults;

    public YouTubeClient(WebClient youtubeWebClient,
                         @Value("${youtube.api.key}") String apiKey,
                         @Value("${youtube.api.default-max-results:10}") int maxResults) {
        this.youtubeWebClient = youtubeWebClient;
        this.apiKey = apiKey;
        this.maxResults = maxResults;
    }

    public Mono<YouTubeSearchResponse> searchVideos(String keyword) {
        return youtubeWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("part", "snippet")
                        .queryParam("type", "video")
                        .queryParam("maxResults", maxResults)
                        .queryParam("q", keyword)
                        .queryParam("key", apiKey)
                        .build()
                )
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new YouTubeApiException("YouTube API error: " + errorBody))))
                .bodyToMono(YouTubeSearchResponse.class);
    }

    public Mono<YouTubeVideoDetailsResponse> getVideoDetails(String videoIds) {
        return youtubeWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/videos")
                        .queryParam("part", "snippet,statistics")
                        .queryParam("id", videoIds)
                        .queryParam("key", apiKey)
                        .build()
                )
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new YouTubeApiException("YouTube API error: " + body))))
                .bodyToMono(YouTubeVideoDetailsResponse.class);
    }
}