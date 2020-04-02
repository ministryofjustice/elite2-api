package net.syscon.elite.repository.jpa.repository;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.syscon.elite.repository.jpa.model.CourtEvent;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CourtEventFilter implements Specification<CourtEvent> {

    private Long bookingId;

    private LocalDate fromDate;

    private LocalDate toDate;

    @Override
    public Predicate toPredicate(final Root<CourtEvent> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
        final ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        if (bookingId != null) {
            predicateBuilder.add(cb.equal(root.get("offenderBooking"), bookingId));
        }

        if (fromDate != null) {
            predicateBuilder.add(cb.greaterThanOrEqualTo(root.get("eventDate"), fromDate));
        }

        if (toDate != null) {
            predicateBuilder.add(cb.lessThanOrEqualTo(root.get("eventDate"), toDate));
        }

        return cb.and(predicateBuilder.build().toArray(new Predicate[0]));
    }
}
