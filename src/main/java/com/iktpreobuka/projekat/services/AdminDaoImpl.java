package com.iktpreobuka.projekat.services;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.projekat.entities.AdminEntity;
import com.iktpreobuka.projekat.entities.UserEntity;
import com.iktpreobuka.projekat.entities.dto.UserDTO;
import com.iktpreobuka.projekat.repositories.AdminRepository;
import com.iktpreobuka.projekat.repositories.UserRepository;
import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.utils.ErrorMessageHelper;
import com.iktpreobuka.projekat.utils.RESTError;
import com.iktpreobuka.projekat.utils.UserCustomValidator;

@Service
public class AdminDaoImpl implements AdminDao {

	@Autowired
	private AdminRepository adminRepository;

	@Autowired
	UserCustomValidator userValidator;
	
	@Autowired
	private UserRepository userRepository;
	
	@JsonView(Views.Admin.class)
	protected final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	
	public ResponseEntity<?> createAdmin(UserDTO newUser, BindingResult result) {
		
		if (result.hasErrors()) {
	        logger.error("Sent incorrect parameters.");
			return new ResponseEntity<>(ErrorMessageHelper.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		} else {
	        logger.info("Validating if the users password matches the confirming password");
			userValidator.validate(newUser, result);
			if (result.hasErrors()) {
		        logger.error("Validation errors detected.");
		        return new ResponseEntity<>(result.getFieldError(), HttpStatus.BAD_REQUEST);
		    }
		}

		UserEntity existingUserWithEmail = userRepository.findByEmail(newUser.getEmail());
        logger.info("Finding out whether there's a user with the same email.");
        
		UserEntity existingUserWithUsername = userRepository.findByUsername(newUser.getUsername());
        logger.info("Finding out whether there's a user with the same username.");

		if (existingUserWithEmail != null) {
	        logger.error("There is a user with the same email.");
			return new ResponseEntity<RESTError>(new RESTError(1, "Email already exists"), HttpStatus.CONFLICT);
		}

		if (existingUserWithUsername != null) {
	        logger.error("There is a user with the same username.");
			return new ResponseEntity<RESTError>(new RESTError(2, "Username already exists"), HttpStatus.CONFLICT);
		}

		AdminEntity newAdmin = new AdminEntity();

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
	
	public ResponseEntity<?> updateAdmin(UserDTO updatedUser, BindingResult result, Integer admin_id) {

		if (result.hasErrors()) {
	        logger.error("Sent incorrect parameters.");
			return new ResponseEntity<>(ErrorMessageHelper.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		} else {
	        logger.info("Validating if the users password matches the confirming password");
			userValidator.validate(updatedUser, result);
			if (result.hasErrors()) {
		        logger.error("Validation errors detected.");
		        return new ResponseEntity<>(result.getFieldError(), HttpStatus.BAD_REQUEST);
		    }
		}

		AdminEntity admin = adminRepository.findById(admin_id).orElse(null);

		if (admin == null) {
	        logger.error("There is no admin found with " + admin_id);
			return new ResponseEntity<RESTError>(new RESTError(1, "Admin not found"), HttpStatus.NOT_FOUND);
		}

		admin.setFirstName(updatedUser.getFirstName());
		admin.setLastName(updatedUser.getLastName());
		admin.setUsername(updatedUser.getUsername());
		admin.setEmail(updatedUser.getEmail());
		admin.setPassword(updatedUser.getPassword());

		adminRepository.save(admin);
        logger.info("Saving admin to the database");

		return new ResponseEntity<AdminEntity>(admin, HttpStatus.OK);
	}
	
	public ResponseEntity<?> deleteAdminByID(Integer adminId) {
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
	
	public ResponseEntity<?> deleteAdminByUsername(String username) {
		Optional<AdminEntity> admin = adminRepository.findByUsername(username);

		if (admin.isPresent()) {
			adminRepository.delete(admin.get());
	        logger.info("Deleting admin from the database");
			return new ResponseEntity<>("Admin with " + username + " username has been successfully deleted.",
					HttpStatus.OK);
		} else {
	        logger.error("There is no admin found with " + username + " username.");
			return new ResponseEntity<RESTError>(new RESTError(1, "Admin not found"), HttpStatus.NOT_FOUND);
		}

	}
	
}
