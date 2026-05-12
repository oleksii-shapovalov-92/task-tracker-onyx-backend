package de.upteams.tasktracker.security.controller;

import de.upteams.tasktracker.security.entities.TokenResponseDto;
import de.upteams.tasktracker.security.filter.JwtTokenFilter;
import de.upteams.tasktracker.security.service.AuthService;
import de.upteams.tasktracker.security.service.CookieService;
import de.upteams.tasktracker.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static de.upteams.tasktracker.security.constants.Constants.ACCESS_TOKEN_COOKIE;
import static de.upteams.tasktracker.security.constants.Constants.REFRESH_TOKEN_COOKIE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(CookieService.class)
@TestPropertySource(properties = {
        "jwt.at.live-in-min=120",
        "jwt.rt.live-in-min=10080",
        "cookie.secure=false"
})
class AuthControllerLoginCookieTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenFilter jwtTokenFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldReturnNonSecureCookiesInDevMode() throws Exception {
        when(authService.login(any())).thenReturn(
                new TokenResponseDto("test-access-token", "test-refresh-token")
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "test@test.com",
                                  "password": "Password123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(ACCESS_TOKEN_COOKIE))
                .andExpect(cookie().exists(REFRESH_TOKEN_COOKIE))
                .andExpect(cookie().secure(ACCESS_TOKEN_COOKIE, false))
                .andExpect(cookie().secure(REFRESH_TOKEN_COOKIE, false))
                .andExpect(cookie().maxAge(ACCESS_TOKEN_COOKIE, 120 * 60))
                .andExpect(cookie().maxAge(REFRESH_TOKEN_COOKIE, 10080 * 60));
    }
}