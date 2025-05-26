package com.example.youtubesearch.service;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.youtubesearch.client.YouTubeClient;
import com.example.youtubesearch.dto.SearchResultDto;
import com.example.youtubesearch.service.impl.YouTubeSearchServiceImpl;
import com.example.youtubesearch.vo.YouTubeSearchResponse;
import com.example.youtubesearch.vo.YouTubeVideoDetailsResponse;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class YouTubeSearchServiceTest {

    @Mock
    private YouTubeClient youTubeClient;

    @InjectMocks
    private YouTubeSearchServiceImpl youTubeSearchService;

    @Test
    @DisplayName("키워드로 검색 시 결과를 정상적으로 반환한다")
    void searchVideosSuccess() {
        // given
        String keyword = "test";
        String videoId = "test-video-id";
        String publishedAt = "2024-01-01T00:00:00Z";
        String title = "Test Video";
        String description = "Test Description";
        String viewCount = "1000";
        
        // 검색 응답 설정
        YouTubeSearchResponse searchResponse = new YouTubeSearchResponse();
        YouTubeSearchResponse.Item searchItem = new YouTubeSearchResponse.Item();
        YouTubeSearchResponse.Id itemId = new YouTubeSearchResponse.Id();
        itemId.setVideoId(videoId);
        searchItem.setId(itemId);
        
        YouTubeSearchResponse.Snippet snippet = new YouTubeSearchResponse.Snippet();
        snippet.setTitle(title);
        snippet.setDescription(description);
        YouTubeSearchResponse.Thumbnails thumbnails = new YouTubeSearchResponse.Thumbnails();
        YouTubeSearchResponse.Thumbnail thumbnail = new YouTubeSearchResponse.Thumbnail();
        thumbnail.setUrl("http://example.com/thumbnail.jpg");
        thumbnails.setDefaultThumbnail(thumbnail);
        snippet.setThumbnails(thumbnails);
        searchItem.setSnippet(snippet);
        
        searchResponse.setItems(Collections.singletonList(searchItem));

        // 비디오 상세 정보 응답 설정
        YouTubeVideoDetailsResponse detailsResponse = new YouTubeVideoDetailsResponse();
        YouTubeVideoDetailsResponse.Item videoItem = new YouTubeVideoDetailsResponse.Item();
        videoItem.setId(videoId);
        
        YouTubeVideoDetailsResponse.Snippet videoSnippet = new YouTubeVideoDetailsResponse.Snippet();
        videoSnippet.setPublishedAt(publishedAt);
        videoItem.setSnippet(videoSnippet);
        
        YouTubeVideoDetailsResponse.Statistics statistics = new YouTubeVideoDetailsResponse.Statistics();
        statistics.setViewCount(viewCount);
        videoItem.setStatistics(statistics);
        
        detailsResponse.setItems(Collections.singletonList(videoItem));

        given(youTubeClient.searchVideos(keyword))
                .willReturn(Mono.just(searchResponse));
        given(youTubeClient.getVideoDetails(anyString()))
                .willReturn(Mono.just(detailsResponse));

        // when
        Mono<List<SearchResultDto>> result = youTubeSearchService.searchVideos(keyword);

        // then
        StepVerifier.create(result)
                .expectNextMatches(results -> {
                    if (results.isEmpty()) return false;
                    SearchResultDto dto = results.get(0);
                    return dto.getVideoId().equals(videoId) &&
                           dto.getTitle().equals(title) &&
                           dto.getDescription().equals(description) &&
                           dto.getViewCount() == Long.parseLong(viewCount) &&
                           dto.getUploadTime().equals(publishedAt) &&
                           dto.getThumbnailUrl().equals("http://example.com/thumbnail.jpg") &&
                           dto.getVideoUrl().equals("https://www.youtube.com/watch?v=" + videoId);
                })
                .verifyComplete();

        verify(youTubeClient).searchVideos(keyword);
        verify(youTubeClient).getVideoDetails(videoId);
    }

    @Test
    @DisplayName("YouTube API 에러 발생 시 에러를 전파한다")
    void searchVideosError() {
        // given
        String keyword = "test";
        RuntimeException apiError = new RuntimeException("API Error");
        given(youTubeClient.searchVideos(keyword))
                .willReturn(Mono.error(apiError));

        // when
        Mono<List<SearchResultDto>> result = youTubeSearchService.searchVideos(keyword);

        // then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable.getMessage().equals("API Error"))
                .verify();

        verify(youTubeClient).searchVideos(keyword);
    }
} 