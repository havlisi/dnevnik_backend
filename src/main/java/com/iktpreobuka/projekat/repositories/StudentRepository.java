package com.iktpreobuka.projekat.repositories;

import org.springframework.data.repository.CrudRepository;
import com.iktpreobuka.projekat.entities.StudentEntity;

public interface StudentRepository extends CrudRepository<StudentEntity, Integer>{

	StudentEntity findByFirstNameAndLastName(String studentFName, String studentLName);

}
