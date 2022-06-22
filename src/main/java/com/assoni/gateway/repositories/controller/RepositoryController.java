package com.assoni.gateway.repositories.controller;

import com.assoni.gateway.repositories.dto.RepositoryDTO;
import com.assoni.gateway.repositories.dto.RepositorySearchRequestDTO;
import com.assoni.gateway.repositories.service.RepositoryService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1")
@AllArgsConstructor
public class RepositoryController {
    private final RepositoryService repositoryService;

    /**
     * Retrieve all repositories based on size/createOn and sort them by stars
     *
     * @param  size      --> size of the list to be fetched default 50
     * @param  language  --> language to be used in the filter
     * @param  createdOn --> time frame for filter
     *
     * @return List<RepositoryDTO>
     * */
    @GetMapping("/repositories")
    public List<RepositoryDTO> findAllOrderByStars(@RequestParam(name = "size", defaultValue = "50") final Optional<Integer> size,
                                                   @RequestParam(name = "language") final Optional<String> language,
                                                   @RequestParam(name = "createdOn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final Optional<LocalDate> createdOn) {
        return repositoryService.findAllPublicRepositories(RepositorySearchRequestDTO.builder().language(language).createdOn(createdOn).size(size).build());
    }
}
