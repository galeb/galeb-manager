package io.galeb.manager.security.user;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.galeb.manager.security.user.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapUserDetails;

public class CustomLdapUserDetails extends CurrentUser {

    private static final long serialVersionUID = 642027546835387428L;

    private final Collection<GrantedAuthority> localAuthorities;

    private final LdapUserDetails details;

    public CustomLdapUserDetails(final LdapUserDetails details,
                                 final Collection<GrantedAuthority> localAuthorities,
                                 final long id, final String email) {
        super(details.getUsername(),
              details.getPassword(),
              details.isEnabled(),
              details.isAccountNonExpired(),
              details.isCredentialsNonExpired(),
              details.isAccountNonLocked(),
              details.getAuthorities(),
              id,
              email);
        this.details = details;
        this.localAuthorities = localAuthorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<? extends GrantedAuthority> ldapAuthorities = details.getAuthorities();
        Set<GrantedAuthority> authorities = new HashSet<>(ldapAuthorities);
        authorities.addAll(localAuthorities);
        return authorities;
    }

    @Override
    public String getPassword() {
        return details.getPassword();
    }

    @Override
    public String getUsername() {
        return details.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return details.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return details.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return details.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return details.isEnabled();
    }

}
