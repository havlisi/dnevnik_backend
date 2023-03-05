package com.iktpreobuka.projekat.controllers;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iktpreobuka.projekat.entities.SubjectEntity;
import com.iktpreobuka.projekat.entities.dto.SubjectDTO;
import com.iktpreobuka.projekat.repositories.SubjectRepository;

@RestController
@RequestMapping(path = "/api/project/subject")
public class SubjectController {

	@Autowired
	private SubjectRepository subjectRepository;

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getAllSubjects() {
		List<SubjectEntity> subjects = (List<SubjectEntity>) subjectRepository.findAll();

		if (subjects.isEmpty()) {
			return new ResponseEntity<>("No subjects found", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(subjects, HttpStatus.OK);
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/newSubject")
	public ResponseEntity<?> createSubject(@Valid @RequestBody SubjectDTO newSubject, BindingResult result) {
		SubjectEntity existingSubject = subjectRepository.findBySubjectName(newSubject.getSubjectName());
		if(result.hasErrors()) {
			return new ResponseEntity<>(createErrorMessage(result), HttpStatus.BAD_REQUEST);
		}
		
		if (existingSubject != null) {
			return new ResponseEntity<>("A subject with the same name already exists", HttpStatus.CONFLICT);
		}

		SubjectEntity subject = new SubjectEntity();
		subject.setSubjectName(newSubject.getSubjectName());
		subject.setFondCasova(newSubject.getFondCasova());

		subjectRepository.save(subject);
		return new ResponseEntity<>(subject, HttpStatus.CREATED);
	}

	private String createErrorMessage(BindingResult result) {
		return result.getAllErrors().stream().map(ObjectError::getDefaultMessage).
				collect(Collectors.joining(" "));
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/updateSubject/{id}")
	public ResponseEntity<?> updateSubject(@RequestBody SubjectDTO updatedSubject, @PathVariable Integer id) {
		SubjectEntity subject = subjectRepository.findById(id).orElse(null);

		if (subject == null) {
			return new ResponseEntity<>("No subject with " + id + " ID found", HttpStatus.NOT_FOUND);
		}

		subject.setSubjectName(updatedSubject.getSubjectName());
		subject.setFondCasova(updatedSubject.getFondCasova());

		subjectRepository.save(subject);
		return new ResponseEntity<SubjectEntity>(subject, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/deleteSubject/by-id/{id}")
	public ResponseEntity<?> deleteSubject(@PathVariable Integer id) {
		SubjectEntity subject = subjectRepository.findById(id).orElse(null);

		if (subject == null) {
			return new ResponseEntity<>("No subject with " + id + " ID found", HttpStatus.NOT_FOUND);
		}

		subjectRepository.delete(subject);
		return new ResponseEntity<SubjectEntity>(subject, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/deleteSubject/by-name/{name}")
	public ResponseEntity<?> deleteSubjectByName(@PathVariable String name) {
		SubjectEntity subject = subjectRepository.findBySubjectName(name);

		if (subject == null) {
			return new ResponseEntity<>("No subject called " + name + " found", HttpStatus.NOT_FOUND);
		}

		subjectRepository.delete(subject);
		return new ResponseEntity<SubjectEntity>(subject, HttpStatus.OK);
	}

}
