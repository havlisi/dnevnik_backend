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
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.projekat.utils.ErrorMessageHelper;
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
import com.iktpreobuka.projekat.utils.RESTError;
import com.iktpreobuka.projekat.utils.UserCustomValidator;

@RestController
@RequestMapping(path = "/api/project/parent")
public class ParentController {

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
	
	
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getAllParents() {
		List<ParentEntity> parents = (List<ParentEntity>) parentRepository.findAll();

		if (parents.isEmpty()) {
	        logger.error("No parents found in the database.");
			return new ResponseEntity<RESTError>(new RESTError(1, "No parents found"), HttpStatus.NOT_FOUND);
		}
	    logger.info("Found parent(s) in the database");
		return new ResponseEntity<List<ParentEntity>>(parents, HttpStatus.OK);
	}

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, value = "/by-id/{id}")
	public ResponseEntity<?> getParentById(@PathVariable Integer id) {
		Optional<ParentEntity> parent = parentRepository.findById(id);
		if (!parent.isPresent()) {
			logger.error("No parent found in the database with: " + id + ".");
			return new ResponseEntity<RESTError>(new RESTError(1, "No parent found"), HttpStatus.NOT_FOUND);  
		}
		logger.info("Parent found in the database: " + parent.get().getFirstName() + parent.get().getLastName() + ".");
		return new ResponseEntity<ParentEntity>(parent.get(), HttpStatus.OK);
	}

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, value = "/by-username/{username}")
	public ResponseEntity<?> getParentByUsername(@PathVariable String username) {
		Optional<ParentEntity> parent = parentRepository.findByUsername(username);
		if (!parent.isPresent()) {
			logger.error("No parent found in the database with " + username + ".");
			return new ResponseEntity<RESTError>(new RESTError(1, "No parent found"), HttpStatus.NOT_FOUND); 
		}
		logger.info("Parent found in the database: " + parent.get().getUsername() + ".");
		return new ResponseEntity<ParentEntity>(parent.get(), HttpStatus.OK);
	}

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, value = "/by-firstName/{firstName}")
	public ResponseEntity<?> getParentByFirstName(@PathVariable String firstName) {
		List<ParentEntity> parents = parentRepository.findByFirstName(firstName);
		if (parents.isEmpty()) {
	        logger.error("No parents found in the database with name: " + firstName);
			return new ResponseEntity<RESTError>(new RESTError(1, "No parents found"), HttpStatus.NOT_FOUND);
		}
	    logger.info("Found parents with name - " + firstName + " in the database.");
	    return new ResponseEntity<List<ParentEntity>>(parents, HttpStatus.OK);
	}

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, value = "/by-lastName/{lastName}")
	public ResponseEntity<?> getParentsByLastName(@PathVariable String lastName) {
		List<ParentEntity> parents = parentRepository.findByLastName(lastName);
		if (parents.isEmpty()) {
	        logger.error("No parents found in the database with lastname: " + lastName);
			return new ResponseEntity<RESTError>(new RESTError(1, "No parents found"), HttpStatus.NOT_FOUND);
		}
		logger.info("Found parents with lastname: " + lastName + " in the database.");
		return new ResponseEntity<List<ParentEntity>>(parents, HttpStatus.OK);
	}

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, value = "/by-firstLetter/{firstLetter}")
	public ResponseEntity<?> getParentByFirstLetter(@PathVariable String firstLetter) {
		List<ParentEntity> parents = parentRepository.findByFirstNameStartingWith(firstLetter);
		if (parents.isEmpty()) {
	        logger.error("No parents found in the database with first letter of the name " + firstLetter);
			return new ResponseEntity<RESTError>(new RESTError(1, "No parents found"), HttpStatus.NOT_FOUND);
		} else {
	        logger.info("Found parents in the database with first letter of the name " + firstLetter);
			return new ResponseEntity<List<ParentEntity>>(parents, HttpStatus.OK);
		}
	}

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, value = "/by-email/{email}")
	public ResponseEntity<?> getParentByEmail(@PathVariable String email) {
		Optional<ParentEntity> parent = parentRepository.findByEmail(email);
		if (parent.isPresent()) {
	        logger.info("Found parent in the database with " + parent.get().getEmail());
			return new ResponseEntity<ParentEntity>(parent.get(), HttpStatus.OK);
		} else {
	        logger.error("No parents found in the database with " + email);
			return new ResponseEntity<RESTError>(new RESTError(1, "No parent found"), HttpStatus.NOT_FOUND);
		}
	}

	@Secured({"ROLE_ADMIN", "ROLE_TEACHER"})
	@RequestMapping(method = RequestMethod.GET, value = "/by-student/{student_id}")
	public ResponseEntity<?> getParentByStudent(@PathVariable Integer student_id, Authentication authentication) {
		
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
				logger.info("Teacher is findig students parent.");
		        return new ResponseEntity<ParentEntity>(parent, HttpStatus.OK);
		    } else {
		        logger.error("Teacher is unauthorized to get parent from " + student.getFirstName() + " " + student.getLastName());
		        RESTError error = new RESTError(3, "Teacher is unauthorized to get parent from " + student.getFirstName() + " " + student.getLastName());
		        return new ResponseEntity<RESTError>(error, HttpStatus.UNAUTHORIZED);
		    }
		}
		
		if (currentUser.getRole().equals("ROLE_ADMIN")) {
			logger.info("Admin is findig students parent.");
			return new ResponseEntity<ParentEntity>(parent, HttpStatus.OK);
		}
		
		RESTError error = new RESTError(4, "User is unauthorized to get parent from " + student.getFirstName() + " " + student.getLastName());
        return new ResponseEntity<RESTError>(error, HttpStatus.UNAUTHORIZED);
	}

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.POST, value = "/newParentUser")
	public ResponseEntity<?> createParent(@Valid @RequestBody UserDTO newUser, BindingResult result) {

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

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.PUT, value = "/updateParent/{id}")
	public ResponseEntity<?> updateParent(@Valid @RequestBody UserDTO updatedUser, BindingResult result, @PathVariable Integer id) {

		if (result.hasErrors()) {
	        logger.error("Sent incorrect parameters.");
			return new ResponseEntity<>(ErrorMessageHelper.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		} else {
	        logger.info("Validating if the users password matches the confirming password");
			userValidator.validate(updatedUser, result);
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

		parentRepository.save(parent);
        logger.info("Saving parent to the database");

		return new ResponseEntity<ParentEntity>(parent, HttpStatus.OK);
	}

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.DELETE, value = "deleteParent/by-id/{id}")
	public ResponseEntity<?> deleteParentByID(@PathVariable Integer id) {
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

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.DELETE, value = "deleteParent/by-username/{username}")
	public ResponseEntity<?> deleteParentByUsername(@PathVariable String username) {
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
