package io.galeb.security;

import java.util.EnumSet;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import io.galeb.entity.Account;

public class CurrentUser extends User {

    private static final long serialVersionUID = -403060077273343289L;

    private static String[] allRoles = EnumSet.allOf(Account.Role.class).stream()
            .map(c -> c.toString()).collect(
                    Collectors.toList()).toArray(new String[EnumSet.allOf(Account.Role.class).size()-1]);

    public CurrentUser(Account account) {
        super(account.getName(),
              "password",
              AuthorityUtils.createAuthorityList(allRoles)
              );
    }

}
