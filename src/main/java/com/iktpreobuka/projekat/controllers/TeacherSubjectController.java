package com.iktpreobuka.projekat.controllers;

import java.util.List;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.projekat.utils.ErrorMessageHelper;
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
import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.utils.RESTError;

@RestController
@RequestMapping(path = "/api/project/teacherSubject")
public class TeacherSubjectController {

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
	
	@JsonView(Views.Admin.class)
	protected final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	
	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getAllTeacherSubject() {
		List<TeacherSubject> teacherSubjects = (List<TeacherSubject>) teacherSubjectRepository.findAll();

		if (teacherSubjects.isEmpty()) {
	        logger.error("No teaching subjects found in the database.");
			return new ResponseEntity<RESTError>(new RESTError(1, "No teaching subjects found"), HttpStatus.NOT_FOUND);
		} else {
	        logger.info("Found teaching subject(s).");
			return new ResponseEntity<List<TeacherSubject>>(teacherSubjects, HttpStatus.OK);
		}
	}
	
	@Secured({"ROLE_ADMIN", "ROLE_TEACHER"})
	@RequestMapping(method = RequestMethod.GET, value = "/all-by-id/{teacher_id}")
	public ResponseEntity<?> getAllTeachersTeachingSubjects(@PathVariable Integer teacher_id, Authentication authentication) {
		
		String signedInUserEmail = authentication.getName();
		UserEntity currentUser = userRepository.findByEmail(signedInUserEmail);
		
		TeacherEntity teacher = teacherRepository.findById(teacher_id).orElse(null);
		List<TeacherSubject> teacherSubjects = (List<TeacherSubject>) teacherSubjectRepository.findByTeacher(teacher);

		if (teacherSubjects.isEmpty()) {
	        logger.error("No teaching subjects found for that teacher in the database.");
			return new ResponseEntity<RESTError>(new RESTError(1, "No teaching subjects found for that teacher"), HttpStatus.NOT_FOUND);
		}
		
		if (teacher == null) {
	        logger.error("No teacher found in the database with " + teacher_id + " .");
			return new ResponseEntity<RESTError>(new RESTError(2, "No teacher found"), HttpStatus.NOT_FOUND);
		}
		
		if (currentUser.getRole().equals("ROLE_TEACHER")) {
			if (!teacher_id.equals(currentUser.getId())) {
				logger.error("Unauthorized teacher tried to give grade.");
				return new ResponseEntity<RESTError>(new RESTError(3, "Teacher is not authorized to look at this teachers subjects."), HttpStatus.UNAUTHORIZED);
			}
			logger.info("Found teaching subject(s).");
			return new ResponseEntity<List<TeacherSubject>>(teacherSubjects, HttpStatus.OK);	
		}
		
		if (currentUser.getRole().equals("ROLE_ADMIN")) {
			logger.info("Found teaching subject(s).");
			return new ResponseEntity<List<TeacherSubject>>(teacherSubjects, HttpStatus.OK);
		}

		return new ResponseEntity<RESTError>(new RESTError(4, "Unauthorized access"), HttpStatus.UNAUTHORIZED);		
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.POST, value = "/newTeacherSubject/subj/{subj_id}/teach/{teacher_id}")
	public ResponseEntity<?> createTeacherSubject(@Valid @RequestBody TeacherSubjectDTO newTeacherSubject,
			BindingResult result, @PathVariable Integer teacher_id, @PathVariable Integer subj_id) {
		
		if(result.hasErrors()) {
	        logger.error("Sent incorrect parameters.");
			return new ResponseEntity<>(ErrorMessageHelper.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		}
		
		TeacherEntity teacher = teacherRepository.findById(teacher_id).orElse(null);
		SubjectEntity subject = subjectRepository.findById(subj_id).orElse(null);

		if (teacher == null) {
	        logger.error("No teacher found in the database with " + teacher_id + " .");
			return new ResponseEntity<RESTError>(new RESTError(1, "No teacher found"), HttpStatus.NOT_FOUND);
		}

		if (subject == null) {
	        logger.error("No subject found in the database with " + subj_id + " .");
			return new ResponseEntity<RESTError>(new RESTError(2, "No subject found"), HttpStatus.NOT_FOUND);
		}

		TeacherSubject teacherSubjects = new TeacherSubject();
        logger.info("Creating new teaching subject");

		teacherSubjects.setClassYear(newTeacherSubject.getClassYear());
		teacherSubjects.setSubject(subject);
		teacherSubjects.setTeacher(teacher);
		
		teacherSubjectRepository.save(teacherSubjects);
        logger.info("Saving values for new teaching subject");

		return new ResponseEntity<TeacherSubject>(teacherSubjects, HttpStatus.CREATED);
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.PUT, value = "/updateTeacherSubject/{id}")
	public ResponseEntity<?> updateTeacherSubject(@Valid @RequestBody TeacherSubjectDTO updatedTeacherSubject,
			BindingResult result, @PathVariable Integer id) {
		
		if(result.hasErrors()) {
	        logger.error("Sent incorrect parameters.");
			return new ResponseEntity<>(ErrorMessageHelper.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		}
		
		TeacherSubject teacherSubjects = teacherSubjectRepository.findById(id).orElse(null);

		if (teacherSubjects == null) {
	        logger.error("No teaching subject found in the database with " + id + " ID");
			return new ResponseEntity<RESTError>(new RESTError(1, "No teaching subject with " + id + " ID found"), HttpStatus.NOT_FOUND);
		}

		teacherSubjects.setClassYear(updatedTeacherSubject.getClassYear());

		if (updatedTeacherSubject.getSubject() == null) {
	        logger.info("Subject value is null");
			return new ResponseEntity<RESTError>(new RESTError(2, "No subject found"), HttpStatus.NOT_FOUND);
		}
		teacherSubjects.setSubject(updatedTeacherSubject.getSubject());
		
		if (updatedTeacherSubject.getTeacher() == null) {
	        logger.info("Teacher value is null");
			return new ResponseEntity<RESTError>(new RESTError(3, "No teacher found"), HttpStatus.NOT_FOUND);
		}
		
		teacherSubjects.setTeacher(updatedTeacherSubject.getTeacher());

		teacherSubjectRepository.save(teacherSubjects);
        logger.info("Saving values for new teaching subject");

		return new ResponseEntity<TeacherSubject>(teacherSubjects, HttpStatus.OK);
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.DELETE, value = "/deleteTeacherSubject/{id}")
	public ResponseEntity<?> deleteTeacherSubject(@PathVariable Integer id) {
		TeacherSubject teacherSubjects = teacherSubjectRepository.findById(id).orElse(null);

		if (teacherSubjects == null) {
	        logger.error("No teaching subject found in the database with " + id + " ID");
			return new ResponseEntity<RESTError>(new RESTError(1, "No teaching subject with " + id + " ID found"), HttpStatus.NOT_FOUND);
		}
		
		for (GradeEntity grade : teacherSubjects.getGrades()) {
			gradeRepository.delete(grade);
		}

		teacherSubjectRepository.delete(teacherSubjects);
        logger.info("Removing teaching subject with " + id + " ID from the database");

		return new ResponseEntity<RESTError>(new RESTError(2, "Teaching subject with id " + id + " was successfully removed"), HttpStatus.OK);
	}

}
