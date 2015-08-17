package io.galeb.repository;

import java.util.List;

//import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import io.galeb.entity.Project;

@RepositoryRestResource(collectionResourceRel = "project", path = "project")
public interface ProjectRepository extends PagingAndSortingRepository<Project, Long> {

//    @Query("SELECT p FROM Project p "
//            + "INNER JOIN p.teams t "
//            + "INNER JOIN t.accounts a WHERE a in ?#{principal.username}")
//    @Override
//    Project findOne(Long id);

    List<Project> findByName(@Param("name") String name);

}
