package com.assoni.gateway.repositories.unit;

import com.assoni.gateway.repositories.logic.api.GitHubClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class GitHubClientUnitTest {

    @Test
    void shouldBeAbleToCreateQueryWhenCreatedOnAndLanguageWereNotSent() {
        final StringBuilder builder = new GitHubClient(Mockito.mock(WebClient.class)).createFiltersForQuery(Optional.empty(), Optional.empty());

        assertThat(builder.toString()).isEqualTo("a");
    }

    @Test
    void shouldBeAbleToCreateQueryWhenOnlyTheCreatedOnWasSent() {
        final LocalDate date = LocalDate.now();
        final StringBuilder builder = new GitHubClient(Mockito.mock(WebClient.class)).createFiltersForQuery(Optional.of(date), Optional.empty());

        assertThat(builder.toString()).isEqualTo("created:>"+date.toString());
    }

    @Test
    void shouldBeAbleToCreateQueryWhenOnlyTheLanguageWasSent() {
        final StringBuilder builder = new GitHubClient(Mockito.mock(WebClient.class)).createFiltersForQuery(Optional.empty(), Optional.of("JAVA"));

        assertThat(builder.toString()).isEqualTo("language:JAVA");
    }

    @Test
    void shouldBeAbleToCreateQueryWhenBothLanguageAndCreatedOnWereSent() {
        final LocalDate date = LocalDate.now();
        final StringBuilder builder = new GitHubClient(Mockito.mock(WebClient.class)).createFiltersForQuery(Optional.of(date), Optional.of("JAVA"));

        assertThat(builder.toString()).isEqualTo("created:>"+date.toString()+"+language:JAVA");
    }
}
