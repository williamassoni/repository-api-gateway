package com.assoni.gateway.repositories.service;

import com.assoni.gateway.repositories.dto.RepositoryDTO;
import com.assoni.gateway.repositories.dto.RepositorySearchRequestDTO;
import com.assoni.gateway.repositories.logic.RepositoryProvider;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.annotation.Counted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class RepositoryServiceImpl implements RepositoryService {

    private final RepositoryProvider repositoryProvider;

    /**
     * Retrieves the most favorite repositories sorted by amount of starts
     * 
     * This uses a provider as main source of information in case of failure(service down and so on) fallback to {@link #findAllLocalRepositories(RepositorySearchRequestDTO, Exception)}
     *
     * @param request --> request with data to filter
     *
     * @return List<RepositoryDTO>
     * */
    @CircuitBreaker(name = "findAllPublicRepositoriesFallBack", fallbackMethod = "findAllLocalRepositories")
    public List<RepositoryDTO> findAllPublicRepositories(final RepositorySearchRequestDTO request) {
        return repositoryProvider.findAllPublicRepositories(request.getCreatedOn(), request.getLanguage(), request.getSize())
                                 .stream().map(model -> RepositoryDTO.builder().name(model.getName()).language(model.getLanguage()).owner(model.getOwner()).description(model.getDescription()).url(model.getUrl()).createdAt(model.getCreatedAt()).stars(model.getStars()).build())
                                 .collect(Collectors.toList());
    }

    /**
     * Fallback the search when there is an issue in the external api,
     *
     * TODO:
     *  Talk with the team and business what would be a nice fallback solution in case of provider return error(because is down or payment issue)
     * proposals:
     *  1. fetch the favorite repository from other source for example gitlab.
     *  2. keep a trace of the most 5000 common searches in a 1h cache and use this cache as fallback, so even if the api is down we can return the most favorite searches.
     *
     * TODO:
     *  Generate metric+alerts based on repository_fallback_metric
     * */
    @Counted(value = "repository_fallback_metric", extraTags = {"status", "failure"})
    public List<RepositoryDTO> findAllLocalRepositories(final RepositorySearchRequestDTO request, final Exception failureError) {
        log.error("Fail while retrieving the favorite repositories from provide, using fallback solution as alternative", failureError);

        return new ArrayList<>();
    }
}
