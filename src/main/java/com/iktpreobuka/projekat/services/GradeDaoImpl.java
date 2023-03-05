package com.iktpreobuka.projekat.services;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.iktpreobuka.projekat.entities.GradeEntity;

@Service
public class GradeDaoImpl implements GradeDao {
	
	@PersistenceContext
	private EntityManager em;

	@Override
	public List<GradeEntity> findGradesBySemester(Integer userId, Integer tsId, 
			Integer sbId, boolean firstsemester) {
		if (userId == null || tsId == null || sbId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
					"One or more required parameters are missing.");
		    }
		
		String hql = "";
		
	    if (firstsemester == true) {
	        hql = "SELECT u.firstName, u.lastName, sb.subjectName, "
	            + "ROUND(AVG(g.grade),2) " 
	            + "FROM UserEntity u "
	            + "JOIN GradeEntity g ON u.id = g.student " 
	            + "JOIN TeacherSubject ts ON g.teacherSubject = ts.id "
	            + "JOIN SubjectEntity sb ON ts.subject = sb.id " 
	            + "WHERE u.id = :userId " + "AND ts.id = :tsId "
	            + "AND sb.id = :sbId " + "AND g.firstSemester = :firstsemester "
	            + "GROUP BY g.student, sb.subjectName";
	    } else {
	        hql = "SELECT u.firstName, u.lastName, sb.subjectName, "
	            + "ROUND(AVG(g.grade),2) " 
	            + "FROM UserEntity u "
	            + "JOIN GradeEntity g ON u.id = g.student " 
	            + "JOIN TeacherSubject ts ON g.teacherSubject = ts.id "
	            + "JOIN SubjectEntity sb ON ts.subject = sb.id " 
	            + "WHERE u.id = :userId " + "AND ts.id = :tsId "
	            + "AND sb.id = :sbId " + "AND g.firstSemester = :firstsemester "
	            + "GROUP BY g.student, sb.subjectName";
	    }
		
		Query query = em.createQuery(hql);
		query.setParameter("userId", userId);
		query.setParameter("tsId", tsId);
		query.setParameter("sbId", sbId);
		query.setParameter("firstsemester", firstsemester);
		
		List<GradeEntity> result = query.getResultList();
		
		if (result.isEmpty()) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
	        		"No result found with the specified parameters.");
	    }
		
		return result;
	}
	
	@Override
	public List<GradeEntity> findFinalGrades(Integer userId, Integer tsId, Integer sbId) {
		if (userId == null || tsId == null || sbId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
					"One or more required parameters are missing.");
			}
		
		String hql  = "SELECT u.firstName, u.lastName, sb.subjectName, "
	            + "ROUND(AVG(g.grade),2) " 
	            + "FROM UserEntity u "
	            + "JOIN GradeEntity g ON u.id = g.student " 
	            + "JOIN TeacherSubject ts ON g.teacherSubject = ts.id "
	            + "JOIN SubjectEntity sb ON ts.subject = sb.id " 
	            + "WHERE u.id = :userId " + "AND ts.id = :tsId "
	            + "AND sb.id = :sbId "
	            + "GROUP BY g.student, sb.subjectName";
	    
		
		Query query = em.createQuery(hql);
		query.setParameter("userId", userId);
		query.setParameter("tsId", tsId);
		query.setParameter("sbId", sbId);
		
		List<GradeEntity> result = query.getResultList();
		
		if (result.isEmpty()) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
	        		"No result found with the specified parameters.");
	    }
		
		return result;
	}

}
