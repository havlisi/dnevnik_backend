package com.iktpreobuka.projekat.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.iktpreobuka.projekat.entities.TeacherEntity;
import com.iktpreobuka.projekat.repositories.TeacherRepository;

@RestController
@RequestMapping(path = "/api/project/teacher")
public class TeacherController {

	@Autowired
	private TeacherRepository teacherRepository;

	@RequestMapping(method = RequestMethod.GET)
	public Iterable<TeacherEntity> getAllTeachers() {
		return teacherRepository.findAll();
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/newTeacherUser")
	public TeacherEntity createTeacher(@RequestBody TeacherEntity newTeacher) {
		newTeacher.setRole("ROLE_TEACHER");
		teacherRepository.save(newTeacher);
		return newTeacher;
	}

}
