package com.iktpreobuka.projekat.entities.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import com.iktpreobuka.projekat.entities.SubjectEntity;
import com.iktpreobuka.projekat.entities.TeacherEntity;

public class TeacherSubjectDTO {

	@Min(value = 1, message = "Class year must be between 1 and 8")
	@Max(value = 8, message = "Class year must be between 1 and 8")
	private Integer classYear;
	
	private TeacherEntity teacher;
	
	private SubjectEntity subject;

	public TeacherSubjectDTO() {}
	
	public TeacherSubjectDTO(
			@Min(value = 1, message = "Class year must be between 1 and 8") @Max(value = 8, message = "Class year must be between 1 and 8") Integer classYear,
			TeacherEntity teacher, SubjectEntity subject) {
		super();
		this.classYear = classYear;
		this.teacher = teacher;
		this.subject = subject;
	}

	public Integer getClassYear() {
		return classYear;
	}

	public void setClassYear(Integer classYear) {
		this.classYear = classYear;
	}

	public TeacherEntity getTeacher() {
		return teacher;
	}

	public void setTeacher(TeacherEntity teacher) {
		this.teacher = teacher;
	}

	public SubjectEntity getSubject() {
		return subject;
	}

	public void setSubject(SubjectEntity subject) {
		this.subject = subject;
	}
    
}
