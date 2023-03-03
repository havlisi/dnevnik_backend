package com.iktpreobuka.projekat.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@DiscriminatorValue("teacher_subject")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TeacherSubject {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	@Min(value = 1, message = "Class year must be between 1 and 8")
	@Max(value = 8, message = "Class year must be between 1 and 8")
	private Integer classYear;
	
	@JsonIgnore
	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
	@JoinColumn(name = "teacher")
	private TeacherEntity teacher;
	
	@JsonIgnore
	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
	@JoinColumn(name = "subject")
	private SubjectEntity subject;
	
	@ManyToMany
	@JoinTable(name = "student_teacherSubject", joinColumns = @JoinColumn(name = "student_id"),
	inverseJoinColumns = @JoinColumn(name = "teacherSubject_id"))
	private List<StudentEntity> student_teacherSubject = new ArrayList<>();
	
	@OneToMany(mappedBy = "grade", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	private List<GradeEntity> grade = new ArrayList<>();

	public TeacherSubject() {}

	public TeacherSubject(Integer id, TeacherEntity teacher, SubjectEntity subject, List<GradeEntity> grade, List<StudentEntity> student_teacherSubject,
			@Min(value = 1, message = "Class year must be between 1 and 8") @Max(value = 8, message = "Class year must be between 1 and 8") Integer classYear) {
		this.id = id;
		this.classYear = classYear;
		this.teacher = teacher;
		this.subject = subject;
		this.student_teacherSubject = student_teacherSubject;
		this.grade = grade;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public List<GradeEntity> getGrade() {
		return grade;
	}

	public void setGrade(List<GradeEntity> grade) {
		this.grade = grade;
	}

	public List<StudentEntity> getStudentTeacherSubject() {
		return student_teacherSubject;
	}

	public void setStudentTeacherSubject(List<StudentEntity> student_teacherSubject) {
		this.student_teacherSubject = student_teacherSubject;
	}

}
