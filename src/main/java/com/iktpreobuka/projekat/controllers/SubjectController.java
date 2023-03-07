package com.iktpreobuka.projekat.controllers;

import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.projekat.entities.Helpers;
import com.iktpreobuka.projekat.entities.SubjectEntity;
import com.iktpreobuka.projekat.entities.dto.SubjectDTO;
import com.iktpreobuka.projekat.repositories.SubjectRepository;
import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.utils.RESTError;

@RestController
@RequestMapping(path = "/api/project/subject")
public class SubjectController {

	@Autowired
	private SubjectRepository subjectRepository;
	
	@JsonView(Views.Admin.class)
	protected final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

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

	//find by id, subjname
	
	@RequestMapping(method = RequestMethod.POST, value = "/newSubject")
	public ResponseEntity<?> createSubject(@Valid @RequestBody SubjectDTO newSubject, BindingResult result) {
		
		SubjectEntity existingSubject = subjectRepository.findBySubjectName(newSubject.getSubjectName());
        logger.info("Checking whether theres an existing subject in the database");

		if(result.hasErrors()) {
	        logger.info("Validating input parameters for subject");
			return new ResponseEntity<>(Helpers.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		}
		
		if (existingSubject != null) {
	        logger.error("Subject with the same name already exists");
			return new ResponseEntity<RESTError>(new RESTError(1, "A subject with the same name already exists"), HttpStatus.CONFLICT);
		}

		SubjectEntity subject = new SubjectEntity();
		subject.setSubjectName(newSubject.getSubjectName());
		subject.setFondCasova(newSubject.getFondCasova());

		subjectRepository.save(subject);
        logger.info("Saving subject to the database");
        
		return new ResponseEntity<SubjectEntity>(subject, HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/updateSubject/{id}")
	public ResponseEntity<?> updateSubject(@Valid @RequestBody SubjectDTO updatedSubject, @PathVariable Integer id, BindingResult result) {
		
		SubjectEntity subject = subjectRepository.findById(id).orElse(null);

		if(result.hasErrors()) {
	        logger.info("Validating input parameters for subject");
			return new ResponseEntity<>(Helpers.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		}
		
		if (subject == null) {
	        logger.error("No subject with " + id + " ID found");
			return new ResponseEntity<RESTError>(new RESTError(1, "No subject with " + id + " ID found"), HttpStatus.NOT_FOUND);
		}

		subject.setSubjectName(updatedSubject.getSubjectName());
		subject.setFondCasova(updatedSubject.getFondCasova());

		subjectRepository.save(subject);
        logger.info("Saving subject to the database");

		return new ResponseEntity<SubjectEntity>(subject, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/deleteSubject/by-id/{id}")
	public ResponseEntity<?> deleteSubject(@PathVariable Integer id) {
		
		SubjectEntity subject = subjectRepository.findById(id).orElse(null);

		if (subject == null) {
	        logger.error("No subject with " + id + " ID found");
			return new ResponseEntity<RESTError>(new RESTError(1, "No subject with " + id + " ID found"), HttpStatus.NOT_FOUND);
		}

		subjectRepository.delete(subject);
        logger.info("Deleting subject from the database");

		return new ResponseEntity<SubjectEntity>(subject, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/deleteSubject/by-name/{name}")
	public ResponseEntity<?> deleteSubjectByName(@PathVariable String name) {
		
		SubjectEntity subject = subjectRepository.findBySubjectName(name);

		if (subject == null) {
	        logger.error("No subject with name: " + name + " found");
			return new ResponseEntity<RESTError>(new RESTError(1, "No subject called " + name + " found"), HttpStatus.NOT_FOUND);
		}

		subjectRepository.delete(subject);
        logger.info("Deleting subject from the database");

		return new ResponseEntity<SubjectEntity>(subject, HttpStatus.OK);
	}

}
