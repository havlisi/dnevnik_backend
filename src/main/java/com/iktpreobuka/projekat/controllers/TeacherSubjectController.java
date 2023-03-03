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

import com.iktpreobuka.projekat.entities.GradeEntity;
import com.iktpreobuka.projekat.entities.SubjectEntity;
import com.iktpreobuka.projekat.entities.TeacherSubject;
import com.iktpreobuka.projekat.entities.dto.SubjectDTO;
import com.iktpreobuka.projekat.entities.dto.TeacherSubjectDTO;
import com.iktpreobuka.projekat.repositories.SubjectRepository;
import com.iktpreobuka.projekat.repositories.TeacherRepository;
import com.iktpreobuka.projekat.repositories.TeacherSubjectRepository;

@RestController
@RequestMapping(path = "/api/project/teacherSubject")
public class TeacherSubjectController {

	@Autowired
	private TeacherSubjectRepository teacherSubjectRepository;
	
	@Autowired
	private SubjectRepository subjectRepository;
	
	@Autowired
	private TeacherRepository teacherRepository;
	
	//TODO URADITI SVE/ SKONTATI STA SVE TREBA UOPSTE
	
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getAllTeacherSubject() {
		List<TeacherSubject> teacherSubjects = (List<TeacherSubject>) teacherSubjectRepository.findAll();

		if (teacherSubjects.isEmpty()) {
			return new ResponseEntity<>("No teaching subjects found", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(teacherSubjects, HttpStatus.OK);
		}
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/newTeacherSubject")
	public ResponseEntity<?> createTeacherSubject(@RequestBody TeacherSubjectDTO newTeacherSubject) {		
		TeacherSubject teacherSubjects = new TeacherSubject();
		
		teacherSubjects.setClassYear(newTeacherSubject.getClassYear());
		teacherSubjects.setSubject(newTeacherSubject.getSubject()); //ubaciti preko id-a
		teacherSubjects.setTeacher(newTeacherSubject.getTeacher());

		teacherSubjectRepository.save(teacherSubjects);
		return new ResponseEntity<TeacherSubject>(teacherSubjects, HttpStatus.CREATED);
	}
	
	@RequestMapping(method = RequestMethod.PUT, value = "/updateTeacherSubject/{id}")
	public ResponseEntity<?> updateTeacherSubject(@RequestBody SubjectDTO updatedSubject,
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
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/deleteTeacherSubject/{id}")
	public ResponseEntity<?> deleteTeacherSubject(@PathVariable Integer id) {		
		SubjectEntity subject = subjectRepository.findById(id).orElse(null);
		
		if (subject == null) {
			return new ResponseEntity<>("No subject with " + id + " ID found", HttpStatus.NOT_FOUND);
		}

		subjectRepository.delete(subject);
		return new ResponseEntity<SubjectEntity>(subject, HttpStatus.CREATED);
	}
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/deleteTeacherSubject/{name}")
	public ResponseEntity<?> deleteTeacherSubjectByName(@PathVariable String name) {		
		SubjectEntity subject = subjectRepository.findBySubjectName(name);
		
		if (subject == null) {
			return new ResponseEntity<>("No subject called " + name + " found", HttpStatus.NOT_FOUND);
		}

		subjectRepository.delete(subject);
		return new ResponseEntity<SubjectEntity>(subject, HttpStatus.CREATED);
	}
	
}
