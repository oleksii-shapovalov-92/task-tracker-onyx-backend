package de.upteams.tasktracker.security.controller;

import de.upteams.tasktracker.security.filter.JwtTokenFilter;
import de.upteams.tasktracker.security.service.AuthService;
import de.upteams.tasktracker.security.service.CookieService;
import de.upteams.tasktracker.security.service.CustomUserDetailsService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static de.upteams.tasktracker.security.constants.Constants.ACCESS_TOKEN_COOKIE;
import static de.upteams.tasktracker.security.constants.Constants.REFRESH_TOKEN_COOKIE;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerLogoutTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CookieService cookieService;

    @MockitoBean
    private JwtTokenFilter jwtTokenFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void logout_shouldClearCookies() throws Exception {
        Cookie accessCookie = new Cookie(ACCESS_TOKEN_COOKIE, null);
        accessCookie.setMaxAge(0);

        Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE, null);
        refreshCookie.setMaxAge(0);

        when(cookieService.generateLogoutCookie(ACCESS_TOKEN_COOKIE)).thenReturn(accessCookie);
        when(cookieService.generateLogoutCookie(REFRESH_TOKEN_COOKIE)).thenReturn(refreshCookie);

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge(ACCESS_TOKEN_COOKIE, 0))
                .andExpect(cookie().maxAge(REFRESH_TOKEN_COOKIE, 0));
    }
}