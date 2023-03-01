package com.iktpreobuka.projekat.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.iktpreobuka.projekat.entities.ParentEntity;
import com.iktpreobuka.projekat.entities.dto.UserDTO;
import com.iktpreobuka.projekat.repositories.ParentRepository;

@RestController
@RequestMapping(path = "/api/project/parent")
public class ParentController {

	@Autowired
	private ParentRepository parentRepository;

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getAllParents() {
		return new ResponseEntity<List<ParentEntity>>((List<ParentEntity>) parentRepository.findAll(), HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/newParentUser")
	public ResponseEntity<?> createParent(@RequestBody UserDTO newUser) {
		
		ParentEntity newParent = new ParentEntity();
		
		newParent.setFirstName(newUser.getFirstName());
		newParent.setLastName(newUser.getLastName());
		newParent.setUsername(newUser.getUsername());
		newParent.setEmail(newUser.getEmail());
		newParent.setPassword(newUser.getPassword());
		newParent.setRole("ROLE_PARENT");
		
		parentRepository.save(newParent);
		return new ResponseEntity<ParentEntity>(newParent, HttpStatus.CREATED);
	}

}
