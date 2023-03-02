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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GradeEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	@Min(value = 1, message = "Grade must be between 1 and 5")
	@Max(value = 5, message = "Grade must be between 1 and 5")
	private Integer grade;
	
	private boolean firstSemester;
	
	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "student")
	private StudentEntity student;
	
	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	@JoinColumn(name = "teacherSubject")
	private TeacherSubject teacherSubject;

	public GradeEntity() {}

	public GradeEntity(Integer id, StudentEntity student, TeacherSubject teacherSubject, boolean firstSemester,
			@Min(value = 1, message = "Grade must be between 1 and 5") @Max(value = 5, message = "Grade must be between 1 and 5") Integer grade) {
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
