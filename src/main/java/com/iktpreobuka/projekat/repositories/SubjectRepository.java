package com.iktpreobuka.projekat.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import com.iktpreobuka.projekat.entities.SubjectEntity;

public interface SubjectRepository extends CrudRepository<SubjectEntity, Integer>{

	SubjectEntity findBySubjectName(String subjectName);

	Optional<SubjectEntity> findById(Long id);

}
