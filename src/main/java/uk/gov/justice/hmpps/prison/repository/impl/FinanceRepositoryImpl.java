package uk.gov.justice.hmpps.prison.repository.impl;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.Account;
import uk.gov.justice.hmpps.prison.api.model.TransactionHistoryItem;
import uk.gov.justice.hmpps.prison.repository.FinanceRepository;
import uk.gov.justice.hmpps.prison.repository.mapping.FieldMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.Row2BeanRowMapper;
import uk.gov.justice.hmpps.prison.repository.storedprocs.TrustProcs.InsertIntoOffenderTrans;
import uk.gov.justice.hmpps.prison.repository.storedprocs.TrustProcs.ProcessGlTransNew;
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.*;
import static uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata.P_TRANS_CSR;

@Repository
public class FinanceRepositoryImpl extends RepositoryBase implements FinanceRepository {

    private final String currency;
    private final InsertIntoOffenderTrans insertIntoOffenderTrans;
    private final ProcessGlTransNew processGlTransNew;
    private final FinanceProcs.GetAccountTransactions getAccountTransactionsProc;

    public FinanceRepositoryImpl(@Value("${api.currency:GBP}") final String currency,
                                 final InsertIntoOffenderTrans insertIntoOffenderTrans,
                                 final ProcessGlTransNew processGlTransNew,
                                 final FinanceProcs.GetAccountTransactions getAccountTransactionsProc) {
        this.currency = currency;
        this.insertIntoOffenderTrans = insertIntoOffenderTrans;
        this.processGlTransNew = processGlTransNew;
        this.getAccountTransactionsProc = getAccountTransactionsProc;
    }

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

    @Override
    public void insertIntoOffenderTrans(final String prisonId, final long offId, final long offBookId,
                                        final String transPostType, final String subActType, final long transNumber,
                                        final long transSeq, final BigDecimal transAmount, final String transDesc,
                                        final Date transDate) {
        final var params = new MapSqlParameterSource()
                .addValue("p_trans_number", transNumber)
                .addValue("p_trans_seq", transSeq)
                .addValue("p_csld_id", prisonId)
                .addValue("p_off_id", offId)
                .addValue("p_off_book_id", offBookId)
                .addValue("p_trans_post_type", transPostType)
                .addValue("p_trans_type", "OT")
                .addValue("p_trans_desc", transDesc)
                .addValue("p_trans_amount", transAmount)
                .addValue("p_trans_date", transDate)
                .addValue("p_sub_act_type", subActType)
                .addValue("p_deduction_flag", null)
                .addValue("p_pre_ded_amount", null)
                .addValue("p_deduction_type", null)
                .addValue("p_payee_corp_id", null)
                .addValue("p_payee_person_id", null)
                .addValue("p_info_number", null)
                .addValue("p_slip_print_flag", "N")
                .addValue("p_allow_overdrawn", "N");

        insertIntoOffenderTrans.execute(params);
    }

    @Override
    public void processGlTransNew(final String prisonId, final long offId, final long offBookId, final String subActTypeDr,
                                  final String subActTypeCr, final long transNumber, final long transSeq,
                                  final BigDecimal transAmount, final String transDesc, final Date transDate) {
        final var params = new MapSqlParameterSource()
                .addValue("p_csld_id", prisonId)
                .addValue("p_trans_type", "OT")
                .addValue("p_operation_type", null)
                .addValue("p_trans_amount", transAmount)
                .addValue("p_trans_number", transNumber)
                .addValue("p_trans_date", transDate)
                .addValue("p_trans_desc", transDesc)
                .addValue("p_trans_seq", transSeq)
                .addValue("p_module_name", "OTDSUBAT")
                .addValue("p_off_id", offId)
                .addValue("p_off_book_id", offBookId)
                .addValue("p_sub_act_type_dr", subActTypeDr)
                .addValue("p_sub_act_type_cr", subActTypeCr)
                .addValue("p_payee_pers_id", null)
                .addValue("p_payee_corp_id", null)
                .addValue("p_payee_name_text", null)
                .addValue("p_gl_sqnc", 0)
                .addValue("p_off_ded_id", null);
        processGlTransNew.execute(params);
    }

    public List<TransactionHistoryItem> getTransactionsHistory(final String prisonId, final String nomisId, final String accountCode, final LocalDate fromDate, final LocalDate toDate) {

        final var params = new MapSqlParameterSource()
                .addValue(P_AGY_LOC_ID, prisonId)
                .addValue(P_NOMS_ID, nomisId)
                .addValue(P_FROM_DATE, DateTimeConverter.toDate(fromDate))
                .addValue(P_TO_DATE, DateTimeConverter.toDate(toDate));

        final var paramsToUse = ofNullable(accountCode)
                .map(code -> params.addValue(P_ACCOUNT_TYPE, code))
                .orElse(params);

        final var result = getAccountTransactionsProc.execute(paramsToUse);

        return (List<TransactionHistoryItem>) result.get(P_TRANS_CSR);
    }
}
