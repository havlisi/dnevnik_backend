package com.iktpreobuka.projekat.controllers;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.iktpreobuka.projekat.entities.ParentEntity;
import com.iktpreobuka.projekat.entities.StudentEntity;
import com.iktpreobuka.projekat.entities.TeacherSubject;
import com.iktpreobuka.projekat.entities.dto.UserDTO;
import com.iktpreobuka.projekat.repositories.ParentRepository;
import com.iktpreobuka.projekat.repositories.StudentRepository;
import com.iktpreobuka.projekat.repositories.TeacherSubjectRepository;

@RestController
@RequestMapping(path = "/api/project/student")
public class StudentController {

	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private ParentRepository parentRepository;
	
	@Autowired
	private TeacherSubjectRepository teacherSubjectRepository;
	

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getAllStudents() {
		List<StudentEntity> students = (List<StudentEntity>) studentRepository.findAll();

		if (students.isEmpty()) {
			return new ResponseEntity<>("No students found", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(students, HttpStatus.OK);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-id/{id}")
	public ResponseEntity<?> getStudentsById(@PathVariable Integer id) {
		Optional<StudentEntity> student = studentRepository.findById(id);

		if (student.isPresent()) {
			return new ResponseEntity<>(student.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Student not found", HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-username/{username}")
	public ResponseEntity<?> getStudentByUsername(@PathVariable String username) {
		Optional<StudentEntity> student = studentRepository.findByUsername(username);

		if (student.isPresent()) {
			return new ResponseEntity<>(student.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Student not found", HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-firstName/{firstName}")
	public ResponseEntity<?> getStudentByFirstName(@PathVariable String firstName) {
		List<StudentEntity> students = studentRepository.findByFirstName(firstName);

		if (students.isEmpty()) {
			return new ResponseEntity<>("No student found", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(students, HttpStatus.OK);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-lastName/{lastName}")
	public ResponseEntity<?> getStudentByLastName(@PathVariable String lastName) {
		List<StudentEntity> students = studentRepository.findByLastName(lastName);

		if (students.isEmpty()) {
			return new ResponseEntity<>("No students found", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(students, HttpStatus.OK);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-firstLetter/{firstLetter}")
	public ResponseEntity<?> getStudentByFirstLetter(@PathVariable String firstLetter) {
		List<StudentEntity> students = studentRepository.findByFirstNameStartingWith(firstLetter);

		if (students.isEmpty()) {
			return new ResponseEntity<>("No students found", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(students, HttpStatus.OK);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-email/{email}")
	public ResponseEntity<?> getByEmail(@PathVariable String email) {
		Optional<StudentEntity> student = studentRepository.findByEmail(email);

		if (student.isPresent()) {
			return new ResponseEntity<>(student.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>("No student found", HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/newStudentUser")
	public ResponseEntity<?> createStudent(@RequestBody UserDTO newUser) {

		StudentEntity newStudent = new StudentEntity();

		StudentEntity existingStudentWithEmail = studentRepository.findByEmail(newUser.getEmail()).orElse(null);
		if (existingStudentWithEmail != null && newUser.getEmail().equals(existingStudentWithEmail.getEmail())) {
			return new ResponseEntity<>("Email already exists", HttpStatus.CONFLICT);
		}

		StudentEntity existingStudentWithUsername = studentRepository.findByUsername(newUser.getUsername())
				.orElse(null);
		if (existingStudentWithUsername != null
				&& newUser.getUsername().equals(existingStudentWithUsername.getUsername())) {
			return new ResponseEntity<>("Username already exists", HttpStatus.CONFLICT);
		}

		newStudent.setFirstName(newUser.getFirstName());
		newStudent.setLastName(newUser.getLastName());
		newStudent.setUsername(newUser.getUsername());
		newStudent.setEmail(newUser.getEmail());
		newStudent.setPassword(newUser.getPassword());

		newStudent.setRole("ROLE_STUDENT");
		studentRepository.save(newStudent);
		return new ResponseEntity<StudentEntity>(newStudent, HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/studentsParent/{parents_id}/{students_id}")
	public ResponseEntity<?> setStudentsParent(@PathVariable Integer parents_id, @PathVariable Integer students_id) {
		StudentEntity student = studentRepository.findById(students_id).orElse(null);
		ParentEntity parent = parentRepository.findById(parents_id).orElse(null);

		if (student == null) {
			return new ResponseEntity<>("No student found", HttpStatus.NOT_FOUND);
		}

		if (parent == null) {
			return new ResponseEntity<>("No parent found", HttpStatus.NOT_FOUND);
		}

		student.setParent(parent);
		studentRepository.save(student);
		return new ResponseEntity<StudentEntity>(student, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.PUT, value = "/studentsTeachingSubj/{teachSubj_id}/{students_id}")
	public ResponseEntity<?> setStudentsTeachingSubj(@PathVariable Integer teachSubj_id, 
			@PathVariable Integer students_id) {
		StudentEntity student = studentRepository.findById(students_id).orElse(null);
		TeacherSubject teacherSubject = teacherSubjectRepository.findById(teachSubj_id).orElse(null);
		
		if (teacherSubject == null) {
			return new ResponseEntity<>("No teaching subject with " + teachSubj_id + " ID found", HttpStatus.NOT_FOUND);
		}

		if (student == null) {
			return new ResponseEntity<>("No student with " + students_id + " ID found", HttpStatus.NOT_FOUND);
		}

		student.getTeacherSubjects().add(teacherSubject);
		studentRepository.save(student);
		return new ResponseEntity<StudentEntity>(student, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/updateStudent/{id}")
	public ResponseEntity<?> updateStudent(@RequestBody UserDTO updatedUser, @PathVariable Integer id,
			@RequestParam String accessPass) {

		StudentEntity student = studentRepository.findById(id).orElse(null);

		if (student == null) {
			return new ResponseEntity<>("No student found", HttpStatus.NOT_FOUND);
		}

		if (!student.getPassword().equals(accessPass)) {
			return new ResponseEntity<>("Password is incorrect", HttpStatus.BAD_REQUEST);
		}

		student.setFirstName(updatedUser.getFirstName());
		student.setLastName(updatedUser.getLastName());
		student.setUsername(updatedUser.getUsername());
		student.setEmail(updatedUser.getEmail());

		if (updatedUser.getPassword().equals(updatedUser.getChanged_password())) {
			student.setPassword(updatedUser.getPassword());
		}

		studentRepository.save(student);
		return new ResponseEntity<StudentEntity>(student, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "deleteStudent/by-id/{id}")
	public ResponseEntity<?> deleteStudentByID(@PathVariable Integer id) {
		Optional<StudentEntity> student = studentRepository.findById(id);

		if (student.isPresent()) {
			studentRepository.delete(student.get());
			return new ResponseEntity<>("Student with ID " + id + " has been successfully deleted.", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Student not found", HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "deleteStudent/by-username/{username}")
	public ResponseEntity<?> deleteStudentByUsername(@PathVariable String username) {
		Optional<StudentEntity> student = studentRepository.findByUsername(username);

		if (student.isPresent()) {
			studentRepository.delete(student.get());
			return new ResponseEntity<>("Student with " + username + " username has been successfully deleted.",
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Student not found", HttpStatus.NOT_FOUND);
		}

	}

	@RequestMapping(method = RequestMethod.DELETE, value = "deleteStudent/{parents_id}/{students_id}")
	public ResponseEntity<?> deleteStudentsParent(@PathVariable Integer parents_id, @PathVariable Integer students_id) {
		StudentEntity student = studentRepository.findById(students_id).orElse(null);
		
		if (student == null) {
			return new ResponseEntity<>("No student found", HttpStatus.NOT_FOUND);
		}
		
		ParentEntity parent = student.getParent();
		 
		if (parent == null || !parent.getId().equals(parents_id)) {
	       return new ResponseEntity<>("No parent found with id " + parents_id + " for student with id " + students_id, HttpStatus.NOT_FOUND);
		}

		student.setParent(null);
		studentRepository.save(student);
		return new ResponseEntity<>("Parent with id " + parents_id + " was successfully removed from student with id " + students_id, HttpStatus.OK);
	}

}
