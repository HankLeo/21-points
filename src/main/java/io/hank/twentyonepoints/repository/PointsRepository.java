package io.hank.twentyonepoints.repository;

import io.hank.twentyonepoints.domain.Points;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Points entity.
 */
@Repository
public interface PointsRepository extends JpaRepository<Points, Long> {
    @Query("select points from Points points where points.user.login = ?#{authentication.name}")
    List<Points> findByUserIsCurrentUser();

    default Optional<Points> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Points> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Points> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(value = "select points from Points points left join fetch points.user", countQuery = "select count(points) from Points points")
    Page<Points> findAllWithToOneRelationships(Pageable pageable);

    @Query("select points from Points points left join fetch points.user")
    List<Points> findAllWithToOneRelationships();

    @Query("select points from Points points left join fetch points.user where points.id =:id")
    Optional<Points> findOneWithToOneRelationships(@Param("id") Long id);
}
