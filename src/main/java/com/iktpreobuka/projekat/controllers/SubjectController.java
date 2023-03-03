package com.iktpreobuka.projekat.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
		return new ResponseEntity<List<SubjectEntity>>((List<SubjectEntity>) subjectRepository.findAll(),
				HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/newSubject")
	public ResponseEntity<?> createSubject(@RequestBody SubjectDTO newSubject) {		
		SubjectEntity subject = new SubjectEntity();
		
		subject.setSubjectName(newSubject.getSubjectName());
		subject.setFondCasova(newSubject.getFondCasova());

		subjectRepository.save(subject);
		return new ResponseEntity<SubjectEntity>(subject, HttpStatus.CREATED);
	}
	
	@RequestMapping(method = RequestMethod.PUT, value = "/updateSubjcet/{id}")
	public ResponseEntity<?> updateSubject(@RequestBody SubjectDTO updatedSubject,
			@PathVariable Integer id) {		
		SubjectEntity subject = subjectRepository.findById(id).orElse(null);
		
		if (subject == null) {
			return new ResponseEntity<>("No subject with " + id + " ID found", HttpStatus.NOT_FOUND);
		}
		
		subject.setSubjectName(updatedSubject.getSubjectName());
		subject.setFondCasova(updatedSubject.getFondCasova());

		subjectRepository.save(subject);
		return new ResponseEntity<SubjectEntity>(subject, HttpStatus.CREATED);
	}
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/deleteSubjcet/{id}")
	public ResponseEntity<?> deleteSubject(@PathVariable Integer id) {		
		SubjectEntity subject = subjectRepository.findById(id).orElse(null);
		
		if (subject == null) {
			return new ResponseEntity<>("No subject with " + id + " ID found", HttpStatus.NOT_FOUND);
		}

		subjectRepository.delete(subject);
		return new ResponseEntity<SubjectEntity>(subject, HttpStatus.CREATED);
	}
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/deleteSubjcet/{name}")
	public ResponseEntity<?> deleteSubjectByName(@PathVariable String name) {		
		SubjectEntity subject = subjectRepository.findByName(name).orElse(null);
		
		if (subject == null) {
			return new ResponseEntity<>("No subject called " + name + " found", HttpStatus.NOT_FOUND);
		}

		subjectRepository.delete(subject);
		return new ResponseEntity<SubjectEntity>(subject, HttpStatus.CREATED);
	}
	
}
