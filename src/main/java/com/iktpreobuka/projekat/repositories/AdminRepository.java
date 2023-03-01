package com.iktpreobuka.projekat.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import com.iktpreobuka.projekat.entities.AdminEntity;

public interface AdminRepository extends CrudRepository<AdminEntity, Integer>{

	Optional<AdminEntity> findByUsername(String username);

	List<AdminEntity> findByFirstName(String firstName);

	List<AdminEntity> findByLastName(String lastName);

	List<AdminEntity> findByFirstNameStartingWith(String firstLetter);

	Optional<AdminEntity> findByEmail(String email);

}
