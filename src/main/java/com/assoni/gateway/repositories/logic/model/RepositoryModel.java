package com.assoni.gateway.repositories.logic.model;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RepositoryModel {
    private String name;
    private String language;
    private String description;
    private String url;
    private String owner;

    private LocalDateTime createdAt;
    private Integer stars;
}
