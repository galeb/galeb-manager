package io.galeb.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import io.galeb.entity.Account;

@RepositoryRestResource(collectionResourceRel = "account", path = "account")
public interface AccountRepository extends PagingAndSortingRepository<Account, Long> {

    @Query("SELECT a FROM Account a WHERE "
            + "a.id = :id AND "
                + "(1 = ?#{hasRole('ADMIN') ? 1 : 0 } OR "
                + "a.createdBy = ?#{principal.username} OR "
                + "a.name = ?#{principal.username})")
    @Override
    Account findOne(@Param("id") Long id);

    @Query("SELECT a FROM Account a WHERE "
            + "1 = ?#{hasRole('ADMIN') ? 1 : 0 } OR "
            + "a.createdBy = ?#{principal.username} OR "
            + "a.name = ?#{principal.username}")
    @Override
    Iterable<Account> findAll();

    List<Account> findByName(@Param("name") String name);

}
