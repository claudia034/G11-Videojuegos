package com.tournament.application.format;

import com.tournament.domain.entity.Match;
import com.tournament.domain.entity.Round;

import java.util.List;

public record BracketResult(List<Round> rounds, List<Match> matches) {}
