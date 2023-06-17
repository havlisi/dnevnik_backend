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
import com.iktpreobuka.projekat.entities.TeacherEntity;
import com.iktpreobuka.projekat.entities.TeacherSubject;
import com.iktpreobuka.projekat.entities.UserEntity;
import com.iktpreobuka.projekat.entities.dto.UserDTO;
import com.iktpreobuka.projekat.repositories.StudentRepository;
import com.iktpreobuka.projekat.repositories.UserRepository;
import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.services.StudentDaoImpl;
import com.iktpreobuka.projekat.utils.RESTError;
import com.iktpreobuka.projekat.utils.UserCustomValidator;

@RestController
@RequestMapping(path = "/api/project/student")
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {

	@Autowired
	private StudentRepository studentRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	UserCustomValidator userValidator;
	
	@Autowired
	private StudentDaoImpl studentDaoImpl;
	
	@JsonView(Views.Admin.class)
	protected final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
	
	
	@Secured({"ROLE_ADMIN", "ROLE_TEACHER", "ROLE_PARENT"})
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getAllStudents(Authentication authentication) {
		String email = (String) authentication.getName();
		UserEntity ulogovanUser = userRepository.findByEmail(email);
		
		if (ulogovanUser.getRole().equals("ROLE_ADMIN")) {
		       Iterable<StudentEntity> allStudents = studentRepository.findAll();

		       return new ResponseEntity<>(allStudents, HttpStatus.OK);
		   
		} else if (ulogovanUser.getRole().equals("ROLE_TEACHER")) {
				TeacherEntity ulogovanNastavnik =  (TeacherEntity) ulogovanUser;
				List<StudentEntity> students = new ArrayList<>();

		       for (TeacherSubject nastavnikovPredmet : ulogovanNastavnik.getTeacherSubject()) {
		    	   students.addAll(nastavnikovPredmet.getStudents());
		       }

		       return new ResponseEntity<>(students, HttpStatus.OK);
		} else if (ulogovanUser.getRole().equals("ROLE_PARENT")) {
			 ParentEntity ulogovanRoditelj = (ParentEntity) ulogovanUser;
		       List<StudentEntity> ucenici = new ArrayList<>();

		       for (StudentEntity child : studentRepository.findAll()) {
		           if (child.getParent() != null && child.getParent().equals(ulogovanRoditelj)) {
		               ucenici.add(child);
		           }
		       }
		       return new ResponseEntity<>(ucenici, HttpStatus.OK);
		}
		
		logger.info("Not authorized to see all students");
		return new ResponseEntity<RESTError>(new RESTError(1, "Not authorized to see students"), HttpStatus.FORBIDDEN);

	}	

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.GET, value = "/by-id/{id}")
	public ResponseEntity<?> getStudentsById(@PathVariable Integer id) {
		Optional<StudentEntity> student = studentRepository.findById(id);

		if (student.isPresent()) {
	        logger.info("Student found in the database: " + student.get().getFirstName() + student.get().getLastName() + ".");
			return new ResponseEntity<StudentEntity>(student.get(), HttpStatus.OK);
		} else {
	        logger.error("No student found in the database with: " + id + ".");
			return new ResponseEntity<RESTError>(new RESTError(1, "No student found"), HttpStatus.NOT_FOUND);
		}
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.GET, value = "/by-username/{username}")
	public ResponseEntity<?> getStudentByUsername(@PathVariable String username) {
		Optional<StudentEntity> student = studentRepository.findByUsername(username);

		if (student.isPresent()) {
	        logger.info("Student found in the database: " + username + ".");
			return new ResponseEntity<StudentEntity>(student.get(), HttpStatus.OK);
		} else {
	        logger.error("No student found in the database with " + username + ".");
			return new ResponseEntity<RESTError>(new RESTError(1, "Student not found"), HttpStatus.NOT_FOUND);
		}
	}

	@Secured({"ROLE_ADMIN", "ROLE_TEACHER"})
	@RequestMapping(method = RequestMethod.GET, value = "/by-firstName/{firstName}")
	public ResponseEntity<?> getStudentByFirstName(@PathVariable String firstName, Authentication authentication) {
		return studentDaoImpl.getStudentByFirstName(firstName, authentication);
	}

	@Secured("ROLE_ADMIN") 
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, value = "/by-lastName/{lastName}")
	public ResponseEntity<?> getStudentByLastName(@PathVariable String lastName) {
		List<StudentEntity> students = studentRepository.findByLastName(lastName);

		if (students.isEmpty()) {
	        logger.error("No students found in the database with lastname" + lastName);
			return new ResponseEntity<RESTError>(new RESTError(1, "No students found"), HttpStatus.NOT_FOUND);
		} else {
	        logger.info("Found students with lastname: " + lastName + " in the database .");
			return new ResponseEntity<List<StudentEntity>>(students, HttpStatus.OK);
		}
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.GET, value = "/by-firstLetter/{firstLetter}")
	public ResponseEntity<?> getStudentByFirstLetter(@PathVariable String firstLetter) {
		List<StudentEntity> students = studentRepository.findByFirstNameStartingWith(firstLetter);

		if (students.isEmpty()) {
	        logger.error("No students found in the database with first letter of the name " + firstLetter);
			return new ResponseEntity<RESTError>(new RESTError(1, "No students found"), HttpStatus.NOT_FOUND);
		} else {
	        logger.info("Found students in the database with first letter of the name " + firstLetter);
			return new ResponseEntity<List<StudentEntity>>(students, HttpStatus.OK);
		}
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.GET, value = "/by-email/{email}")
	public ResponseEntity<?> getByEmail(@PathVariable String email) {
		Optional<StudentEntity> student = studentRepository.findByEmail(email);

		if (student.isPresent()) {
	        logger.info("Found students in the database with " + student.get().getEmail());
			return new ResponseEntity<StudentEntity>(student.get(), HttpStatus.OK);
		} else {
	        logger.error("No student found in the database with " + email);
			return new ResponseEntity<RESTError>(new RESTError(1, "No student found"), HttpStatus.NOT_FOUND);
		}
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.POST, value = "/newStudentUser")
	public ResponseEntity<?> createStudent(@Valid @RequestBody UserDTO newUser, BindingResult result) {
		return studentDaoImpl.createStudent(newUser, result);
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.PUT, value = "/studentsParent/{parents_id}/student/{students_id}")
	public ResponseEntity<?> setStudentsParent(@PathVariable Integer parents_id, @PathVariable Integer students_id) {
		return studentDaoImpl.setStudentsParent(parents_id, students_id);
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.PUT, value = "/studentsTeachingSubj/teachsubj/{teachSubj_id}/student/{students_id}")
	public ResponseEntity<?> setStudentsTeachingSubj(@PathVariable Integer teachSubj_id,
			@PathVariable Integer students_id) {
		return studentDaoImpl.setStudentsTeachingSubj(teachSubj_id, students_id);
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.PUT, value = "/updateStudent/{id}")
	public ResponseEntity<?> updateStudent(@RequestBody UserDTO updatedUser, BindingResult result, @PathVariable Integer id) {
		return studentDaoImpl.updateStudent(updatedUser, result, id);
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.DELETE, value = "deleteStudent/by-id/{id}")
	public ResponseEntity<?> deleteStudentByID(@PathVariable Integer id) {
		return studentDaoImpl.deleteStudentByID(id);
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.DELETE, value = "deleteStudent/by-username/{username}")
	public ResponseEntity<?> deleteStudentByUsername(@PathVariable String username) {
		return studentDaoImpl.deleteStudentByUsername(username);
	}

	@Secured("ROLE_ADMIN")
	@RequestMapping(method = RequestMethod.DELETE, value = "deleteStudent/parents_id/{parents_id}/students_id/{students_id}")
	public ResponseEntity<?> deleteStudentsParent(@PathVariable Integer parents_id, @PathVariable Integer students_id) {
		return studentDaoImpl.deleteStudentsParent(parents_id, students_id);
	}
	
	//EASTER EGG REST ENDPOINT
	@Secured("ROLE_STUDENT")
	@RequestMapping(method = RequestMethod.POST, value = "/dositejeva")
	public String zahtevZaDositejevuStipendiju () {
		return "Uspešno ste poslali zahtev za Dositejevu stipendiju!\n"
				+ "Udjite na link da vidite status: https://i.kym-cdn.com/photos/images/newsfeed/001/499/826/2f0.png ";
	}

}
