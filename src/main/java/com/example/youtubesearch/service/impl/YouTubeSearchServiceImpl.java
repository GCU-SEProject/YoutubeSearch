package com.example.youtubesearch.service.impl;

import com.example.youtubesearch.client.YouTubeClient;
import com.example.youtubesearch.vo.YouTubeSearchResponse;
import com.example.youtubesearch.dto.SearchResultDto;
import com.example.youtubesearch.exception.YouTubeApiException;
import com.example.youtubesearch.service.YouTubeSearchService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class YouTubeSearchServiceImpl implements YouTubeSearchService {
    private final YouTubeClient youTubeClient;

    public YouTubeSearchServiceImpl(YouTubeClient youTubeClient) {
        this.youTubeClient = youTubeClient;
    }

    @Override
    public Mono<List<SearchResultDto>> searchVideos(String keyword) {
        return youTubeClient.searchVideos(keyword)
                .flatMapMany(response -> {
                    if (response.getItems() == null) {
                        return Flux.error(new YouTubeApiException("No items returned from YouTube API"));
                    }
                    return Flux.fromIterable(response.getItems());
                })
                .map(item -> {
                    String videoId = (item.getId() != null) ? item.getId().getVideoId() : null;
                    String title   = (item.getSnippet() != null) ? item.getSnippet().getTitle() : "";
                    String desc    = (item.getSnippet() != null) ? item.getSnippet().getDescription() : "";
                    String thumbUrl = "";

                    if (item.getSnippet() != null && item.getSnippet().getThumbnails() != null) {
                        YouTubeSearchResponse.Thumbnail thumb =
                                item.getSnippet()
                                        .getThumbnails()
                                        .getDefaultThumbnail();  // may be null
                        if (thumb != null && thumb.getUrl() != null) {
                            thumbUrl = thumb.getUrl();
                        }
                    }

                    return new SearchResultDto(videoId, title, desc, thumbUrl);
                })
                .collectList();
    }
}