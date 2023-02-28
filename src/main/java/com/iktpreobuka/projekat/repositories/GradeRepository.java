package com.iktpreobuka.projekat.repositories;

import org.springframework.data.repository.CrudRepository;
import com.iktpreobuka.projekat.entities.GradeEntity;
import com.iktpreobuka.projekat.entities.StudentEntity;

public interface GradeRepository extends CrudRepository<GradeEntity, Integer>{

	Iterable<GradeEntity> findByStudent(StudentEntity student);

}
