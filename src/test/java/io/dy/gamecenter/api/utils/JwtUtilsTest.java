package io.dy.gamecenter.api.utils;

import io.dy.gamecenter.api.models.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The token helper needs no Spring context and no database: it signs a string and parses it back.
 */
class JwtUtilsTest {

    private static final String SECRET = "a-secret-long-enough-to-sign-with-0123456789";

    private JwtUtils utils;

    @BeforeEach
    void setUp() {
        utils = new JwtUtils();
        utils.jwtSecret = SECRET;
    }

    private UserModel user(String userId) {
        UserModel user = new UserModel();
        user.setUserId(userId);
        return user;
    }

    @Test
    @DisplayName("the userId a token was minted for comes back out of it")
    void aTokenCarriesTheUserIdItWasMintedFor() {
        long now = System.currentTimeMillis() / 1000;
        String token = utils.generateToken(user("user-1"), now, now + 3600);

        assertNotNull(token);
        assertEquals("user-1", utils.extractUsername(token));
    }

    @Test
    @DisplayName("the expiry the caller asked for is the expiry that comes back")
    void aTokenCarriesTheExpiryTheCallerChose() {
        long now = System.currentTimeMillis() / 1000;
        long expiresAt = now + 900;
        String token = utils.generateToken(user("user-1"), now, expiresAt);

        assertEquals(new Date(expiresAt * 1000), utils.extractExpiration(token));
        assertEquals(new Date(now * 1000), utils.extractAllClaims(token).getIssuedAt());
    }

    @Test
    @DisplayName("a token signed with another secret is not the token this server issued")
    void aTokenSignedWithAnotherSecretIsRefused() {
        long now = System.currentTimeMillis() / 1000;
        JwtUtils other = new JwtUtils();
        other.jwtSecret = "a-different-secret-that-is-also-long-enough-0123456789";
        String foreignToken = other.generateToken(user("user-1"), now, now + 3600);

        // parsing returns nothing rather than throwing, so every caller has to handle null
        assertNull(utils.extractAllClaims(foreignToken));
        assertNull(utils.extractUsername(foreignToken));
        assertFalse(utils.validateToken(foreignToken));
    }

    @Test
    @DisplayName("a token that cannot be parsed at all is invalid, not a crash")
    void aGarbageTokenIsInvalidRatherThanACrash() {
        for (String garbage : new String[]{"", "not-a-token", "a.b.c", "Bearer abc", "eyJhbGciOiJIUzI1NiJ9..x"}) {
            assertNull(utils.extractAllClaims(garbage), "claims of " + garbage);
            assertFalse(utils.validateToken(garbage), "validateToken(" + garbage + ")");
        }
        // validateToken used to throw a NullPointerException here: extractExpiration returns null for
        // a token that does not parse, and comparing that null to a date is a 500, not a 401
    }

    @Test
    @DisplayName("an expired token is refused even though it is signed correctly")
    void anExpiredTokenIsRefused() {
        long now = System.currentTimeMillis() / 1000;
        String expired = utils.generateToken(user("user-1"), now - 7200, now - 3600);
        String live = utils.generateToken(user("user-1"), now, now + 3600);

        // jjwt refuses an expired token at parse time, so it never reaches the caller as claims:
        // every reader sees null, and validateToken turns that into a plain false
        assertNull(utils.extractAllClaims(expired), "an expired token is not parsed");
        assertNull(utils.extractUsername(expired));
        assertFalse(utils.validateToken(expired), "an expired token must not validate");
        assertTrue(utils.validateToken(live), "a live token must validate");
    }

    @Test
    @DisplayName("every token carries an id of its own, which is what a logout can revoke")
    void everyTokenCarriesItsOwnId() {
        long now = System.currentTimeMillis() / 1000;
        String first = utils.generateToken(user("user-1"), now, now + 3600);
        String second = utils.generateToken(user("user-1"), now, now + 3600);

        String firstId = utils.extractJwtId(first);
        assertNotNull(firstId);
        assertTrue(firstId.length() > 0);
        assertFalse(firstId.equals(utils.extractJwtId(second)), "two tokens must not share an id");
    }
}
