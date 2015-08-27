package io.galeb.manager.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.galeb.manager.entity.Account;

public interface AccountRepositoryCustom {

    Iterable<Account> findAll();

    Page<Account> findAll(Pageable pageable);

    Account findByName(String name);

}
