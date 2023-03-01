package com.iktpreobuka.projekat.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.iktpreobuka.projekat.entities.ParentEntity;
import com.iktpreobuka.projekat.entities.StudentEntity;
import com.iktpreobuka.projekat.entities.dto.UserDTO;
import com.iktpreobuka.projekat.repositories.ParentRepository;
import com.iktpreobuka.projekat.repositories.StudentRepository;

@RestController
@RequestMapping(path = "/api/project/student")
public class StudentController {

	@Autowired
	private StudentRepository studentRepository;
	
	@Autowired
	private ParentRepository parentRepository;

	@RequestMapping(method = RequestMethod.GET)
	public Iterable<StudentEntity> getAllStudents() {
		return studentRepository.findAll();
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/newStudentUser")
	public ResponseEntity<?>  createStudent(@RequestBody UserDTO newUser) {
		
		StudentEntity newStudent = new StudentEntity();
		
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
	public StudentEntity setStudentsParent(@PathVariable Integer parents_id, @PathVariable Integer students_id) {
		StudentEntity student = studentRepository.findById(students_id).get();
		ParentEntity parent = parentRepository.findById(parents_id).get();
		
		if (student == null || parent == null ) {
			return null;
		}
		
		student.setParent(parent);
		studentRepository.save(student);
		return student;
	}

}
