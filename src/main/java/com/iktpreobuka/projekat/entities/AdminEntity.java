package com.iktpreobuka.projekat.entities;

import javax.persistence.Entity;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class AdminEntity extends UserEntity {

	public AdminEntity() {}

	public AdminEntity(Integer id, String role,
			@NotNull(message = "First name must be provided.") @Size(min = 2, max = 30, message = "First name must be between {min} and {max} characters long.") String firstName,
			@NotNull(message = "Last name must be provided.") @Size(min = 2, max = 30, message = "Last name must be between {min} and {max} characters long.") String lastName,
			@NotNull(message = "Username must be provided.") @Size(min = 5, max = 15, message = "Username must be between {min} and {max} characters long.") String username,
			@NotNull(message = "Please provide email address.") @Email(message = "Email is not valid.") String email,
			@NotNull(message = "Password must be provided.") @Size(min = 5, max = 15, message = "Password must be between {min} and {max} characters long.") String password) {
		super(id, role, firstName, lastName, username, email, password);
	}

}
