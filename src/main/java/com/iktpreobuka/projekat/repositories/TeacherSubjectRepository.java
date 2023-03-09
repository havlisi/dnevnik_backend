package com.iktpreobuka.projekat.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.iktpreobuka.projekat.entities.TeacherEntity;
import com.iktpreobuka.projekat.entities.TeacherSubject;

public interface TeacherSubjectRepository extends CrudRepository<TeacherSubject, Integer>{

	public List<TeacherSubject> findByTeacher(TeacherEntity teacher);

}
