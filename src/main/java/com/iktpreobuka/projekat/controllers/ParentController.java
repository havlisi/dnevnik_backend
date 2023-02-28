package com.iktpreobuka.projekat.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.iktpreobuka.projekat.entities.ParentEntity;
import com.iktpreobuka.projekat.repositories.ParentRepository;

@RestController
@RequestMapping(path = "/api/project/parent")
public class ParentController {

	@Autowired
	private ParentRepository parentRepository;

	@RequestMapping(method = RequestMethod.GET)
	public Iterable<ParentEntity> getAllParents() {
		return parentRepository.findAll();
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/newParentUser")
	public ParentEntity createParent(@RequestBody ParentEntity newParent) {
		newParent.setRole("ROLE_PARENT");
		parentRepository.save(newParent);
		return newParent;
	}

}
