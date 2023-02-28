package com.iktpreobuka.projekat.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class SubjectEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	private String subjectName;
	
	private Integer fondCasova;
	
	@OneToMany(mappedBy = "subject", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
	private List<TeacherSubject> teacherSubject = new ArrayList<>();

	public SubjectEntity() {}

	public SubjectEntity(Integer id, String subjectName, Integer fondCasova, List<TeacherSubject> teacherSubject) {
		super();
		this.id = id;
		this.subjectName = subjectName;
		this.fondCasova = fondCasova;
		this.teacherSubject = teacherSubject;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public List<TeacherSubject> getTeacherSubject() {
		return teacherSubject;
	}

	public void setTeacherSubject(List<TeacherSubject> teacherSubject) {
		this.teacherSubject = teacherSubject;
	}

}
