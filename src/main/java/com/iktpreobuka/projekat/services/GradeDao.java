package com.iktpreobuka.projekat.services;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public interface GradeDao {

	public ResponseEntity<?> findGradesBySemester(Integer userId, Integer tsId, Integer sbId, boolean firstsemester,
			Authentication authentication);

	public ResponseEntity<?> findFinalGrades(Integer userId, Integer tsId, Integer sbId, Authentication authentication);

}
