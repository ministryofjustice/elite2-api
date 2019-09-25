package net.syscon.elite.repository.impl;

import com.google.common.collect.Lists;
import lombok.val;
import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.adjudications.*;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.AdjudicationsRepository;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.service.AdjudicationSearchCriteria;
import net.syscon.elite.service.support.AdjudicationChargeDto;
import net.syscon.util.DateTimeConverter;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Repository
public class AdjudicationsRepositoryImpl extends RepositoryBase implements AdjudicationsRepository {

    private final StandardBeanPropertyRowMapper<Award> rowMapper = new StandardBeanPropertyRowMapper<>(Award.class);
    private final StandardBeanPropertyRowMapper<Agency> agencyMapper = new StandardBeanPropertyRowMapper<>(Agency.class);
    private final StandardBeanPropertyRowMapper<AdjudicationChargeDto> adjudicationMapper = new StandardBeanPropertyRowMapper<>(AdjudicationChargeDto.class);
    private final StandardBeanPropertyRowMapper<AdjudicationOffence> offenceMapper = new StandardBeanPropertyRowMapper<>(AdjudicationOffence.class);
    private final StandardBeanPropertyRowMapper<AdjudicationDetail> detailMapper = new StandardBeanPropertyRowMapper<>(AdjudicationDetail.class);
    private final StandardBeanPropertyRowMapper<Hearing> hearingMapper = new StandardBeanPropertyRowMapper<>(Hearing.class);
    private final StandardBeanPropertyRowMapper<HearingResult> resultMapper = new StandardBeanPropertyRowMapper<>(HearingResult.class);
    private final StandardBeanPropertyRowMapper<Sanction> sanctionMapper = new StandardBeanPropertyRowMapper<>(Sanction.class);

    @Override
    public List<Award> findAwards(final long bookingId) {
        return jdbcTemplate.query(getQuery("FIND_AWARDS"), createParams("bookingId", bookingId), rowMapper);
    }

    @Override
    public List<AdjudicationOffence> findAdjudicationOffences(final String offenderNumber) {
        return jdbcTemplate.query(getQuery("FIND_ADJUDICATION_OFFENCE_TYPES_FOR_OFFENDER"),
                createParams("offenderNo", offenderNumber),
                offenceMapper);
    }

    @Override
    public List<Agency> findAdjudicationAgencies(final String offenderNumber) {
        return jdbcTemplate.query(getQuery("FIND_ADJUDICATION_AGENCIES_FOR_OFFENDER"),
                createParams("offenderNo", offenderNumber),
                agencyMapper);
    }

    @Override
    public Optional<AdjudicationDetail> findAdjudicationDetails(final String offenderNumber,
                                                                final long adjudicationNumber) {

        val details = jdbcTemplate.query(getQuery("FIND_ADJUDICATION"),
                createParams(
                        "offenderNo", offenderNumber,
                        "adjudicationNo", adjudicationNumber),
                detailMapper);

        return details.stream().map(detail -> populateDetails(adjudicationNumber, detail)).findFirst();
    }

    private AdjudicationDetail populateDetails(final long adjudicationNumber, final AdjudicationDetail detail) {

        val hearings = jdbcTemplate.query(getQuery("FIND_HEARINGS"), createParams("adjudicationNo", adjudicationNumber), hearingMapper);

        val hearingIds = Lists.transform(hearings, Hearing::getOicHearingId);

        val results = getResults(hearingIds);

        val sanctions = getSanctions(hearingIds);

        val populatedHearings = hearings.stream().map(hearing ->
                populateHearing(
                        hearing,
                        results.getOrDefault(hearing.getOicHearingId(), List.of()),
                        sanctions.getOrDefault(hearing.getOicHearingId(), List.of())))
                .collect(toList());

        return detail.toBuilder().hearings(populatedHearings).build();
    }

    private Hearing populateHearing(final Hearing hearing, final List<HearingResult> results, final List<Sanction> sanctions) {

        val sanctionsByResult = sanctions.stream().collect(groupingBy(Sanction::getResultSeq));

        val populatedResults = results.stream().map(result ->
                result.toBuilder()
                        .sanctions(sanctionsByResult.getOrDefault(result.getResultSeq(), List.of()))
                        .build())
                .collect(toList());

        return hearing.toBuilder().results(populatedResults).build();
    }

    private Map<Long, List<Sanction>> getSanctions(List<Long> hearingIds) {
        return hearingIds.isEmpty()
                ? Map.of()
                : jdbcTemplate.query(getQuery("FIND_SANCTIONS"), createParams("hearingIds", hearingIds), sanctionMapper)
                .stream()
                .collect(groupingBy(Sanction::getOicHearingId));
    }

    private Map<Long, List<HearingResult>> getResults(List<Long> hearingIds) {
        return hearingIds.isEmpty()
                ? Map.of()
                : jdbcTemplate.query(getQuery("FIND_RESULTS"), createParams("hearingIds", hearingIds), resultMapper)
                .stream()
                .collect(groupingBy(HearingResult::getOicHearingId));
    }

    @Override
    public Page<Adjudication> findAdjudications(final AdjudicationSearchCriteria criteria) {

        val pageRequest = criteria.getPageRequest();

        val params = createParamSource(pageRequest,
                "offenderNo", criteria.getOffenderNumber(),
                "offenceId", criteria.getOffenceId(),
                "agencyLocationId", criteria.getAgencyId(),
                "startDate", asDate(criteria.getStartDate()),
                "endDate", asDate(criteria.getEndDate()));

        val adjudicationCharges = jdbcTemplate.query(getQuery("FIND_ADJUDICATIONS_FOR_OFFENDER"), params, adjudicationMapper);

        val chargesGroupedByAdjudication = adjudicationCharges.stream()
                .collect(groupingBy(AdjudicationChargeDto::getAdjudicationNumber))
                .values();

        val page = chargesGroupedByAdjudication.stream()
                .map(this::toAdjudication)
                .sorted(comparing(Adjudication::getReportTime).reversed())
                .skip(criteria.getPageRequest().getOffset())
                .limit(criteria.getPageRequest().getLimit())
                .collect(toList());

        return new Page<>(page, chargesGroupedByAdjudication.size(), pageRequest);
    }


    private Adjudication toAdjudication(List<AdjudicationChargeDto> charges) {

        val firstCharge = charges.get(0);
        val convertedCharges = charges.stream().map(this::toCharge).collect(toList());

        return Adjudication.builder()
                .agencyIncidentId(firstCharge.getAgencyIncidentId())
                .partySeq(firstCharge.getPartySeq())
                .agencyId(firstCharge.getAgencyId())
                .adjudicationNumber(firstCharge.getAdjudicationNumber())
                .reportTime(firstCharge.getReportTime())
                .adjudicationCharges(convertedCharges)
                .build();
    }

    private AdjudicationCharge toCharge(final AdjudicationChargeDto charge) {
        return AdjudicationCharge.builder()
                .oicChargeId(charge.getOicChargeId())
                .offenceCode(charge.getOffenceCode())
                .offenceDescription(charge.getOffenceDescription())
                .findingCode(charge.getFindingCode())
                .build();
    }

    private SqlParameterValue asDate(final LocalDate startDate) {
        return new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(startDate));
    }
}
