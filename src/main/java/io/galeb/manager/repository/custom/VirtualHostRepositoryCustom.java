package io.galeb.manager.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.galeb.manager.entity.VirtualHost;

public interface VirtualHostRepositoryCustom {

    Page<VirtualHost> findAll(Pageable pageable);

    Page<VirtualHost> findByName(String name, Pageable pageable);

}
