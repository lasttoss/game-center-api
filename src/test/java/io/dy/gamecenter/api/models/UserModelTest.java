package io.dy.gamecenter.api.models;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Recorded, not fixed: UserModel implements Spring Security's UserDetails, and two of that
 * interface's methods are written here as stubs that return null - getUsername() and getPassword().
 * Lombok's @Data does not generate a getter where one already exists, so those stubs are the getters
 * every caller sees: the field holds the username, and getUsername() answers null.
 *
 * Nothing breaks in this service today, because requests are authenticated by JwtAuthFilter and the
 * login path never asks for a password. But anything that reaches for the obvious accessor - a
 * UserDetailsService, an encoder's matches() call, a log line - silently reads null, and a test that
 * asserts on the getter ends up testing the stub. Hence the reflection below.
 */
class UserModelTest {

    private static final String USERNAME = "player";

    static Object field(Object target, String name) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(target);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("cannot read " + name, e);
        }
    }

    @Test
    void theConstructorFillsInTheAccount() {
        UserModel user = new UserModel(USERNAME, "$2a$10$hash");

        assertEquals(USERNAME, field(user, "username"), "the username field is set");
        assertEquals("$2a$10$hash", field(user, "password"), "the stored value is whatever the caller passed, already hashed");
        assertNotNull(field(user, "userId"), "the user id is what a token is about");
        assertNotNull(field(user, "socialId"));
        assertNotNull(user.getDisplayName());
        assertNotNull(user.getCreatedAt(), "the timestamps come from the constructor, not from the database");

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) field(user, "roles");
        assertEquals(List.of("USER"), roles, "the one role the interceptor expects");
    }

    @Test
    void theUserDetailsGettersAreStubsThatAnswerNull() {
        UserModel user = new UserModel(USERNAME, "$2a$10$hash");

        assertNull(user.getUsername(),
                "UserDetails.getUsername() answers null even though the username field is set");
        assertNull(user.getPassword(),
                "UserDetails.getPassword() answers null, so no password encoder can ever match a login");
        assertNull(user.getAuthorities(), "the authorities are a stub too");
        assertFalse(user.isEnabled(), "isEnabled() is a stub that answers false");
    }

    @Test
    void theGettersThatAreNotStubsDoWork() {
        UserModel user = new UserModel(USERNAME, "$2a$10$hash");

        assertNotNull(user.getDisplayName());
        assertNotNull(user.getAvatarUrl());
        assertEquals("", user.getEmail());
        assertNotNull(user.getSocialId());
        assertTrue(user.getSocialType() >= 0);
    }
}
