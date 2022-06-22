package com.assoni.gateway.repositories.unit;

import com.assoni.gateway.repositories.controller.RepositoryController;
import com.assoni.gateway.repositories.dto.RepositoryDTO;
import com.assoni.gateway.repositories.dto.RepositorySearchRequestDTO;
import com.assoni.gateway.repositories.service.RepositoryService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RepositoryController.class)
public class RepositoryControllerUnitTest {
    static class RepositoryControllerUnitTestHelper {

        public static List<RepositoryDTO> createMockResponseForRepositories() {
            final RepositoryDTO springRepo = RepositoryDTO.builder().name("Spring").description("spring framework").language("JAVA").owner("pivotal").stars(600).url("https://github.com/spring-projects/spring-framework").createdAt(LocalDateTime.now()).build();
            final RepositoryDTO goLandRepo = RepositoryDTO.builder().name("GoLand").language("golang").owner("google").stars(600).url("https://github.com/google").createdAt(
                LocalDateTime.now().now()).build();

            return List.of(springRepo, goLandRepo);
        }

        public static RepositorySearchRequestDTO createMockAndReturnTheRequestObject(final RepositoryService repositoryService) {
            final RepositorySearchRequestDTO dto = RepositorySearchRequestDTO.builder().build();
            Mockito.when(repositoryService.findAllPublicRepositories(Mockito.any())).thenAnswer((Answer<List>) invocation -> {
                final RepositorySearchRequestDTO searchDto = invocation.getArgument(0, RepositorySearchRequestDTO.class);

                BeanUtils.copyProperties(searchDto, dto);
                return List.of(RepositoryDTO.builder().build());
            });

            return dto;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RepositoryService repositoryService;

    @Test
    @SneakyThrows
    void shouldBeAbleToReturnTheRepresentationOfTheJsonPerVersion1() {
        Mockito.when(repositoryService.findAllPublicRepositories(Mockito.any())).thenReturn(RepositoryControllerUnitTestHelper.createMockResponseForRepositories());

        this.mockMvc.perform(get("/v1/repositories"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.[0].name").value("Spring"))
                    .andExpect(jsonPath("$.[0].language").value("JAVA"))
                    .andExpect(jsonPath("$.[0].description").value("spring framework"))
                    .andExpect(jsonPath("$.[0].url").value("https://github.com/spring-projects/spring-framework"))
                    .andExpect(jsonPath("$.[0].owner").value("pivotal"))
                    .andExpect(jsonPath("$.[0].createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.[0].stars").value("600"));

    }

    @Test
    @SneakyThrows
    void shouldBeAcceptedTheSizeOfTheSearchAsQueryParam() {
        final RepositorySearchRequestDTO request = RepositoryControllerUnitTestHelper.createMockAndReturnTheRequestObject(repositoryService);
        this.mockMvc.perform(get("/v1/repositories?size=50"))
                    .andDo(print())
                    .andExpect(status().isOk());

        assertThat(request.getSize()).isPresent().get().isEqualTo(50);
    }

    @Test
    @SneakyThrows
    void shouldBeAbleToAcceptedTheDateOfCreationAsQueryParam() {
        final RepositorySearchRequestDTO request = RepositoryControllerUnitTestHelper.createMockAndReturnTheRequestObject(repositoryService);
        this.mockMvc.perform(get("/v1/repositories?createdOn=2019-01-10"))
            .andDo(print())
            .andExpect(status().isOk());

        assertThat(request.getCreatedOn()).isPresent().get().isEqualTo(LocalDate.of(2019,01,10));
    }

    @Test
    @SneakyThrows
    void shouldBeAbleToAcceptedTheLanguageAsQueryParam() {
        final RepositorySearchRequestDTO request = RepositoryControllerUnitTestHelper.createMockAndReturnTheRequestObject(repositoryService);
        this.mockMvc.perform(get("/v1/repositories?language=java"))
            .andDo(print())
            .andExpect(status().isOk());

        assertThat(request.getLanguage()).isPresent().get().isEqualTo("java");
    }
}
