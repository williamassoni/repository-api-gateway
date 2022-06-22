package com.assoni.gateway.repositories.logic;

import com.assoni.gateway.repositories.logic.model.RepositoryModel;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RepositoryProvider {

    List<RepositoryModel> findAllPublicRepositories(final Optional<LocalDate> createdOn, final Optional<String> language, final Optional<Integer> size);

}
