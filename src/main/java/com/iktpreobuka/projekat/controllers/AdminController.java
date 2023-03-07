package com.iktpreobuka.projekat.controllers;

import java.util.List;
import java.util.Optional;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.projekat.entities.AdminEntity;
import com.iktpreobuka.projekat.entities.Helpers;
import com.iktpreobuka.projekat.entities.dto.UserDTO;
import com.iktpreobuka.projekat.repositories.AdminRepository;
import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.utils.RESTError;
import com.iktpreobuka.projekat.utils.UserCustomValidator;

@RestController
@RequestMapping(path = "/api/project/admin")
public class AdminController {

	@Autowired
	private AdminRepository adminRepository;

	@Autowired
	UserCustomValidator userValidator;
	
	@JsonView(Views.Admin.class)
	protected final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

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

	@RequestMapping(method = RequestMethod.GET, value = "/by-id/{adminId}")
	public ResponseEntity<?> getAdminById(@PathVariable Integer adminId) {
		Optional<AdminEntity> admin = adminRepository.findById(adminId);
		if (admin.isPresent()) {
	        logger.info("Admin found in the database: " + admin.get().getFirstName() + admin.get().getLastName() + " .");
			return new ResponseEntity<AdminEntity>(admin.get(), HttpStatus.OK);
		} else {
	        logger.error("No admins found in the database with: " + admin.get().getId() + " .");
			return new ResponseEntity<RESTError>(new RESTError(1, "Admin not found"), HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-username/{username}")
	public ResponseEntity<?> getAdminByUsername(@PathVariable String username) {
		Optional<AdminEntity> admin = adminRepository.findByUsername(username);
		if (admin.isPresent()) {
	        logger.info("Admin found in the database: " + admin.get().getUsername() + " .");
			return new ResponseEntity<AdminEntity>(admin.get(), HttpStatus.OK);
		} else {
	        logger.error("No admins found in the database with " + admin.get().getUsername() + " .");
			return new ResponseEntity<RESTError>(new RESTError(1, "Admin not found"), HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-firstName/{firstName}")
	public ResponseEntity<?> getAdminByFirstName(@PathVariable String firstName) {
		List<AdminEntity> admins = adminRepository.findByFirstName(firstName);
		if (admins.isEmpty()) {
	        logger.error("No admins found in the database with name : " + firstName);
			return new ResponseEntity<RESTError>(new RESTError(1, "No admins found"), HttpStatus.NOT_FOUND);
		} else {
	        logger.info("Found admins with name - " + firstName + " in the database .");
			return new ResponseEntity<List<AdminEntity>>(admins, HttpStatus.OK);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-lastName/{lastName}")
	public ResponseEntity<?> getAdminByLastName(@PathVariable String lastName) {
		List<AdminEntity> admins = adminRepository.findByLastName(lastName);
		if (admins.isEmpty()) {
	        logger.error("No admins found in the database with lastname: " + lastName);
			return new ResponseEntity<RESTError>(new RESTError(1, "No admins found"), HttpStatus.NOT_FOUND);
		} else {
	        logger.info("Found admins with lastname: " + lastName + " in the database .");
			return new ResponseEntity<List<AdminEntity>>(admins, HttpStatus.OK);
		}
	}

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

	@RequestMapping(method = RequestMethod.GET, value = "/by-email/{email}")
	public ResponseEntity<?> getAdminByEmail(@PathVariable String email) {
		Optional<AdminEntity> admin = adminRepository.findByEmail(email);
		if (admin.isPresent()) {
	        logger.info("Found admin in the database with " + admin.get().getEmail());
			return new ResponseEntity<AdminEntity>(admin.get(), HttpStatus.OK);
		} else {
	        logger.error("No admins found in the database with " + admin.get().getEmail());
			return new ResponseEntity<RESTError>(new RESTError(1, "Admin not found"), HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/newAdminUser")
	public ResponseEntity<?> createAdmin(@Valid @RequestBody UserDTO newUser, BindingResult result) {

		if (result.hasErrors()) {
	        logger.info("Validating users input parameters");
			return new ResponseEntity<>(Helpers.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		} //TODO <?>

		AdminEntity newAdmin = new AdminEntity();

		AdminEntity existingAdminWithEmail = adminRepository.findByEmail(newUser.getEmail()).orElse(null);		
        logger.info("Fiding out whether there's a user with the same email.");

		if (existingAdminWithEmail != null && newUser.getEmail().equals(existingAdminWithEmail.getEmail())) {
	        logger.error("There is a user with the same email.");
			return new ResponseEntity<RESTError>(new RESTError(1, "Email already exists"), HttpStatus.CONFLICT);
		}

		AdminEntity existingAdminWithUsername = adminRepository.findByUsername(newUser.getUsername()).orElse(null);		
        logger.info("Fiding out whether there's a user with the same username.");

		if (existingAdminWithUsername != null && newUser.getUsername().equals(existingAdminWithUsername.getUsername())) {
	        logger.error("There is a user with the same username.");
			return new ResponseEntity<RESTError>(new RESTError(2, "Username already exists"), HttpStatus.CONFLICT);
		}

		newAdmin.setFirstName(newUser.getFirstName());
		newAdmin.setLastName(newUser.getLastName());
		newAdmin.setUsername(newUser.getUsername());
		newAdmin.setEmail(newUser.getEmail());
		newAdmin.setPassword(newUser.getPassword());
		
		newAdmin.setRole("ROLE_ADMIN");
        logger.info("Setting users role.");

		adminRepository.save(newAdmin);
        logger.info("Saving admin to the database");

		return new ResponseEntity<AdminEntity>(newAdmin, HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/updateAdmin/{admin_id}")
	public ResponseEntity<?> updateAdmin(@Valid @RequestBody UserDTO updatedUser, @PathVariable Integer admin_id,
			@RequestParam String accessPass, BindingResult result) {

		if (result.hasErrors()) {
	        logger.info("Validating users input parameters");
			return new ResponseEntity<>(Helpers.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		} else {
	        logger.info("Validating if the users password matches the confirming password");
			userValidator.validate(updatedUser, result);
		}

		AdminEntity admin = adminRepository.findById(admin_id).orElse(null);

		if (admin == null) {
	        logger.error("There is no admin found with " + admin_id);
			return new ResponseEntity<RESTError>(new RESTError(1, "Admin not found"), HttpStatus.NOT_FOUND);
		}
		if (!admin.getPassword().equals(accessPass)) {
	        logger.error("The password isn't correct");
			return new ResponseEntity<RESTError>(new RESTError(2, "Password is incorrect"), HttpStatus.BAD_REQUEST);
		}

		admin.setFirstName(updatedUser.getFirstName());
		admin.setLastName(updatedUser.getLastName());
		admin.setUsername(updatedUser.getUsername());
		admin.setEmail(updatedUser.getEmail());

		adminRepository.save(admin);
        logger.info("Saving admin to the database");

		return new ResponseEntity<AdminEntity>(admin, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "deleteAdmin/by-id/{adminId}")
	public ResponseEntity<?> deleteAdminByID(@PathVariable Integer adminId) {
		Optional<AdminEntity> admin = adminRepository.findById(adminId);

		if (admin.isPresent()) {
			adminRepository.delete(admin.get());
	        logger.info("Deleting admin from the database");
			return new ResponseEntity<>("Admin with ID " + adminId + " has been successfully deleted.", HttpStatus.OK);
		} else {
	        logger.error("There is no admin found with " + adminId);
			return new ResponseEntity<RESTError>(new RESTError(1, "Admin not found"), HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "deleteAdmin/by-username/{username}")
	public ResponseEntity<?> deleteAdminByUsername(@PathVariable String username) {
		Optional<AdminEntity> admin = adminRepository.findByUsername(username);

		if (admin.isPresent()) {
			adminRepository.delete(admin.get());
	        logger.info("Deleting admin from the database");
			return new ResponseEntity<>("Admin with " + username + " username has been successfully deleted.",
					HttpStatus.OK);
		} else {
	        logger.error("There is no admin found with " + username);
			return new ResponseEntity<RESTError>(new RESTError(1, "Admin not found"), HttpStatus.NOT_FOUND);
		}

	}

}