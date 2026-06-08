package com.tournament;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tournament.domain.entity.Tournament;
import com.tournament.domain.enums.TournamentFormat;
import com.tournament.factory.FormatFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
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
                  "name": "Copa G11",
                  "description": "Torneo de videojuegos del grupo G11",
                  "gameName": "Valorant",
                  "format": "SINGLE_ELIMINATION",
                  "maxParticipants": 16,
                  "rounds": [
                    {
                      "roundNumber": 1,
                      "name": "Primera ronda"
                    }
                  ],
                  "prizes": [
                    {
                      "position": 1,
                      "name": "Primer lugar",
                      "prizeType": "CASH",
                      "amount": 100.00,
                      "currency": "USD"
                    }
                  ]
                }
                """;

        MvcResult result = mockMvc.perform(post("/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("Copa G11"))
                .andExpect(jsonPath("$.rounds[0].name").value("Primera ronda"))
                .andExpect(jsonPath("$.prizes[0].name").value("Primer lugar"))
                .andReturn();

        String location = result.getResponse().getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Copa G11"))
                .andExpect(jsonPath("$.rounds[0].roundNumber").value(1))
                .andExpect(jsonPath("$.prizes[0].position").value(1));

        mockMvc.perform(get("/tournaments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    void generatesRoundsWithStrategyWhenRequestHasNoRounds() throws Exception {
        String requestBody = """
                {
                  "name": "Swiss Cup",
                  "description": "Generated rounds test",
                  "gameName": "Chess",
                  "format": "SWISS",
                  "maxParticipants": 16
                }
                """;

        mockMvc.perform(post("/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.format").value("SWISS"))
                .andExpect(jsonPath("$.rounds.length()").value(4))
                .andExpect(jsonPath("$.rounds[0].name").value("Swiss Round 1"))
                .andExpect(jsonPath("$.rounds[3].name").value("Swiss Round 4"));
    }

    @Test
    void updatesTournamentThroughCrudEndpoint() throws Exception {
        String createBody = """
                {
                  "name": "Update Cup",
                  "gameName": "Valorant",
                  "format": "SINGLE_ELIMINATION",
                  "maxParticipants": 8
                }
                """;

        MvcResult result = mockMvc.perform(post("/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        String updateBody = """
                {
                  "name": "Updated Cup",
                  "description": "Tournament updated with PUT",
                  "gameName": "Rocket League",
                  "format": "ROUND_ROBIN",
                  "status": "REGISTRATION_OPEN",
                  "maxParticipants": 4,
                  "prizes": [
                    {
                      "position": 1,
                      "name": "Champion",
                      "prizeType": "POINTS"
                    }
                  ]
                }
                """;

        mockMvc.perform(put(location)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Cup"))
                .andExpect(jsonPath("$.gameName").value("Rocket League"))
                .andExpect(jsonPath("$.format").value("ROUND_ROBIN"))
                .andExpect(jsonPath("$.status").value("REGISTRATION_OPEN"))
                .andExpect(jsonPath("$.rounds.length()").value(3))
                .andExpect(jsonPath("$.prizes[0].name").value("Champion"));
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

        MvcResult result = mockMvc.perform(post("/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();

        String location = result.getResponse().getHeader("Location");

        mockMvc.perform(post(location + "/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REGISTRATION_OPEN"));

        mockMvc.perform(post(location + "/publish"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("Only DRAFT tournaments can be published"));
    }

    @Test
    void rejectsPublishWhenRequiredDatesAreMissing() throws Exception {
        String createBody = """
                {
                  "name": "Missing Dates Cup",
                  "gameName": "Valorant",
                  "format": "SINGLE_ELIMINATION",
                  "maxParticipants": 8
                }
                """;

        MvcResult result = mockMvc.perform(post("/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        String location = result.getResponse().getHeader("Location");

        mockMvc.perform(post(location + "/publish"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]")
                        .value("registrationStartAt, registrationEndAt and startAt are required before publishing"));
    }

    @Test
    void generatesAndPersistsRoundsThroughEndpoint() throws Exception {
        String createBody = """
                {
                  "name": "Rounds Cup",
                  "gameName": "Rocket League",
                  "format": "ROUND_ROBIN",
                  "maxParticipants": 4,
                  "rounds": [
                    {
                      "roundNumber": 1,
                      "name": "Manual seed round"
                    }
                  ]
                }
                """;

        MvcResult result = mockMvc.perform(post("/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rounds.length()").value(1))
                .andReturn();

        String location = result.getResponse().getHeader("Location");

        mockMvc.perform(post(location + "/rounds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rounds.length()").value(3))
                .andExpect(jsonPath("$.rounds[0].name").value("Round Robin Round 1"))
                .andExpect(jsonPath("$.rounds[2].name").value("Round Robin Round 3"));

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rounds.length()").value(3))
                .andExpect(jsonPath("$.rounds[0].name").value("Round Robin Round 1"));
    }

    @Test
    void rejectsRoundGenerationWhenLimitsAreExceeded() throws Exception {
        String createBody = """
                {
                  "name": "Large Round Robin Cup",
                  "gameName": "Chess",
                  "format": "ROUND_ROBIN",
                  "maxParticipants": 65,
                  "rounds": [
                    {
                      "roundNumber": 1,
                      "name": "Manual seed round"
                    }
                  ]
                }
                """;

        MvcResult result = mockMvc.perform(post("/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        String location = result.getResponse().getHeader("Location");

        mockMvc.perform(post(location + "/rounds"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]")
                        .value("ROUND_ROBIN cannot generate rounds for more than 64 participants"));
    }

    @Test
    void rejectsInvalidTournamentDates() throws Exception {
        String requestBody = """
                {
                  "name": "Invalid Dates Cup",
                  "gameName": "Valorant",
                  "format": "SINGLE_ELIMINATION",
                  "maxParticipants": 8,
                  "registrationStartAt": "2026-07-02T10:00:00",
                  "registrationEndAt": "2026-07-01T10:00:00"
                }
                """;

        mockMvc.perform(post("/tournaments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").value("registrationStartAt must be before or equal to registrationEndAt"));
    }

    @Test
    void formatFactoryResolvesAllStrategies() {
        Tournament tournament = Tournament.builder()
                .name("Format Test")
                .gameName("Valorant")
                .maxParticipants(16)
                .build();

        assertThat(formatFactory.getStrategy(TournamentFormat.SINGLE_ELIMINATION)
                .generateRounds(tournament)).hasSize(4);
        assertThat(formatFactory.getStrategy(TournamentFormat.DOUBLE_ELIMINATION)
                .generateRounds(tournament)).isNotEmpty();
        assertThat(formatFactory.getStrategy(TournamentFormat.ROUND_ROBIN)
                .generateRounds(tournament)).hasSize(15);
        assertThat(formatFactory.getStrategy(TournamentFormat.SWISS)
                .generateRounds(tournament)).hasSize(4);
    }

}
