package com.iktpreobuka.projekat.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import com.iktpreobuka.projekat.entities.StudentEntity;

public interface StudentRepository extends CrudRepository<StudentEntity, Integer>{

	Optional<StudentEntity> findByFirstNameAndLastName(String studentFName, String studentLName);

	Optional<StudentEntity> findByUsername(String username);

	List<StudentEntity> findByFirstName(String firstName);

	List<StudentEntity> findByLastName(String lastName);

	List<StudentEntity> findByFirstNameStartingWith(String firstLetter);

	Optional<StudentEntity> findByEmail(String email);

}
