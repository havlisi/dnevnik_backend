package com.iktpreobuka.projekat.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.iktpreobuka.projekat.entities.TeacherEntity;
import com.iktpreobuka.projekat.entities.dto.UserDTO;
import com.iktpreobuka.projekat.repositories.TeacherRepository;

@RestController
@RequestMapping(path = "/api/project/teacher")
public class TeacherController {

	@Autowired
	private TeacherRepository teacherRepository;

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getAllTeachers() {
		return new ResponseEntity<List<TeacherEntity>>((List<TeacherEntity>) teacherRepository.findAll(), HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/newTeacherUser")
	public ResponseEntity<?> createTeacher(@RequestBody UserDTO newUser) {
		
		TeacherEntity newTeacher = new TeacherEntity();
		
		newTeacher.setFirstName(newUser.getFirstName());
		newTeacher.setLastName(newUser.getLastName());
		newTeacher.setUsername(newUser.getUsername());
		newTeacher.setEmail(newUser.getEmail());
		newTeacher.setPassword(newUser.getPassword());
		
		newTeacher.setRole("ROLE_TEACHER");
		teacherRepository.save(newTeacher);
		return new ResponseEntity<TeacherEntity>(newTeacher, HttpStatus.CREATED);
	}

}
