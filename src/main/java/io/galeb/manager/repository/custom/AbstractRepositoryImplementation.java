package io.galeb.manager.repository.custom;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.security.CurrentUser;

public abstract class AbstractRepositoryImplementation<T extends AbstractEntity<?>> {

    protected abstract String getFindAllStr();

    protected abstract EntityManager getEntityManager();

    public AbstractRepositoryImplementation() {
        super();
    }

    @SuppressWarnings("unchecked")
    private Iterable<T> findAll() {
        Authentication currentUser = CurrentUser.getCurrentAuth();
        int hasRoleAdmin = currentUser.getAuthorities().contains(
                AuthorityUtils.createAuthorityList("ROLE_ADMIN").get(0)) ? 1 : 0;
        String name = currentUser.getName();

        Query query = getEntityManager().createQuery(getFindAllStr())
                                        .setParameter("hasRoleAdmin", hasRoleAdmin)
                                        .setParameter("principalName", name);

        return query.getResultList();
    }

    public Page<T> findAll(Pageable pageable) {
        final List<T> entity = (List<T>) findAll();
        return new PageImpl<>(entity, pageable, entity.size());
    }

    public Page<T> findByName(String name, Pageable pageable) {
        final List<T> entity = StreamSupport.stream(findAll().spliterator(), false)
                .filter(e -> e.getName().equals(name)).collect(Collectors.toList());
        return new PageImpl<>(entity, pageable, entity.size());
    }

}
