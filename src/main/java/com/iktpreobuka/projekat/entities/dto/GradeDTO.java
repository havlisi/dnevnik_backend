package com.iktpreobuka.projekat.entities.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class GradeDTO {

	private Integer student_id;
	
	private Integer teachsubj_id;
	
	private Integer grade_id;
	
	@NotNull(message = "The 'firstSemester' field must be set to true or false")
	private boolean firstSemester;

	@NotNull(message = "Grade must be provided")
	@Min(value = 1, message = "Grade must be between 1 and 5")
	@Max(value = 5, message = "Grade must be between 1 and 5")
	private Integer gradeValue;

	public GradeDTO() {}
	
	public GradeDTO(Integer student_id, Integer teachsubj_id, Integer grade_id,
			@NotNull(message = "The 'firstSemester' field must be set to true or false") boolean firstSemester,
			@NotNull(message = "Grade must be provided") @Min(value = 1, message = "Grade must be between 1 and 5") @Max(value = 5, message = "Grade must be between 1 and 5") Integer gradeValue) {
		super();
		this.student_id = student_id;
		this.teachsubj_id = teachsubj_id;
		this.grade_id = grade_id;
		this.firstSemester = firstSemester;
		this.gradeValue = gradeValue;
	}

	public Integer getStudent_id() {
		return student_id;
	}

	public void setStudent_id(Integer student_id) {
		this.student_id = student_id;
	}

	public Integer getTeachsubj_id() {
		return teachsubj_id;
	}

	public void setTeachsubj_id(Integer teachsubj_id) {
		this.teachsubj_id = teachsubj_id;
	}

	public Integer getGrade_id() {
		return grade_id;
	}

	public void setGrade_id(Integer grade_id) {
		this.grade_id = grade_id;
	}

	public boolean isFirstSemester() {
		return firstSemester;
	}

	public void setFirstSemester(boolean firstSemester) {
		this.firstSemester = firstSemester;
	}

	public Integer getGradeValue() {
		return gradeValue;
	}

	public void setGradeValue(Integer gradeValue) {
		this.gradeValue = gradeValue;
	}
	
}
