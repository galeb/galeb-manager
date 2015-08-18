package io.galeb.manager.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import io.galeb.manager.entity.RuleType;

@RepositoryRestResource(collectionResourceRel = "ruletype", path = "ruletype")
public interface RuleTypeRepository extends PagingAndSortingRepository<RuleType, Long> {

    List<RuleType> findByName(@Param("name") String name);

}
