package io.galeb.manager.security.services;

import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SystemUserService {

    public static final UsernamePasswordAuthenticationToken AUTH =
                        new UsernamePasswordAuthenticationToken("system", UUID.randomUUID().toString(),
                                AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER"));

    public static void clearContext() {
        SecurityContextHolder.clearContext();
    }

    public static void runAs(final Authentication authentication) {
        clearContext();
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static void runAs() {
        runAs(AUTH);
    }
}
