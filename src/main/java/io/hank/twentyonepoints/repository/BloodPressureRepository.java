package io.hank.twentyonepoints.repository;

import io.hank.twentyonepoints.domain.BloodPressure;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the BloodPressure entity.
 */
@Repository
public interface BloodPressureRepository extends JpaRepository<BloodPressure, Long> {
    @Query("select bloodPressure from BloodPressure bloodPressure where bloodPressure.user.login = ?#{authentication.name}")
    List<BloodPressure> findByUserIsCurrentUser();

    default Optional<BloodPressure> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<BloodPressure> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<BloodPressure> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select bloodPressure from BloodPressure bloodPressure left join fetch bloodPressure.user",
        countQuery = "select count(bloodPressure) from BloodPressure bloodPressure"
    )
    Page<BloodPressure> findAllWithToOneRelationships(Pageable pageable);

    @Query("select bloodPressure from BloodPressure bloodPressure left join fetch bloodPressure.user")
    List<BloodPressure> findAllWithToOneRelationships();

    @Query("select bloodPressure from BloodPressure bloodPressure left join fetch bloodPressure.user where bloodPressure.id =:id")
    Optional<BloodPressure> findOneWithToOneRelationships(@Param("id") Long id);
}
