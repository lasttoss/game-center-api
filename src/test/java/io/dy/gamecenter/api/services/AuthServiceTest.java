package io.dy.gamecenter.api.services;

import io.dy.gamecenter.api.constants.ApiErrorEnum;
import io.dy.gamecenter.api.constants.JwtConstants;
import io.dy.gamecenter.api.dto.requests.AuthRenewRequest;
import io.dy.gamecenter.api.dto.requests.AuthRequest;
import io.dy.gamecenter.api.dto.responses.AuthResponse;
import io.dy.gamecenter.api.dto.responses.Response;
import io.dy.gamecenter.api.models.UserModel;
import io.dy.gamecenter.api.repositories.UserRepository;
import io.dy.gamecenter.api.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The auth service decides who gets a token, so these tests are about that decision: which status and
 * which error code each way of failing produces, what was written to the database, and how long the
 * tokens the caller receives are meant to last.
 *
 * No Spring context and no MongoDB: the repository and the token signer are mocks. The password
 * encoder is the real one, because "the stored password is not the password" is the kind of thing a
 * mock would agree with no matter what.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String USERNAME = "player";

    @Mock
    UserRepository userRepository;

    @Mock
    JwtUtils jwtUtils;

    @InjectMocks
    AuthService authService;

    /** Reads a field directly: two of this model's getters are stubs (see UserModelTest). */
    static Object field(Object target, String name) {
        try {
            java.lang.reflect.Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(target);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("cannot read " + name, e);
        }
    }

    static AuthRequest credentials(String username, String password) {
        AuthRequest request = new AuthRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    static UserModel existingUser() {
        UserModel user = new UserModel(USERNAME, "$2a$10$stored-hash");
        user.setUserId("user-1");
        user.setId("doc-1");
        return user;
    }

    static void assertRefused(Response response, ApiErrorEnum error) {
        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
        assertNull(response.getData(), "a refused request must not carry a token");
        assertNotNull(response.getError(), "a refusal has to say what was wrong");
        assertEquals(error.getCode(), response.getError().getCode());
        assertEquals(error.getMessage(), response.getError().getMessage());
    }

    static AuthResponse assertIssued(Response response) {
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNotNull(response.getData());
        return (AuthResponse) response.getData();
    }

    static void assertTokenLifetimes(AuthResponse auth) {
        long now = System.currentTimeMillis() / 1000;
        long access = auth.getAccessTokenExpiredTime() - now;
        long refresh = auth.getRefreshTokenExpiredTime() - now;
        assertTrue(Math.abs(access - JwtConstants.ACCESS_TOKEN_EXPIRED_TIME) <= 5, "access lifetime = " + access + "s");
        assertTrue(Math.abs(refresh - JwtConstants.REFRESH_TOKEN_EXPIRED_TIME) <= 5, "refresh lifetime = " + refresh + "s");
    }

    @Test
    void registerRefusesAnEmptyUsernameOrPassword() {
        for (AuthRequest request : List.of(credentials("", "secret"), credentials(USERNAME, ""))) {
            Response response = authService.register(request);

            assertRefused(response, ApiErrorEnum.INVALID_REQUEST);
        }
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerRefusesAUsernameThatIsTaken() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(existingUser());

        assertRefused(authService.register(credentials(USERNAME, "secret")), ApiErrorEnum.EXIST_USER);

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerStoresThePasswordHashedAndHandsOutBothTokens() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(null);
        when(userRepository.save(any())).thenAnswer(call -> {
            UserModel saved = call.getArgument(0);
            saved.setId("doc-1");
            return saved;
        });
        when(jwtUtils.generateToken(any(), anyLong(), anyLong())).thenReturn("access-token", "refresh-token");

        AuthResponse auth = assertIssued(authService.register(credentials(USERNAME, "secret")));

        ArgumentCaptor<UserModel> stored = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository).save(stored.capture());
        UserModel user = stored.getValue();
        assertEquals("DEFAULT DEFAULT", user.getDisplayName(), "the user was not built by the constructor");
        assertNotNull(user.getUserId(), "nothing could look this account up without a user id");

        // Asserted on the field, not on getUsername()/getPassword(): those two are overridden to
        // return null to satisfy UserDetails (see UserModelTest), so a test that asked them would be
        // testing the stubs rather than the registration.
        assertNotEquals("secret", field(user, "password"), "the password was stored as given");
        assertTrue(((String) field(user, "password")).startsWith("$2"),
                "not a bcrypt hash: " + field(user, "password"));
        assertEquals(USERNAME, field(user, "username"));

        assertEquals("access-token", auth.getAccessToken());
        assertEquals("refresh-token", auth.getRefreshToken());
        assertTokenLifetimes(auth);
    }

    @Test
    void loginRefusesAUsernameNobodyHas() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(null);

        assertRefused(authService.login(credentials(USERNAME, "secret")), ApiErrorEnum.USER_NOT_FOUND);
    }

    @Test
    void loginIssuesBothTokensForAKnownUsername() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(existingUser());
        when(jwtUtils.generateToken(any(), anyLong(), anyLong())).thenReturn("access-token", "refresh-token");

        AuthResponse auth = assertIssued(authService.login(credentials(USERNAME, "secret")));

        assertEquals("access-token", auth.getAccessToken());
        assertEquals("refresh-token", auth.getRefreshToken());
        assertTokenLifetimes(auth);
    }

    /**
     * Recorded, not fixed: login looks the username up and issues tokens without ever asking the
     * password encoder to check anything, so any password at all signs you in as a user whose name
     * you know. The encoder is created as a field and only register uses it.
     *
     * Whether that is a bug or a decision depends on whether this endpoint is meant to serve social
     * logins too - the request only carries a username and a password, so as written it is a bug. It
     * is left alone because adding a password check would lock out any client that signs in through
     * this route today, and that is a call for the service owner.
     */
    @Test
    void loginAcceptsAnyPasswordAtAllForAKnownUsername() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(existingUser());
        when(jwtUtils.generateToken(any(), anyLong(), anyLong())).thenReturn("access-token", "refresh-token");

        Response response = authService.login(credentials(USERNAME, "not-the-password"));

        assertEquals(HttpStatus.OK.value(), response.getStatus(),
                "a wrong password was accepted: login never verifies it");
        assertNotNull(response.getData());
    }

    @Test
    void renewRefusesATokenThatDoesNotValidate() {
        when(jwtUtils.validateToken("bad-token")).thenReturn(false);

        AuthRenewRequest request = new AuthRenewRequest();
        request.setRefreshToken("bad-token");

        assertRefused(authService.renewToken(request), ApiErrorEnum.FAILED_TO_VERIFY_TOKEN);

        verify(jwtUtils, never()).extractAllClaims(any());
    }

    @Test
    void renewRefusesAValidTokenWhoseUserIsGone() {
        Claims claims = org.mockito.Mockito.mock(Claims.class);
        when(jwtUtils.validateToken("good-token")).thenReturn(true);
        when(jwtUtils.extractAllClaims("good-token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("user-1");
        when(userRepository.findByUserId("user-1")).thenReturn(null);

        AuthRenewRequest request = new AuthRenewRequest();
        request.setRefreshToken("good-token");

        assertRefused(authService.renewToken(request), ApiErrorEnum.FAILED_TO_VERIFY_TOKEN);
    }

    @Test
    void renewIssuesFreshTokensForTheSubjectOfTheToken() {
        Claims claims = org.mockito.Mockito.mock(Claims.class);
        when(jwtUtils.validateToken("good-token")).thenReturn(true);
        when(jwtUtils.extractAllClaims("good-token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("user-1");
        when(userRepository.findByUserId("user-1")).thenReturn(existingUser());
        when(jwtUtils.generateToken(any(), anyLong(), anyLong())).thenReturn("new-access", "new-refresh");

        AuthRenewRequest request = new AuthRenewRequest();
        request.setRefreshToken("good-token");

        AuthResponse auth = assertIssued(authService.renewToken(request));

        assertEquals("new-access", auth.getAccessToken());
        assertEquals("new-refresh", auth.getRefreshToken());
        verify(userRepository).findByUserId("user-1");
        assertTokenLifetimes(auth);
    }
}
