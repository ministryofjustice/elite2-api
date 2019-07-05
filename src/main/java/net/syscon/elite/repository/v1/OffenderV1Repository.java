package net.syscon.elite.repository.v1;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.repository.impl.RepositoryBase;
import net.syscon.elite.repository.v1.model.OffenderSP;
import net.syscon.elite.repository.v1.storedprocs.OffenderProcs.GetOffenderDetails;
import net.syscon.elite.repository.v1.storedprocs.OffenderProcs.GetOffenderImage;
import org.apache.commons.io.IOUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static net.syscon.elite.repository.v1.storedprocs.OffenderProcs.GetOffenderImage.P_IMAGE;
import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.P_NOMS_ID;
import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.P_OFFENDER_CSR;

@Repository
@Slf4j
public class OffenderV1Repository extends RepositoryBase {

    private final GetOffenderDetails getOffenderDetailsProc;
    private final GetOffenderImage getOffenderImageProc;

    public OffenderV1Repository(final GetOffenderDetails getOffenderDetailsProc,
                                final GetOffenderImage getOffenderImageProc) {
        this.getOffenderDetailsProc = getOffenderDetailsProc;
        this.getOffenderImageProc = getOffenderImageProc;
    }

    public Optional<OffenderSP> getOffender(final String nomsId) {
        final var param = new MapSqlParameterSource().addValue(P_NOMS_ID, nomsId);
        final var result = getOffenderDetailsProc.execute(param);
        //noinspection unchecked
        final var offender = (List<OffenderSP>) result.get(P_OFFENDER_CSR);

        return Optional.ofNullable(offender.isEmpty() ? null : offender.get(0));
    }

    public Optional<byte[]> getPhoto(final String nomsId) {

        final var param = new MapSqlParameterSource().addValue(P_NOMS_ID, nomsId);
        final var result = getOffenderImageProc.execute(param);
        final var blobBytes = (Blob) result.get(P_IMAGE);
        try {
            return Optional.ofNullable(blobBytes != null ? IOUtils.toByteArray(blobBytes.getBinaryStream()) : null);
        } catch (final IOException | SQLException e) {
            log.error("Caught {} trying to get photo for {}", e.getClass().getName(), nomsId, e);
            return Optional.empty();
        }
    }

}
