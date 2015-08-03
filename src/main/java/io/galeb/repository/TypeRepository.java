package io.galeb.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import io.galeb.entity.Type;

@RepositoryRestResource(collectionResourceRel = "type", path = "type")
public interface TypeRepository extends PagingAndSortingRepository<Type, Long> {

    List<Type> findByName(@Param("name") String name);

}
