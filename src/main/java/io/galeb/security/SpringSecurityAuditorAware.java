package io.galeb.security;

import java.io.Serializable;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@SuppressWarnings("serial")
public class SpringSecurityAuditorAware implements AuditorAware<String>, Serializable {

    @Override
    public String getCurrentAuditor() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = "anonymousUser";

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                currentUser = ((UserDetails)principal).getUsername();
            } else {
                currentUser = principal.toString();
            }
        }

        return currentUser;
    }
}