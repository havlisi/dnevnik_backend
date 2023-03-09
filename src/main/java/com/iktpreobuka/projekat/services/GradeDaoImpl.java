package com.iktpreobuka.projekat.services;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.projekat.entities.GradeEntity;
import com.iktpreobuka.projekat.entities.ParentEntity;
import com.iktpreobuka.projekat.entities.StudentEntity;
import com.iktpreobuka.projekat.entities.SubjectEntity;
import com.iktpreobuka.projekat.entities.TeacherEntity;
import com.iktpreobuka.projekat.entities.TeacherSubject;
import com.iktpreobuka.projekat.entities.UserEntity;
import com.iktpreobuka.projekat.repositories.SubjectRepository;
import com.iktpreobuka.projekat.repositories.TeacherSubjectRepository;
import com.iktpreobuka.projekat.repositories.UserRepository;
import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.utils.RESTError;

@Service
public class GradeDaoImpl implements GradeDao {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private TeacherSubjectRepository teacherSubjectRepository;
	
	@Autowired
	private SubjectRepository subjectRepository;
	
	@PersistenceContext
	private EntityManager em;
	
	@JsonView(Views.Admin.class)
	protected final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	@Override
	public ResponseEntity<?> findGradesBySemester(Integer userId, Integer tsId, 
			Integer sbId, boolean firstsemester, Authentication authentication) {
		
		String signedInUserEmail = authentication.getName();
		UserEntity currentUser = userRepository.findByEmail(signedInUserEmail);
		
		Optional<UserEntity> user = userRepository.findById(userId);
		Optional<TeacherSubject> teacherSubject = teacherSubjectRepository.findById(tsId);
		Optional<SubjectEntity> subject = subjectRepository.findById(sbId);
		
		if (user == null) {
			logger.error("User with " + userId + " doesn't exist.");
			return new ResponseEntity<RESTError>(new RESTError(1, "No user found"), HttpStatus.NOT_FOUND);
		}

		if (teacherSubject == null) {
			logger.error("Teaching subject with " + tsId + " doesn't exist.");
			return new ResponseEntity<RESTError>(new RESTError(2, "No teaching subject found"), HttpStatus.NOT_FOUND);
		}
		
		if (subject == null) {
			logger.error("Subject with " + sbId + " doesn't exist.");
			return new ResponseEntity<RESTError>(new RESTError(3, "No subject found"), HttpStatus.NOT_FOUND);
		}
		
		if (userId == null || tsId == null || sbId == null) {
            logger.error("No grade found with these parameters.");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
					"One or more required parameters are missing.");
		}
		
		String hql = "";
		
	    if (firstsemester == true) {
            logger.info("Finding " + user.get().getFirstName() + user.get().getLastName() + " final grade for " 
            		+ subject.get().getSubjectName() + " for first semester");
            
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
	    	logger.info("Finding " + user.get().getFirstName() + user.get().getLastName() + " final grade for " 
            		+ subject.get().getSubjectName() + " for second semester");
	    	
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
		
		logger.info("Checking who is logged in user.");

		if (currentUser.getRole().equals("ROLE_TEACHER")) {
			logger.info("Logged in user is a teacher.");
		    TeacherEntity teacher = (TeacherEntity) currentUser;
		    boolean isTeachingStudent = false;
		    for (TeacherSubject teachingSubject : teacher.getTeacherSubject()) {
		        if (teachingSubject.getStudents().contains(user.get())) {
					logger.info("Correct! Student is taking this teaching subject");
		            isTeachingStudent = true;
		        }
		    }
		    if (isTeachingStudent) {
				logger.info("Teacher is looking at students grades.");
		        return new ResponseEntity<List<GradeEntity>>(result, HttpStatus.OK);
		    } else {
		        logger.error("Teacher is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
		        RESTError error = new RESTError(4, "Teacher is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
		        return new ResponseEntity<RESTError>(error, HttpStatus.UNAUTHORIZED);
		    }
		}
		
		if (currentUser.getRole().equals("ROLE_PARENT")) {
			logger.info("Logged in user is a students parent.");
			ParentEntity parent = (ParentEntity) currentUser;
		    boolean isParentOfStudent = false;
		    for (StudentEntity child : parent.getStudent()) {
		    	if (child.getId().equals(user.get().getId())) {
					logger.info("Correct! This is a parent to this student");
		    		isParentOfStudent = true;
		        }
		    }
		    if (isParentOfStudent) {
				logger.info("Parent is looking at childs grades.");
		        return new ResponseEntity<List<GradeEntity>>(result, HttpStatus.OK);
		    } else {
				logger.error("Parent is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
				RESTError error = new RESTError(5, "Parent is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
		        return new ResponseEntity<RESTError>(error, HttpStatus.UNAUTHORIZED);
			}
		}
		
		if (currentUser.getRole().equals("ROLE_STUDENT")) {
			logger.info("Logged in user is a student.");
			StudentEntity loggedStudent = (StudentEntity) currentUser;
			if (loggedStudent.getId().equals(user.get().getId())) {
				logger.info("Student is looking at its own grades.");
		        return new ResponseEntity<List<GradeEntity>>(result, HttpStatus.OK);
			} else {
				logger.error("Student is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
				RESTError error = new RESTError(6, "Student is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
		        return new ResponseEntity<RESTError>(error, HttpStatus.UNAUTHORIZED);
			}
		}
		
		if (currentUser.getRole().equals("ROLE_ADMIN")) {
			return new ResponseEntity<List<GradeEntity>>(result, HttpStatus.OK);
		}

		return new ResponseEntity<RESTError>(new RESTError(7, "Unauthorized access"), HttpStatus.UNAUTHORIZED);
	}
	
	@Override
	public ResponseEntity<?> findFinalGrades(Integer userId, Integer tsId, Integer sbId, Authentication authentication) {
		
		String signedInUserEmail = authentication.getName();
		UserEntity currentUser = userRepository.findByEmail(signedInUserEmail);
		
		Optional<UserEntity> user = userRepository.findById(userId);
		Optional<TeacherSubject> teacherSubject = teacherSubjectRepository.findById(tsId);
		Optional<SubjectEntity> subject = subjectRepository.findById(sbId);
		
		if (user == null) {
			logger.error("User with " + userId + " doesn't exist.");
			return new ResponseEntity<RESTError>(new RESTError(1, "No user found"), HttpStatus.NOT_FOUND);
		}

		if (teacherSubject == null) {
			logger.error("Teaching subject with " + tsId + " doesn't exist.");
			return new ResponseEntity<RESTError>(new RESTError(2, "No teaching subject found"), HttpStatus.NOT_FOUND);
		}
		
		if (subject == null) {
			logger.error("Subject with " + sbId + " doesn't exist.");
			return new ResponseEntity<RESTError>(new RESTError(3, "No subject found"), HttpStatus.NOT_FOUND);
		}
		
		if (userId == null || tsId == null || sbId == null) {
            logger.error("No grade found with these parameters.");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
					"One or more required parameters are missing.");
		}
		
		logger.info("Finding " + user.get().getFirstName() + user.get().getLastName() + " final grade for " 
        		+ subject.get().getSubjectName());
		
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
		
		logger.info("Checking who is logged in user.");

		if (currentUser.getRole().equals("ROLE_TEACHER")) {
			logger.info("Logged in user is a teacher.");
		    TeacherEntity teacher = (TeacherEntity) currentUser;
		    boolean isTeachingStudent = false;
		    for (TeacherSubject teachingSubject : teacher.getTeacherSubject()) {
		        if (teachingSubject.getStudents().contains(user.get())) {
					logger.info("Correct! Student is taking this teaching subject");
		            isTeachingStudent = true;
		        }
		    }
		    if (isTeachingStudent) {
				logger.info("Teacher is looking at students grades.");
		        return new ResponseEntity<List<GradeEntity>>(result, HttpStatus.OK);
		    } else {
		        logger.error("Teacher is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
		        RESTError error = new RESTError(4, "Teacher is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
		        return new ResponseEntity<RESTError>(error, HttpStatus.UNAUTHORIZED);
		    }
		}
		
		if (currentUser.getRole().equals("ROLE_PARENT")) {
			logger.info("Logged in user is a students parent.");
			ParentEntity parent = (ParentEntity) currentUser;
		    boolean isParentOfStudent = false;
		    for (StudentEntity child : parent.getStudent()) {
		    	if (child.getId().equals(user.get().getId())) {
					logger.info("Correct! This is a parent to this student");
		    		isParentOfStudent = true;
		        }
		    }
		    if (isParentOfStudent) {
				logger.info("Parent is looking at childs grades.");
		        return new ResponseEntity<List<GradeEntity>>(result, HttpStatus.OK);
		    } else {
				logger.error("Parent is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
				RESTError error = new RESTError(5, "Parent is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
		        return new ResponseEntity<RESTError>(error, HttpStatus.UNAUTHORIZED);
			}
		}
		
		if (currentUser.getRole().equals("ROLE_STUDENT")) {
			logger.info("Logged in user is a student.");
			StudentEntity loggedStudent = (StudentEntity) currentUser;
			if (loggedStudent.getId().equals(user.get().getId())) {
				logger.info("Student is looking at its own grades.");
		        return new ResponseEntity<List<GradeEntity>>(result, HttpStatus.OK);
			} else {
				logger.error("Student is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
				RESTError error = new RESTError(6, "Student is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
		        return new ResponseEntity<RESTError>(error, HttpStatus.UNAUTHORIZED);
			}
		}
		
		if (currentUser.getRole().equals("ROLE_ADMIN")) {
			return new ResponseEntity<List<GradeEntity>>(result, HttpStatus.OK);
		}

		return new ResponseEntity<RESTError>(new RESTError(7, "Unauthorized access"), HttpStatus.UNAUTHORIZED);
	}

}
