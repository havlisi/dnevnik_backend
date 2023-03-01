package com.iktpreobuka.projekat.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import com.iktpreobuka.projekat.entities.TeacherEntity;

public interface TeacherRepository extends CrudRepository<TeacherEntity, Integer>{

	Optional<TeacherEntity> findByUsername(String username);

	Optional<TeacherEntity> findByEmail(String email);

	List<TeacherEntity> findByFirstNameStartingWith(String firstLetter);

	List<TeacherEntity> findByLastName(String lastName);

	List<TeacherEntity> findByFirstName(String firstName);

}
