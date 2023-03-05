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
import com.iktpreobuka.projekat.entities.ParentEntity;
import com.iktpreobuka.projekat.entities.StudentEntity;
import com.iktpreobuka.projekat.entities.dto.UserDTO;
import com.iktpreobuka.projekat.repositories.ParentRepository;
import com.iktpreobuka.projekat.repositories.StudentRepository;

@RestController
@RequestMapping(path = "/api/project/parent")
public class ParentController {

	@Autowired
	private ParentRepository parentRepository;
	
	@Autowired
	private StudentRepository studentRepository;

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getAllParents() {
		List<ParentEntity> parents = (List<ParentEntity>) parentRepository.findAll();

		if (parents.isEmpty()) {
			return new ResponseEntity<>("No parents found", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(parents, HttpStatus.OK);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-id/{id}")
	public ResponseEntity<?> getParentById(@PathVariable Integer id) {
		Optional<ParentEntity> parent = parentRepository.findById(id);
		if (parent.isPresent()) {
			return new ResponseEntity<>(parent.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Parent not found", HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-username/{username}")
	public ResponseEntity<?> getParentByUsername(@PathVariable String username) {
		Optional<ParentEntity> parent = parentRepository.findByUsername(username);
		if (parent.isPresent()) {
			return new ResponseEntity<>(parent.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Parent not found", HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-firstName/{firstName}")
	public ResponseEntity<?> getParentByFirstName(@PathVariable String firstName) {
		List<ParentEntity> parents = parentRepository.findByFirstName(firstName);
		if (parents.isEmpty()) {
			return new ResponseEntity<>("No parent found", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(parents, HttpStatus.OK);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-lastName/{lastName}")
	public ResponseEntity<?> getParentsByLastName(@PathVariable String lastName) {
		List<ParentEntity> parents = parentRepository.findByLastName(lastName);
		if (parents.isEmpty()) {
			return new ResponseEntity<>("No parents found", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(parents, HttpStatus.OK);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-firstLetter/{firstLetter}")
	public ResponseEntity<?> getParentByFirstLetter(@PathVariable String firstLetter) {
		List<ParentEntity> parents = parentRepository.findByFirstNameStartingWith(firstLetter);
		if (parents.isEmpty()) {
			return new ResponseEntity<>("No parents found", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(parents, HttpStatus.OK);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-email/{email}")
	public ResponseEntity<?> getParentByEmail(@PathVariable String email) {
		Optional<ParentEntity> parent = parentRepository.findByEmail(email);
		if (parent.isPresent()) {
			return new ResponseEntity<>(parent.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>("No parent found", HttpStatus.NOT_FOUND);
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/by-student/{student_id}")
	public ResponseEntity<?> getParentByStudent(@PathVariable Integer student_id) {
		StudentEntity student = studentRepository.findById(student_id).orElse(null);
	    ParentEntity parent = parentRepository.findByStudent(student);

		if (student == null) {
			return new ResponseEntity<>("No student found", HttpStatus.NOT_FOUND);
		}
		
		if (parent == null) {
	    	return new ResponseEntity<>("No parent found", HttpStatus.NOT_FOUND);
		}
		
	    return new ResponseEntity<>(parent, HttpStatus.OK);
	}
	    
	@RequestMapping(method = RequestMethod.POST, value = "/newParentUser")
	public ResponseEntity<?> createParent(@Valid @RequestBody UserDTO newUser, BindingResult result) {
		if(result.hasErrors()) {
			return new ResponseEntity<>(createErrorMessage(result), HttpStatus.BAD_REQUEST);
		}
		
		ParentEntity newParent = new ParentEntity();
		
		ParentEntity existingParentWithEmail = parentRepository.findByEmail(newUser.getEmail()).orElse(null);
		if (existingParentWithEmail != null && newUser.getEmail().equals(existingParentWithEmail.getEmail())) {
		    return new ResponseEntity<>("Email already exists", HttpStatus.CONFLICT);
		}

		ParentEntity existingParentWithUsername = parentRepository.findByUsername(newUser.getUsername()).orElse(null);
		if (existingParentWithUsername != null && newUser.getUsername().equals(existingParentWithUsername.getUsername())) {
		    return new ResponseEntity<>("Username already exists", HttpStatus.CONFLICT);
		}

		newParent.setFirstName(newUser.getFirstName());
		newParent.setLastName(newUser.getLastName());
		newParent.setUsername(newUser.getUsername());
		newParent.setEmail(newUser.getEmail());
		newParent.setPassword(newUser.getPassword());
		newParent.setRole("ROLE_PARENT");

		parentRepository.save(newParent);
		return new ResponseEntity<ParentEntity>(newParent, HttpStatus.CREATED);
	}
	
	private String createErrorMessage(BindingResult result) {
		return result.getAllErrors().stream().map(ObjectError::getDefaultMessage)
		.collect(Collectors.joining(" "));
		}

	@RequestMapping(method = RequestMethod.PUT, value = "/updateParent/{id}")
	public ResponseEntity<?> updateParent(@RequestBody UserDTO updatedUser, @PathVariable Integer id,
			@RequestParam String accessPass) {

		ParentEntity parent = parentRepository.findById(id).orElse(null);;

		if (parent == null) {
			return new ResponseEntity<>("No parent found", HttpStatus.NOT_FOUND);
		}

		if (!parent.getPassword().equals(accessPass)) {
			return new ResponseEntity<>("Password is incorrect", HttpStatus.BAD_REQUEST);
		}

		parent.setFirstName(updatedUser.getFirstName());
		parent.setLastName(updatedUser.getLastName());
		parent.setUsername(updatedUser.getUsername());
		parent.setEmail(updatedUser.getEmail());

		if (updatedUser.getPassword().equals(updatedUser.getChanged_password())) {
			parent.setPassword(updatedUser.getPassword());
		}

		parentRepository.save(parent);
		return new ResponseEntity<ParentEntity>(parent, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "deleteParent/by-id/{id}")
	public ResponseEntity<?> deleteParentByID(@PathVariable Integer id) {
		Optional<ParentEntity> parent = parentRepository.findById(id);

		if (parent.isPresent()) {
			parentRepository.delete(parent.get());
			return new ResponseEntity<>("Parent with ID " + id + " has been successfully deleted.", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Parent not found", HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "deleteParent/by-username/{username}")
	public ResponseEntity<?> deleteParentByUsername(@PathVariable String username) {
		Optional<ParentEntity> parent = parentRepository.findByUsername(username);

		if (parent.isPresent()) {
			parentRepository.delete(parent.get());
			return new ResponseEntity<>("Parent with " + username + " username has been successfully deleted.",
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Parent not found", HttpStatus.NOT_FOUND);
		}

	}

}
