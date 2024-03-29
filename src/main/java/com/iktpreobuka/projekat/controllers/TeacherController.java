package com.iktpreobuka.projekat.controllers;

import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.projekat.entities.TeacherEntity;
import com.iktpreobuka.projekat.entities.dto.UserDTO;
import com.iktpreobuka.projekat.repositories.TeacherRepository;
import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.services.TeacherDaoImpl;
import com.iktpreobuka.projekat.utils.RESTError;
import com.iktpreobuka.projekat.utils.UserCustomValidator;

@RestController
@RequestMapping(path = "/api/project/teacher")
public class TeacherController {

	@Autowired
	private TeacherRepository teacherRepository;
	
	@Autowired
	private TeacherDaoImpl teacherDaoImpl;
	
	@Autowired
	UserCustomValidator userValidator;
	
	@JsonView(Views.Admin.class)
	protected final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	
	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getAllTeachers() {
		List<TeacherEntity> teachers = (List<TeacherEntity>) teacherRepository.findAll();

		if (teachers.isEmpty()) {
	        logger.error("No teachers found in the database.");
			return new ResponseEntity<RESTError>(new RESTError(1, "Teachers not found"), HttpStatus.NOT_FOUND);
		} else {
	        logger.info("Found teacher(s) in the database");
			return new ResponseEntity<List<TeacherEntity>>(teachers, HttpStatus.OK);
		}
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.GET, value = "/by-id/{id}")
	public ResponseEntity<?> getTeacherById(@PathVariable Integer id) {
		Optional<TeacherEntity> teacher = teacherRepository.findById(id);
		if (teacher.isPresent()) {
	        logger.info("Teacher found in the database: " + teacher.get().getFirstName() + teacher.get().getLastName() + ".");
			return new ResponseEntity<TeacherEntity>(teacher.get(), HttpStatus.OK);
		} else {
	        logger.error("No teacher found in the database with: " + id + ".");
			return new ResponseEntity<RESTError>(new RESTError(1, "Teacher not found"), HttpStatus.NOT_FOUND);
		}
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.GET, value = "/by-username/{username}")
	public ResponseEntity<?> getTeacherByUsername(@PathVariable String username) {
		Optional<TeacherEntity> teacher = teacherRepository.findByUsername(username);
		if (teacher.isPresent()) {
	        logger.info("Teacher found in the database: " + teacher.get().getUsername() + ".");
			return new ResponseEntity<TeacherEntity>(teacher.get(), HttpStatus.OK);
		} else {
	        logger.error("No teacher found in the database with " + username + ".");
			return new ResponseEntity<RESTError>(new RESTError(1, "Teacher not found"), HttpStatus.NOT_FOUND);
		}
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.GET, value = "/by-firstName/{firstName}")
	public ResponseEntity<?> getTeacherByFirstName(@PathVariable String firstName) {
		List<TeacherEntity> teacher = teacherRepository.findByFirstName(firstName);
		if (teacher.isEmpty()) {
	        logger.error("No teachers found in the database with name: " + firstName);
			return new ResponseEntity<RESTError>(new RESTError(1, "No teacher found"), HttpStatus.NOT_FOUND);
		} else {
	        logger.info("Found teachers with name - " + firstName + " in the database.");
			return new ResponseEntity<List<TeacherEntity>>(teacher, HttpStatus.OK);
		}
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.GET, value = "/by-lastName/{lastName}")
	public ResponseEntity<?> getTeacherByLastName(@PathVariable String lastName) {
		List<TeacherEntity> teachers = teacherRepository.findByLastName(lastName);
		if (teachers.isEmpty()) {
	        logger.error("No teachers found in the database with lastname" + lastName);
			return new ResponseEntity<RESTError>(new RESTError(1, "No teachers found"), HttpStatus.NOT_FOUND);
		} else {
	        logger.info("Found teachers with lastname: " + lastName + " in the database.");
			return new ResponseEntity<List<TeacherEntity>>(teachers, HttpStatus.OK);
		}
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.GET, value = "/by-firstLetter/{firstLetter}")
	public ResponseEntity<?> getTeacherByFirstLetter(@PathVariable String firstLetter) {
		List<TeacherEntity> teachers = teacherRepository.findByFirstNameStartingWith(firstLetter);
		if (teachers.isEmpty()) {
	        logger.error("No teachers found in the database with first letter of the name " + firstLetter);
			return new ResponseEntity<RESTError>(new RESTError(1, "No teacher found"), HttpStatus.NOT_FOUND);
		} else {
	        logger.info("Found teacher in the database with first letter of the name " + firstLetter);
			return new ResponseEntity<List<TeacherEntity>>(teachers, HttpStatus.OK);
		}
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.GET, value = "/by-email/{email}")
	public ResponseEntity<?> getTeacherByEmail(@PathVariable String email) {
		Optional<TeacherEntity> teacher = teacherRepository.findByEmail(email);
		if (teacher.isPresent()) {
	        logger.info("Found teacher in the database with " + teacher.get().getEmail());
			return new ResponseEntity<TeacherEntity>(teacher.get(), HttpStatus.OK);
		} else {
	        logger.error("No teacher found in the database with " + email);
			return new ResponseEntity<RESTError>(new RESTError(1, "No teacher found"), HttpStatus.NOT_FOUND);
		}
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.POST, value = "/newTeacherUser")
	public ResponseEntity<?> createTeacher(@Valid @RequestBody UserDTO newUser, BindingResult result) {
		return teacherDaoImpl.createTeacher(newUser, result);
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.PUT, value = "/updateTeacher/{id}")
	public ResponseEntity<?> updateTeacher(@Valid @RequestBody UserDTO updatedUser, BindingResult result, @PathVariable Integer id) {
		return teacherDaoImpl.updateTeacher(updatedUser, result, id);
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.DELETE, value = "deleteTeacher/by-id/{id}")
	public ResponseEntity<?> deleteTeacherByID(@PathVariable Integer id) {
		return teacherDaoImpl.deleteTeacherByID(id);
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.DELETE, value = "deleteTeacher/by-username/{username}")
	public ResponseEntity<?> deleteTeacherByUsername(@PathVariable String username) {
		return teacherDaoImpl.deleteTeacherByUsername(username);
	}

}
