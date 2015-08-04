package io.galeb.repository;

import io.galeb.entity.VirtualHost;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "virtualhost", path = "virtualhost")
public interface VirtualHostRepository extends PagingAndSortingRepository<VirtualHost, Long> {

    List<VirtualHost> findByName(@Param("name") String name);

}
