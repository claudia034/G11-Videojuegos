package com.tournament.application.service;

import com.tournament.application.dto.response.NotificationDto;
import com.tournament.application.event.MatchCompletedEvent;
import com.tournament.application.event.TournamentStartedEvent;
import com.tournament.domain.entity.*;
import com.tournament.domain.repository.MatchRepository;
import com.tournament.domain.repository.NotificationRepository;
import com.tournament.domain.repository.RegistrationRepository;
import com.tournament.domain.repository.TournamentRepository;
import com.tournament.domain.enums.RegistrationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MatchRepository matchRepository;
    private final RegistrationRepository registrationRepository;
    private final TournamentRepository tournamentRepository;

    private final Map<Long, SseEmitter> userEmitters = new ConcurrentHashMap<>();

    public SseEmitter registerClient(Long userId) {
        SseEmitter emitter = new SseEmitter(3600000L);
        userEmitters.put(userId, emitter);

        emitter.onCompletion(() -> userEmitters.remove(userId));
        emitter.onTimeout(() -> userEmitters.remove(userId));
        emitter.onError(e -> userEmitters.remove(userId));

        return emitter;
    }

    @Async
    @Transactional
    public void notifyUser(Long userId, String title, String message) {
        Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .build();
        
        notification = notificationRepository.save(notification);
        
        NotificationDto dto = NotificationDto.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();

        SseEmitter emitter = userEmitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(dto));
            } catch (IOException e) {
                userEmitters.remove(userId);
                log.warn("No se pudo enviar la notificación al usuario: {}", userId);
            }
        }
    }

    @EventListener
    public void handleMatchCompleted(MatchCompletedEvent event) {
        Match match = matchRepository.findById(event.getMatchId()).orElse(null);
        if (match == null) return;

        String title = "Partido Finalizado";
        String msg = "Tu partido del torneo ha finalizado y el resultado se ha procesado.";

        List<Long> userIds = new ArrayList<>();
        if (match.getRegistration1() != null) userIds.addAll(extractUserIds(match.getRegistration1()));
        if (match.getRegistration2() != null) userIds.addAll(extractUserIds(match.getRegistration2()));

        userIds.forEach(uid -> notifyUser(uid, title, msg));

        notifyNextMatchParticipants(match);
    }

    @EventListener
    public void handleTournamentStarted(TournamentStartedEvent event) {
        Tournament tournament = tournamentRepository.findById(event.getTournamentId()).orElse(null);
        if (tournament == null) return;

        String title = "¡Torneo Iniciado!";
        String msg = "El torneo " + tournament.getName() + " ha comenzado. ¡Revisa el bracket para ver tus partidos!";

        List<Registration> registrations = registrationRepository
                .findByTournamentIdAndStatus(event.getTournamentId(), RegistrationStatus.CONFIRMED);

        registrations.stream()
                .flatMap(reg -> extractUserIds(reg).stream())
                .distinct()
                .forEach(uid -> notifyUser(uid, title, msg));
    }

    private List<Long> extractUserIds(Registration registration) {
        if (registration.isPlayerRegistration()) {
            return List.of(registration.getPlayer().getUserId());
        }
        return registration.getTeam().getMembers().stream()
                .map(member -> member.getPlayer().getUserId())
                .collect(Collectors.toList());
    }

    private void notifyNextMatchParticipants(Match completedMatch) {
        if (completedMatch.getNextMatch() == null) {
            return;
        }

        Match nextMatch = matchRepository.findById(completedMatch.getNextMatch().getId()).orElse(null);
        if (nextMatch == null || nextMatch.getRegistration1() == null || nextMatch.getRegistration2() == null) {
            return;
        }

        String tournamentName = nextMatch.getRound().getBracket().getTournament().getName();
        String title = "Proximo partido listo";
        String msg = "Tu siguiente enfrentamiento en " + tournamentName + " ya esta listo en el bracket.";

        List<Long> userIds = new ArrayList<>();
        userIds.addAll(extractUserIds(nextMatch.getRegistration1()));
        userIds.addAll(extractUserIds(nextMatch.getRegistration2()));
        userIds.stream().distinct().forEach(uid -> notifyUser(uid, title, msg));
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(notif -> NotificationDto.builder()
                        .id(notif.getId())
                        .title(notif.getTitle())
                        .message(notif.getMessage())
                        .read(notif.isRead())
                        .createdAt(notif.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUserId().equals(userId)) {
                n.setRead(true);
                notificationRepository.save(n);
            } else {
                log.warn("El usuario {} intentó marcar la notificación {} como leída, pero no le pertenece.", userId, notificationId);
            }
        });
    }
}
