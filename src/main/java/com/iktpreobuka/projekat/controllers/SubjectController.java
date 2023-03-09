package com.iktpreobuka.projekat.controllers;

import java.util.List;
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
import com.iktpreobuka.projekat.entities.SubjectEntity;
import com.iktpreobuka.projekat.entities.dto.SubjectDTO;
import com.iktpreobuka.projekat.repositories.SubjectRepository;
import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.services.SubjectDaoImpl;
import com.iktpreobuka.projekat.utils.RESTError;

@RestController
@RequestMapping(path = "/api/project/subject")
public class SubjectController {

	@Autowired
	private SubjectRepository subjectRepository;
	
	@Autowired
	private SubjectDaoImpl subjectDaoImpl;
	
	@JsonView(Views.Admin.class)
	protected final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	
	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getAllSubjects() {
		List<SubjectEntity> subjects = (List<SubjectEntity>) subjectRepository.findAll();

		if (subjects.isEmpty()) {
	        logger.error("No subjects found in the database.");
			return new ResponseEntity<RESTError>(new RESTError(1, "No subjects found"), HttpStatus.NOT_FOUND);
		} else {
	        logger.info("Found subject(s) in the database");
			return new ResponseEntity<List<SubjectEntity>>(subjects, HttpStatus.OK);
		}
	}
	
	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.POST, value = "/newSubject")
	public ResponseEntity<?> createSubject(@Valid @RequestBody SubjectDTO newSubject, BindingResult result) {
		return subjectDaoImpl.createSubject(newSubject, result);
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.PUT, value = "/updateSubject/{id}")
	public ResponseEntity<?> updateSubject(@Valid @RequestBody SubjectDTO updatedSubject, BindingResult result, @PathVariable Integer id) {
		return subjectDaoImpl.updateSubject(updatedSubject, result, id);
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.DELETE, value = "/deleteSubject/by-id/{id}")
	public ResponseEntity<?> deleteSubject(@PathVariable Integer id) {
		return subjectDaoImpl.deleteSubject(id);
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.DELETE, value = "/deleteSubject/by-name/{name}")
	public ResponseEntity<?> deleteSubjectByName(@PathVariable String name) {
		return subjectDaoImpl.deleteSubjectByName(name);
	}

}
