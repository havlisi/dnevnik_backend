package com.iktpreobuka.projekat.services;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.projekat.entities.ParentEntity;
import com.iktpreobuka.projekat.entities.StudentEntity;
import com.iktpreobuka.projekat.entities.TeacherEntity;
import com.iktpreobuka.projekat.entities.TeacherSubject;
import com.iktpreobuka.projekat.entities.UserEntity;
import com.iktpreobuka.projekat.entities.dto.UserDTO;
import com.iktpreobuka.projekat.repositories.ParentRepository;
import com.iktpreobuka.projekat.repositories.StudentRepository;
import com.iktpreobuka.projekat.repositories.UserRepository;
import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.utils.ErrorMessageHelper;
import com.iktpreobuka.projekat.utils.RESTError;
import com.iktpreobuka.projekat.utils.UserCustomValidator;

@Service
public class ParentDaoImpl {

	@Autowired
	private ParentRepository parentRepository;

	@Autowired
	private StudentRepository studentRepository;
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	UserCustomValidator userValidator;

	@JsonView(Views.Admin.class)
	protected final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
	
	
	public ResponseEntity<?> getParentByStudent(Integer student_id, Authentication authentication) {
		
		String signedInUserEmail = authentication.getName();
		UserEntity currentUser = userRepository.findByEmail(signedInUserEmail);
		
		StudentEntity student = studentRepository.findById(student_id).orElse(null);
		ParentEntity parent = parentRepository.findByStudent(student);

		if (student == null) {
	        logger.error("No student found in the database with " + student_id);
			return new ResponseEntity<RESTError>(new RESTError(1, "No student found"), HttpStatus.NOT_FOUND);
		}

		if (parent == null) {
	        logger.error("No such parent found in the database");
			return new ResponseEntity<RESTError>(new RESTError(2, "No parent found"), HttpStatus.NOT_FOUND);
		}
		
		logger.info("Checking who is the logged in user.");
		
		if (currentUser.getRole().equals("ROLE_TEACHER")) {
			logger.info("Logged in user is a teacher.");
		    TeacherEntity teacher = (TeacherEntity) currentUser;
		    boolean isTeachingStudent = false;
		    for (TeacherSubject teachingSubject : teacher.getTeacherSubject()) {
		        if (teachingSubject.getStudents().contains(student)) {
					logger.info("Correct! Student is taking this teaching subject");
		            isTeachingStudent = true;
		        }
		    }
		    if (isTeachingStudent) {
				logger.info("Teacher is finding students parent.");
		        return new ResponseEntity<ParentEntity>(parent, HttpStatus.OK);
		    } else {
		        logger.error("Teacher is unauthorized to get parent from " + student.getFirstName() + " " + student.getLastName());
		        RESTError error = new RESTError(3, "Teacher is unauthorized to get parent from " + student.getFirstName() + " " + student.getLastName());
		        return new ResponseEntity<RESTError>(error, HttpStatus.UNAUTHORIZED);
		    }
		}
		
		if (currentUser.getRole().equals("ROLE_ADMIN")) {
			logger.info("Admin is finding students parent.");
			return new ResponseEntity<ParentEntity>(parent, HttpStatus.OK);
		}
		
		RESTError error = new RESTError(4, "User is unauthorized to get parent from " + student.getFirstName() + " " + student.getLastName());
        return new ResponseEntity<RESTError>(error, HttpStatus.UNAUTHORIZED);
	}
	
	
	public ResponseEntity<?> createParent(UserDTO newUser, BindingResult result) {

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

		ParentEntity newParent = new ParentEntity();

		newParent.setFirstName(newUser.getFirstName());
		newParent.setLastName(newUser.getLastName());
		newParent.setUsername(newUser.getUsername());
		newParent.setEmail(newUser.getEmail());
		newParent.setPassword(newUser.getPassword());
		
		newParent.setRole("ROLE_PARENT");
        logger.info("Setting users role.");

		parentRepository.save(newParent);
        logger.info("Saving parent to the database");
		
		return new ResponseEntity<ParentEntity>(newParent, HttpStatus.CREATED);
	}
	
	
	public ResponseEntity<?> updateParent(UserDTO updatedUser, BindingResult result, Integer id) {

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

		ParentEntity parent = parentRepository.findById(id).orElse(null);

		if (parent == null) {
	        logger.error("There is no parent found with " + id);
			return new ResponseEntity<RESTError>(new RESTError(1, "No parents found"), HttpStatus.NOT_FOUND);
		}

		parent.setFirstName(updatedUser.getFirstName());
		parent.setLastName(updatedUser.getLastName());
		parent.setUsername(updatedUser.getUsername());
		parent.setEmail(updatedUser.getEmail());
		parent.setPassword(updatedUser.getPassword());

		parentRepository.save(parent);
        logger.info("Saving parent to the database");

		return new ResponseEntity<ParentEntity>(parent, HttpStatus.OK);
	}
	
	
	public ResponseEntity<?> deleteParentByID(Integer id) {
		Optional<ParentEntity> parent = parentRepository.findById(id);

		if (parent.isPresent()) {
			parentRepository.delete(parent.get());	        
			logger.info("Deleting the parent from the database");
			return new ResponseEntity<>("Parent with ID " + id + " has been successfully deleted.", HttpStatus.OK);
		} else {
	        logger.error("There is no parent found with " + id);
			return new ResponseEntity<RESTError>(new RESTError(1, "No parents found"), HttpStatus.NOT_FOUND);
		}
	}
	
	
	public ResponseEntity<?> deleteParentByUsername(String username) {
		Optional<ParentEntity> parent = parentRepository.findByUsername(username);

		if (parent.isPresent()) {
			parentRepository.delete(parent.get());
	        logger.info("Deleting the parent from the database");
			return new ResponseEntity<>("Parent with " + username + " username has been successfully deleted.",
					HttpStatus.OK);
		} else {
	        logger.error("There is no parent found with " + username);
			return new ResponseEntity<RESTError>(new RESTError(1, "No parents found"), HttpStatus.NOT_FOUND);
		}

	}
	
}
