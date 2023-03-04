package com.iktpreobuka.projekat.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER )
	@JoinColumn(name = "teacher")
	private TeacherEntity teacher;

	@JsonIgnore
	@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
	@JoinColumn(name = "subject")
	private SubjectEntity subject;

	@JsonIgnore
	@ManyToMany(mappedBy = "teacherSubjects")
	private Set<StudentEntity> students = new HashSet<>();
	
	@OneToMany(mappedBy = "grade", cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
	private List<GradeEntity> grades = new ArrayList<>();

	public TeacherSubject() {}

	public TeacherSubject(Integer id, TeacherEntity teacher, SubjectEntity subject, List<GradeEntity> grades, Set<StudentEntity> students,
			@Min(value = 1, message = "Class year must be between 1 and 8") @Max(value = 8, message = "Class year must be between 1 and 8") Integer classYear) {
		this.id = id;
		this.classYear = classYear;
		this.teacher = teacher;
		this.subject = subject;
		this.students = students;
		this.grades = grades;
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

	public List<GradeEntity> getGrades() {
		return grades;
	}

	public void setGrades(List<GradeEntity> grades) {
		this.grades = grades;
	}

	public Set<StudentEntity> getStudents() {
		return students;
	}

	public void setStudents(Set<StudentEntity> students) {
		this.students = students;
	}

}
