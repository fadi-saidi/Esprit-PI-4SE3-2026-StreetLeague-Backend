package tn.esprit.pi;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to verify the test suite is working
 */
class TestSuiteVerificationTest {

    @Test
    void testSuite_ShouldBeWorking() {
        // Simple verification test
        assertTrue(true, "Test suite is operational");
        assertEquals(2, 1 + 1, "Basic arithmetic works");
        assertNotNull("test", "String is not null");
    }

    @Test
    void testEnvironment_ShouldBeConfigured() {
        // Verify Java environment
        assertNotNull(System.getProperty("java.version"), "Java version should be available");
        assertNotNull(System.getProperty("user.dir"), "Working directory should be available");
    }

    @Test
    void testPackageStructure_ShouldBeValid() {
        // Verify package structure
        String packageName = this.getClass().getPackage().getName();
        assertEquals("tn.esprit.pi", packageName, "Package structure should be correct");
    }
}