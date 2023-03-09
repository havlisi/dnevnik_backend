package com.iktpreobuka.projekat.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.iktpreobuka.projekat.entities.TeacherEntity;
import com.iktpreobuka.projekat.entities.UserEntity;

public interface UserRepository extends CrudRepository<UserEntity, Integer>{

	UserEntity findByEmail(String email);

	Optional<TeacherEntity> findByUsername(String username);

}
