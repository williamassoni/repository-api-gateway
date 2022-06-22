package com.assoni.gateway.repositories.integration;

import com.assoni.gateway.repositories.dto.RepositoryDTO;
import com.assoni.gateway.repositories.dto.RepositorySearchRequestDTO;
import com.assoni.gateway.repositories.logic.RepositoryProvider;
import com.assoni.gateway.repositories.logic.model.RepositoryModel;
import com.assoni.gateway.repositories.service.RepositoryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RepositoryServiceIntegrationTest {

    static class RepositoryServiceIntegrationTestHelper {

        public static List<RepositoryModel> createMockList() {
            final RepositoryModel model = new RepositoryModel();

            model.setName("gateway-api");
            model.setLanguage("java");
            model.setDescription("something nice");
            model.setUrl("NOP");
            model.setOwner("William");
            model.setCreatedAt(LocalDateTime.of(1991,12,01,15,00,00));
            model.setStars(5000);

            return List.of(model);
        }
    }

    @Autowired
    private RepositoryService service;

    @MockBean
    private RepositoryProvider repositoryProvider;

    @Test
    void shouldBeAbleToReturnListOfRepositoriesDTOInCaseOfSuccess() {
        Mockito.when(repositoryProvider.findAllPublicRepositories(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(RepositoryServiceIntegrationTestHelper.createMockList());

        final List<RepositoryDTO> dtos = service.findAllPublicRepositories(RepositorySearchRequestDTO.builder().build());
        assertThat(dtos).isNotEmpty();
        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0)).satisfies(model -> {
            assertThat(model.getName()).isEqualTo("gateway-api");
            assertThat(model.getLanguage()).isEqualTo("java");
            assertThat(model.getDescription()).isEqualTo("something nice");
            assertThat(model.getUrl()).isEqualTo("NOP");
            assertThat(model.getOwner()).isEqualTo("William");
            assertThat(model.getCreatedAt()).isEqualTo(LocalDateTime.of(1991,12,01,15,00,00));
            assertThat(model.getStars()).isEqualTo(5000);
        });
    }

    @Test
    void shouldBeAbleToFallBackToAnEmptyListInCaseOfError() {
        Mockito.when(repositoryProvider.findAllPublicRepositories(Mockito.any(), Mockito.any(), Mockito.any())).thenThrow(new RuntimeException("This exploded"));

        final List<RepositoryDTO> dtos = service.findAllPublicRepositories(RepositorySearchRequestDTO.builder().build());
        assertThat(dtos).isEmpty();
    }
}
