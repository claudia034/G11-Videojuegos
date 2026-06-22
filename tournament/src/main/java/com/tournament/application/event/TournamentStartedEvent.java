package com.tournament.application.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TournamentStartedEvent extends ApplicationEvent {
    private final Long tournamentId;

    public TournamentStartedEvent(Object source, Long tournamentId) {
        super(source);
        this.tournamentId = tournamentId;
    }
}
