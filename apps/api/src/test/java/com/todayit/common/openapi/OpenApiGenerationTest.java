package com.todayit.common.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@ActiveProfiles("test")
@SpringBootTest(properties = "OPENAPI_ENABLED=true")
@AutoConfigureMockMvc
class OpenApiGenerationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("OpenAPI 명세 JSON을 생성하여 스키마를 확인한다")
  void generatesOpenApiSpec() throws Exception {
    MvcResult result = mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
    String content = result.getResponse().getContentAsString();
    Files.writeString(Path.of("build/openapi.json"), content);

    assertThat(content).contains("CommonResponseCreateCourseResponse");
    assertThat(content).contains("CommonResponseCourseDetailResponse");
    assertThat(content).contains("{\"bearerAuth\":[]},{}");
  }
}
