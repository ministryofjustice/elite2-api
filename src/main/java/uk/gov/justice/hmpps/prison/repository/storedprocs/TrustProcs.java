package uk.gov.justice.hmpps.prison.repository.storedprocs;

import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.repository.v1.NomisV1SQLErrorCodeTranslator;
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.SimpleJdbcCallWithExceptionTranslater;

import javax.sql.DataSource;
import java.sql.Types;



@Component
public class TrustProcs {

    @Component
    public static class InsertIntoOffenderTrans extends SimpleJdbcCallWithExceptionTranslater {
        public InsertIntoOffenderTrans(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName("OMS_OWNER")
                    .withCatalogName("TRUST")
                    .withProcedureName("insert_into_offender_trans")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter("p_trans_number", Types.NUMERIC),
                            new SqlParameter("p_trans_seq", Types.NUMERIC),
                            new SqlParameter("p_csld_id", Types.VARCHAR),
                            new SqlParameter("p_off_id", Types.NUMERIC),
                            new SqlParameter("p_off_book_id", Types.NUMERIC),
                            new SqlParameter("p_trans_post_type", Types.VARCHAR),
                            new SqlParameter("p_trans_type", Types.VARCHAR),
                            new SqlParameter("p_trans_desc", Types.VARCHAR),
                            new SqlParameter("p_trans_amount", Types.NUMERIC),
                            new SqlParameter("p_trans_date", Types.DATE),
                            new SqlParameter("p_sub_act_type", Types.VARCHAR),
                            new SqlParameter("p_deduction_flag", Types.VARCHAR),
                            new SqlParameter("p_pre_ded_amount", Types.NUMERIC),
                            new SqlParameter("p_deduction_type", Types.VARCHAR),
                            new SqlParameter("p_payee_corp_id", Types.NUMERIC),
                            new SqlParameter("p_payee_person_id", Types.NUMERIC),
                            new SqlParameter("p_info_number", Types.VARCHAR),
                            new SqlParameter("p_slip_print_flag", Types.VARCHAR),
                            new SqlParameter("p_allow_overdrawn", Types.VARCHAR),
                            new SqlParameter("p_pay_from_date", Types.DATE),
                            new SqlParameter("p_pay_to_date", Types.DATE)
                    );
            compile();
        }
    }

    @Component
    public static class ProcessGlTransNew extends SimpleJdbcCallWithExceptionTranslater {
        public ProcessGlTransNew(final DataSource dataSource, final NomisV1SQLErrorCodeTranslator errorCodeTranslator) {
            super(dataSource, errorCodeTranslator);
            withSchemaName("OMS_OWNER")
                    .withCatalogName("TRUST")
                    .withProcedureName("process_gl_trans_new")
                    .withNamedBinding()
                    .declareParameters(
                            new SqlParameter("p_csld_id", Types.VARCHAR),
                            new SqlParameter("p_trans_type", Types.VARCHAR),
                            new SqlParameter("p_operation_type", Types.VARCHAR),
                            new SqlParameter("p_trans_amount", Types.NUMERIC),
                            new SqlParameter("p_trans_number", Types.NUMERIC),
                            new SqlParameter("p_trans_date", Types.DATE),
                            new SqlParameter("p_trans_desc", Types.VARCHAR),
                            new SqlParameter("p_trans_seq", Types.NUMERIC),
                            new SqlParameter("p_module_name", Types.VARCHAR),
                            new SqlParameter("p_off_id", Types.NUMERIC),
                            new SqlParameter("p_off_book_id", Types.NUMERIC),
                            new SqlParameter("p_sub_act_type_dr", Types.VARCHAR),
                            new SqlParameter("p_sub_act_type_cr", Types.VARCHAR),
                            new SqlParameter("p_payee_pers_id", Types.NUMERIC),
                            new SqlParameter("p_payee_corp_id", Types.NUMERIC),
                            new SqlParameter("p_payee_name_text", Types.VARCHAR),
                            new SqlOutParameter("p_gl_sqnc", Types.NUMERIC),
                            new SqlParameter("p_off_ded_id", Types.NUMERIC));
            compile();
        }
    }

}
