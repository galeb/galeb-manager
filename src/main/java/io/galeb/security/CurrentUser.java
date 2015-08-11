package io.galeb.security;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import io.galeb.entity.Account;

public class CurrentUser extends User {

    private static final long serialVersionUID = -403060077273343289L;

    public CurrentUser(Account account) {
        super(account.getEmail(), "password", AuthorityUtils.createAuthorityList(account.getRole().toString()));
    }

}
