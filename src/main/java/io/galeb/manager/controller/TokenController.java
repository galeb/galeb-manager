package io.galeb.manager.controller;

//import static io.galeb.core.util.consistenthash.HashAlgorithm.HashType.MD5;

import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
//import io.galeb.core.util.consistenthash.HashAlgorithm;
import io.galeb.manager.security.user.CurrentUser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private final ObjectMapper mapper = new ObjectMapper();

    @RequestMapping(value = "/token", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isFullyAuthenticated()")
    public String token(HttpSession session) {

        Authentication currentUser = CurrentUser.getCurrentAuth();

        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("token", session.getId());
        tokenInfo.put("account", currentUser.getName());
        Set<String> roles = currentUser.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        tokenInfo.put("admin", roles.contains("ROLE_ADMIN"));
//        tokenInfo.put("gravatar_hash", new HashAlgorithm(MD5).hash(currentUser.getEmail()).asString());
        String json = "{}";
        try {
            json = mapper.writeValueAsString(tokenInfo);
        } catch (JsonProcessingException e) {
            LOGGER.error(e);
        }

        return json;
    }

}
