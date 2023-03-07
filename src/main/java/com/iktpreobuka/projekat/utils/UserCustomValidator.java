package com.iktpreobuka.projekat.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.projekat.entities.dto.UserDTO;
import com.iktpreobuka.projekat.security.Views;

@Component
public class UserCustomValidator implements Validator {

	@JsonView(Views.Admin.class)
	protected final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
	
	@Override
	public boolean supports(Class<?> myClass) {
		return UserDTO.class.equals(myClass);
	}

	@Override
	public void validate(Object target, Errors errors) {
		UserDTO user = (UserDTO) target;
		if(!user.getPassword().equals(user.getConfirmed_password())) {
	        logger.error("Users password doesn't matche the confirming password");
			errors.reject("400", "Passwords must be the same");
		}
		
	}
	
	
}
