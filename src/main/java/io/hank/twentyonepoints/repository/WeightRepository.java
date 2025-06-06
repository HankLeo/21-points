package io.hank.twentyonepoints.repository;

import io.hank.twentyonepoints.domain.Weight;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Weight entity.
 */
@Repository
public interface WeightRepository extends JpaRepository<Weight, Long> {
    @Query("select weight from Weight weight where weight.user.login = ?#{authentication.name}")
    List<Weight> findByUserIsCurrentUser();

    default Optional<Weight> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Weight> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Weight> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(value = "select weight from Weight weight left join fetch weight.user", countQuery = "select count(weight) from Weight weight")
    Page<Weight> findAllWithToOneRelationships(Pageable pageable);

    @Query("select weight from Weight weight left join fetch weight.user")
    List<Weight> findAllWithToOneRelationships();

    @Query("select weight from Weight weight left join fetch weight.user where weight.id =:id")
    Optional<Weight> findOneWithToOneRelationships(@Param("id") Long id);
}
