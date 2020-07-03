package uk.gov.justice.hmpps.prison.repository.impl;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.NewBooking;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import javax.sql.DataSource;
import java.sql.Types;
import java.time.LocalDateTime;

@Repository
public class CreateBookingImpl {
    public static final String DEFAULT_IMPRISONMENT_STATUS = "UNKNOWN";
    public static final String DEFAULT_FROM_LOCATION = "OUT";

    @Qualifier("dataSource")
    private final DataSource dataSource;

    public CreateBookingImpl(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long createBooking(final String agencyId, final NewBooking newBooking) {
        Validate.notNull(newBooking);

        // Set up custom error translation
        final var jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.setExceptionTranslator(new BookingRepositorySQLErrorCodeTranslator());

        // Prepare Stored Procedure call
        final var createBookingProc = new SimpleJdbcCall(jdbcTemplate)
                .withSchemaName("api2_owner")
                .withCatalogName("api2_offender_booking")
                .withProcedureName("create_offender_booking");

        // Initialise parameters
        final var now = DateTimeConverter.toDate(LocalDateTime.now());

        final var youthOffender = newBooking.isYouthOffender() ? "Y" : "N";

        final var params = new MapSqlParameterSource()
                .addValue("p_noms_id", newBooking.getOffenderNo(), Types.VARCHAR)
                .addValue("p_last_name", newBooking.getLastName(), Types.VARCHAR)
                .addValue("p_first_name", newBooking.getFirstName(), Types.VARCHAR)
                .addValue("p_given_name_2", newBooking.getMiddleName1(), Types.VARCHAR)
                .addValue("p_given_name_3", newBooking.getMiddleName2(), Types.VARCHAR)
                .addValue("p_title", newBooking.getTitle(), Types.VARCHAR)
                .addValue("p_suffix", newBooking.getSuffix(), Types.VARCHAR)
                .addValue("p_birth_date", newBooking.getDateOfBirth(), Types.DATE)
                .addValue("p_gender", newBooking.getGender(), Types.VARCHAR)
                .addValue("p_ethnicity", newBooking.getEthnicity(), Types.VARCHAR)
                .addValue("p_pnc_number", newBooking.getPncNumber(), Types.VARCHAR)
                .addValue("p_cro_number", newBooking.getCroNumber(), Types.VARCHAR)
                .addValue("p_extn_identifier", newBooking.getExternalIdentifier(), Types.VARCHAR)
                .addValue("p_extn_ident_type", newBooking.getExternalIdentifierType(), Types.VARCHAR)
                .addValue("p_force_creation", "N", Types.VARCHAR)
                .addValue("p_date", now, Types.DATE)
                .addValue("p_time", now, Types.DATE)
                .addValue("p_from_location", DEFAULT_FROM_LOCATION, Types.VARCHAR)
                .addValue("p_to_location", agencyId, Types.VARCHAR)
                .addValue("p_reason", newBooking.getReason(), Types.VARCHAR)
                .addValue("p_youth_offender", youthOffender, Types.VARCHAR)
                .addValue("p_housing_location", null, Types.VARCHAR)
                .addValue("p_imprisonment_status", DEFAULT_IMPRISONMENT_STATUS, Types.VARCHAR);

        // Execute call
        final var result = createBookingProc.execute(params);

        return ((Number) result.get("P_OFFENDER_BOOK_ID")).longValue();
    }
}
