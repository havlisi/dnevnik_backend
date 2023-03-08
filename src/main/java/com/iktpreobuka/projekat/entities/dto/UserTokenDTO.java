package com.iktpreobuka.projekat.entities.dto;

public class UserTokenDTO {

	private String email;
	
	private String token;

	public UserTokenDTO() {}
	
	public UserTokenDTO(String email, String token) {
		super();
		this.email = email;
		this.token = token;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
