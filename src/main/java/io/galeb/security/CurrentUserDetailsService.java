package io.galeb.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import io.galeb.entity.Account;
import io.galeb.repository.AccountRepository;

@Service
public class CurrentUserDetailsService implements UserDetailsService {

    @Autowired
    AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        List<Account> accounts = accountRepository.findByName(userName);
        Account account = null;
        if (!accounts.isEmpty()) {
            account = accounts.stream().findFirst().get();
        } else {
            throw new UsernameNotFoundException("Account "+userName+" NOT FOUND");
        }
        return new CurrentUser(account);
    }

}
