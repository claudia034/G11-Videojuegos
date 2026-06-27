package com.tournament.application.service;

import com.stripe.Stripe;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.tournament.domain.entity.Tournament;
import com.tournament.domain.entity.TournamentPrize;
import com.tournament.domain.enums.PrizeType;
import com.tournament.domain.enums.TournamentStatus;
import com.tournament.domain.repository.TournamentRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeService {

    private final TournamentRepository tournamentRepository;

    @Value("${app.stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${app.stripe.webhook-secret}")
    private String endpointSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Transactional(readOnly = true)
    public String createCheckoutSession(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));

        if (tournament.getStatus() != TournamentStatus.DRAFT) {
            throw new RuntimeException("Solo se pueden fondear torneos en estado DRAFT");
        }

        BigDecimal totalAmount = tournament.getPrizes().stream()
                .filter(p -> p.getPrizeType() == PrizeType.CASH && p.getAmount() != null)
                .map(TournamentPrize::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El torneo no tiene premios en efectivo configurados.");
        }

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:5173/tournaments/" + tournamentId + "/success")
                    .setCancelUrl("http://localhost:5173/tournaments/" + tournamentId + "/cancel")
                    .putMetadata("tournament_id", String.valueOf(tournamentId))
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("usd")
                                                    .setUnitAmount(totalAmount.multiply(new BigDecimal(100)).longValue())
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Fondeo de Premios: " + tournament.getName())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);
            return session.getUrl();
        } catch (StripeException e) {
            log.error("Error creando sesión de pago con Stripe", e);
            throw new RuntimeException("No se pudo iniciar el pago con Stripe", e);
        }
    }

    @Transactional
    public void handleWebhook(String payload, String sigHeader) throws EventDataObjectDeserializationException {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            log.error("Firma de webhook de Stripe inválida");
            throw new RuntimeException("Firma inválida");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().deserializeUnsafe();

            if (session != null) {
                if (session.getMetadata() != null) {
                    String tournamentIdStr = session.getMetadata().get("tournament_id");
                    
                    if (tournamentIdStr != null) {
                        Long tournamentId = Long.parseLong(tournamentIdStr);
                        Optional<Tournament> optionalTournament = tournamentRepository.findById(tournamentId);

                        if (optionalTournament.isPresent()) {
                            Tournament tournament = optionalTournament.get();
                            tournament.setStatus(TournamentStatus.REGISTRATION_OPEN);
                            tournamentRepository.save(tournament);
                            log.info("Torneo {} fondeado exitosamente. Estado cambiado a REGISTRATION_OPEN.", tournamentId);
                        } else {
                            log.warn("El torneo {} no fue encontrado en la base de datos.", tournamentId);
                        }
                    }
                }
            }
        }
    }
}
