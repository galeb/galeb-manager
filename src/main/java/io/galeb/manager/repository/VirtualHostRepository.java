package io.galeb.manager.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import io.galeb.manager.entity.VirtualHost;

@RepositoryRestResource(collectionResourceRel = "virtualhost", path = "virtualhost")
public interface VirtualHostRepository extends PagingAndSortingRepository<VirtualHost, Long> {

    List<VirtualHost> findByName(@Param("name") String name);

}
