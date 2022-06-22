package com.assoni.gateway.repositories.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Optional;

@Builder
@Getter
@Setter
public class RepositorySearchRequestDTO {
    private Optional<String> language;
    private Optional<LocalDate> createdOn;
    private Optional<Integer> size;
}
