package com.iktpreobuka.projekat.controllers;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
	public ResponseEntity<?> createAdmin(@RequestBody UserDTO newUser) {

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
	
	@RequestMapping(method = RequestMethod.POST, value = "/updateAdmin/{admin_id}")
	public ResponseEntity<?> updateAdmin(@RequestBody UserDTO updatedUser, @PathVariable Integer admin_id, 
			@RequestParam String accessPass) {

		AdminEntity admin = adminRepository.findById(admin_id).get();

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

		if (updatedUser.getChanged_password().equals(updatedUser.getPassword())) {
			admin.setPassword(updatedUser.getPassword());
		}

		adminRepository.save(admin);
		return new ResponseEntity<AdminEntity>(admin, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "deleteAdmin/by-id/{adminId}")
	public ResponseEntity<?> deleteAdminByID(@PathVariable Integer adminId) {
		AdminEntity admin = adminRepository.findById(adminId).get();
		
		if (admin == null) {
			return new ResponseEntity<>("Admin not found", HttpStatus.NOT_FOUND);
		}
		
		adminRepository.delete(admin);
		return new ResponseEntity<>("Admin with ID " + adminId + " has been successfully deleted.", HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "deleteAdmin/by-username/{username}")
	public ResponseEntity<?> deleteAdminByUsername(@PathVariable String username) {
		AdminEntity admin = adminRepository.findByUsername(username).get();
		
		if (admin == null) {
			return new ResponseEntity<>("Admin not found", HttpStatus.NOT_FOUND);
		}
		
		adminRepository.delete(admin);
		return new ResponseEntity<>("Admin with " + username + " username has been successfully deleted.", HttpStatus.OK);
	}
	
}