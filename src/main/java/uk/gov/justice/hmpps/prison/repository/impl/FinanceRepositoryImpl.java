package uk.gov.justice.hmpps.prison.repository.impl;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.Account;
import uk.gov.justice.hmpps.prison.repository.FinanceRepository;
import uk.gov.justice.hmpps.prison.repository.mapping.FieldMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.Row2BeanRowMapper;
import uk.gov.justice.hmpps.prison.repository.storedprocs.TrustProcs.InsertIntoOffenderTrans;
import uk.gov.justice.hmpps.prison.repository.storedprocs.TrustProcs.ProcessGlTransNew;

import java.util.Map;

@Repository
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class FinanceRepositoryImpl extends RepositoryBase implements FinanceRepository {

    private @Value("${api.currency:GBP}")
    String currency;

    private final InsertIntoOffenderTrans insertIntoOffenderTrans;
    private final ProcessGlTransNew processGlTransNew;

    private final Map<String, FieldMapper> accountMapping = new ImmutableMap.Builder<String, FieldMapper>()
            .put("cash_balance", new FieldMapper("cash"))//
            .put("spends_balance", new FieldMapper("spends"))//
            .put("savings_balance", new FieldMapper("savings")).build();

    @Override
    public Account getBalances(final long bookingId, final String agencyId) {
        final var sql = getQuery("GET_ACCOUNT");
        final var rowMapper = Row2BeanRowMapper.makeMapping(sql, Account.class, accountMapping);
        final var balances = jdbcTemplate.queryForObject(sql, createParams("bookingId", bookingId, "agencyId", agencyId), rowMapper);
        balances.setCurrency(currency);
        return balances;
    }

    public void insertIntoOffenderTrans(final String txType) {
        final var params = new MapSqlParameterSource()
                // add the rest!
                .addValue("p_trans_type", txType);

        final var result = insertIntoOffenderTrans.execute(params);
    }

    public String processGlTransNew() {
        final var params = new MapSqlParameterSource()
                // add the rest!
                .addValue("p_module_name", "OTDSUBAT");
        final var result = processGlTransNew.execute(params);

        return String.valueOf(result.get("p_gl_sqnc"));
    }
}
