package com.iktpreobuka.projekat.services;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.projekat.entities.TeacherEntity;
import com.iktpreobuka.projekat.entities.TeacherSubject;
import com.iktpreobuka.projekat.entities.UserEntity;
import com.iktpreobuka.projekat.entities.dto.UserDTO;
import com.iktpreobuka.projekat.repositories.TeacherRepository;
import com.iktpreobuka.projekat.repositories.TeacherSubjectRepository;
import com.iktpreobuka.projekat.repositories.UserRepository;
//import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.utils.ErrorMessageHelper;
import com.iktpreobuka.projekat.utils.RESTError;
import com.iktpreobuka.projekat.utils.UserCustomValidator;

@Service
public class TeacherDaoImpl implements TeacherDao {

	@Autowired
	private TeacherRepository teacherRepository;
	
	@Autowired
	private TeacherSubjectRepository teacherSubjectRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	UserCustomValidator userValidator;
	
	//@JsonView(Views.Admin.class)
	protected final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	
	public ResponseEntity<?> createTeacher(UserDTO newUser, BindingResult result) {
		
		if (result.hasErrors()) {
	        logger.error("Sent incorrect parameters.");
			return new ResponseEntity<>(ErrorMessageHelper.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		} else {
	        logger.info("Validating if the users password matches the confirming password");
			userValidator.validate(newUser, result);
			if (result.hasErrors()) {
		        logger.error("Validation errors detected.");
		        return new ResponseEntity<>(result.getFieldError(), HttpStatus.BAD_REQUEST);
		    }
		}

		UserEntity existingUserWithEmail = userRepository.findByEmail(newUser.getEmail());
        logger.info("Finding out whether there's a user with the same email.");
        
		UserEntity existingUserWithUsername = userRepository.findByUsername(newUser.getUsername());
        logger.info("Finding out whether there's a user with the same username.");

		if (existingUserWithEmail != null) {
	        logger.error("There is a user with the same email.");
			return new ResponseEntity<RESTError>(new RESTError(1, "Email already exists"), HttpStatus.CONFLICT);
		}

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
	
	
	public ResponseEntity<?> updateTeacher(UserDTO updatedUser, BindingResult result, Integer id) {

		if (result.hasErrors()) {
	        logger.error("Sent incorrect parameters.");
			return new ResponseEntity<>(ErrorMessageHelper.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		} else {
	        logger.info("Validating if the users password matches the confirming password");
			userValidator.validate(updatedUser, result);
			if (result.hasErrors()) {
		        logger.error("Validation errors detected.");
		        return new ResponseEntity<>(result.getFieldError(), HttpStatus.BAD_REQUEST);
		    }
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
		teacher.setPassword(updatedUser.getPassword());

		teacherRepository.save(teacher);
        logger.info("Saving teacher to the database");

		return new ResponseEntity<TeacherEntity>(teacher, HttpStatus.OK);
	}
	
	
	public ResponseEntity<?> deleteTeacherByID(Integer id) {
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
	
	public ResponseEntity<?> deleteTeacherByUsername(String username) {
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
