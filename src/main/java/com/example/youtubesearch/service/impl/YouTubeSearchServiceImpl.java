package com.example.youtubesearch.service.impl;

import com.example.youtubesearch.client.YouTubeClient;
import com.example.youtubesearch.vo.YouTubeSearchResponse;
import com.example.youtubesearch.dto.SearchResultDto;
import com.example.youtubesearch.exception.YouTubeApiException;
import com.example.youtubesearch.service.YouTubeSearchService;
import com.example.youtubesearch.vo.YouTubeVideoDetailsResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class YouTubeSearchServiceImpl implements YouTubeSearchService {
    private final YouTubeClient youTubeClient;

    public YouTubeSearchServiceImpl(YouTubeClient youTubeClient) {
        this.youTubeClient = youTubeClient;
    }

    @Override
    public Mono<List<SearchResultDto>> searchVideos(String keyword) {
        return youTubeClient.searchVideos(keyword)
                .flatMapMany(resp -> {
                    if (resp.getItems() == null) {
                        return Flux.error(new YouTubeApiException("No items returned"));
                    }
                    return Flux.fromIterable(resp.getItems());
                })
                // 1) 기본 필드 매핑
                .map(item -> {
                    String vid = item.getId().getVideoId();
                    String title = item.getSnippet().getTitle();
                    String desc  = item.getSnippet().getDescription();
                    String thumb = Optional.ofNullable(item.getSnippet().getThumbnails())
                            .map(t -> t.getDefaultThumbnail().getUrl())
                            .orElse("");
                    String url   = "https://www.youtube.com/watch?v=" + vid;
                    return new SearchResultDto(vid, title, desc, thumb, url, null, null, null);
                })
                .collectList()
                .flatMap(list -> {
                    // 2) videoIds 콤마로 결합
                    String ids = list.stream()
                            .map(SearchResultDto::getVideoId)
                            .collect(Collectors.joining(","));
                    return youTubeClient.getVideoDetails(ids)
                            .map(details -> {
                                // id → Item 매핑
                                Map<String, YouTubeVideoDetailsResponse.Item> map = details.getItems().stream()
                                        .collect(Collectors.toMap(YouTubeVideoDetailsResponse.Item::getId, Function.identity()));
                                // DTO 업데이트
                                list.forEach(dto -> {
                                    YouTubeVideoDetailsResponse.Item info = map.get(dto.getVideoId());
                                    if (info != null) {
                                        dto.setViewCount(Long.valueOf(info.getStatistics().getViewCount()));
                                        dto.setUploadTime(info.getSnippet().getPublishedAt());
                                        dto.setTags(info.getSnippet().getTags());
                                    }
                                });
                                return list;
                            });
                });
    }
}