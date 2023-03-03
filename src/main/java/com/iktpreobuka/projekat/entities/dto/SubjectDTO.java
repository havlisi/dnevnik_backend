package com.iktpreobuka.projekat.entities.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class SubjectDTO {

	@NotNull(message = "Subject name must be provided.")
    private String subjectName;

    @NotNull(message = "Number of classes must be provided.")
    @Min(value = 1, message = "Number of classes must be a positive number") //promeniti vred
    private Integer fondCasova;
    
	public SubjectDTO() {}

	public SubjectDTO(@NotNull(message = "Subject name must be provided.") String subjectName,
			@NotNull(message = "Number of classes must be provided.") @Min(value = 1, message = "Number of classes must be a positive number") Integer fondCasova) {
		super();
		this.subjectName = subjectName;
		this.fondCasova = fondCasova;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public Integer getFondCasova() {
		return fondCasova;
	}

	public void setFondCasova(Integer fondCasova) {
		this.fondCasova = fondCasova;
	}
    
}
