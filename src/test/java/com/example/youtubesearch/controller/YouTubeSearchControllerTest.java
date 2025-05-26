package com.example.youtubesearch.controller;

import com.example.youtubesearch.dto.SearchResultDto;
import com.example.youtubesearch.service.YouTubeSearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@WebFluxTest(YouTubeSearchController.class)
class YouTubeSearchControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private YouTubeSearchService youTubeSearchService;

    @Test
    @DisplayName("키워드로 검색 시 정상적으로 결과를 반환한다")
    void searchWithValidKeyword() {
        // given
        String keyword = "test";
        SearchResultDto mockResult = new SearchResultDto();
        mockResult.setTitle("Test Video");
        mockResult.setDescription("Test Description");
        
        List<SearchResultDto> mockResults = Arrays.asList(mockResult);
        given(youTubeSearchService.searchVideos(keyword))
                .willReturn(Mono.just(mockResults));

        // when & then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/video")
                        .queryParam("keyword", keyword)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SearchResultDto.class)
                .hasSize(1)
                .contains(mockResult);

        verify(youTubeSearchService).searchVideos(keyword);
    }

    @Test
    @DisplayName("키워드가 비어있을 경우 400 에러를 반환한다")
    void searchWithEmptyKeyword() {
        // when & then
        webTestClient.get()
                .uri("/search/video?keyword=")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("키워드 파라미터가 없을 경우 400 에러를 반환한다")
    void searchWithoutKeyword() {
        // when & then
        webTestClient.get()
                .uri("/search/video")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("서비스에서 에러 발생 시 500 에러를 반환한다")
    void searchWithServiceError() {
        // given
        String keyword = "test";
        given(youTubeSearchService.searchVideos(anyString()))
                .willReturn(Mono.error(new RuntimeException("Service Error")));

        // when & then
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/video")
                        .queryParam("keyword", keyword)
                        .build())
                .exchange()
                .expectStatus().isEqualTo(500);
    }
} 