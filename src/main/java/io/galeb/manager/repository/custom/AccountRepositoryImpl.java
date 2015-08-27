package io.galeb.manager.repository.custom;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Repository;

import io.galeb.manager.entity.Account;
import io.galeb.manager.security.CurrentUser;

@Repository
public class AccountRepositoryImpl implements AccountRepositoryCustom {

    @SuppressWarnings("unused")
    private static final Log LOGGER = LogFactory.getLog(AccountRepositoryImpl.class);

    @PersistenceContext
    private EntityManager em;

    @SuppressWarnings("unused")
    private JpaEntityInformation<Account, ?> entityInformation;

    @PostConstruct
    public void init() {
        entityInformation = JpaEntityInformationSupport.getMetadata(Account.class, em);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<Account> findAll() {
        Authentication currentUser = CurrentUser.getCurrentAuth();
        int hasRoleAdmin = currentUser.getAuthorities().contains(
                AuthorityUtils.createAuthorityList("ROLE_ADMIN").get(0)) ? 1 : 0;
        String name = currentUser.getName();

        Query query = em.createQuery("SELECT a FROM Account a WHERE "
                                    + "1 = :hasRoleAdmin OR "
                                    + "a.name = :principalName")
                        .setParameter("hasRoleAdmin", hasRoleAdmin)
                        .setParameter("principalName", name);

        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Page<Account> findAll(Pageable pageable) {
        Authentication currentUser = CurrentUser.getCurrentAuth();
        int hasRoleAdmin = currentUser.getAuthorities().contains(
                AuthorityUtils.createAuthorityList("ROLE_ADMIN").get(0)) ? 1 : 0;
        String name = currentUser.getName();

        Query query = em.createQuery("SELECT a FROM Account a WHERE "
                                    + "1 = :hasRoleAdmin OR "
                                    + "a.name = :principalName")
                        .setParameter("hasRoleAdmin", hasRoleAdmin)
                        .setParameter("principalName", name);

        List<Account> result = query.getResultList();
        Page<Account> page = new PageImpl<Account>(result, pageable, result.size());
        return page;
    }

    @Override
    public Account findByName(String name) {
        Optional<Account> anAccount = StreamSupport.stream(findAll().spliterator(), false)
                .filter(account -> account.getName().equals(name)).findFirst();
        return anAccount.orElse(null);
    }

}
