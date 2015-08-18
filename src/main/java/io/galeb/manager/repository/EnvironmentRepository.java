package io.galeb.manager.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import io.galeb.manager.entity.Environment;

@RepositoryRestResource(collectionResourceRel = "environment", path = "environment")
public interface EnvironmentRepository extends PagingAndSortingRepository<Environment, Long> {

    List<Environment> findByName(@Param("name") String name);

}
