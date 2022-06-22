package com.assoni.gateway.repositories.logic;

import com.assoni.gateway.repositories.logic.api.GitHubClient;
import com.assoni.gateway.repositories.logic.api.GitHubResponse;
import com.assoni.gateway.repositories.logic.model.RepositoryModel;
import io.micrometer.core.annotation.Counted;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class GitHubRepositoryProvider implements RepositoryProvider {
    private final GitHubClient gitHubClient;

    @Cacheable(value = "repositories_cache", key = "{#createdOn, #language, #size}")
    @Override
    public List<RepositoryModel> findAllPublicRepositories(final Optional<LocalDate> createdOn, final Optional<String> language, final Optional<Integer> size) {
        return gitHubClient.findAllPublicRepositories(createdOn, language, size).getItems().stream().map(item -> mapModelBasedOnGitHubResponse(item)).collect(Collectors.toList());
    }

    private final RepositoryModel mapModelBasedOnGitHubResponse(final GitHubResponse.GitHubRepository response) {
        return RepositoryModel.builder()
                              .name(response.getName())
                              .description(response.getDescription())
                              .language(response.getLanguage())
                              .owner(response.getOwner().getLogin())
                              .stars(response.getStargazers_count())
                              .url(response.getUrl())
                              .createdAt(response.getCreated_at())
                              .build();
    }
}
