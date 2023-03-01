package com.iktpreobuka.projekat.controllers;

import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.iktpreobuka.projekat.entities.AdminEntity;
import com.iktpreobuka.projekat.entities.dto.UserDTO;
import com.iktpreobuka.projekat.repositories.AdminRepository;

@RestController
@RequestMapping(path = "/api/project/admin")
public class AdminController {

	@Autowired
	private AdminRepository adminRepository;

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getAllAdmin() {
		return new ResponseEntity<List<AdminEntity>>((List<AdminEntity>) adminRepository.findAll(), HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/newAdminUser")
	public ResponseEntity<?> createAdmin(@Valid @RequestBody UserDTO newUser) {
		
		AdminEntity newAdmin = new AdminEntity();

		newAdmin.setFirstName(newUser.getFirstName());
		newAdmin.setLastName(newUser.getLastName());
		newAdmin.setUsername(newUser.getUsername());
		newAdmin.setEmail(newUser.getEmail());
		newAdmin.setPassword(newUser.getPassword());
		newAdmin.setRole("ROLE_ADMIN");
		
		adminRepository.save(newAdmin);
		return new ResponseEntity<AdminEntity>(newAdmin, HttpStatus.CREATED);
	}

}
