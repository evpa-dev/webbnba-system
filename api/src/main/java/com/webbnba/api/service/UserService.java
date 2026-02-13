package com.webbnba.api.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.webbnba.api.dto.KeycloakCredentialsRepresentation;
import com.webbnba.api.mapper.KeycloakMapper;
import com.webbnba.api.mapper.TokenResponseMapper;
import com.webbnba.individual.dto.IndividualWriteDto;
import com.webbnba.individual.dto.TokenResponse;
import com.webbnba.individual.dto.UserInfoResponse;
import com.webbnba.individual.dto.UserLoginRequest;
import com.webbnba.api.client.KeycloakClient;
import com.webbnba.api.dto.KeycloakUserRepresentation;
import com.webbnba.api.exception.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final PersonService personService;
    private final KeycloakClient keycloakClient;
    private final TokenResponseMapper tokenResponseMapper;

    @WithSpan(value = "userService.getCurrentUserInfo")
    public Mono<UserInfoResponse> getUserInfo() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(UserService::getUserInfoResponseMono)
                .switchIfEmpty(Mono.error(new ApiException("No authentication present")));
    }

    private static Mono<UserInfoResponse> getUserInfoResponseMono(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            var userInfoResponse = new UserInfoResponse();
            userInfoResponse.setId(jwt.getSubject());
            userInfoResponse.setEmail(jwt.getClaimAsString("email"));
            userInfoResponse.setRoles(jwt.getClaimAsStringList("roles"));

            if (jwt.getIssuedAt() != null) {
                userInfoResponse.setCreatedAt(jwt.getIssuedAt().atOffset(ZoneOffset.UTC));
            }
            log.info("User[email={}] was successfully get info", jwt.getClaimAsString("email"));

            return Mono.just(userInfoResponse);
        }

        log.error("Can not get current user info: Invalid principal");
        return Mono.error(new ApiException("Can not get current user info: Invalid principal"));
    }

    @WithSpan("userService.register")
    public Mono<TokenResponse> register(IndividualWriteDto request) {
        return personService.register(request) // Mono<UUID> personId
                .flatMap(personId ->
                        keycloakClient.adminLogin()
                                .flatMap(adminToken -> {
                                    var kcUser = new com.webbnba.api.dto.KeycloakUserRepresentation(
                                            null,
                                            request.getEmail(),
                                            request.getEmail(),
                                            true,
                                            true,
                                            null
                                    );
                                    return keycloakClient.registerUser(adminToken, kcUser)
                                            .flatMap(kcUserId -> {
                                                var cred = new com.webbnba.api.dto.KeycloakCredentialsRepresentation(
                                                        "password",
                                                        request.getPassword(),
                                                        false
                                                );
                                                return keycloakClient
                                                        .resetUserPassword(kcUserId, cred, adminToken.getAccessToken())
                                                        .thenReturn(kcUserId);
                                            })
                                            .flatMap(r ->
                                                    keycloakClient.login(
                                                            new com.webbnba.keycloak.dto.UserLoginRequest(
                                                                    request.getEmail(),
                                                                    request.getPassword()
                                                            )
                                                    )
                                            )
                                            .onErrorResume(err ->
                                                    personService.compensateRegistration(personId.getId().toString())
                                                            .then(Mono.error(err))
                                            )
                                            .map(tokenResponseMapper::toTokenResponse);

                                })
                );
    }

}
