package tn.esprit.pi.gestiontournoi.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GestionApprovalEntityTest {

    @Test
    void onCreate_initializesTimestampsAndDefaultStatus() {
        GestionEvent event = new GestionEvent();
        event.setApprovalStatus(null);

        event.onCreate();

        assertEquals(ApprovalStatus.PENDING, event.getApprovalStatus());
        assertNotNull(event.getCreatedAt());
        assertNotNull(event.getUpdatedAt());
    }

    @Test
    void onCreate_preservesExistingStatus() {
        GestionEvent event = new GestionEvent();
        event.setApprovalStatus(ApprovalStatus.APPROVED);

        event.onCreate();

        assertEquals(ApprovalStatus.APPROVED, event.getApprovalStatus());
        assertNotNull(event.getCreatedAt());
        assertNotNull(event.getUpdatedAt());
    }

    @Test
    void onUpdate_refreshesUpdatedAt() {
        GestionEvent event = new GestionEvent();
        event.setApprovalStatus(ApprovalStatus.APPROVED);
        event.setCreatedAt(LocalDateTime.now().minusDays(1));
        event.setUpdatedAt(LocalDateTime.now().minusDays(1));

        event.onUpdate();

        assertNotNull(event.getUpdatedAt());
        assertTrue(event.getUpdatedAt().isAfter(event.getCreatedAt()));
        assertEquals(ApprovalStatus.APPROVED, event.getApprovalStatus());
    }
}
