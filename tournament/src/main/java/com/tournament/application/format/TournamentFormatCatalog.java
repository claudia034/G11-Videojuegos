package com.tournament.application.format;

import com.tournament.domain.enums.TournamentFormat;
import com.tournament.domain.enums.TournamentFormatFamily;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class TournamentFormatCatalog {

    private final Map<TournamentFormat, TournamentFormatProfile> profiles;

    public TournamentFormatCatalog() {
        this.profiles = new EnumMap<>(TournamentFormat.class);
        registerSingleEliminationProfiles();
        registerDoubleEliminationProfiles();
        registerRoundRobinProfiles();
        registerSwissProfiles();
    }

    public TournamentFormatProfile getProfile(TournamentFormat format) {
        TournamentFormatProfile profile = profiles.get(format);
        if (profile == null) {
            throw new IllegalArgumentException("Formato de torneo no soportado: " + format);
        }
        return profile;
    }

    public List<TournamentFormatProfile> getAllProfiles() {
        return profiles.values().stream()
                .sorted(Comparator
                        .comparing(TournamentFormatProfile::family)
                        .thenComparing(TournamentFormatProfile::displayName))
                .toList();
    }

    private void registerSingleEliminationProfiles() {
        register(profile(TournamentFormat.SINGLE_ELIMINATION, TournamentFormatFamily.SINGLE_ELIMINATION,
                "Eliminacion simple", "Bracket clasico a una derrota.", 2, 256, true, true, 1, 1, null));
        register(profile(TournamentFormat.SINGLE_ELIMINATION_BO3, TournamentFormatFamily.SINGLE_ELIMINATION,
                "Eliminacion simple Bo3", "Mismo bracket, series al mejor de 3.", 2, 128, true, true, 3, 1, null));
        register(profile(TournamentFormat.SINGLE_ELIMINATION_BO5, TournamentFormatFamily.SINGLE_ELIMINATION,
                "Eliminacion simple Bo5", "Version premium para playoffs largos.", 2, 64, true, true, 5, 1, null));
        register(profile(TournamentFormat.SINGLE_ELIMINATION_SEEDED, TournamentFormatFamily.SINGLE_ELIMINATION,
                "Eliminacion simple seeded", "Pensado para siembra por ranking ELO.", 2, 256, true, true, 1, 1, null));
        register(profile(TournamentFormat.SINGLE_ELIMINATION_RANDOM, TournamentFormatFamily.SINGLE_ELIMINATION,
                "Eliminacion simple random", "Cruces rapidos y aleatorios desde la primera ronda.", 2, 256, true, false, 1, 1, null));
        register(profile(TournamentFormat.SINGLE_ELIMINATION_OPEN, TournamentFormatFamily.SINGLE_ELIMINATION,
                "Open bracket", "Formato abierto para copas comunitarias.", 2, 512, true, true, 1, 1, null));
        register(profile(TournamentFormat.SINGLE_ELIMINATION_INVITATIONAL, TournamentFormatFamily.SINGLE_ELIMINATION,
                "Invitational bracket", "Ideal para eventos cerrados o invitados especiales.", 2, 32, true, true, 3, 1, null));
        register(profile(TournamentFormat.SINGLE_ELIMINATION_FAST, TournamentFormatFamily.SINGLE_ELIMINATION,
                "Lightning bracket", "Copa corta con administracion y rondas aceleradas.", 2, 64, true, false, 1, 1, null));
    }

    private void registerDoubleEliminationProfiles() {
        register(profile(TournamentFormat.DOUBLE_ELIMINATION, TournamentFormatFamily.DOUBLE_ELIMINATION,
                "Doble eliminacion", "Cada participante necesita dos derrotas para quedar fuera.", 4, 128, false, true, 1, 1, null));
        register(profile(TournamentFormat.DOUBLE_ELIMINATION_BO3, TournamentFormatFamily.DOUBLE_ELIMINATION,
                "Doble eliminacion Bo3", "Series Bo3 en winners y losers bracket.", 4, 64, false, true, 3, 1, null));
        register(profile(TournamentFormat.DOUBLE_ELIMINATION_BO5, TournamentFormatFamily.DOUBLE_ELIMINATION,
                "Doble eliminacion Bo5", "Pensado para finales largas y broadcast.", 4, 32, false, true, 5, 1, null));
        register(profile(TournamentFormat.DOUBLE_ELIMINATION_SEEDED, TournamentFormatFamily.DOUBLE_ELIMINATION,
                "Doble eliminacion seeded", "Siembra por ranking para torneos competitivos.", 4, 128, false, true, 1, 1, null));
        register(profile(TournamentFormat.DOUBLE_ELIMINATION_RANDOM, TournamentFormatFamily.DOUBLE_ELIMINATION,
                "Doble eliminacion random", "Cruces iniciales aleatorios con losers bracket.", 4, 128, false, false, 1, 1, null));
        register(profile(TournamentFormat.DOUBLE_ELIMINATION_OPEN, TournamentFormatFamily.DOUBLE_ELIMINATION,
                "Open double elim", "Version abierta para eventos masivos.", 4, 256, false, true, 1, 1, null));
        register(profile(TournamentFormat.DOUBLE_ELIMINATION_INVITATIONAL, TournamentFormatFamily.DOUBLE_ELIMINATION,
                "Invitational double elim", "Cuadros chicos de alto nivel competitivo.", 4, 16, false, true, 3, 1, null));
        register(profile(TournamentFormat.DOUBLE_ELIMINATION_FAST, TournamentFormatFamily.DOUBLE_ELIMINATION,
                "Rapid double elim", "Recorrido corto para fines de semana o ladders live.", 4, 64, false, false, 1, 1, null));
    }

    private void registerRoundRobinProfiles() {
        register(profile(TournamentFormat.ROUND_ROBIN, TournamentFormatFamily.ROUND_ROBIN,
                "Round robin", "Todos contra todos a una vuelta.", 2, 64, true, true, 1, 1, null));
        register(profile(TournamentFormat.ROUND_ROBIN_DOUBLE_LEG, TournamentFormatFamily.ROUND_ROBIN,
                "Round robin ida y vuelta", "Dos vueltas contra cada rival.", 2, 32, true, true, 1, 2, null));
        register(profile(TournamentFormat.ROUND_ROBIN_BO3, TournamentFormatFamily.ROUND_ROBIN,
                "Round robin Bo3", "Liga regular con series al mejor de 3.", 2, 48, true, true, 3, 1, null));
        register(profile(TournamentFormat.ROUND_ROBIN_BO5, TournamentFormatFamily.ROUND_ROBIN,
                "Round robin Bo5", "Calendario premium para ligas cortas.", 2, 24, true, true, 5, 1, null));
        register(profile(TournamentFormat.ROUND_ROBIN_SEEDED, TournamentFormatFamily.ROUND_ROBIN,
                "Round robin seeded", "Orden de cruces guiado por ranking.", 2, 64, true, true, 1, 1, null));
        register(profile(TournamentFormat.ROUND_ROBIN_OPEN, TournamentFormatFamily.ROUND_ROBIN,
                "Open league", "Liga comunitaria para cupos mas grandes.", 2, 96, true, false, 1, 1, null));
        register(profile(TournamentFormat.ROUND_ROBIN_DIVISIONAL, TournamentFormatFamily.ROUND_ROBIN,
                "Round robin divisional", "Pensado para separar grupos o divisiones.", 4, 128, false, true, 1, 1, null));
        register(profile(TournamentFormat.ROUND_ROBIN_FAST, TournamentFormatFamily.ROUND_ROBIN,
                "Sprint league", "Pocas fechas y resolucion rapida.", 2, 20, true, false, 1, 1, null));
    }

    private void registerSwissProfiles() {
        register(profile(TournamentFormat.SWISS, TournamentFormatFamily.SWISS,
                "Swiss", "Emparejamientos por puntaje, sin eliminacion directa.", 4, 256, false, true, 1, 1, 5));
        register(profile(TournamentFormat.SWISS_ACCELERATED, TournamentFormatFamily.SWISS,
                "Swiss accelerated", "Acelera primeras rondas para separar seeds rapido.", 8, 256, false, true, 1, 1, 6));
        register(profile(TournamentFormat.SWISS_BO3, TournamentFormatFamily.SWISS,
                "Swiss Bo3", "Sistema suizo con matches al mejor de 3.", 4, 128, false, true, 3, 1, 5));
        register(profile(TournamentFormat.SWISS_BO5, TournamentFormatFamily.SWISS,
                "Swiss Bo5", "Reservado para ligas chicas o finales previas.", 4, 64, false, true, 5, 1, 5));
        register(profile(TournamentFormat.SWISS_SEEDED, TournamentFormatFamily.SWISS,
                "Swiss seeded", "Primera ronda basada en ranking y luego puntaje.", 4, 256, false, true, 1, 1, 5));
        register(profile(TournamentFormat.SWISS_OPEN, TournamentFormatFamily.SWISS,
                "Open Swiss", "Torneos abiertos con volumen alto de jugadores.", 8, 512, false, false, 1, 1, 7));
        register(profile(TournamentFormat.SWISS_TOP8, TournamentFormatFamily.SWISS,
                "Swiss + Top 8", "Fase suiza seguida por corte a playoff Top 8.", 8, 256, false, true, 1, 1, 6));
        register(profile(TournamentFormat.SWISS_TOP16, TournamentFormatFamily.SWISS,
                "Swiss + Top 16", "Fase suiza seguida por corte a playoff Top 16.", 16, 512, false, true, 1, 1, 7));
        register(profile(TournamentFormat.SWISS_FAST, TournamentFormatFamily.SWISS,
                "Swiss rapid", "Menos rondas para clasificatorias express.", 4, 128, false, false, 1, 1, 4));
    }

    private TournamentFormatProfile profile(
            TournamentFormat format,
            TournamentFormatFamily family,
            String displayName,
            String description,
            int minimumParticipants,
            Integer maximumParticipants,
            boolean supportsBracketGeneration,
            boolean supportsRankingSeeding,
            int defaultBestOf,
            int roundRobinCycles,
            Integer swissRounds
    ) {
        return new TournamentFormatProfile(
                format,
                family,
                displayName,
                description,
                minimumParticipants,
                maximumParticipants,
                supportsBracketGeneration,
                supportsRankingSeeding,
                true,
                true,
                defaultBestOf,
                roundRobinCycles,
                swissRounds
        );
    }

    private void register(TournamentFormatProfile profile) {
        profiles.put(profile.format(), profile);
    }
}
