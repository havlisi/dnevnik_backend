package com.iktpreobuka.projekat.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.iktpreobuka.projekat.entities.AdminEntity;
import com.iktpreobuka.projekat.repositories.AdminRepository;

@RestController
@RequestMapping(path = "/api/project/admin")
public class AdminController {

	@Autowired
	private AdminRepository adminRepository; // git branch merging test

	@RequestMapping(method = RequestMethod.GET)
	public Iterable<AdminEntity> getAllAdmin() {
		return adminRepository.findAll();
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/newAdminUser")
	public AdminEntity createAdmin(@RequestBody AdminEntity newAdmin) {
		newAdmin.setRole("ROLE_ADMIN");
		adminRepository.save(newAdmin);
		return newAdmin;
	}

}
