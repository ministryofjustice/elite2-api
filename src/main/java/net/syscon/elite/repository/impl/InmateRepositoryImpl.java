package net.syscon.elite.repository.impl;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.AssessmentStatusType;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.InmateRepository;
import net.syscon.elite.repository.OffenderBookingSearchRequest;
import net.syscon.elite.repository.mapping.FieldMapper;
import net.syscon.elite.repository.mapping.PageAwareRowMapper;
import net.syscon.elite.repository.mapping.Row2BeanRowMapper;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.support.AssessmentDto;
import net.syscon.elite.service.support.InmateDto;
import net.syscon.elite.service.support.Language;
import net.syscon.util.DateTimeConverter;
import net.syscon.util.IQueryBuilder;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;

import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static net.syscon.elite.repository.ImageRepository.IMAGE_DETAIL_MAPPER;

@Repository
@Slf4j
public class InmateRepositoryImpl extends RepositoryBase implements InmateRepository {

    private static final Map<String, FieldMapper> OFFENDER_BOOKING_MAPPING = new ImmutableMap.Builder<String, FieldMapper>()
            .put("OFFENDER_BOOK_ID", new FieldMapper("bookingId"))
            .put("BOOKING_NO", new FieldMapper("bookingNo"))
            .put("OFFENDER_ID_DISPLAY", new FieldMapper("offenderNo"))
            .put("AGY_LOC_ID", new FieldMapper("agencyId"))
            .put("FIRST_NAME", new FieldMapper("firstName", null, null, StringUtils::upperCase))
            .put("MIDDLE_NAME", new FieldMapper("middleName", null, null, StringUtils::upperCase))
            .put("LAST_NAME", new FieldMapper("lastName", null, null, StringUtils::upperCase))
            .put("BIRTH_DATE", new FieldMapper("dateOfBirth", DateTimeConverter::toISO8601LocalDate))
            .put("ALERT_TYPES", new FieldMapper("alertsCodes", value -> Arrays.asList(value.toString().split(","))))
            .put("ALIASES", new FieldMapper("aliases", value -> Arrays.asList(value.toString().split(","))))
            .put("FACE_IMAGE_ID", new FieldMapper("facialImageId"))
            .put("LIVING_UNIT_ID", new FieldMapper("assignedLivingUnitId"))
            .put("LIVING_UNIT_DESC", new FieldMapper("assignedLivingUnitDesc", value -> RegExUtils.replaceFirst((String) value, "^[A-Z|a-z|0-9]+\\-", "")))
            .put("ASSIGNED_OFFICER_ID", new FieldMapper("assignedOfficerId"))
            .put("BAND_CODE", new FieldMapper("bandCode"))
            .put("IMPRISONMENT_STATUS", new FieldMapper("imprisonmentStatus"))
            .build();

    private final Map<String, FieldMapper> inmateDetailsMapping = new ImmutableMap.Builder<String, FieldMapper>()
            .put("OFFENDER_BOOK_ID", new FieldMapper("bookingId"))
            .put("BOOKING_NO", new FieldMapper("bookingNo"))
            .put("OFFENDER_ID_DISPLAY", new FieldMapper("offenderNo"))
            .put("FIRST_NAME", new FieldMapper("firstName"))
            .put("MIDDLE_NAME", new FieldMapper("middleName"))
            .put("LAST_NAME", new FieldMapper("lastName"))
            .put("AGY_LOC_ID", new FieldMapper("agencyId"))
            .put("LIVING_UNIT_ID", new FieldMapper("assignedLivingUnitId"))
            .put("RELIGION", new FieldMapper("religion")) // deprecated, please remove
            .put("FACE_IMAGE_ID", new FieldMapper("facialImageId"))
            .put("BIRTH_DATE", new FieldMapper("dateOfBirth", DateTimeConverter::toISO8601LocalDate))
            .put("ASSIGNED_OFFICER_ID", new FieldMapper("assignedOfficerId"))
            .put("ACTIVE_FLAG", new FieldMapper("activeFlag", value -> "Y".equalsIgnoreCase(value.toString())))
            .build();

    private final Map<String, FieldMapper> physicalAttributesMapping = new ImmutableMap.Builder<String, FieldMapper>()
            .put("GENDER", new FieldMapper("gender"))
            .put("ETHNICITY", new FieldMapper("ethnicity"))
            .put("RACE_CODE", new FieldMapper("raceCode"))
            .put("HEIGHT_FT", new FieldMapper("heightFeet"))
            .put("HEIGHT_IN", new FieldMapper("heightInches"))
            .put("HEIGHT_CM", new FieldMapper("heightCentimetres"))
            .put("WEIGHT_LBS", new FieldMapper("weightPounds"))
            .put("WEIGHT_KG", new FieldMapper("weightKilograms"))
            .build();


    private final Map<String, FieldMapper> assignedLivingUnitMapping = new ImmutableMap.Builder<String, FieldMapper>()
            .put("AGY_LOC_ID", new FieldMapper("agencyId"))
            .put("LIVING_UNIT_ID", new FieldMapper("locationId"))
            .put("LIVING_UNIT_DESCRIPTION", new FieldMapper("description", value -> RegExUtils.replaceFirst((String) value, "^[A-Z|a-z|0-9]+\\-", "")))
            .put("AGENCY_NAME", new FieldMapper("agencyName"))
            .build();

    private final Map<String, FieldMapper> physicalMarkMapping = new ImmutableMap.Builder<String, FieldMapper>()
            .put("COMMENT_TEXT", new FieldMapper("comment"))
            .build();

    private static final StandardBeanPropertyRowMapper<PersonalCareNeed> PERSONAL_CARE_NEEDS_MAPPER = new StandardBeanPropertyRowMapper<>(PersonalCareNeed.class);
    private static final StandardBeanPropertyRowMapper<ReasonableAdjustment> REASONABLE_ADJUSTMENTS_MAPPER = new StandardBeanPropertyRowMapper<>(ReasonableAdjustment.class);

    private static final StandardBeanPropertyRowMapper<AssessmentDto> ASSESSMENT_MAPPER = new StandardBeanPropertyRowMapper<>(AssessmentDto.class);
    private static final StandardBeanPropertyRowMapper<PhysicalCharacteristic> PHYSICAL_CHARACTERISTIC_MAPPER = new StandardBeanPropertyRowMapper<>(PhysicalCharacteristic.class);
    private static final StandardBeanPropertyRowMapper<InmateDto> INMATE_MAPPER = new StandardBeanPropertyRowMapper<>(InmateDto.class);
    private static final StandardBeanPropertyRowMapper<ProfileInformation> PROFILE_INFORMATION_MAPPER = new StandardBeanPropertyRowMapper<>(ProfileInformation.class);
    private static final StandardBeanPropertyRowMapper<Language> LANGUAGE_MAPPER = new StandardBeanPropertyRowMapper<>(Language.class);
    private static final StandardBeanPropertyRowMapper<OffenderIdentifier> OFFENDER_IDENTIFIER_MAPPER = new StandardBeanPropertyRowMapper<>(OffenderIdentifier.class);
    private static final StandardBeanPropertyRowMapper<OffenderCategorise> OFFENDER_CATEGORY_MAPPER = new StandardBeanPropertyRowMapper<>(OffenderCategorise.class);

    private static final StandardBeanPropertyRowMapper<PrisonerDetail> PRISONER_DETAIL_MAPPER = new StandardBeanPropertyRowMapper<>(PrisonerDetail.class);

    private static final StandardBeanPropertyRowMapper<InmateBasicDetails> OFFENDER_BASIC_DETAILS_MAPPER = new StandardBeanPropertyRowMapper<>(InmateBasicDetails.class);

    private static final StandardBeanPropertyRowMapper<ImprisonmentStatus> IMPRISONMENT_STATUS_MAPPER = new StandardBeanPropertyRowMapper<>(ImprisonmentStatus.class);

    private final Map<String, FieldMapper> PRISONER_DETAIL_WITH_OFFENDER_ID_FIELD_MAP;

    private final Map<String, FieldMapper> aliasMapping = new ImmutableMap.Builder<String, FieldMapper>()
            .put("LAST_NAME", new FieldMapper("lastName"))
            .put("FIRST_NAME", new FieldMapper("firstName"))
            .put("MIDDLE_NAME", new FieldMapper("middleName"))
            .put("BIRTH_DATE", new FieldMapper("dob", DateTimeConverter::toISO8601LocalDate))
            .put("SEX", new FieldMapper("gender"))
            .put("ETHNICITY", new FieldMapper("ethnicity"))
            .put("ALIAS_TYPE", new FieldMapper("nameType"))
            .put("CREATE_DATE", new FieldMapper("createDate", DateTimeConverter::toISO8601LocalDate))
            .build();

    private static final Set<String> UNSENTENCED_OR_UNCLASSIFIED_CATEGORY_CODES = Set.of("U", "X", "Z");

    InmateRepositoryImpl() {
        final Map<String, FieldMapper> map = new HashMap<>(PRISONER_DETAIL_MAPPER.getFieldMap());
        map.put("OFFENDER_ID", new FieldMapper("OFFENDER_ID"));
        PRISONER_DETAIL_WITH_OFFENDER_ID_FIELD_MAP = map;
    }

    @Override
    public Page<OffenderBooking> findInmatesByLocation(final Long locationId, final String locationTypeRoot, final String caseLoadId, final String query, final String orderByField, final Order order, final long offset, final long limit) {
        final var initialSql = getQuery("FIND_INMATES_BY_LOCATION");
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, OFFENDER_BOOKING_MAPPING);

        final var sql = builder
                .addRowCount()
                .addQuery(query)
                .addOrderBy(order, orderByField)
                .addPagination()
                .build();

        final var assignedInmateRowMapper =
                Row2BeanRowMapper.makeMapping(sql, OffenderBooking.class, OFFENDER_BOOKING_MAPPING);

        final var paRowMapper = new PageAwareRowMapper<>(assignedInmateRowMapper);

        final var results = jdbcTemplate.query(
                sql,
                createParams("locationId", locationId,
                        "locationTypeRoot", locationTypeRoot,
                        "caseLoadId", caseLoadId,
                        "offset", offset,
                        "limit", limit),
                paRowMapper);

        results.forEach(b -> b.setAge(DateTimeConverter.getAge(b.getDateOfBirth())));

        return new Page<>(results, paRowMapper.getTotalRecords(), offset, limit);
    }

    @Override
    public List<InmateDto> findInmatesByLocation(final String agencyId, final List<Long> locations, final Set<String> caseLoadIds) {
        return jdbcTemplate.query(getQuery("FIND_INMATES_OF_LOCATION_LIST"),
                createParams("agencyId", agencyId, "locations", locations, "caseLoadIds", caseLoadIds), INMATE_MAPPER);
    }

    @Override
    public Page<OffenderBooking> findAllInmates(final Set<String> caseloads, final String locationTypeRoot, final String query, final PageRequest pageRequest) {
        var initialSql = getQuery("FIND_ALL_INMATES");
        if (!caseloads.isEmpty()) {
            initialSql += " AND " + getQuery("CASELOAD_FILTER");
        }
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, OFFENDER_BOOKING_MAPPING);

        final var sql = builder
                .addRowCount()
                .addQuery(query)
                .addOrderBy(pageRequest.getOrder(), pageRequest.getOrderBy())
                .addPagination()
                .build();

        final var assignedInmateRowMapper =
                Row2BeanRowMapper.makeMapping(sql, OffenderBooking.class, OFFENDER_BOOKING_MAPPING);

        final var paRowMapper = new PageAwareRowMapper<>(assignedInmateRowMapper);

        final var inmates = jdbcTemplate.query(
                sql,
                createParams("caseLoadId", caseloads,
                        "locationTypeRoot", locationTypeRoot,
                        "offset", pageRequest.getOffset(),
                        "limit", pageRequest.getLimit()),
                paRowMapper);

        inmates.forEach(b -> b.setAge(DateTimeConverter.getAge(b.getDateOfBirth())));

        return new Page<>(inmates, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }

    @Override
    @Cacheable("searchForOffenderBookings")
    public Page<OffenderBooking> searchForOffenderBookings(final OffenderBookingSearchRequest request) {
        var initialSql = getQuery("FIND_ALL_INMATES");
        initialSql += " AND " + getQuery("LOCATION_FILTER_SQL");

        if (!request.getCaseloads().isEmpty()) {
            initialSql += " AND " + getQuery("CASELOAD_FILTER");
        }

        if (StringUtils.isNotBlank(request.getOffenderNo())) {
            initialSql += " AND O.OFFENDER_ID_DISPLAY = :offenderNo ";
        }

        if (StringUtils.isNotBlank(request.getSearchTerm1()) && StringUtils.isNotBlank(request.getSearchTerm2())) {
            initialSql += " AND ((O.LAST_NAME like :searchTerm1 and O.FIRST_NAME like :searchTerm2) " +
                    "OR (O.FIRST_NAME like :searchTerm1 and O.LAST_NAME like :searchTerm2) " +
                    "OR (O.FIRST_NAME like :searchTermCombined) " +
                    "OR (O.LAST_NAME like :searchTermCombined)) ";
        } else if (StringUtils.isNotBlank(request.getSearchTerm1())) {
            initialSql += " AND (O.FIRST_NAME like :searchTerm1 OR O.LAST_NAME like :searchTerm1) ";
        } else if (StringUtils.isNotBlank(request.getSearchTerm2())) {
            initialSql += " AND (O.FIRST_NAME like :searchTerm2 OR O.LAST_NAME like :searchTerm2) ";
        }

        if (request.getAlerts() != null && !request.getAlerts().isEmpty()) {
            initialSql += " AND " + getQuery("ALERT_FILTER");
        }

        // Search by specific convictedStatus (Convicted is any sentence with a bandCode <=8, Remand is any with a bandCode > 8)

        if (request.getConvictedStatus() != null && !StringUtils.equalsIgnoreCase(request.getConvictedStatus(), "all")) {
            if (StringUtils.equalsIgnoreCase(request.getConvictedStatus(), "convicted")) {
                initialSql += " AND (CAST(IST.BAND_CODE AS int) <= 8 OR CAST(IST.BAND_CODE AS int) = 11) ";
            } else if (StringUtils.equalsIgnoreCase(request.getConvictedStatus(), "remand")) {
                initialSql += " AND ((CAST(IST.BAND_CODE AS int) > 8 AND CAST(IST.BAND_CODE AS int) < 11) OR CAST(IST.BAND_CODE AS int) > 11)";
            } else {
                log.info("Ignoring unrecognised value requested for convictionStatus [" + request.getConvictedStatus() + "]");
            }
        }

        if (request.getFromDob() != null || request.getToDob() != null) {
            if (request.getFromDob() != null) {
                initialSql += " AND O.BIRTH_DATE >= :fromDob ";
            }
            if (request.getToDob() != null) {
                initialSql += " AND O.BIRTH_DATE <= :toDob ";
            }
        }


        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, OFFENDER_BOOKING_MAPPING);

        final var sql = builder
                .addRowCount()
                .addOrderBy(request.getPageRequest().getOrder(), request.getPageRequest().getOrderBy())
                .addPagination()
                .build();

        final var offenderBookingRowMapper =
                Row2BeanRowMapper.makeMapping(sql, OffenderBooking.class, OFFENDER_BOOKING_MAPPING);

        final var paRowMapper = new PageAwareRowMapper<>(offenderBookingRowMapper);

        final var trimmedSearch1 = StringUtils.trimToEmpty(request.getSearchTerm1());
        final var trimmedSearch2 = StringUtils.trimToEmpty(request.getSearchTerm2());
        final var offenderBookings = jdbcTemplate.query(
                sql,
                createParams(
                        "offenderNo", request.getOffenderNo(),
                        "searchTerm1", trimmedSearch1 + "%",
                        "searchTerm2", trimmedSearch2 + "%",
                        "searchTermCombined", trimmedSearch1 + "%" + trimmedSearch2 + "%",
                        "locationPrefix", StringUtils.trimToEmpty(request.getLocationPrefix()) + "-%",
                        "caseLoadId", request.getCaseloads(),
                        "fromDob", request.getFromDob(),
                        "toDob", request.getToDob(),
                        "alerts", request.getAlerts(),
                        "offset", request.getPageRequest().getOffset(),
                        "limit", request.getPageRequest().getLimit()),
                paRowMapper);

        offenderBookings.forEach(b -> b.setAge(DateTimeConverter.getAge(b.getDateOfBirth())));

        return new Page<>(offenderBookings, paRowMapper.getTotalRecords(), request.getPageRequest().getOffset(), request.getPageRequest().getLimit());
    }


    @Override
    public List<Long> getPersonalOfficerBookings(final long staffId) {
        return jdbcTemplate.queryForList(
                getQuery("FIND_PERSONAL_OFFICER_BOOKINGS"),
                createParams("staffId", staffId),
                Long.class);
    }

    @Override
    public Page<PrisonerDetail> findOffenders(final String query, final PageRequest pageRequest) {
        final var initialSql = getQuery("FIND_OFFENDERS");
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, PRISONER_DETAIL_MAPPER.getFieldMap());
        return getPrisonerDetailPage(query, pageRequest, builder);
    }

    @Override
    public Page<PrisonerDetail> findOffendersWithAliases(final String query, final PageRequest pageRequest) {
        final var initialSql = getQuery("FIND_OFFENDERS_WITH_ALIASES");
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, PRISONER_DETAIL_WITH_OFFENDER_ID_FIELD_MAP);

        return getPrisonerDetailPage(
                query,
                new PageRequest(
                        pageRequest.getOrderBy() + ",OFFENDER_ID",
                        pageRequest.getOrder(),
                        pageRequest.getOffset(),
                        pageRequest.getLimit()
                ),
                builder);
    }

    private Page<PrisonerDetail> getPrisonerDetailPage(final String query, final PageRequest pageRequest, final IQueryBuilder builder) {
        final var sql = builder
                .addQuery(query)
                .addRowCount()
                .addPagination()
                .addOrderBy(pageRequest.getOrder(), pageRequest.getOrderBy())
                .build();

        final var paRowMapper = new PageAwareRowMapper<>(PRISONER_DETAIL_MAPPER);

        final var params = createParams("offset", pageRequest.getOffset(), "limit", pageRequest.getLimit());

        final var prisonerDetails = jdbcTemplate.query(sql, params, paRowMapper);

        return new Page<>(prisonerDetails, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }

    @Override
    @Cacheable("bookingPhysicalMarks")
    public List<PhysicalMark> findPhysicalMarks(final long bookingId) {
        final var sql = getQuery("FIND_PHYSICAL_MARKS_BY_BOOKING");

        final var physicalMarkRowMapper =
                Row2BeanRowMapper.makeMapping(sql, PhysicalMark.class, physicalMarkMapping);

        return jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId),
                physicalMarkRowMapper);
    }

    @Override
    @Cacheable("bookingPersonalCareNeeds")
    public List<PersonalCareNeed> findPersonalCareNeeds(final long bookingId, final Set<String> problemCodes) {
        final var sql = getQuery("FIND_PERSONAL_CARE_NEEDS_BY_BOOKING");

        return jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId, "problemCodes", problemCodes),
                PERSONAL_CARE_NEEDS_MAPPER);
    }

    @Override
    public List<PersonalCareNeed> findPersonalCareNeeds(final List<String> offenderNos, final Set<String> problemCodes) {
        final var sql = getQuery("FIND_PERSONAL_CARE_NEEDS_BY_OFFENDER");

        return jdbcTemplate.query(
                sql,
                createParams("offenderNos", offenderNos, "problemCodes", problemCodes),
                PERSONAL_CARE_NEEDS_MAPPER);
    }

    @Override
    @Cacheable("bookingReasonableAdjustments")
    public List<ReasonableAdjustment> findReasonableAdjustments(final long bookingId, final List<String> treatmentCodes) {
        final var sql = getQuery("FIND_REASONABLE_ADJUSTMENTS_BY_BOOKING");

        return jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId, "treatmentCodes", treatmentCodes),
                REASONABLE_ADJUSTMENTS_MAPPER);
    }

    @Override
    @Cacheable("bookingPhysicalCharacteristics")
    public List<PhysicalCharacteristic> findPhysicalCharacteristics(final long bookingId) {
        final var sql = getQuery("FIND_PHYSICAL_CHARACTERISTICS_BY_BOOKING");

        return jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId),
                PHYSICAL_CHARACTERISTIC_MAPPER);
    }

    @Override
    @Cacheable("bookingProfileInformation")
    public List<ProfileInformation> getProfileInformation(final long bookingId) {
        final var sql = getQuery("FIND_PROFILE_INFORMATION_BY_BOOKING");

        return jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId),
                PROFILE_INFORMATION_MAPPER);
    }

    @Override
    @Cacheable("bookingLanguages")
    public List<Language> getLanguages(final long bookingId) {
        return jdbcTemplate.query(
                getQuery("FIND_LANGUAGES_BY_BOOKING"),
                createParams("bookingId", bookingId),
                LANGUAGE_MAPPER
        );
    }

    @Override
    public Optional<ImageDetail> getMainBookingImage(final long bookingId) {
        final var sql = getQuery("GET_IMAGE_DATA_FOR_BOOKING");
        ImageDetail imageDetail;
        try {
            imageDetail = jdbcTemplate.queryForObject(sql,
                    createParams("bookingId", bookingId),
                    IMAGE_DETAIL_MAPPER);
        } catch (final EmptyResultDataAccessException e) {
            imageDetail = null;
        }
        return Optional.ofNullable(imageDetail);
    }

    @Override
    @Cacheable("offenderIdentifiers")
    public List<OffenderIdentifier> getOffenderIdentifiers(final long bookingId) {
        final var sql = getQuery("GET_OFFENDER_IDENTIFIERS_BY_BOOKING");

        return jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId),
                OFFENDER_IDENTIFIER_MAPPER);
    }

    @Override
    public List<OffenderIdentifier> getOffenderIdentifiersByTypeAndValue(final String identifierType, final String identifierValue) {
        final var sql = getQuery("FIND_IDENTIFIER_RECORDS_BY_TYPE_AND_VALUE");

        return jdbcTemplate.query(
                sql,
                createParams("identifierType", identifierType, "identifierValue", identifierValue),
                OFFENDER_IDENTIFIER_MAPPER);
    }

    @Override
    @Cacheable("bookingPhysicalAttributes")
    public Optional<PhysicalAttributes> findPhysicalAttributes(final long bookingId) {
        final var sql = getQuery("FIND_PHYSICAL_ATTRIBUTES_BY_BOOKING");

        final var physicalAttributesRowMapper =
                Row2BeanRowMapper.makeMapping(sql, PhysicalAttributes.class, physicalAttributesMapping);

        PhysicalAttributes physicalAttributes;
        try {
            physicalAttributes = jdbcTemplate.queryForObject(
                    sql,
                    createParams("bookingId", bookingId),
                    physicalAttributesRowMapper);
        } catch (final EmptyResultDataAccessException e) {
            physicalAttributes = null;
        }
        return Optional.ofNullable(physicalAttributes);
    }

    @Override
    @Cacheable("bookingAssessments")
    public List<AssessmentDto> findAssessments(final List<Long> bookingIds, final String assessmentCode, final Set<String> caseLoadId) {
        var initialSql = getQuery("FIND_ACTIVE_APPROVED_ASSESSMENT");
        if (!caseLoadId.isEmpty()) {
            initialSql += " AND " + getQuery("ASSESSMENT_CASELOAD_FILTER");
        }
        return doFindAssessments(bookingIds, assessmentCode, caseLoadId, initialSql, "bookingIds");
    }

    @Override
    @Cacheable("offenderAssessments")
    public List<AssessmentDto> findAssessmentsByOffenderNo(final List<String> offenderNos, final String assessmentCode, final Set<String> caseLoadId, final boolean latestOnly, boolean activeOnly) {
        var initialSql = getQuery("FIND_APPROVED_ASSESSMENT_BY_OFFENDER_NO");
        if (!caseLoadId.isEmpty()) {
            initialSql += " AND " + getQuery("ASSESSMENT_CASELOAD_FILTER");
        }
        if (latestOnly) {
            initialSql += " AND OB.BOOKING_SEQ = 1";
        }
        if (activeOnly) {
            initialSql += " AND OFF_ASS.ASSESS_STATUS = 'A'";
        }
        return doFindAssessments(offenderNos, assessmentCode, caseLoadId, initialSql, "offenderNos");
    }

    private List<AssessmentDto> doFindAssessments(final List<?> ids, final String assessmentCode,
                                                  final Set<String> caseLoadId, final String initialSql, final String idParam) {
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, ASSESSMENT_MAPPER.getFieldMap());

        final var sql = builder
                .addOrderBy(Order.ASC, "bookingId")
                // ensure CSRA is the first:
                .addOrderBy(Order.DESC, "cellSharingAlertFlag,assessmentDate,assessmentSeq")
                .build();

        final var params = createParams(
                idParam, ids,
                "assessmentCode", assessmentCode,
                "caseLoadId", caseLoadId);

        return jdbcTemplate.query(sql, params, ASSESSMENT_MAPPER);
    }

    @Override
    public List<OffenderCategorise> getUncategorised(final String agencyId) {
        final var rawData = jdbcTemplate.query(
                getQuery("GET_UNCATEGORISED"),
                createParams("agencyId", agencyId, "assessmentId", getCategoryAssessmentTypeId()),
                OFFENDER_CATEGORY_MAPPER);

        return applyCategorisationRestrictions(rawData);
    }

    @Override
    public List<OffenderCategorise> getApprovedCategorised(final String agencyId, final LocalDate cutoffDate) {
        final var rawData = jdbcTemplate.query(
                getQuery("GET_APPROVED_CATEGORISED"),
                createParams("agencyId", agencyId,
                        "cutOffDate", DateTimeConverter.toDate(cutoffDate),
                        "assessStatus", "A",
                        "assessmentId", getCategoryAssessmentTypeId()),
                OFFENDER_CATEGORY_MAPPER);

        return removeEarlierCategorisations(rawData);
    }

    @Override
    public List<OffenderCategorise> getRecategorise(final String agencyId, final LocalDate cutoffDate) {
        final var rawData = jdbcTemplate.query(
                getQuery("GET_RECATEGORISE"),
                createParams("agencyId", agencyId,
                        "assessStatus", Set.of("A", "P"),
                        "assessmentId", getCategoryAssessmentTypeId()),
                OFFENDER_CATEGORY_MAPPER);

        return applyCutoffDateForActiveCategorisations(
                removeNonStandardCategoryRecords(removeEarlierCategorisations(rawData)),
                cutoffDate);
    }

    @Override
    public List<OffenderCategorise> getOffenderCategorisations(final List<Long> bookingIds, final String agencyId, final boolean latestOnly) {
        final var rawData = jdbcTemplate.query(
                getQuery("GET_OFFENDER_CATEGORISATIONS"),
                createParams("bookingIds", bookingIds,
                        "agencyId", agencyId,
                        "assessmentId", getCategoryAssessmentTypeId()),
                OFFENDER_CATEGORY_MAPPER);

        return latestOnly ? removeEarlierCategorisations(rawData) : rawData;
    }

    private Long getCategoryAssessmentTypeId() {
        return jdbcTemplate.queryForObject(getQuery("GET_CATEGORY_ASSESSMENT_ID"), Map.of(), Long.class);
    }

    private List<OffenderCategorise> applyCategorisationRestrictions(final List<OffenderCategorise> catListRaw) {
        // for every group check that assessment is null OR it is the latest categorisation record
        final var catList = removeEarlierCategorisations(catListRaw);

        // remove the active assessment status offenders - we only want null assessment, pending assessments, or
        // 'unclassified' (Z,X) or 'unsentenced' (U) categories
        return catList.stream()
                .filter(o -> o.getAssessStatus() == null || o.getAssessStatus().equals("P")
                        || UNSENTENCED_OR_UNCLASSIFIED_CATEGORY_CODES.contains(o.getCategory()))

                .map(OffenderCategorise::deriveStatus)
                .collect(Collectors.toList());
    }

    private List<OffenderCategorise> removeEarlierCategorisations(final List<OffenderCategorise> catList) {
        final var bookingIdMap = catList.stream().collect(Collectors.groupingBy(OffenderCategorise::getBookingId));
        bookingIdMap.replaceAll((k, v) -> cleanDuplicateRecordsUsingAssessmentSeq(v));

        return bookingIdMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<OffenderCategorise> cleanDuplicateRecordsUsingAssessmentSeq(final List<OffenderCategorise> individualCatList) {
        final var maxSeqOpt = individualCatList.stream().max(Comparator.comparing(OffenderCategorise::getAssessmentSeq));
        final var maxDateOpt = individualCatList.stream().max(Comparator.comparing(OffenderCategorise::getAssessmentDate));
        if (maxDateOpt.isEmpty() || maxSeqOpt.isEmpty()) return individualCatList;

        return individualCatList.stream()
                .filter(oc -> oc.getAssessmentSeq() == null || (oc.getAssessmentSeq().equals(maxSeqOpt.get().getAssessmentSeq()) && oc.getAssessmentDate().equals(maxDateOpt.get().getAssessmentDate())))
                .collect(Collectors.toList());
    }

    private List<OffenderCategorise> applyCutoffDateForActiveCategorisations(List<OffenderCategorise> catList, final LocalDate cutoffDate) {
        return catList
                .stream()
                .filter(cat -> "P".equals(cat.getAssessStatus()) || (cat.getNextReviewDate() != null && !cutoffDate.isBefore(cat.getNextReviewDate())))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AssignedLivingUnit> findAssignedLivingUnit(final long bookingId, final String locationTypeRoot) {
        final var sql = getQuery("FIND_ASSIGNED_LIVING_UNIT");

        final var assignedLivingUnitRowMapper =
                Row2BeanRowMapper.makeMapping(sql, AssignedLivingUnit.class, assignedLivingUnitMapping);

        AssignedLivingUnit assignedLivingUnit;
        try {
            assignedLivingUnit = jdbcTemplate.queryForObject(
                    sql,
                    createParams("bookingId", bookingId, "locationTypeRoot", locationTypeRoot),
                    assignedLivingUnitRowMapper);
        } catch (final EmptyResultDataAccessException ex) {
            assignedLivingUnit = null;
        }

        return Optional.ofNullable(assignedLivingUnit);
    }

    @Override
    public Optional<InmateDetail> findInmate(final Long bookingId) {
        final var builder = queryBuilderFactory.getQueryBuilder(getQuery("FIND_INMATE_DETAIL"), inmateDetailsMapping);
        final var sql = builder.build();

        final var inmateRowMapper = Row2BeanRowMapper.makeMapping(sql, InmateDetail.class, inmateDetailsMapping);
        InmateDetail inmate;
        try {
            inmate = jdbcTemplate.queryForObject(
                    sql,
                    createParams("bookingId", bookingId),
                    inmateRowMapper);
            if (inmate != null) {
                inmate.setAge(DateTimeConverter.getAge(inmate.getDateOfBirth()));
            }
        } catch (final EmptyResultDataAccessException ex) {
            inmate = null;
        }

        return Optional.ofNullable(inmate);
    }

    @Override
    @Cacheable("basicInmateDetail")
    public Optional<InmateDetail> getBasicInmateDetail(final Long bookingId) {
        final var builder = queryBuilderFactory.getQueryBuilder(getQuery("FIND_BASIC_INMATE_DETAIL"), inmateDetailsMapping);
        final var sql = builder.build();

        final var inmateRowMapper = Row2BeanRowMapper.makeMapping(sql, InmateDetail.class, inmateDetailsMapping);
        InmateDetail inmate;
        try {
            inmate = jdbcTemplate.queryForObject(
                    sql,
                    createParams("bookingId", bookingId),
                    inmateRowMapper);
        } catch (final EmptyResultDataAccessException ex) {
            inmate = null;
        }

        return Optional.ofNullable(inmate);
    }

    @Override
    public Page<Alias> findInmateAliases(final Long bookingId, final String orderByFields, final Order order, final long offset, final long limit) {
        final var initialSql = getQuery("FIND_INMATE_ALIASES");
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, aliasMapping);

        final var sql = builder
                .addRowCount()
                .addPagination()
                .addOrderBy(order, orderByFields)
                .build();

        final var aliasAttributesRowMapper = Row2BeanRowMapper.makeMapping(sql, Alias.class, aliasMapping);
        final var paRowMapper = new PageAwareRowMapper<>(aliasAttributesRowMapper);

        final var results = jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId, "offset", offset, "limit", limit),
                paRowMapper);
        results.forEach(alias -> alias.setAge(DateTimeConverter.getAge(alias.getDob())));
        return new Page<>(results, paRowMapper.getTotalRecords(), offset, limit);
    }

    @Override
    public Map<String, Long> insertCategory(final CategorisationDetail detail, final String agencyId, final Long assessStaffId, final String userId) {

        final var newSeq = getOffenderAssessmentSeq(detail.getBookingId()) + 1;
        jdbcTemplate.update(
                getQuery("INSERT_CATEGORY"),
                createParams("bookingId", detail.getBookingId(),
                        "assessmentTypeId", getCategoryAssessmentTypeId(),
                        "seq", newSeq,
                        "assessmentDate", LocalDate.now(),
                        "assessStatus", "P",
                        "category", detail.getCategory(),
                        "assessStaffId", assessStaffId,
                        "assessComment", detail.getComment(),
                        "reviewDate", detail.getNextReviewDate(),
                        "userId", userId,
                        "assessCommitteeCode", detail.getCommittee(),
                        "dateTime", LocalDateTime.now(),
                        "agencyId", agencyId,
                        "placementAgencyId", detail.getPlacementAgencyId()));

        return Map.of("sequenceNumber", (long) newSeq, "bookingId", detail.getBookingId());
    }

    @Override
    public void updateCategory(final CategorisationUpdateDetail detail) {

        final int result = jdbcTemplate.update(
                getQuery("UPDATE_CATEGORY"),
                createParams("bookingId", detail.getBookingId(),
                        "seq", detail.getAssessmentSeq(),
                        "assessmentTypeId", getCategoryAssessmentTypeId(),
                        "assessmentDate", LocalDate.now(),
                        "category", detail.getCategory(),
                        "assessComment", detail.getComment(),
                        "reviewDate", detail.getNextReviewDate(),
                        "assessCommitteeCode", detail.getCommittee()));
        if (result != 1) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, String.format("No pending category assessment found, category %.10s, booking %d, seq %d",
                    detail.getCategory(),
                    detail.getBookingId(),
                    detail.getAssessmentSeq()));
        }
    }

    @Override
    public void approveCategory(final CategoryApprovalDetail detail) {
        final var assessmentId = getCategoryAssessmentTypeId();

        // get all active or pending categorisation sequences ordered desc
        final var sequences = jdbcTemplate.query(
                getQuery("GET_OFFENDER_CATEGORY_SEQUENCES"),
                createParams("bookingId", detail.getBookingId(),
                        "assessmentTypeId", assessmentId,
                        "statuses", Arrays.asList("A", "P")),
                SingleColumnRowMapper.newInstance(Integer.class));
        if (CollectionUtils.isEmpty(sequences)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, String.format("No category assessment found, category %.10s, booking %d",
                    detail.getCategory(),
                    detail.getBookingId()));
        }
        final int maxSequence = sequences.get(0);

        if (detail.getAssessmentSeq() != null && detail.getAssessmentSeq() != maxSequence) {
            log.warn(String.format("approveCategory: sequences do not match for booking id %d: maxSequence = %d, PG Nomis seq = %d",
                    detail.getBookingId(),
                    maxSequence,
                    detail.getAssessmentSeq()));
        }

        final var approvalResult = jdbcTemplate.update(
                getQuery("APPROVE_CATEGORY"),
                createParams("bookingId", detail.getBookingId(),
                        "seq", maxSequence,
                        "assessmentTypeId", assessmentId,
                        "assessStatus", "A",
                        "category", detail.getCategory(),
                        "evaluationDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(detail.getEvaluationDate())),
                        "evaluationResultCode", "APP",
                        "reviewCommitteeCode", detail.getReviewCommitteeCode(),
                        "committeeCommentText", detail.getCommitteeCommentText(),
                        "nextReviewDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(detail.getNextReviewDate())),
                        "approvedCategoryComment", detail.getApprovedCategoryComment(),
                        "approvedPlacementAgencyId", detail.getApprovedPlacementAgencyId(),
                        "approvedPlacementText", detail.getApprovedPlacementText()
                )
        );
        if (approvalResult != 1) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, String.format("No pending category assessment found, category %.10s, booking %d, seq %d",
                    detail.getCategory(),
                    detail.getBookingId(),
                    maxSequence));
        }
        if (sequences.size() > 1) {
            final var previousSequences = sequences.stream().skip(1)
                    .collect(Collectors.toList());
            final var updatePreviousResult = jdbcTemplate.update(
                    getQuery("CATEGORY_SET_STATUS"),
                    createParams("bookingId", detail.getBookingId(),
                            "seq", previousSequences,
                            "assessStatus", "I"
                    )
            );
            if (updatePreviousResult < 1) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, String.format("Previous category assessment not found, booking %d, seq %s",
                        detail.getBookingId(),
                        previousSequences));
            }
        }
    }

    @Override
    public void rejectCategory(final CategoryRejectionDetail detail) {
        final var assessmentId = getCategoryAssessmentTypeId();
        final var result = jdbcTemplate.update(
                getQuery("REJECT_CATEGORY"),
                createParams("bookingId", detail.getBookingId(),
                        "seq", detail.getAssessmentSeq(),
                        "assessmentTypeId", assessmentId,
                        "evaluationDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(detail.getEvaluationDate())),
                        "evaluationResultCode", "REJ",
                        "reviewCommitteeCode", detail.getReviewCommitteeCode(),
                        "committeeCommentText", detail.getCommitteeCommentText()
                )
        );
        if (result != 1) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, String.format("Category assessment not found, booking %d, seq %d",
                    detail.getBookingId(),
                    detail.getAssessmentSeq()));
        }
    }

    @Override
    public int setCategorisationInactive(final long bookingId, final AssessmentStatusType status) {
        final var assessmentId = getCategoryAssessmentTypeId();
        final var mapper = SingleColumnRowMapper.newInstance(Integer.class);
        // get all active categorisation sequences
        final var sequences = jdbcTemplate.query(
                getQuery("GET_OFFENDER_CATEGORY_SEQUENCES"),
                createParams("bookingId", bookingId,
                        "assessmentTypeId", assessmentId,
                        "statuses", Arrays.asList(status == AssessmentStatusType.PENDING ? "P" : "A")),
                mapper);
        if (CollectionUtils.isEmpty(sequences)) {
            log.warn(String.format("No active category assessments found for booking id %d", bookingId));
            return 0;
        }
        final var updateResult = jdbcTemplate.update(
                getQuery("CATEGORY_SET_STATUS"),
                createParams("bookingId", bookingId,
                        "seq", sequences,
                        "assessStatus", "I"
                )
        );
        if (updateResult != 1) {
            log.warn(String.format("Expected one row to be updated, got %d for booking id %d", updateResult, bookingId));
        }
        return updateResult;
    }

    @Override
    public void updateActiveCategoryNextReviewDate(final long bookingId, final LocalDate date) {
        log.debug("Updating categorisation next Review date for booking id {} with value {}", bookingId, date);
        final var assessmentId = getCategoryAssessmentTypeId();

        final var result = jdbcTemplate.update(
                getQuery("UPDATE_CATEORY_NEXT_REVIEW_DATE"),
                createParams("bookingId", bookingId,
                        "assessmentTypeId", assessmentId,
                        "nextReviewDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(date))
                )
        );

        if (result != 1) {
            var message = String.format("Unable to update next review date, could not find latest, active categorisation for booking id %d, result count = %d", bookingId, result);
            log.error(message);
            throw new EntityNotFoundException(String.format(message));
        }
    }

    @Override
    public List<InmateBasicDetails> getBasicInmateDetailsForOffenders(final Set<String> offenders, final boolean accessToAllData, final Set<String> caseloads, boolean active) {
        final var baseSql = getQuery("FIND_BASIC_INMATE_DETAIL_BY_OFFENDER_NO");
        final var withCaseloadSql = accessToAllData ? baseSql : String.format("%s AND %s", baseSql, getQuery("CASELOAD_FILTER"));
        final var sql = active ? String.format("%s AND %s", withCaseloadSql, getQuery("ACTIVE_BOOKING_FILTER")) : withCaseloadSql;

        return jdbcTemplate.query(
                sql,
                createParams("offenders", offenders, "caseLoadId", caseloads, "bookingSeq", 1),
                OFFENDER_BASIC_DETAILS_MAPPER);
    }

    @Override
    public Optional<ImprisonmentStatus> getImprisonmentStatus(final long bookingId) {
        ImprisonmentStatus imprisonmentStatus;
        try {
            imprisonmentStatus = jdbcTemplate.queryForObject(
                    getQuery("GET_IMPRISONMENT_STATUS"),
                    createParams("bookingId", bookingId),
                    IMPRISONMENT_STATUS_MAPPER);
            if (imprisonmentStatus != null) {
                imprisonmentStatus.deriveLegalStatus();
            }
        } catch (final EmptyResultDataAccessException e) {
            imprisonmentStatus = null;
        }
        return Optional.ofNullable(imprisonmentStatus);
    }


    @Override
    public List<InmateBasicDetails> getBasicInmateDetailsByBookingIds(final String caseload, final List<Long> bookingIds) {
        final var sql = getQuery("FIND_BASIC_INMATE_DETAIL_BY_BOOKING_IDS");
        return jdbcTemplate.query(
                sql,
                createParams("bookingIds", bookingIds, "caseloadId", caseload),
                OFFENDER_BASIC_DETAILS_MAPPER);
    }

    private Integer getOffenderAssessmentSeq(final Long bookingId) {

        Integer maxSeq = null;

        try {
            maxSeq = jdbcTemplate.queryForObject(
                    getQuery("OFFENDER_ASSESSMENTS_SEQ_MAX"),
                    createParams("bookingId", bookingId), Integer.class);
        } catch (final EmptyResultDataAccessException ex) {
            // no row - null response
        }

        return maxSeq == null ? 1 : maxSeq;
    }

    private List<OffenderCategorise> removeNonStandardCategoryRecords(List<OffenderCategorise> rawData) {
        final var validCategoryCodes = Set.of("B", "C", "D");
        return rawData.stream().filter(cat -> cat.getCategory() != null && validCategoryCodes.contains(cat.getCategory())).collect(Collectors.toList());
    }

}
