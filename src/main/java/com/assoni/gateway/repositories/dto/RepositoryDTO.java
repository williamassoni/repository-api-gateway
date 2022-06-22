package com.assoni.gateway.repositories.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RepositoryDTO {
    private String name;
    private String language;
    private String description;
    private String url;
    private String owner;

    private LocalDateTime createdAt;
    private Integer stars;
}
