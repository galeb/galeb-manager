package io.galeb.manager.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import io.galeb.manager.entity.Provider;

@RepositoryRestResource(collectionResourceRel = "provider", path = "provider")
public interface ProviderRepository extends PagingAndSortingRepository<Provider, Long> {

    List<Provider> findByName(@Param("name") String name);

}
