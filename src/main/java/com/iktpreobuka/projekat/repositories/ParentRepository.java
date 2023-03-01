package com.iktpreobuka.projekat.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import com.iktpreobuka.projekat.entities.ParentEntity;

public interface ParentRepository extends CrudRepository<ParentEntity, Integer>{

	Optional<ParentEntity> findByUsername(String username);

	List<ParentEntity> findByFirstName(String firstName);

	List<ParentEntity> findByLastName(String lastName);

	List<ParentEntity> findByFirstNameStartingWith(String firstLetter);

	Optional<ParentEntity> findByEmail(String email);


}
