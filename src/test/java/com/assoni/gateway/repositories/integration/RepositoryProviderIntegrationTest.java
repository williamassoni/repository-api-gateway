package com.assoni.gateway.repositories.integration;

import com.assoni.gateway.repositories.logic.GitHubRepositoryProvider;
import com.assoni.gateway.repositories.logic.RepositoryProvider;
import com.assoni.gateway.repositories.logic.api.GitHubClient;
import com.assoni.gateway.repositories.logic.api.GitHubResponse;
import com.assoni.gateway.repositories.logic.model.RepositoryModel;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {RepositoryProvider.class, GitHubRepositoryProvider.class, RepositoryProviderIntegrationTest.LocalConfiguration.class})
public class RepositoryProviderIntegrationTest {

    @Configuration
    @EnableCaching
    @EnableAspectJAutoProxy
    public static class LocalConfiguration {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("repositories_cache");
        }
    }

    static class RepositoryProviderIntegrationTestHelper {

        public static GitHubResponse createFakeGitHubResponse() {
            final GitHubResponse.GitHubRepository spring = new GitHubResponse.GitHubRepository();
            spring.setCreated_at(LocalDateTime.now());
            spring.setLanguage("java");
            spring.setName("Spring");
            spring.setDescription("spring framework");
            spring.setStargazers_count(5);
            spring.setUrl("NOP");
            spring.setOwner(new GitHubResponse.Owner());
            spring.getOwner().setLogin("pivotal");

            GitHubResponse fakeResponse = new GitHubResponse();
            fakeResponse.setItems(List.of(spring));

            return fakeResponse;
        }

        public static GitHubResponse createFakeEmptyGitHubResponse() {
            GitHubResponse fakeResponse = new GitHubResponse();
            fakeResponse.setItems(List.of());

            return fakeResponse;
        }
    }

    @MockBean
    private GitHubClient gitHubClient;

    @Autowired
    private RepositoryProvider repositoryProvider;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldBeAbleToMapFieldsFromTheClientToTheModel() {
        Mockito.when(gitHubClient.findAllPublicRepositories(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(RepositoryProviderIntegrationTestHelper.createFakeGitHubResponse());

        final List<RepositoryModel> result = repositoryProvider.findAllPublicRepositories(Optional.of(LocalDate.now()), Optional.of(UUID.randomUUID().toString()), Optional.empty());
        assertThat(result).isNotEmpty();
        assertThat(result.get(0)).satisfies(con -> {
            assertThat(con.getOwner()).isEqualTo("pivotal");
            assertThat(con.getName()).isEqualTo("Spring");
            assertThat(con.getStars()).isEqualTo(5L);

            assertThat(con.getCreatedAt()).isNotNull();
            assertThat(con.getLanguage()).isEqualTo("java");
            assertThat(con.getDescription()).isEqualTo("spring framework");
            assertThat(con.getUrl()).isEqualTo("NOP");
        });
    }

    @Test
    void shouldNotFailWhenTheResponseFromTheServerIsEmpty() {
        Mockito.when(gitHubClient.findAllPublicRepositories(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(RepositoryProviderIntegrationTestHelper.createFakeEmptyGitHubResponse());

        final List<RepositoryModel> result = repositoryProvider.findAllPublicRepositories(Optional.of(LocalDate.now()), Optional.empty(), Optional.empty());
        assertThat(result).isEmpty();
    }

    @Test
    void shouldBeAbleToCacheServiceResponseWhenTheDateIsBeingSent() {
        Mockito.when(gitHubClient.findAllPublicRepositories(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(RepositoryProviderIntegrationTestHelper.createFakeGitHubResponse());

        final LocalDate executionTime = LocalDate.now();
        repositoryProvider.findAllPublicRepositories(Optional.of(executionTime), Optional.empty(), Optional.empty());

        final Cache currentCache = cacheManager.getCache("repositories_cache");
        assertThat(currentCache.evictIfPresent(List.of(Optional.of(executionTime), Optional.empty(), Optional.empty()))).isTrue();
    }

    @Test
    void shouldBeAbleToCacheServiceResponseWhenTheLanguageIsBeingSent() {
        Mockito.when(gitHubClient.findAllPublicRepositories(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(RepositoryProviderIntegrationTestHelper.createFakeGitHubResponse());

        repositoryProvider.findAllPublicRepositories(Optional.empty(), Optional.of("java"), Optional.empty());

        final Cache currentCache = cacheManager.getCache("repositories_cache");
        assertThat(currentCache.evictIfPresent(List.of(Optional.empty(), Optional.of("java"), Optional.empty()))).isTrue();
    }

    @Test
    void shouldBeAbleToCacheServiceResponseWhenTheSizeIsBeingSent() {
        Mockito.when(gitHubClient.findAllPublicRepositories(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(RepositoryProviderIntegrationTestHelper.createFakeGitHubResponse());

        repositoryProvider.findAllPublicRepositories(Optional.empty(), Optional.empty(), Optional.of(500));

        final Cache currentCache = cacheManager.getCache("repositories_cache");
        assertThat(currentCache.evictIfPresent(List.of(Optional.empty(), Optional.empty(), Optional.of(500)))).isTrue();
    }
}
