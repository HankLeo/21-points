package io.hank.twentyonepoints.web.rest;

import static io.hank.twentyonepoints.test.util.OAuth2TestUtil.TEST_USER_LOGIN;
import static io.hank.twentyonepoints.test.util.OAuth2TestUtil.registerAuthenticationToken;
import static io.hank.twentyonepoints.test.util.OAuth2TestUtil.testAuthenticationToken;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.hank.twentyonepoints.IntegrationTest;
import io.hank.twentyonepoints.repository.UserRepository;
import io.hank.twentyonepoints.security.AuthoritiesConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link AccountResource} REST controller.
 */
@AutoConfigureMockMvc
@IntegrationTest
class AccountResourceIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc restAccountMockMvc;

    @Autowired
    OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    ClientRegistration clientRegistration;

    @AfterEach
    public void cleanup() {
        // Remove syncUserWithIdp users
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    void testGetExistingAccount() throws Exception {
        TestSecurityContextHolder.getContext()
            .setAuthentication(registerAuthenticationToken(authorizedClientService, clientRegistration, testAuthenticationToken()));

        restAccountMockMvc
            .perform(get("/api/account").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.login").value(TEST_USER_LOGIN))
            .andExpect(jsonPath("$.email").value("john.doe@jhipster.com"))
            .andExpect(jsonPath("$.authorities").value(AuthoritiesConstants.ADMIN));
    }

    @Test
    void testGetUnknownAccount() throws Exception {
        restAccountMockMvc.perform(get("/api/account").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithUnauthenticatedMockUser
    void testNonAuthenticatedUser() throws Exception {
        restAccountMockMvc
            .perform(get("/api/authenticate").accept(MediaType.TEXT_PLAIN))
            .andExpect(status().isOk())
            .andExpect(content().string(""));
    }

    @Test
    @WithMockUser(TEST_USER_LOGIN)
    void testAuthenticatedUser() throws Exception {
        restAccountMockMvc
            .perform(get("/api/authenticate").with(request -> request).accept(MediaType.TEXT_PLAIN))
            .andExpect(status().isOk())
            .andExpect(content().string(TEST_USER_LOGIN));
    }
}
