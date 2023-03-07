package com.iktpreobuka.projekat.controllers;

import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.iktpreobuka.projekat.entities.Helpers;
import com.iktpreobuka.projekat.entities.SubjectEntity;
import com.iktpreobuka.projekat.entities.TeacherEntity;
import com.iktpreobuka.projekat.entities.TeacherSubject;
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

	// TODO URADITI SVE/ SKONTATI STA SVE TREBA UOPSTE

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getAllTeacherSubject() {
		List<TeacherSubject> teacherSubjects = (List<TeacherSubject>) teacherSubjectRepository.findAll();

		if (teacherSubjects.isEmpty()) {
			return new ResponseEntity<>("No teaching subjects found", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(teacherSubjects, HttpStatus.OK);
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/newTeacherSubject/subj/{subj_id}/teach/{teacher_id}")
	public ResponseEntity<?> createTeacherSubject(@Valid @RequestBody TeacherSubjectDTO newTeacherSubject,
			@PathVariable Integer teacher_id, @PathVariable Integer subj_id, BindingResult result) {
		if(result.hasErrors()) {
			return new ResponseEntity<>(Helpers.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		}
		
		TeacherEntity teacher = teacherRepository.findById(teacher_id).orElse(null);
		SubjectEntity subject = subjectRepository.findById(subj_id).orElse(null);

		if (teacher == null) {
			return new ResponseEntity<>("No teacher found", HttpStatus.NOT_FOUND);
		}

		if (subject == null) {
			return new ResponseEntity<>("No subject found", HttpStatus.NOT_FOUND);
		}

		TeacherSubject teacherSubjects = new TeacherSubject();

		teacherSubjects.setClassYear(newTeacherSubject.getClassYear());
		teacherSubjects.setSubject(subject);
		teacherSubjects.setTeacher(teacher);

		teacherSubjectRepository.save(teacherSubjects);
		return new ResponseEntity<TeacherSubject>(teacherSubjects, HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/updateTeacherSubject/{id}")
	public ResponseEntity<?> updateTeacherSubject(@RequestBody TeacherSubjectDTO updatedTeacherSubject,
			@PathVariable Integer id) {
		TeacherSubject teacherSubjects = teacherSubjectRepository.findById(id).orElse(null);

		if (teacherSubjects == null) {
			return new ResponseEntity<>("No teaching subject with " + id + " ID found", HttpStatus.NOT_FOUND);
		}

		teacherSubjects.setClassYear(updatedTeacherSubject.getClassYear());

		if (updatedTeacherSubject.getSubject() != null) {
			teacherSubjects.setSubject(updatedTeacherSubject.getSubject());
		}

		if (updatedTeacherSubject.getTeacher() != null) {
			teacherSubjects.setTeacher(updatedTeacherSubject.getTeacher());
		}

		teacherSubjectRepository.save(teacherSubjects);
		return new ResponseEntity<TeacherSubject>(teacherSubjects, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/deleteTeacherSubject/{id}")
	public ResponseEntity<?> deleteTeacherSubject(@PathVariable Integer id) {
		TeacherSubject teacherSubjects = teacherSubjectRepository.findById(id).orElse(null);

		if (teacherSubjects == null) {
			return new ResponseEntity<>("No teaching subject with " + id + " ID found", HttpStatus.NOT_FOUND);
		}

		teacherSubjectRepository.delete(teacherSubjects);
		return new ResponseEntity<>("Teaching subject with id " + id + " was successfully removed", HttpStatus.OK);
	}

}
