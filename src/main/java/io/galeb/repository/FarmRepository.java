package io.galeb.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import io.galeb.entity.AbstractEntity.EntityStatus;
import io.galeb.entity.Environment;
import io.galeb.entity.Farm;

@RepositoryRestResource(collectionResourceRel = "farm", path = "farm")
public interface FarmRepository extends PagingAndSortingRepository<Farm, Long> {

    List<Farm> findByName(@Param("name") String name);

    List<Farm> findById(@Param("id") long id);

    List<Farm> findByEnvironmentAndStatus(@Param("environment") Environment environment,
                                          @Param("status") EntityStatus status);

}
