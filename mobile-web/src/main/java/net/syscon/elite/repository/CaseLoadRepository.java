package net.syscon.elite.repository;

import net.syscon.elite.api.model.CaseLoad;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CaseLoadRepository {
	Optional<CaseLoad> find(String caseLoadId);

	List<CaseLoad> findCaseLoadsByUsername(String username);

	Optional<CaseLoad> getCurrentCaseLoadDetail(String username);

	Set<String> getUserCaseloadIds(String username);
}
