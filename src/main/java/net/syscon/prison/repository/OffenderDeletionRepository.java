package net.syscon.prison.repository;

import java.util.Set;

public interface OffenderDeletionRepository {

    Set<Long> deleteOffender(String offenderNo);
}
