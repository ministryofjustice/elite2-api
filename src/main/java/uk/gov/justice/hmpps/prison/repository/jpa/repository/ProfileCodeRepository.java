package uk.gov.justice.hmpps.prison.repository.jpa.repository;


import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileCode;

public interface ProfileCodeRepository extends CrudRepository<ProfileCode, ProfileCode.PK> {
}
