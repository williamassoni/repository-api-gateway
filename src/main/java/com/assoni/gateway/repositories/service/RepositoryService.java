package com.assoni.gateway.repositories.service;

import com.assoni.gateway.repositories.dto.RepositoryDTO;
import com.assoni.gateway.repositories.dto.RepositorySearchRequestDTO;

import java.util.List;

public interface RepositoryService {

    List<RepositoryDTO> findAllPublicRepositories(final RepositorySearchRequestDTO request);

}
