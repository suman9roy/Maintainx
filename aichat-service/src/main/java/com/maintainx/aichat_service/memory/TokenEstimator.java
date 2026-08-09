package com.maintainx.aichat_service.memory;

import org.springframework.stereotype.Component;

/**
 * Rough token-count estimate (~4 chars/token, English average).
 * Good enough for budget management; swap for a real tokenizer
 * (e.g. jtokkit) later if precise counts become important for billing.
 */
@Component
public class TokenEstimator {

    public int estimate(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return Math.max(1, text.length() / 4);
    }
}
