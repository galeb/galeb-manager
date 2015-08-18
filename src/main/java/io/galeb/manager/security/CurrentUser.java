package io.galeb.manager.security;

import java.util.stream.Collectors;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import io.galeb.manager.entity.Account;

public class CurrentUser extends User {

    private static final long serialVersionUID = -403060077273343289L;

    public CurrentUser(Account account) {
        super(account.getName(),
              "password",
              AuthorityUtils.createAuthorityList(account.getRoles().stream()
                      .map(role -> role.toString()).collect(Collectors.toList())
                      .toArray(new String[account.getRoles().size()-1]))
              );
    }

}
