package com.assoni.gateway.repositories.logic.api;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * This class represents the client of github, it was created using github docs, the main responsibility of this class is behave as bridge
 * for consumption and deserialization of github responses.
 *
 * Docs about the api:
 *  https://docs.github.com/en/search-github/searching-on-github/searching-for-repositories
 *
 * */
@Component
@AllArgsConstructor
public class GitHubClient {

    @Configuration
    public static class ClientConfiguration {

        @Bean
        public WebClient gitHubWebClient(@Value("${client.github.baseUrl}") final String baseUrl,
                                         @Value("${client.github.timeout}") final Integer timeout) {
            HttpClient httpClient = HttpClient.create()
                                              .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
                                              .responseTimeout(Duration.ofMillis(timeout))
                                              .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(timeout, TimeUnit.MILLISECONDS)).addHandlerLast(new WriteTimeoutHandler(timeout, TimeUnit.MILLISECONDS)));

            final int size = 16 * 1024 * 1024;
            final ExchangeStrategies strategies = ExchangeStrategies.builder().codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size)).build();

            return WebClient.builder().exchangeStrategies(strategies).clientConnector(new ReactorClientHttpConnector(httpClient)).baseUrl(baseUrl).build();
        }
    }

    private final WebClient webClient;

    /**
     * Build up the query following the concepts of github search(https://docs.github.com/en/search-github/getting-started-with-searching-on-github/understanding-the-search-syntax)
     *
     * The main intention is to consume the search-api from github and deserialize the response in a shape where can be use by other layers of the application.
     *
     * @param createdOn --> date to filter starting the search from
     * @param language  --> language such as JAVA/kotlin and so on
     * @param  size     --> amount of elements that you would like to see as return
     *
     * @return GitHubResponse
     * */
    public GitHubResponse findAllPublicRepositories(final Optional<LocalDate> createdOn, final Optional<String> language, final Optional<Integer> size) {
        final StringBuilder filter = createFiltersForQuery(createdOn, language);

        return webClient.get()
                        .uri(uriBuilder -> uriBuilder.path("repositories")
                                                     .queryParam("q", filter)
                                                     .queryParam("sort","stars")
                                                     .queryParam("order","desc")
                                                     .queryParam("per_page",size)
                                                     .build()
                        )
                        .retrieve()
                        .bodyToMono(GitHubResponse.class)
                        .block();
    }

    /**
     * Creates the query in a shape that github expects.
     *
     * @param createdOn --> date to filter starting the search from
     * @param language  --> language such as JAVA/kotlin and so on
     * */
    public StringBuilder createFiltersForQuery(final Optional<LocalDate> createdOn, final Optional<String> language) {
        final StringBuilder query = new StringBuilder();

        //fallback which tells github to return by alphabetical order
        if(createdOn.isEmpty() && language.isEmpty()) {
            query.append("a");
            return query;
        }

        createdOn.ifPresent(data -> query.append("created:>").append(createdOn.get()));
        language.ifPresent(data -> {
            if(createdOn.isPresent()) {
                query.append("+language:").append(language.get());
            }else{
                query.append("language:").append(language.get());
            }
        } );

        return query;
    }
}
