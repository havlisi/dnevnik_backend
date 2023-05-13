package com.iktpreobuka.projekat.services;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.projekat.entities.GradeEntity;
import com.iktpreobuka.projekat.entities.SubjectEntity;
import com.iktpreobuka.projekat.entities.TeacherEntity;
import com.iktpreobuka.projekat.entities.TeacherSubject;
import com.iktpreobuka.projekat.entities.UserEntity;
import com.iktpreobuka.projekat.entities.dto.TeacherSubjectDTO;
import com.iktpreobuka.projekat.repositories.GradeRepository;
import com.iktpreobuka.projekat.repositories.SubjectRepository;
import com.iktpreobuka.projekat.repositories.TeacherRepository;
import com.iktpreobuka.projekat.repositories.TeacherSubjectRepository;
import com.iktpreobuka.projekat.repositories.UserRepository;
//import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.utils.ErrorMessageHelper;
import com.iktpreobuka.projekat.utils.RESTError;

@Service
public class TeacherSubjectDaoImpl implements TeacherSubjectDao {

	@Autowired
	private TeacherSubjectRepository teacherSubjectRepository;

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private TeacherRepository teacherRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private GradeRepository gradeRepository;
	
	//@JsonView(Views.Admin.class)
	protected final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	
//	public ResponseEntity<?> getAllTeachersTeachingSubjects(Integer teacher_id, Authentication authentication) {
//		
//		String signedInUserEmail = authentication.getName();
//		UserEntity currentUser = userRepository.findByEmail(signedInUserEmail);
//		
//		TeacherEntity teacher = teacherRepository.findById(teacher_id).orElse(null);
//		List<TeacherSubject> teacherSubjects = (List<TeacherSubject>) teacherSubjectRepository.findByTeacher(teacher);
//
//		if (teacherSubjects.isEmpty()) {
//	        logger.error("No teaching subjects found for that teacher in the database.");
//			return new ResponseEntity<RESTError>(new RESTError(1, "No teaching subjects found for that teacher"), HttpStatus.NOT_FOUND);
//		}
//		
//		if (teacher == null) {
//	        logger.error("No teacher found in the database with " + teacher_id + " .");
//			return new ResponseEntity<RESTError>(new RESTError(2, "No teacher found"), HttpStatus.NOT_FOUND);
//		}
//		
//		if (currentUser.getRole().equals("ROLE_TEACHER")) {
//			if (!teacher_id.equals(currentUser.getId())) {
//				logger.error("Unauthorized teacher tried to give grade.");
//				return new ResponseEntity<RESTError>(new RESTError(3, "Teacher is not authorized to look at this teachers subjects."), HttpStatus.UNAUTHORIZED);
//			}
//			logger.info("Found teaching subject(s).");
//			return new ResponseEntity<List<TeacherSubject>>(teacherSubjects, HttpStatus.OK);	
//		}
//		
//		if (currentUser.getRole().equals("ROLE_ADMIN")) {
//			logger.info("Found teaching subject(s).");
//			return new ResponseEntity<List<TeacherSubject>>(teacherSubjects, HttpStatus.OK);
//		}
//
//		return new ResponseEntity<RESTError>(new RESTError(4, "Unauthorized access"), HttpStatus.UNAUTHORIZED);		
//	}
//
//	
//	public ResponseEntity<?> createTeacherSubject(TeacherSubjectDTO newTeacherSubject, BindingResult result, 
//			Integer teacher_id, Integer subj_id) {
//		
//		if(result.hasErrors()) {
//	        logger.error("Sent incorrect parameters.");
//			return new ResponseEntity<>(ErrorMessageHelper.createErrorMessage(result), HttpStatus.BAD_REQUEST);
//		}
//		
//		TeacherEntity teacher = teacherRepository.findById(teacher_id).orElse(null);
//		SubjectEntity subject = subjectRepository.findById(subj_id).orElse(null);
//
//		if (teacher == null) {
//	        logger.error("No teacher found in the database with " + teacher_id + " .");
//			return new ResponseEntity<RESTError>(new RESTError(1, "No teacher found"), HttpStatus.NOT_FOUND);
//		}
//
//		if (subject == null) {
//	        logger.error("No subject found in the database with " + subj_id + " .");
//			return new ResponseEntity<RESTError>(new RESTError(2, "No subject found"), HttpStatus.NOT_FOUND);
//		}
//
//		TeacherSubject teacherSubjects = new TeacherSubject();
//        logger.info("Creating new teaching subject");
//
//		teacherSubjects.setClassYear(newTeacherSubject.getClassYear());
//		teacherSubjects.setSubject(subject);
//		teacherSubjects.setTeacher(teacher);
//		
//		teacherSubjectRepository.save(teacherSubjects);
//        logger.info("Saving values for new teaching subject");
//
//		return new ResponseEntity<TeacherSubject>(teacherSubjects, HttpStatus.CREATED);
//	}
//
//	public ResponseEntity<?> updateTeacherSubject(TeacherSubjectDTO updatedTeacherSubject,
//			BindingResult result, Integer id) {
//		
//		if(result.hasErrors()) {
//	        logger.error("Sent incorrect parameters.");
//			return new ResponseEntity<>(ErrorMessageHelper.createErrorMessage(result), HttpStatus.BAD_REQUEST);
//		}
//		
//		TeacherSubject teacherSubjects = teacherSubjectRepository.findById(id).orElse(null);
//
//		if (teacherSubjects == null) {
//	        logger.error("No teaching subject found in the database with " + id + " ID");
//			return new ResponseEntity<RESTError>(new RESTError(1, "No teaching subject with " + id + " ID found"), HttpStatus.NOT_FOUND);
//		}
//
//		teacherSubjects.setClassYear(updatedTeacherSubject.getClassYear());
//		
//		SubjectEntity newSubject = subjectRepository.findById(updatedTeacherSubject.getSubject_id()).get();
//
//		if (newSubject == null) {
//	        logger.error("Subject value is null");
//			return new ResponseEntity<RESTError>(new RESTError(2, "No subject found"), HttpStatus.NOT_FOUND);
//		}
//		teacherSubjects.setSubject(newSubject);
//		
//		TeacherEntity newTeacher = teacherRepository.findById(updatedTeacherSubject.getTeacher_id()).get();
//		
//		if (newTeacher == null) {
//	        logger.error("Teacher value is null");
//			return new ResponseEntity<RESTError>(new RESTError(3, "No teacher found"), HttpStatus.NOT_FOUND);
//		}
//		
//		teacherSubjects.setTeacher(newTeacher);
//
//		teacherSubjectRepository.save(teacherSubjects);
//        logger.info("Saving values for new teaching subject");
//
//		return new ResponseEntity<TeacherSubject>(teacherSubjects, HttpStatus.OK);
//	}
//	
//	public ResponseEntity<?> deleteTeacherSubject(Integer id) {
//		TeacherSubject teacherSubjects = teacherSubjectRepository.findById(id).orElse(null);
//
//		if (teacherSubjects == null) {
//	        logger.error("No teaching subject found in the database with " + id + " ID");
//			return new ResponseEntity<RESTError>(new RESTError(1, "No teaching subject with " + id + " ID found"), HttpStatus.NOT_FOUND);
//		}
//		
//		for (GradeEntity grade : teacherSubjects.getGrades()) {
//			gradeRepository.delete(grade);
//		}
//
//		teacherSubjectRepository.delete(teacherSubjects);
//        logger.info("Removing teaching subject with " + id + " ID from the database");
//
//		return new ResponseEntity<RESTError>(new RESTError(2, "Teaching subject with id " + id + " was successfully removed"), HttpStatus.OK);
//	}
	
}
