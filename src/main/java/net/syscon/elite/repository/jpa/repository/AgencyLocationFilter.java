package net.syscon.elite.repository.jpa.repository;

import com.google.common.collect.ImmutableList;
import lombok.*;
import net.syscon.elite.repository.jpa.model.ActiveFlag;
import net.syscon.elite.repository.jpa.model.AgencyLocation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.List;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AgencyLocationFilter implements Specification<AgencyLocation>  {
    private String id;
    @Builder.Default
    private ActiveFlag activeFlag = ActiveFlag.Y;
    private String type;
    @Builder.Default
    private List<String> excludedAgencies = List.of("OUT", "TRN");

    @Override
    public Predicate toPredicate(final Root<AgencyLocation> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
        final ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        if (StringUtils.isNotBlank(id)) {
            predicateBuilder.add(cb.equal(root.get("id"), id));
        }

        if (activeFlag != null) {
            predicateBuilder.add(cb.equal(root.get("activeFlag"), activeFlag));
        }

        if (StringUtils.isNotBlank(type)) {
            predicateBuilder.add(cb.equal(root.get("type"), type));
        }

        if (StringUtils.isBlank(id) && excludedAgencies != null && !excludedAgencies.isEmpty()) {
            predicateBuilder.add(cb.not(root.get("id").in(excludedAgencies)));
        }

        final var predicates = predicateBuilder.build();
        return cb.and(predicates.toArray(new Predicate[0]));
    }
}