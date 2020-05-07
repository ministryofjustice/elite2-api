package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.Address;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AddressRepository extends CrudRepository<Address, Long> {
    List<Address> findAllByOwnerClassAndOwnerId(String ownerClass, Long ownerId);

}
