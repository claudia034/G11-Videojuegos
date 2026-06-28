package com.tournament.application.format;

import com.tournament.domain.entity.Bracket;
import com.tournament.domain.entity.Registration;
import com.tournament.domain.entity.Tournament;
import com.tournament.domain.entity.TournamentRound;
import com.tournament.domain.enums.TournamentFormat;
import com.tournament.domain.enums.TournamentFormatFamily;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class FormatFactory {

    private final TournamentFormatCatalog catalog;
    private final Map<TournamentFormatFamily, TournamentFormatEngine> engines;

    public FormatFactory(TournamentFormatCatalog catalog, List<TournamentFormatEngine> engineList) {
        this.catalog = catalog;
        this.engines = new EnumMap<>(TournamentFormatFamily.class);
        engineList.forEach(engine -> engines.put(engine.family(), engine));
    }

    public TournamentFormatStrategy getFormat(TournamentFormat format) {
        TournamentFormatProfile profile = catalog.getProfile(format);
        TournamentFormatEngine engine = engines.get(profile.family());

        if (engine == null) {
            throw new IllegalArgumentException("No existe motor para la familia de formato: " + profile.family());
        }

        return new ResolvedTournamentFormatStrategy(profile, engine);
    }

    public List<TournamentFormatStrategy> getAllFormats() {
        return catalog.getAllProfiles().stream()
                .map(profile -> getFormat(profile.format()))
                .toList();
    }

    private record ResolvedTournamentFormatStrategy(
            TournamentFormatProfile profile,
            TournamentFormatEngine engine
    ) implements TournamentFormatStrategy {

        @Override
        public TournamentFormat getFormat() {
            return profile.format();
        }

        @Override
        public TournamentFormatProfile getProfile() {
            return profile;
        }

        @Override
        public List<TournamentRound> generateRounds(Tournament tournament) {
            return engine.generateRounds(tournament, profile);
        }

        @Override
        public BracketResult generateBracket(List<Registration> seeded, Bracket bracket) {
            return engine.generateBracket(seeded, bracket, profile);
        }

        @Override
        public int getMinimumParticipants() {
            return profile.minimumParticipants();
        }

        @Override
        public boolean isComplete(Bracket bracket) {
            return engine.isComplete(bracket, profile);
        }

        @Override
        public void validateTournamentConfiguration(Tournament tournament) {
            engine.validateTournamentConfiguration(tournament, profile);
        }
    }
}
