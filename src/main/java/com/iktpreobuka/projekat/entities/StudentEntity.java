package com.iktpreobuka.projekat.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StudentEntity extends UserEntity {
	
	@ManyToMany(mappedBy = "student_teacherSubject")
	private List<TeacherSubject> teacherSubjects = new ArrayList<>();
	
	@OneToMany(mappedBy = "student", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	private List<GradeEntity> grades = new ArrayList<>();
	
	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "parent")
	private ParentEntity parent;

	public StudentEntity() {}

	public StudentEntity(Integer id, String role, List<TeacherSubject> teacherSubjects, List<GradeEntity> grades, ParentEntity parent,
			@NotNull(message = "First name must be provided.") @Size(min = 2, max = 30, message = "First name must be between {min} and {max} characters long.") String firstName,
			@NotNull(message = "Last name must be provided.") @Size(min = 2, max = 30, message = "Last name must be between {min} and {max} characters long.") String lastName,
			@NotNull(message = "Username must be provided.") @Size(min = 5, max = 25, message = "Username must be between {min} and {max} characters long.") String username,
			@NotNull(message = "Please provide email address.") @Email(message = "Email is not valid.") String email,
			@NotNull(message = "Password must be provided.") @Size(min = 5, max = 15, message = "Password must be between {min} and {max} characters long.") String password) {
		super(id, role, firstName, lastName, username, email, password);
		this.teacherSubjects = teacherSubjects;
		this.grades = grades;
		this.parent = parent;
	}

	public List<TeacherSubject> getTeacherSubjects() {
		return teacherSubjects;
	}

	public void setTeacherSubjects(List<TeacherSubject> teacherSubjects) {
		this.teacherSubjects = teacherSubjects;
	}

	public List<GradeEntity> getGrades() {
		return grades;
	}

	public void setGrades(List<GradeEntity> grades) {
		this.grades = grades;
	}

	public ParentEntity getParent() {
		return parent;
	}

	public void setParent(ParentEntity parent) {
		this.parent = parent;
	}

}
