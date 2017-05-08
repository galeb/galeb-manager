package io.galeb.manager.controller;

import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.galeb.manager.entity.Account;
import io.galeb.manager.repository.AccountRepository;
import io.galeb.manager.security.user.CurrentUser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class TokenController {

    private static final Log LOGGER = LogFactory.getLog(TokenController.class);
    private static final Pageable ALL_PAGE = new PageRequest(0, Integer.MAX_VALUE);

    private final ObjectMapper mapper = new ObjectMapper();

    private AccountRepository accountRepository;

    @RequestMapping(value = "/token", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isFullyAuthenticated()")
    public String token(HttpSession session) {

        Authentication currentUser = CurrentUser.getCurrentAuth();
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("token", session.getId());
        String loginName = currentUser.getName();
        tokenInfo.put("account", loginName);
        Page<Account> accountPage = accountRepository.findByName(loginName, ALL_PAGE);
        if (accountPage != null && accountPage.hasContent()) {
            final Account account = accountPage.getContent().get(0);
            tokenInfo.put("email", account.getEmail());
            tokenInfo.put("hasTeam", !account.getTeams().isEmpty());
        }
        Set<String> roles = currentUser.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        tokenInfo.put("admin", roles.contains("ROLE_ADMIN"));
        String json = "{}";
        try {
            json = mapper.writeValueAsString(tokenInfo);
        } catch (JsonProcessingException e) {
            LOGGER.error(e);
        }

        return json;
    }

    @Autowired
    public void setAccountRepository(final AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

}
