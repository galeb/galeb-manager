package io.galeb.manager.repository.custom;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.security.CurrentUser;

@NoRepositoryBean
public abstract class AbstractRepositoryCustom<T extends AbstractEntity<?>> {

    protected abstract String getFindAllStr();

    protected abstract EntityManager getEntityManager();

    @SuppressWarnings("unchecked")
    public Iterable<T> findAll() {
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
        return new PageImpl<T>(entity, pageable, entity.size());
    }

    public T findByName(String name) {
        Optional<T> anEntity = StreamSupport.stream(findAll().spliterator(), false)
                .filter(entity -> entity.getName().equals(name)).findFirst();
        return anEntity.orElse(null);
    }

}
