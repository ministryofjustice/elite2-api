package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection;

import java.time.LocalDateTime;

public interface ExternalMovementRepository extends PagingAndSortingRepository<ExternalMovement, Long> {

    @Query("select m from ExternalMovement m " +
            "where m.toAgency.id = :agencyId " +
            "and m.activeFlag = :activeFlag " +
            "and m.movementDirection = :direction " +
            "and m.movementTime > :start " +
            "and (:end is null or m.movementTime < :end)"
    )
    Page<ExternalMovement> findMovements(
            @Param("agencyId") String agencyId,
            @Param("activeFlag") ActiveFlag activeFlag,
            @Param("direction") MovementDirection direction,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);
}