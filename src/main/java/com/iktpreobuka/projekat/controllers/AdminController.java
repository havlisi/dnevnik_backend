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
import com.iktpreobuka.projekat.entities.AdminEntity;
import com.iktpreobuka.projekat.entities.dto.UserDTO;
import com.iktpreobuka.projekat.repositories.AdminRepository;
import com.iktpreobuka.projekat.utils.UserCustomValidator;

@RestController
@RequestMapping(path = "/api/project/admin")
public class AdminController {

	@Autowired
	private AdminRepository adminRepository;
	
	@Autowired
	UserCustomValidator userValidator;

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getAllAdmin() {
		List<AdminEntity> admins = (List<AdminEntity>) adminRepository.findAll();
		 
	    if (admins.isEmpty()) {
	        return new ResponseEntity<>("No admins found", HttpStatus.NOT_FOUND);
	    } else {
	        return new ResponseEntity<>(admins, HttpStatus.OK);
	    }
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-id/{adminId}")
	public ResponseEntity<?> getAdminById(@PathVariable Integer adminId) {
		Optional<AdminEntity> admin = adminRepository.findById(adminId);
		if (admin.isPresent()) {
			return new ResponseEntity<>(admin.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Admin not found", HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-username/{username}")
	public ResponseEntity<?> getAdminByUsername(@PathVariable String username) {
		Optional<AdminEntity> admin = adminRepository.findByUsername(username);
		if (admin.isPresent()) {
			return new ResponseEntity<>(admin.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Admin not found", HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-firstName/{firstName}")
	public ResponseEntity<?> getAdminByFirstName(@PathVariable String firstName) {
		List<AdminEntity> admins = adminRepository.findByFirstName(firstName);
		if (admins.isEmpty()) {
			return new ResponseEntity<>("No admin found", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(admins, HttpStatus.OK);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-lastName/{lastName}")
	public ResponseEntity<?> getAdminByLastName(@PathVariable String lastName) {
		List<AdminEntity> admins = adminRepository.findByLastName(lastName);
		if (admins.isEmpty()) {
			return new ResponseEntity<>("No admin found", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(admins, HttpStatus.OK);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-firstLetter/{firstLetter}")
	public ResponseEntity<?> getAdminByFirstLetter(@PathVariable String firstLetter) {
		List<AdminEntity> admins = adminRepository.findByFirstNameStartingWith(firstLetter);
		if (admins.isEmpty()) {
			return new ResponseEntity<>("No admin found", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(admins, HttpStatus.OK);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/by-email/{email}")
	public ResponseEntity<?> getAdminByEmail(@PathVariable String email) {
		Optional<AdminEntity> admin = adminRepository.findByEmail(email);
		if (admin.isPresent()) {
			return new ResponseEntity<>(admin.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>("No admin found", HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/newAdminUser")
	public ResponseEntity<?> createAdmin(@Valid @RequestBody UserDTO newUser, BindingResult result) {
		
		if(result.hasErrors()) {
			return new ResponseEntity<>(createErrorMessage(result), HttpStatus.BAD_REQUEST);
		}
		
		AdminEntity newAdmin = new AdminEntity();
		
		AdminEntity existingAdminWithEmail = adminRepository.findByEmail(newUser.getEmail()).orElse(null);
		if (existingAdminWithEmail != null && newUser.getEmail().equals(existingAdminWithEmail.getEmail())) {
		    return new ResponseEntity<>("Email already exists", HttpStatus.CONFLICT);
		}

		AdminEntity existingAdminWithUsername = adminRepository.findByUsername(newUser.getUsername()).orElse(null);
		if (existingAdminWithUsername != null && newUser.getUsername().equals(existingAdminWithUsername.getUsername())) {
		    return new ResponseEntity<>("Username already exists", HttpStatus.CONFLICT);
		}

		newAdmin.setFirstName(newUser.getFirstName());
		newAdmin.setLastName(newUser.getLastName());
		newAdmin.setUsername(newUser.getUsername());
		newAdmin.setEmail(newUser.getEmail());
		newAdmin.setPassword(newUser.getPassword());
		newAdmin.setRole("ROLE_ADMIN");

		adminRepository.save(newAdmin);
		return new ResponseEntity<AdminEntity>(newAdmin, HttpStatus.CREATED);
	}

	private String createErrorMessage(BindingResult result) {
		return result.getAllErrors().stream().map(ObjectError::getDefaultMessage)
		.collect(Collectors.joining(" "));
	}
	
	@RequestMapping(method = RequestMethod.PUT, value = "/updateAdmin/{admin_id}")
	public ResponseEntity<?> updateAdmin(@Valid @RequestBody UserDTO updatedUser, @PathVariable Integer admin_id,
			@RequestParam String accessPass, BindingResult result) {
		
		if (result.hasErrors()) {
			return new ResponseEntity<>(createErrorMessage(result), HttpStatus.BAD_REQUEST);
		} else {
			userValidator.validate(updatedUser, result);
		}

		AdminEntity admin = adminRepository.findById(admin_id).orElse(null);

		if (admin == null) {
			return new ResponseEntity<>("No admin found", HttpStatus.NOT_FOUND);
		}

		if (!admin.getPassword().equals(accessPass)) {
			return new ResponseEntity<>("Password is incorrect", HttpStatus.BAD_REQUEST);
		}

		admin.setFirstName(updatedUser.getFirstName());
		admin.setLastName(updatedUser.getLastName());
		admin.setUsername(updatedUser.getUsername());
		admin.setEmail(updatedUser.getEmail());

		adminRepository.save(admin);
		return new ResponseEntity<AdminEntity>(admin, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "deleteAdmin/by-id/{adminId}")
	public ResponseEntity<?> deleteAdminByID(@PathVariable Integer adminId) {
		Optional<AdminEntity> admin = adminRepository.findById(adminId);

		if (admin.isPresent()) {
			adminRepository.delete(admin.get());
			return new ResponseEntity<>("Admin with ID " + adminId + " has been successfully deleted.", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Admin not found", HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "deleteAdmin/by-username/{username}")
	public ResponseEntity<?> deleteAdminByUsername(@PathVariable String username) {
		Optional<AdminEntity> admin = adminRepository.findByUsername(username);

		if (admin.isPresent()) {
			adminRepository.delete(admin.get());
			return new ResponseEntity<>("Admin with " + username + " username has been successfully deleted.",
					HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Admin not found", HttpStatus.NOT_FOUND);
		}

	}

}