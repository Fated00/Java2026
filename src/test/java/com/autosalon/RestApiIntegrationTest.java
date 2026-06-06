package com.autosalon;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RestApiIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void swaggerUiIsAvailable() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
    }

    @Test
    void filtersAvailableCarsThroughRestApi() throws Exception {
        mockMvc.perform(get("/api/catalog/cars/available")
                        .param("brand", "BMW")
                        .param("model", "320i")
                        .param("color", "Black"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].modelName", is("BMW 320i")))
                .andExpect(jsonPath("$[0].available", is(true)));
    }

    @Test
    void buildsConfigurationThroughRestApi() throws Exception {
        String request = """
                {
                  "modelId": "20000000-0000-0000-0000-000000000001",
                  "selectedOptionIds": {
                    "WHEELS": "30000000-0000-0000-0000-000000000002",
                    "TRANSMISSION": "30000000-0000-0000-0000-000000000004",
                    "STEERING_WHEEL": "30000000-0000-0000-0000-000000000007",
                    "INTERIOR": "30000000-0000-0000-0000-000000000009"
                  }
                }
                """;

        mockMvc.perform(post("/api/configurator/configurations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelName", is("BMW 320i")))
                .andExpect(jsonPath("$.totalPrice", is(4230000.0)));
    }

    @Test
    void createsCustomOrderThroughRestApi() throws Exception {
        String request = """
                {
                  "clientId": "10000000-0000-0000-0000-000000000001",
                  "modelId": "20000000-0000-0000-0000-000000000001",
                  "selectedOptionIds": {
                    "WHEELS": "30000000-0000-0000-0000-000000000002",
                    "TRANSMISSION": "30000000-0000-0000-0000-000000000004",
                    "STEERING_WHEEL": "30000000-0000-0000-0000-000000000007",
                    "INTERIOR": "30000000-0000-0000-0000-000000000009"
                  }
                }
                """;

        mockMvc.perform(post("/api/orders/custom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("CREATED")))
                .andExpect(jsonPath("$.configuration.totalPrice", is(4230000.0)));
    }

    @Test
    void createsTestDriveRequestThroughRestApi() throws Exception {
        String request = """
                {
                  "clientId": "10000000-0000-0000-0000-000000000001",
                  "carId": "40000000-0000-0000-0000-000000000002",
                  "startsAt": "2030-06-07T12:00:00"
                }
                """;

        mockMvc.perform(post("/api/test-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.carId", is("40000000-0000-0000-0000-000000000002")));
    }
}
