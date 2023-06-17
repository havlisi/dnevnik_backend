package com.iktpreobuka.projekat.controllers;

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.projekat.entities.ParentEntity;
import com.iktpreobuka.projekat.entities.StudentEntity;
import com.iktpreobuka.projekat.entities.SubjectEntity;
import com.iktpreobuka.projekat.entities.TeacherEntity;
import com.iktpreobuka.projekat.entities.TeacherSubject;
import com.iktpreobuka.projekat.entities.UserEntity;
import com.iktpreobuka.projekat.entities.dto.SubjectDTO;
import com.iktpreobuka.projekat.repositories.SubjectRepository;
import com.iktpreobuka.projekat.repositories.UserRepository;
import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.services.SubjectDaoImpl;
import com.iktpreobuka.projekat.utils.RESTError;

@RestController
@RequestMapping(path = "/api/project/subject")
@CrossOrigin(origins = "http://localhost:3000")
public class SubjectController {

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private SubjectDaoImpl subjectDaoImpl;

	@JsonView(Views.Admin.class)
	protected final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	@Secured({ "ROLE_ADMIN", "ROLE_TEACHER", "ROLE_STUDENT", "ROLE_PARENT" })
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getAllSubjects(Authentication authentication) {
		String email = (String) authentication.getName();
		UserEntity ulogovanUser = userRepository.findByEmail(email);

		if (ulogovanUser.getRole().equals("ROLE_ADMIN")) {
			Iterable<SubjectEntity> allSubjects = subjectRepository.findAll();

			return new ResponseEntity<>(allSubjects, HttpStatus.OK);

		} else if (ulogovanUser.getRole().equals("ROLE_TEACHER")) {
			TeacherEntity ulogovanNastavnik = (TeacherEntity) ulogovanUser;
			List<SubjectEntity> subjects = new ArrayList<>();

			for (TeacherSubject nastavnikovPredmet : ulogovanNastavnik.getTeacherSubject()) {
				subjects.add(nastavnikovPredmet.getSubject());
			}

			return new ResponseEntity<>(subjects, HttpStatus.OK);
		} else if (ulogovanUser.getRole().equals("ROLE_STUDENT")) {
			StudentEntity ulogovanStudent = (StudentEntity) ulogovanUser;
			List <SubjectEntity> subjects = new ArrayList<>();
			
			for (TeacherSubject teachingSubject : ulogovanStudent.getTeacherSubjects()) {
				subjects.add(teachingSubject.getSubject());
			}
			
			return new ResponseEntity<>(subjects, HttpStatus.OK);
			
		} else if (ulogovanUser.getRole().equals("ROLE_PARENT")) {
			ParentEntity ulogovanParent = (ParentEntity) ulogovanUser;
			List <SubjectEntity> subjects = new ArrayList<>();
			
			for (StudentEntity student : ulogovanParent.getStudent()) {
				for (TeacherSubject teachingSubject1 : student.getTeacherSubjects()) {
					boolean found = false;
					for (SubjectEntity teachingSubject2 : subjects) {
						if (teachingSubject1.getSubject().getId().equals(teachingSubject2.getId())) {
							found = true;
							break;
						}
					}
					if (!found) {
						subjects.add(teachingSubject1.getSubject());
					}
				}
			}
			
			return new ResponseEntity<>(subjects, HttpStatus.OK);
			
		} else {
			logger.info("Not authorized to see all subjects");
			return new ResponseEntity<RESTError>(new RESTError(1, "Not authorized to see subjects"),
					HttpStatus.FORBIDDEN);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{id}")
	public ResponseEntity<?> getSubjectById(@PathVariable Integer id) {
		Optional<SubjectEntity> subject = subjectRepository.findById(id);
		if (subject.isPresent()) {
			return new ResponseEntity<SubjectEntity>(subject.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<RESTError>(new RESTError(1, "No subject found"), HttpStatus.NOT_FOUND);
		}
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.POST, value = "/newSubject")
	public ResponseEntity<?> createSubject(@Valid @RequestBody SubjectDTO newSubject, BindingResult result) {
		return subjectDaoImpl.createSubject(newSubject, result);
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.PUT, value = "/updateSubject/{id}")
	public ResponseEntity<?> updateSubject(@Valid @RequestBody SubjectDTO updatedSubject, BindingResult result,
			@PathVariable Integer id) {
		return subjectDaoImpl.updateSubject(updatedSubject, result, id);
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.DELETE, value = "/deleteSubject/by-id/{id}")
	public ResponseEntity<?> deleteSubjectById(@PathVariable Integer id) {
		return subjectDaoImpl.deleteSubject(id);
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.DELETE, value = "/deleteSubject/by-name/{name}")
	public ResponseEntity<?> deleteSubjectByName(@PathVariable String name) {
		return subjectDaoImpl.deleteSubjectByName(name);
	}

}
