package com.assoni.gateway.repositories.logic.api;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class GitHubResponse {

    @Data
    public static class GitHubRepository {
        private String name;
        private String language;
        private String description;
        private String url;

        private Owner owner;
        private LocalDateTime created_at;
        private Integer stargazers_count;
    }

    @Data
    public static class Owner {
        private String login;
    }

    private String total_count;
    private List<GitHubRepository> items;
}
