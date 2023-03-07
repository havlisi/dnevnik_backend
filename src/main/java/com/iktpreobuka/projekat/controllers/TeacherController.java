package com.iktpreobuka.projekat.controllers;

import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.projekat.entities.Helpers;
import com.iktpreobuka.projekat.entities.TeacherEntity;
import com.iktpreobuka.projekat.entities.dto.UserDTO;
import com.iktpreobuka.projekat.repositories.TeacherRepository;
import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.utils.RESTError;
import com.iktpreobuka.projekat.utils.UserCustomValidator;

@RestController
@RequestMapping(path = "/api/project/teacher")
public class TeacherController {

	@Autowired
	private TeacherRepository teacherRepository;
	
	@Autowired
	UserCustomValidator userValidator;
	
	@JsonView(Views.Admin.class)
	protected final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

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

	@RequestMapping(method = RequestMethod.GET, value = "/by-id/{id}")
	public ResponseEntity<?> getTeacherById(@PathVariable Integer id) {
		Optional<TeacherEntity> teacher = teacherRepository.findById(id);
		if (teacher.isPresent()) {
	        logger.info("Teacher found in the database: " + teacher.get().getFirstName() + teacher.get().getLastName() + " .");
			return new ResponseEntity<TeacherEntity>(teacher.get(), HttpStatus.OK);
		} else {
	        logger.error("No teacher found in the database with: " + teacher.get().getId() + " .");
			return new ResponseEntity<RESTError>(new RESTError(1, "Teacher not found"), HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-username/{username}")
	public ResponseEntity<?> getTeacherByUsername(@PathVariable String username) {
		Optional<TeacherEntity> teacher = teacherRepository.findByUsername(username);
		if (teacher.isPresent()) {
	        logger.info("Teacher found in the database: " + teacher.get().getUsername() + " .");
			return new ResponseEntity<TeacherEntity>(teacher.get(), HttpStatus.OK);
		} else {
	        logger.error("No teacher found in the database with " + teacher.get().getUsername() + " .");
			return new ResponseEntity<RESTError>(new RESTError(1, "Teacher not found"), HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-firstName/{firstName}")
	public ResponseEntity<?> getTeacherByFirstName(@PathVariable String firstName) {
		List<TeacherEntity> teacher = teacherRepository.findByFirstName(firstName);
		if (teacher.isEmpty()) {
	        logger.error("No teachers found in the database with name: " + firstName);
			return new ResponseEntity<RESTError>(new RESTError(1, "No teacher found"), HttpStatus.NOT_FOUND);
		} else {
	        logger.info("Found teachers with name - " + firstName + " in the database .");
			return new ResponseEntity<List<TeacherEntity>>(teacher, HttpStatus.OK);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-lastName/{lastName}")
	public ResponseEntity<?> getTeacherByLastName(@PathVariable String lastName) {
		List<TeacherEntity> teachers = teacherRepository.findByLastName(lastName);
		if (teachers.isEmpty()) {
	        logger.error("No teachers found in the database with lastname" + lastName);
			return new ResponseEntity<RESTError>(new RESTError(1, "No teachers found"), HttpStatus.NOT_FOUND);
		} else {
	        logger.info("Found teachers with lastname: " + lastName + " in the database .");
			return new ResponseEntity<List<TeacherEntity>>(teachers, HttpStatus.OK);
		}
	}

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

	@RequestMapping(method = RequestMethod.GET, value = "/by-email/{email}")
	public ResponseEntity<?> getTeacherByEmail(@PathVariable String email) {
		Optional<TeacherEntity> teacher = teacherRepository.findByEmail(email);
		if (teacher.isPresent()) {
	        logger.info("Found teacher in the database with " + teacher.get().getEmail());
			return new ResponseEntity<TeacherEntity>(teacher.get(), HttpStatus.OK);
		} else {
	        logger.error("No teacher found in the database with " + teacher.get().getEmail());
			return new ResponseEntity<RESTError>(new RESTError(1, "No teacher found"), HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/newTeacherUser")
	public ResponseEntity<?> createTeacher(@Valid @RequestBody UserDTO newUser, BindingResult result) {
		
		if(result.hasErrors()) {
	        logger.info("Validating users input parameters");
			return new ResponseEntity<>(Helpers.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		}
		
		TeacherEntity newTeacher = new TeacherEntity();

		TeacherEntity existingTeacherWithEmail = teacherRepository.findByEmail(newUser.getEmail()).orElse(null);
        logger.info("Fiding out whether there's a user with the same email.");

		if (existingTeacherWithEmail != null && newUser.getEmail().equals(existingTeacherWithEmail.getEmail())) {
	        logger.error("There is a user with the same email.");
			return new ResponseEntity<RESTError>(new RESTError(1, "Email already exists"), HttpStatus.CONFLICT);
		}

		TeacherEntity existingTeacherWithUsername = teacherRepository.findByUsername(newUser.getUsername()).orElse(null);
        logger.info("Fiding out whether there's a user with the same username.");

		if (existingTeacherWithUsername != null && newUser.getUsername().equals(existingTeacherWithUsername.getUsername())) {
	        logger.error("There is a user with the same username.");
			return new ResponseEntity<RESTError>(new RESTError(2, "Username already exists"), HttpStatus.CONFLICT);
		}
		
		newTeacher.setFirstName(newUser.getFirstName());
		newTeacher.setLastName(newUser.getLastName());
		newTeacher.setUsername(newUser.getUsername());
		newTeacher.setEmail(newUser.getEmail());
		newTeacher.setPassword(newUser.getPassword());

		newTeacher.setRole("ROLE_TEACHER");
        logger.info("Setting users role.");

		teacherRepository.save(newTeacher);
        logger.info("Saving teacher to the database");

		return new ResponseEntity<TeacherEntity>(newTeacher, HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/updateTeacher/{id}")
	public ResponseEntity<?> updateTeacher(@Valid @RequestBody UserDTO updatedUser, @PathVariable Integer id,
			@RequestParam String accessPass, BindingResult result) {

		if (result.hasErrors()) {
	        logger.info("Validating users input parameters");
			return new ResponseEntity<>(Helpers.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		} else {
	        logger.info("Validating if the users password matches the confirming password");
			userValidator.validate(updatedUser, result);
		}
		
		TeacherEntity teacher = teacherRepository.findById(id).orElse(null);

		if (teacher == null) {
	        logger.error("There is no teacher found with " + id);
			return new ResponseEntity<RESTError>(new RESTError(1, "No teacher found"), HttpStatus.NOT_FOUND);
		}

		if (!teacher.getPassword().equals(accessPass)) {
	        logger.error("The password isn't correct");
			return new ResponseEntity<RESTError>(new RESTError(2, "Password is incorrect"), HttpStatus.BAD_REQUEST);
		}

		teacher.setFirstName(updatedUser.getFirstName());
		teacher.setLastName(updatedUser.getLastName());
		teacher.setUsername(updatedUser.getUsername());
		teacher.setEmail(updatedUser.getEmail());

		teacherRepository.save(teacher);
        logger.info("Saving teacher to the database");

		return new ResponseEntity<TeacherEntity>(teacher, HttpStatus.OK);
	}

	// TODO dodati metodu za postavljanje predmeta profesoru

	@RequestMapping(method = RequestMethod.DELETE, value = "deleteTeacher/by-id/{id}")
	public ResponseEntity<?> deleteTeacherByID(@PathVariable Integer id) {
		Optional<TeacherEntity> teacher = teacherRepository.findById(id);

		if (teacher.isPresent()) {
			teacherRepository.delete(teacher.get());
	        logger.info("Deleting the teacher from the database");
			return new ResponseEntity<>("Teacher with ID " + id + " has been successfully deleted.", HttpStatus.OK);
		} else {
	        logger.error("There is no teacher found with " + id);
			return new ResponseEntity<RESTError>(new RESTError(1, "Teacher not found"), HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "deleteTeacher/by-username/{username}")
	public ResponseEntity<?> deleteTeacherByUsername(@PathVariable String username) {
		Optional<TeacherEntity> teacher = teacherRepository.findByUsername(username);

		if (teacher.isPresent()) {
			teacherRepository.delete(teacher.get());
	        logger.info("Deleting the teacher from the database");
			return new ResponseEntity<>("Teacher with " + username + " username has been successfully deleted.",
					HttpStatus.OK);
		} else {
	        logger.error("There is no teacher found with " + username);
			return new ResponseEntity<RESTError>(new RESTError(1, "Teacher not found"), HttpStatus.NOT_FOUND);
		}

	}

}
