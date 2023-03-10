package com.iktpreobuka.projekat.entities.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import com.iktpreobuka.projekat.entities.SubjectEntity;
import com.iktpreobuka.projekat.entities.TeacherEntity;

public class TeacherSubjectDTO {

	@Min(value = 1, message = "Class year must be between 1 and 8")
	@Max(value = 8, message = "Class year must be between 1 and 8")
	private Integer classYear;
	
	private Integer teacher_id;
	
	private Integer subject_id;

	public TeacherSubjectDTO() {}

	public TeacherSubjectDTO(
			@Min(value = 1, message = "Class year must be between 1 and 8") @Max(value = 8, message = "Class year must be between 1 and 8") Integer classYear,
			Integer teacher_id, Integer subject_id) {
		super();
		this.classYear = classYear;
		this.teacher_id = teacher_id;
		this.subject_id = subject_id;
	}

	public Integer getClassYear() {
		return classYear;
	}

	public void setClassYear(Integer classYear) {
		this.classYear = classYear;
	}

	public Integer getTeacher_id() {
		return teacher_id;
	}

	public void setTeacher_id(Integer teacher_id) {
		this.teacher_id = teacher_id;
	}

	public Integer getSubject_id() {
		return subject_id;
	}

	public void setSubject_id(Integer subject_id) {
		this.subject_id = subject_id;
	}
    
}
