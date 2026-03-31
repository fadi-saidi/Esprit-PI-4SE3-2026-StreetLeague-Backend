package tn.esprit.pi.domain;

public enum PlayerPredictionResult {
    PENDING,   // not yet resolved
    CORRECT,   // scored / won → positive points
    WRONG,     // didn't score / lost → 0 points
    PARTIAL    // scored but also got card → mixed points
}