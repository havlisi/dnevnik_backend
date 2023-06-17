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
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.projekat.security.Views;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SubjectEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonProperty("ID")
	private Integer id;
	
	@NotNull(message = "Subject name must be provided.")
	private String subjectName;
	
	private Integer fondCasova;
	
	@OneToMany(mappedBy = "subject", cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
	private List<TeacherSubject> teacherSubjects = new ArrayList<>();

	public SubjectEntity() {}

	public SubjectEntity(Integer id, @NotNull(message = "Subject name must be provided.") String subjectName, 
			Integer fondCasova, List<TeacherSubject> teacherSubject) {
		super();
		this.id = id;
		this.subjectName = subjectName;
		this.fondCasova = fondCasova;
		this.teacherSubjects = teacherSubject;
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

	public List<TeacherSubject> getTeacherSubjects() {
		return teacherSubjects;
	}

	public void setTeacherSubjects(List<TeacherSubject> teacherSubject) {
		this.teacherSubjects = teacherSubject;
	}

}
