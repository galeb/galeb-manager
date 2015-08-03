package io.galeb.repository;

import io.galeb.entity.Farm;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "farm", path = "farm")
public interface FarmRepository extends PagingAndSortingRepository<Farm, Long> {

    List<Farm> findByName(@Param("name") String name);

}
