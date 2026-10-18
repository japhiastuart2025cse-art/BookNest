package com.booknest.backend.controller;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HealthControllerTest {

    @Test
    void testCheckHealthReturnsUp() {
        HealthController controller = new HealthController();
        Map<String, Object> response = controller.checkHealth();

        assertEquals("UP", response.get("status"));
        assertEquals("BookNest Backend API", response.get("service"));
        assertNotNull(response.get("timestamp"));
    }
}
