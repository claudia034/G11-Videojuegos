package com.tournament;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tournament.application.format.FormatFactory;
import com.tournament.domain.enums.TournamentFormat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
class TournamentApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FormatFactory formatFactory;

    @Test
    void contextLoads() {
    }

    @Test
    void createsAndReadsTournamentThroughLayers() throws Exception {
        String requestBody = """
                {
                  "name": "G11 Cup",
                  "description": "G11 gaming tournament",
                  "gameName": "Valorant",
                  "format": "SINGLE_ELIMINATION",
                  "maxParticipants": 16,
                  "isTeamBased": true,
                  "minElo": 1000,
                  "maxElo": 2500,
                  "organizerId": 42,
                  "rounds": [
                    {
                      "roundNumber": 1,
                      "name": "First round"
                    }
                  ],
                  "prizes": [
                    {
                      "position": 1,
                      "name": "First place",
                      "prizeType": "CASH",
                      "amount": 100.00,
                      "currency": "USD"
                    }
                  ]
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("G11 Cup"))
                .andExpect(jsonPath("$.data.isTeamBased").value(true))
                .andExpect(jsonPath("$.data.minElo").value(1000))
                .andExpect(jsonPath("$.data.maxElo").value(2500))
                .andExpect(jsonPath("$.data.rounds[0].name").value("First round"))
                .andReturn();
    }

    @Test
    void publishesDraftTournament() throws Exception {
        String createBody = """
                {
                  "name": "Publish Cup",
                  "gameName": "Valorant",
                  "format": "SINGLE_ELIMINATION",
                  "maxParticipants": 8,
                  "registrationStartAt": "2099-01-01T10:00:00",
                  "registrationEndAt": "2099-01-05T10:00:00",
                  "startAt": "2099-01-06T10:00:00"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();

    }

    @Test
    void rejectsInvalidTournamentEloRange() throws Exception {
        String requestBody = """
                {
                  "name": "Invalid Elo Cup",
                  "gameName": "Valorant",
                  "format": "SINGLE_ELIMINATION",
                  "maxParticipants": 8,
                  "minElo": 2500,
                  "maxElo": 1000
                }
                """;

        mockMvc.perform(post("/api/v1/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void formatFactoryResolvesAllStrategies() {
        assertThat(formatFactory.getFormat(TournamentFormat.SINGLE_ELIMINATION)).isNotNull();
        assertThat(formatFactory.getFormat(TournamentFormat.DOUBLE_ELIMINATION)).isNotNull();
        assertThat(formatFactory.getFormat(TournamentFormat.ROUND_ROBIN)).isNotNull();
    }
}