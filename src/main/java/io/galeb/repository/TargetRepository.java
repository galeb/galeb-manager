package io.galeb.repository;

import io.galeb.entity.Rule;
import io.galeb.entity.Target;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "target", path = "target")
public interface TargetRepository extends PagingAndSortingRepository<Target, Long> {

    List<Target> findByName(@Param("name") String name);

    List<Target> findByParent(@Param("rule") Rule rule);

}
