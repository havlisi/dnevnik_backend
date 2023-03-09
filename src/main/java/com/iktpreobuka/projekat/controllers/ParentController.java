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
import com.iktpreobuka.projekat.entities.ParentEntity;
import com.iktpreobuka.projekat.entities.dto.UserDTO;
import com.iktpreobuka.projekat.repositories.ParentRepository;
import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.services.ParentDaoImpl;
import com.iktpreobuka.projekat.utils.RESTError;
import com.iktpreobuka.projekat.utils.UserCustomValidator;

@RestController
@RequestMapping(path = "/api/project/parent")
public class ParentController {

	@Autowired
	private ParentRepository parentRepository;

	@Autowired
	UserCustomValidator userValidator;
	
	@Autowired
	ParentDaoImpl parentDaoImpl;
	
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
        return parentDaoImpl.getParentByStudent(student_id, authentication);
	}

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.POST, value = "/newParentUser")
	public ResponseEntity<?> createParent(@Valid @RequestBody UserDTO newUser, BindingResult result) {
        return parentDaoImpl.createParent(newUser, result);
	}

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.PUT, value = "/updateParent/{id}")
	public ResponseEntity<?> updateParent(@Valid @RequestBody UserDTO updatedUser, BindingResult result, @PathVariable Integer id) {
		return parentDaoImpl.updateParent(updatedUser, result, id);
	}

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.DELETE, value = "deleteParent/by-id/{id}")
	public ResponseEntity<?> deleteParentByID(@PathVariable Integer id) {
		return parentDaoImpl.deleteParentByID(id);
	}

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.DELETE, value = "deleteParent/by-username/{username}")
	public ResponseEntity<?> deleteParentByUsername(@PathVariable String username) {
		return parentDaoImpl.deleteParentByUsername(username);
	}

}
