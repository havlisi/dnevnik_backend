package com.iktpreobuka.projekat.entities;

import java.util.stream.Collectors;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

public class Helpers {

	public static String createErrorMessage(BindingResult result) {
		return result.getAllErrors().stream().map(ObjectError::getDefaultMessage)
		.collect(Collectors.joining(" "));

	}
}
