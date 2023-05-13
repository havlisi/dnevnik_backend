package com.iktpreobuka.projekat.controllers;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;
//import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.iktpreobuka.projekat.entities.UserEntity;
import com.iktpreobuka.projekat.entities.dto.UserTokenDTO;
import com.iktpreobuka.projekat.repositories.UserRepository;
//import com.iktpreobuka.projekat.utils.Encryption;
//import io.jsonwebtoken.Jwts;

@RestController
public class UserController {
	
//	@Autowired
//	private SecretKey secretKey;
//	
//	@Value("${spring.security.token-duration}")
//	private Integer tokenDuration;
//	
//	@Autowired
//	private UserRepository userRepository;
//
//	private String getJWTToken(UserEntity userEntity) {
//		List<GrantedAuthority> grantedAuthorities = AuthorityUtils
//				.commaSeparatedStringToAuthorityList(userEntity.getRole());
//		String token = Jwts.builder().setId("softtekJWT").setSubject(userEntity.getEmail())
//				.claim("authorities",
//						grantedAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
//				.setIssuedAt(new Date(System.currentTimeMillis()))
//				.setExpiration(new Date(System.currentTimeMillis() + this.tokenDuration)).signWith(this.secretKey)
//				.compact();
//		return "Bearer " + token;
//	}
//
//	@RequestMapping(path = "/api/v1/login", method = RequestMethod.POST)
//	public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
//		UserEntity userEntity = userRepository.findByEmail(email);
//		if (userEntity != null && Encryption.validatePassword(password, userEntity.getPassword())) {
//			String token = getJWTToken(userEntity);
//			UserTokenDTO user = new UserTokenDTO();
//			user.setEmail(email);
//			user.setToken(token);
//			return new ResponseEntity<>(user, HttpStatus.OK);
//		}
//		return new ResponseEntity<>("Wrong credentials", HttpStatus.UNAUTHORIZED);
//		
//	}

}