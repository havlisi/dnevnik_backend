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
import com.iktpreobuka.projekat.entities.TeacherSubject;
import com.iktpreobuka.projekat.entities.dto.TeacherSubjectDTO;
import com.iktpreobuka.projekat.repositories.TeacherSubjectRepository;
import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.services.TeacherSubjectDaoImpl;
import com.iktpreobuka.projekat.utils.RESTError;

@RestController
@RequestMapping(path = "/api/project/teacherSubject")
public class TeacherSubjectController {

	@Autowired
	private TeacherSubjectRepository teacherSubjectRepository;
	
	@Autowired
	private TeacherSubjectDaoImpl teacherSubjectDaoImpl;
	
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
		return teacherSubjectDaoImpl.getAllTeachersTeachingSubjects(teacher_id, authentication);	
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.POST, value = "/newTeacherSubject/subj/{subj_id}/teach/{teacher_id}")
	public ResponseEntity<?> createTeacherSubject(@Valid @RequestBody TeacherSubjectDTO newTeacherSubject,
			BindingResult result, @PathVariable Integer teacher_id, @PathVariable Integer subj_id) {
		return teacherSubjectDaoImpl.createTeacherSubject(newTeacherSubject, result, teacher_id, subj_id);
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.PUT, value = "/updateTeacherSubject/{id}")
	public ResponseEntity<?> updateTeacherSubject(@Valid @RequestBody TeacherSubjectDTO updatedTeacherSubject,
			BindingResult result, @PathVariable Integer id) {
		return teacherSubjectDaoImpl.updateTeacherSubject(updatedTeacherSubject, result, id);
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.DELETE, value = "/deleteTeacherSubject/{id}")
	public ResponseEntity<?> deleteTeacherSubject(@PathVariable Integer id) {
		return teacherSubjectDaoImpl.deleteTeacherSubject(id);
	}

}
