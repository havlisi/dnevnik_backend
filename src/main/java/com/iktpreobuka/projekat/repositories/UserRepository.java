package com.iktpreobuka.projekat.repositories;

import org.springframework.data.repository.CrudRepository;
import com.iktpreobuka.projekat.entities.UserEntity;

public interface UserRepository extends CrudRepository<UserEntity, Integer>{

	UserEntity findByEmail(String email);

	UserEntity findByUsername(String username);

}
