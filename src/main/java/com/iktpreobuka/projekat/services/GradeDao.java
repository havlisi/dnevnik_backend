package com.iktpreobuka.projekat.services;

import org.springframework.http.ResponseEntity;

public interface GradeDao {

	public ResponseEntity<?> findGradesBySemester(Integer userId, Integer tsId, Integer sbId, boolean firstsemester);

	public ResponseEntity<?> findFinalGrades(Integer userId, Integer tsId, Integer sbId);
}
