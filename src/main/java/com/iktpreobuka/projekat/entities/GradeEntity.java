package com.iktpreobuka.projekat.entities;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
//import com.iktpreobuka.projekat.security.Views;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GradeEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	//@JsonView(Views.Public.class)
	@JsonProperty("ID")
	private Integer id;
	
	@NotNull(message = "Grade must be provided")
	@Min(value = 1, message = "Grade must be between 1 and 5")
	@Max(value = 5, message = "Grade must be between 1 and 5")
	//@JsonView(Views.Admin.class)
	private Integer grade;
	
	@NotNull(message = "The 'firstSemester' field must be set to true or false")
	//@JsonView(Views.Admin.class)
	private boolean firstSemester;
	
	@JsonIgnore
	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "student")
	private StudentEntity student;
	
	@JsonIgnore
	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "teacherSubject")
	private TeacherSubject teacherSubject;

	public GradeEntity() {}

	public GradeEntity(Integer id, StudentEntity student, TeacherSubject teacherSubject, 
			@NotNull(message = "The 'firstSemester' field must be set to true or false") boolean firstSemester,
			@NotNull(message = "Grade must be provided") @Min(value = 1, message = "Grade must be between 1 and 5") 
			@Max(value = 5, message = "Grade must be between 1 and 5") Integer grade) {
		super();
		this.id = id;
		this.grade = grade;
		this.firstSemester = firstSemester;
		this.student = student;
		this.teacherSubject = teacherSubject;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getGrade() {
		return grade;
	}

	public void setGrade(Integer grade) {
		this.grade = grade;
	}

	public StudentEntity getStudent() {
		return student;
	}

	public void setStudent(StudentEntity student) {
		this.student = student;
	}

	public TeacherSubject getTeacherSubject() {
		return teacherSubject;
	}

	public void setTeacherSubject(TeacherSubject teacherSubject) {
		this.teacherSubject = teacherSubject;
	}

	public boolean isFirstSemester() {
		return firstSemester;
	}

	public void setFirstSemester(boolean firstSemester) {
		this.firstSemester = firstSemester;
	}

}
