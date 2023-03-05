package com.iktpreobuka.projekat.controllers;

import java.util.List;
import java.util.Optional;
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
import org.springframework.web.bind.annotation.RequestParam;
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
		List<TeacherEntity> teachers = (List<TeacherEntity>) teacherRepository.findAll();

		if (teachers.isEmpty()) {
			return new ResponseEntity<>("No teachers found", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(teachers, HttpStatus.OK);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-id/{id}")
	public ResponseEntity<?> getTeacherById(@PathVariable Integer id) {
		Optional<TeacherEntity> teacher = teacherRepository.findById(id);
		if (teacher.isPresent()) {
			return new ResponseEntity<>(teacher.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Teacher not found", HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-username/{username}")
	public ResponseEntity<?> getTeacherByUsername(@PathVariable String username) {
		Optional<TeacherEntity> teacher = teacherRepository.findByUsername(username);
		if (teacher.isPresent()) {
			return new ResponseEntity<>(teacher.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Teacher not found", HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-firstName/{firstName}")
	public ResponseEntity<?> getTeacherByFirstName(@PathVariable String firstName) {
		List<TeacherEntity> teacher = teacherRepository.findByFirstName(firstName);
		if (teacher.isEmpty()) {
			return new ResponseEntity<>("No teacher found", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(teacher, HttpStatus.OK);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-lastName/{lastName}")
	public ResponseEntity<?> getTeacherByLastName(@PathVariable String lastName) {
		List<TeacherEntity> teachers = teacherRepository.findByLastName(lastName);
		if (teachers.isEmpty()) {
			return new ResponseEntity<>("No teachers found", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(teachers, HttpStatus.OK);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-firstLetter/{firstLetter}")
	public ResponseEntity<?> getTeacherByFirstLetter(@PathVariable String firstLetter) {
		List<TeacherEntity> teachers = teacherRepository.findByFirstNameStartingWith(firstLetter);
		if (teachers.isEmpty()) {
			return new ResponseEntity<>("No teachers found", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(teachers, HttpStatus.OK);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-email/{email}")
	public ResponseEntity<?> getTeacherByEmail(@PathVariable String email) {
		Optional<TeacherEntity> teacher = teacherRepository.findByEmail(email);
		if (teacher.isPresent()) {
			return new ResponseEntity<>(teacher.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>("No teacher found", HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/newTeacherUser")
	public ResponseEntity<?> createTeacher(@Valid @RequestBody UserDTO newUser, BindingResult result) {
		if(result.hasErrors()) {
			return new ResponseEntity<>(createErrorMessage(result), HttpStatus.BAD_REQUEST);
		}
		
		TeacherEntity newTeacher = new TeacherEntity();

		TeacherEntity existingTeacherWithEmail = teacherRepository.findByEmail(newUser.getEmail()).orElse(null);
		if (existingTeacherWithEmail != null && newUser.getEmail().equals(existingTeacherWithEmail.getEmail())) {
			return new ResponseEntity<>("Email already exists", HttpStatus.CONFLICT);
		}

		TeacherEntity existingTeacherWithUsername = teacherRepository.findByUsername(newUser.getUsername())
				.orElse(null);
		if (existingTeacherWithUsername != null
				&& newUser.getUsername().equals(existingTeacherWithUsername.getUsername())) {
			return new ResponseEntity<>("Username already exists", HttpStatus.CONFLICT);
		}

		newTeacher.setFirstName(newUser.getFirstName());
		newTeacher.setLastName(newUser.getLastName());
		newTeacher.setUsername(newUser.getUsername());
		newTeacher.setEmail(newUser.getEmail());
		newTeacher.setPassword(newUser.getPassword());

		newTeacher.setRole("ROLE_TEACHER");
		teacherRepository.save(newTeacher);
		return new ResponseEntity<TeacherEntity>(newTeacher, HttpStatus.CREATED);

	}

	private String createErrorMessage(BindingResult result) {
		return result.getAllErrors().stream().map(ObjectError::getDefaultMessage)
				.collect(Collectors.joining(" "));
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/updateTeacher/{id}")
	public ResponseEntity<?> updateTeacher(@RequestBody UserDTO updatedUser, @PathVariable Integer id,
			@RequestParam String accessPass) {

		TeacherEntity teacher = teacherRepository.findById(id).orElse(null);

		if (teacher == null) {
			return new ResponseEntity<>("No teacher found", HttpStatus.NOT_FOUND);
		}

		if (!teacher.getPassword().equals(accessPass)) {
			return new ResponseEntity<>("Password is incorrect", HttpStatus.BAD_REQUEST);
		}

		teacher.setFirstName(updatedUser.getFirstName());
		teacher.setLastName(updatedUser.getLastName());
		teacher.setUsername(updatedUser.getUsername());
		teacher.setEmail(updatedUser.getEmail());

		if (updatedUser.getPassword().equals(updatedUser.getChanged_password())) {
			teacher.setPassword(updatedUser.getPassword());
		}

		teacherRepository.save(teacher);
		return new ResponseEntity<TeacherEntity>(teacher, HttpStatus.OK);
	}

	// TODO dodati metodu za postavljanje predmeta profesoru

	@RequestMapping(method = RequestMethod.DELETE, value = "deleteTeacher/by-id/{id}")
	public ResponseEntity<?> deleteTeacherByID(@PathVariable Integer id) {
		Optional<TeacherEntity> teacher = teacherRepository.findById(id);

		if (teacher.isPresent()) {
			teacherRepository.delete(teacher.get());
			return new ResponseEntity<>("Teacher with ID " + id + " has been successfully deleted.", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Teacher not found", HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "deleteTeacher/by-username/{username}")
	public ResponseEntity<?> deleteTeacherByUsername(@PathVariable String username) {
		Optional<TeacherEntity> teacher = teacherRepository.findByUsername(username);

		if (teacher.isPresent()) {
			teacherRepository.delete(teacher.get());
			return new ResponseEntity<>("Teacher with " + username + " username has been successfully deleted.",
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Teacher not found", HttpStatus.NOT_FOUND);
		}

	}

}
