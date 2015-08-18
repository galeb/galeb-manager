package io.galeb.manager.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import io.galeb.manager.entity.TargetType;

@RepositoryRestResource(collectionResourceRel = "targettype", path = "targettype")
public interface TargetTypeRepository extends PagingAndSortingRepository<TargetType, Long> {

    List<TargetType> findByName(@Param("name") String name);

}
