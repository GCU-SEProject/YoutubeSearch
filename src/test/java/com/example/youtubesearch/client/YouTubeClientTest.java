package com.example.youtubesearch.client;

import com.example.youtubesearch.exception.YouTubeApiException;
import com.example.youtubesearch.vo.YouTubeSearchResponse;
import com.example.youtubesearch.vo.YouTubeVideoDetailsResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

class YouTubeClientTest {

    private MockWebServer mockWebServer;
    private YouTubeClient youTubeClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        youTubeClient = new YouTubeClient(webClient, "test-api-key", 10);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("검색 API 호출 시 정상적으로 결과를 반환한다")
    void searchVideosSuccess() {
        // given
        String successResponse = """
                {
                    "items": [
                        {
                            "id": {
                                "videoId": "test-video-id"
                            },
                            "snippet": {
                                "title": "Test Video",
                                "description": "Test Description"
                            }
                        }
                    ]
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(successResponse)
                .addHeader("Content-Type", "application/json"));

        // when
        var result = youTubeClient.searchVideos("test");

        // then
        StepVerifier.create(result)
                .expectNextMatches(response -> 
                    response.getItems() != null && 
                    !response.getItems().isEmpty() &&
                    response.getItems().get(0).getId().getVideoId().equals("test-video-id"))
                .verifyComplete();
    }

    @Test
    @DisplayName("검색 API 에러 발생 시 YouTubeApiException을 던진다")
    void searchVideosError() {
        // given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("Bad Request")
                .addHeader("Content-Type", "application/json"));

        // when
        var result = youTubeClient.searchVideos("test");

        // then
        StepVerifier.create(result)
                .expectError(YouTubeApiException.class)
                .verify();
    }

    @Test
    @DisplayName("비디오 상세 정보 API 호출 시 정상적으로 결과를 반환한다")
    void getVideoDetailsSuccess() {
        // given
        String successResponse = """
                {
                    "items": [
                        {
                            "id": "test-video-id",
                            "snippet": {
                                "title": "Test Video",
                                "description": "Test Description"
                            },
                            "statistics": {
                                "viewCount": "1000",
                                "likeCount": "100"
                            }
                        }
                    ]
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(successResponse)
                .addHeader("Content-Type", "application/json"));

        // when
        var result = youTubeClient.getVideoDetails("test-video-id");

        // then
        StepVerifier.create(result)
                .expectNextMatches(response -> 
                    response.getItems() != null && 
                    !response.getItems().isEmpty() &&
                    response.getItems().get(0).getId().equals("test-video-id"))
                .verifyComplete();
    }
} 