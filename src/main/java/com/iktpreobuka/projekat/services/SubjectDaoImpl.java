package com.iktpreobuka.projekat.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.projekat.entities.SubjectEntity;
import com.iktpreobuka.projekat.entities.TeacherSubject;
import com.iktpreobuka.projekat.entities.dto.SubjectDTO;
import com.iktpreobuka.projekat.repositories.SubjectRepository;
import com.iktpreobuka.projekat.repositories.TeacherSubjectRepository;
import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.utils.ErrorMessageHelper;
import com.iktpreobuka.projekat.utils.RESTError;

@Service
public class SubjectDaoImpl {
	
	@Autowired
	private SubjectRepository subjectRepository;
	
	@Autowired
	private TeacherSubjectRepository teacherSubjectRepository;
	
	@JsonView(Views.Admin.class)
	protected final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	
	public ResponseEntity<?> createSubject(SubjectDTO newSubject, BindingResult result) {

		if (result.hasErrors()) {
	        logger.info("Validating input parameters for subject");
			return new ResponseEntity<>(ErrorMessageHelper.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		}
		
		SubjectEntity existingSubject = subjectRepository.findBySubjectName(newSubject.getSubjectName());
        logger.info("Checking whether theres an existing subject in the database");

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
	
	public ResponseEntity<?> updateSubject(SubjectDTO updatedSubject, BindingResult result, Integer id) {

		if (result.hasErrors()) {
	        logger.info("Validating input parameters for subject");
			return new ResponseEntity<>(ErrorMessageHelper.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		}
		
		SubjectEntity subject = subjectRepository.findById(id).orElse(null);

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
	
	
	public ResponseEntity<?> deleteSubject(Integer id) {
		
		SubjectEntity subject = subjectRepository.findById(id).orElse(null);

		if (subject == null) {
	        logger.error("No subject with " + id + " ID found");
			return new ResponseEntity<RESTError>(new RESTError(1, "No subject with " + id + " ID found"), HttpStatus.NOT_FOUND);
		}

		for (TeacherSubject teachingSubject : subject.getTeacherSubjects()) {
			teacherSubjectRepository.delete(teachingSubject);
		}
		
		subjectRepository.delete(subject);
        logger.info("Deleting subject from the database");

		return new ResponseEntity<SubjectEntity>(subject, HttpStatus.OK);
	}
	
	public ResponseEntity<?> deleteSubjectByName(String name) {
			
		SubjectEntity subject = subjectRepository.findBySubjectName(name);

		if (subject == null) {
	        logger.error("No subject with name: " + name + " found");
			return new ResponseEntity<RESTError>(new RESTError(1, "No subject called " + name + " found"), HttpStatus.NOT_FOUND);
		}
		
		for (TeacherSubject teachingSubject : subject.getTeacherSubjects()) {
			teacherSubjectRepository.delete(teachingSubject);
		}
		
		subjectRepository.delete(subject);
        logger.info("Deleting subject from the database");

		return new ResponseEntity<SubjectEntity>(subject, HttpStatus.OK);
	}
	
}
