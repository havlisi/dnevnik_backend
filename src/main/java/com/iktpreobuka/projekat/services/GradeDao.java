package com.iktpreobuka.projekat.services;

import java.util.List;
import com.iktpreobuka.projekat.entities.GradeEntity;

public interface GradeDao {

	public List<GradeEntity> findGradesBySemester(Integer userId, Integer tsId, Integer sbId, boolean firstsemester);

	public List<GradeEntity> findFinalGrades(Integer userId, Integer tsId, Integer sbId);
}
