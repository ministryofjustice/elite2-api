package net.syscon.prison.repository;

import java.util.Set;

public interface OffenderDeletionRepository {

    Set<String> deleteOffender(String offenderNo);
}