package io.galeb.manager.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.galeb.manager.entity.Account;

public interface AccountRepositoryCustom {

    Page<Account> findAll(Pageable pageable);

    Page<Account> findByName(String name, Pageable pageable);

}
