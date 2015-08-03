package io.galeb.repository;

import io.galeb.entity.Environment;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "environment", path = "environment")
public interface EnvironmentRepository extends PagingAndSortingRepository<Environment, Long> {

    List<Environment> findByName(@Param("name") String name);

}
