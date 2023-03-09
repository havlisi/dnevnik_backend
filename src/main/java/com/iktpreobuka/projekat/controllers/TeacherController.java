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
import com.iktpreobuka.projekat.utils.ErrorMessageHelper;
import com.iktpreobuka.projekat.entities.TeacherEntity;
import com.iktpreobuka.projekat.entities.TeacherSubject;
import com.iktpreobuka.projekat.entities.UserEntity;
import com.iktpreobuka.projekat.entities.dto.UserDTO;
import com.iktpreobuka.projekat.repositories.TeacherRepository;
import com.iktpreobuka.projekat.repositories.TeacherSubjectRepository;
import com.iktpreobuka.projekat.repositories.UserRepository;
import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.utils.RESTError;
import com.iktpreobuka.projekat.utils.UserCustomValidator;

@RestController
@RequestMapping(path = "/api/project/teacher")
public class TeacherController {

	@Autowired
	private TeacherRepository teacherRepository;
	
	@Autowired
	private TeacherSubjectRepository teacherSubjectRepository;
	
	@Autowired
	private UserRepository userRepository;
	
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
		
		if (result.hasErrors()) {
	        logger.error("Sent incorrect parameters.");
			return new ResponseEntity<>(ErrorMessageHelper.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		} else {
	        logger.info("Validating if the users password matches the confirming password");
			userValidator.validate(newUser, result);
		}

		UserEntity existingUserWithEmail = userRepository.findByEmail(newUser.getEmail());
        logger.info("Finding out whether there's a user with the same email.");

		if (existingUserWithEmail != null) {
	        logger.error("There is a user with the same email.");
			return new ResponseEntity<RESTError>(new RESTError(1, "Email already exists"), HttpStatus.CONFLICT);
		}

		UserEntity existingUserWithUsername = userRepository.findByUsername(newUser.getUsername()).orElse(null);
        logger.info("Finding out whether there's a user with the same username.");

		if (existingUserWithUsername != null) {
	        logger.error("There is a user with the same username.");
			return new ResponseEntity<RESTError>(new RESTError(2, "Username already exists"), HttpStatus.CONFLICT);
		}
		
		TeacherEntity newTeacher = new TeacherEntity();
		
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

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.PUT, value = "/updateTeacher/{id}")
	public ResponseEntity<?> updateTeacher(@Valid @RequestBody UserDTO updatedUser, BindingResult result, @PathVariable Integer id) {

		if (result.hasErrors()) {
	        logger.error("Sent incorrect parameters.");
			return new ResponseEntity<>(ErrorMessageHelper.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		} else {
	        logger.info("Validating if the users password matches the confirming password");
			userValidator.validate(updatedUser, result);
		}
		
		TeacherEntity teacher = teacherRepository.findById(id).orElse(null);

		if (teacher == null) {
	        logger.error("There is no teacher found with " + id);
			return new ResponseEntity<RESTError>(new RESTError(1, "No teacher found"), HttpStatus.NOT_FOUND);
		}

		teacher.setFirstName(updatedUser.getFirstName());
		teacher.setLastName(updatedUser.getLastName());
		teacher.setUsername(updatedUser.getUsername());
		teacher.setEmail(updatedUser.getEmail());

		teacherRepository.save(teacher);
        logger.info("Saving teacher to the database");

		return new ResponseEntity<TeacherEntity>(teacher, HttpStatus.OK);
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.DELETE, value = "deleteTeacher/by-id/{id}")
	public ResponseEntity<?> deleteTeacherByID(@PathVariable Integer id) {
		Optional<TeacherEntity> teacher = teacherRepository.findById(id);

		if (!teacher.isPresent()) {
			logger.error("There is no teacher found with " + id);
			return new ResponseEntity<RESTError>(new RESTError(1, "Teacher not found"), HttpStatus.NOT_FOUND);
		}
		
		for (TeacherSubject teacherSubject : teacher.get().getTeacherSubject()) {
			teacherSubjectRepository.delete(teacherSubject);
		}
		
		teacherRepository.delete(teacher.get());
        logger.info("Deleting the teacher from the database");
		return new ResponseEntity<>("Teacher with ID " + id + " has been successfully deleted.", HttpStatus.OK);
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.DELETE, value = "deleteTeacher/by-username/{username}")
	public ResponseEntity<?> deleteTeacherByUsername(@PathVariable String username) {
		Optional<TeacherEntity> teacher = teacherRepository.findByUsername(username);

		if (!teacher.isPresent()) {
			logger.error("There is no teacher found with " + username);
			return new ResponseEntity<RESTError>(new RESTError(1, "Teacher not found"), HttpStatus.NOT_FOUND);
		}
		
		for (TeacherSubject teacherSubject : teacher.get().getTeacherSubject()) {
			teacherSubjectRepository.delete(teacherSubject);
		}
		
		teacherRepository.delete(teacher.get());
        logger.info("Deleting the teacher from the database");
		return new ResponseEntity<>("Teacher with " + username + " has been successfully deleted.", HttpStatus.OK);
	}

}
