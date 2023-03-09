package com.iktpreobuka.projekat.controllers;

import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.projekat.entities.AdminEntity;
import com.iktpreobuka.projekat.entities.dto.UserDTO;
import com.iktpreobuka.projekat.repositories.AdminRepository;
import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.services.AdminDaoImpl;
import com.iktpreobuka.projekat.utils.RESTError;
import com.iktpreobuka.projekat.utils.UserCustomValidator;

@RestController
@Secured("ROLE_ADMIN")
@RequestMapping(path = "/api/project/admin")
public class AdminController {

	@Autowired
	private AdminRepository adminRepository;

	@Autowired
	UserCustomValidator userValidator;
	
	@Autowired
	private AdminDaoImpl adminDaoImpl;
	
	@JsonView(Views.Admin.class)
	protected final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getAllAdmin() {
		try {
			List<AdminEntity> admins = (List<AdminEntity>) adminRepository.findAll();

			if (admins.isEmpty()) {
		        logger.error("No admins found in the database.");
				return new ResponseEntity<RESTError>(new RESTError(1, "No admins found"), HttpStatus.NOT_FOUND);
			} else {
		        logger.info("Found admin(s) in the database");
				return new ResponseEntity<List<AdminEntity>>(admins, HttpStatus.OK);
			}
		} catch (Exception e) {
			return new ResponseEntity<RESTError>(new RESTError(2, "Exception occurred: " + e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, value = "/by-id/{adminId}")
	public ResponseEntity<?> getAdminById(@PathVariable Integer adminId) {
		Optional<AdminEntity> admin = adminRepository.findById(adminId);
		if (admin.isPresent()) {
	        logger.info("Admin found in the database: " + adminId + ".");
			return new ResponseEntity<AdminEntity>(admin.get(), HttpStatus.OK);
		} else {
	        logger.error("No admins found in the database with: " + adminId + ".");
			return new ResponseEntity<RESTError>(new RESTError(1, "Admin not found"), HttpStatus.NOT_FOUND);
		}
	}

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, value = "/by-username/{username}")
	public ResponseEntity<?> getAdminByUsername(@PathVariable String username) {
		Optional<AdminEntity> admin = adminRepository.findByUsername(username);
		if (admin.isPresent()) {
	        logger.info("Admin found in the database: " + username + " username.");
			return new ResponseEntity<AdminEntity>(admin.get(), HttpStatus.OK);
		} else {
	        logger.error("No admins found in the database with " + username + " username.");
			return new ResponseEntity<RESTError>(new RESTError(1, "Admin not found"), HttpStatus.NOT_FOUND);
		}
	}

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, value = "/by-firstName/{firstName}")
	public ResponseEntity<?> getAdminByFirstName(@PathVariable String firstName) {
		List<AdminEntity> admins = adminRepository.findByFirstName(firstName);
		if (admins.isEmpty()) {
	        logger.error("No admins found in the database with name : " + firstName);
			return new ResponseEntity<RESTError>(new RESTError(1, "No admins found"), HttpStatus.NOT_FOUND);
		} else {
	        logger.info("Found admins with name - " + firstName + " in the database.");
			return new ResponseEntity<List<AdminEntity>>(admins, HttpStatus.OK);
		}
	}

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, value = "/by-lastName/{lastName}")
	public ResponseEntity<?> getAdminByLastName(@PathVariable String lastName) {
		List<AdminEntity> admins = adminRepository.findByLastName(lastName);
		if (admins.isEmpty()) {
	        logger.error("No admins found in the database with lastname: " + lastName);
			return new ResponseEntity<RESTError>(new RESTError(1, "No admins found"), HttpStatus.NOT_FOUND);
		} else {
	        logger.info("Found admins with lastname: " + lastName + " in the database.");
			return new ResponseEntity<List<AdminEntity>>(admins, HttpStatus.OK);
		}
	}

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, value = "/by-firstLetter/{firstLetter}")
	public ResponseEntity<?> getAdminByFirstLetter(@PathVariable String firstLetter) {
		List<AdminEntity> admins = adminRepository.findByFirstNameStartingWith(firstLetter);
		if (admins.isEmpty()) {
	        logger.error("No admins found in the database with first letter of the name " + firstLetter);
			return new ResponseEntity<RESTError>(new RESTError(1, "No admins found"), HttpStatus.NOT_FOUND);
		} else {
	        logger.info("Found admin(s) in the database with first letter of the name " + firstLetter);
			return new ResponseEntity<List<AdminEntity>>(admins, HttpStatus.OK);
		}
	}

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.GET, value = "/by-email/{email}")
	public ResponseEntity<?> getAdminByEmail(@PathVariable String email) {
		Optional<AdminEntity> admin = adminRepository.findByEmail(email);
		if (admin.isPresent()) {
	        logger.info("Found admin in the database with " + email);
			return new ResponseEntity<AdminEntity>(admin.get(), HttpStatus.OK);
		} else {
	        logger.error("No admins found in the database with " + email);
			return new ResponseEntity<RESTError>(new RESTError(1, "Admin not found"), HttpStatus.NOT_FOUND);
		}
	}

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.POST, value = "/newAdminUser")
	public ResponseEntity<?> createAdmin(@Valid @RequestBody UserDTO newUser, BindingResult result) {
		return adminDaoImpl.createAdmin(newUser, result);
	}

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.PUT, value = "/updateAdmin/{admin_id}")
	public ResponseEntity<?> updateAdmin(@Valid @RequestBody UserDTO updatedUser, BindingResult result, @PathVariable Integer admin_id) {
		return adminDaoImpl.updateAdmin(updatedUser, result, admin_id);
	}

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.DELETE, value = "deleteAdmin/by-id/{adminId}")
	public ResponseEntity<?> deleteAdminByID(@PathVariable Integer adminId) {
		return adminDaoImpl.deleteAdminByID(adminId);
	}

	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
	@RequestMapping(method = RequestMethod.DELETE, value = "deleteAdmin/by-username/{username}")
	public ResponseEntity<?> deleteAdminByUsername(@PathVariable String username) {
		return adminDaoImpl.deleteAdminByUsername(username);
	}

}