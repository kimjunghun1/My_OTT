package com.example.myott

import org.junit.Assert.*
import org.junit.Test

class RegistrationRequestTest {
    private val valid = RegistrationRequest("member", "member@example.com", "2000-02-29", "Password123", "Password123", true)

    @Test fun acceptsValidRequest() { assertNull(valid.issue()) }
    @Test fun rejectsEmptyId() { assertNotNull(valid.copy(id = "  ").issue()) }
    @Test fun rejectsInvalidEmail() { assertNotNull(valid.copy(email = "bad@email").issue()) }
    @Test fun rejectsImpossibleAndFutureDates() {
        listOf("2001-02-29", "2000-13-01", "2999-01-01", "2000-1-1", "0000-01-01").forEach {
            assertNotNull(it, valid.copy(birth = it).issue())
        }
    }
    @Test fun rejectsWeakPasswords() {
        listOf("short1", "abcdefgh", "12345678").forEach {
            assertNotNull(valid.copy(password = it, confirmation = it).issue())
        }
    }
    @Test fun rejectsMismatch() { assertNotNull(valid.copy(confirmation = "other").issue()) }
    @Test fun requiresConsent() { assertNotNull(valid.copy(agreed = false).issue()) }
    @Test fun formatsBirthday() { assertEquals("2000-02-29", formatBirth("20000229")) }
}
