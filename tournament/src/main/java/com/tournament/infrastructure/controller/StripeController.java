package com.tournament.infrastructure.controller;

import com.stripe.exception.EventDataObjectDeserializationException;
import com.tournament.application.service.StripeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/stripe")
@RequiredArgsConstructor
public class StripeController {

    private final StripeService stripeService;

    @PostMapping("/checkout-tournament/{tournamentId}")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@PathVariable Long tournamentId) {
        String url = stripeService.createCheckoutSession(tournamentId);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) throws EventDataObjectDeserializationException {
        stripeService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok().build();
    }
}
