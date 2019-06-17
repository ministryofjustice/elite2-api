package net.syscon.elite.repository.v1.storedprocs;

import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.repository.v1.model.OffenderSP;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Types;


public class OffenderProcs {

    private static final String API_OFFENDER_PROCS = "api_offender_procs";

    @Component
    public static class GetOffenderDetails extends SimpleJdbcCall {

        public GetOffenderDetails(DataSource dataSource) {
            super(dataSource);
            this
                    .withSchemaName(StoreProcMetadata.API_OWNER)
                    .withCatalogName(API_OFFENDER_PROCS)
                    .withProcedureName("get_offender_details")
                    .declareParameters(
                            new SqlParameter(StoreProcMetadata.P_NOMS_ID, Types.VARCHAR),
                            new SqlOutParameter(StoreProcMetadata.P_OFFENDER_CSR, Types.REF_CURSOR))
                    .returningResultSet(StoreProcMetadata.P_OFFENDER_CSR,
                            StandardBeanPropertyRowMapper.newInstance(OffenderSP.class));
            compile();
        }
    }

    @Component
    public static class GetOffenderImage extends SimpleJdbcCall {

        public static final String P_IMAGE = "p_image";

        public GetOffenderImage(DataSource dataSource) {
            super(dataSource);
            this
                    .withSchemaName(StoreProcMetadata.API_OWNER)
                    .withCatalogName(API_OFFENDER_PROCS)
                    .withProcedureName("get_offender_image")
                    .declareParameters(
                            new SqlParameter(StoreProcMetadata.P_NOMS_ID, Types.VARCHAR),
                            new SqlOutParameter(P_IMAGE, Types.BLOB));
            compile();
        }
    }

}
