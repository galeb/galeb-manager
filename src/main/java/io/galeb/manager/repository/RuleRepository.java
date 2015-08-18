package io.galeb.manager.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import io.galeb.manager.entity.Rule;

@RepositoryRestResource(collectionResourceRel = "rule", path = "rule")
public interface RuleRepository extends PagingAndSortingRepository<Rule, Long> {

    List<Rule> findByName(@Param("name") String name);

}
