package com.iktpreobuka.projekat.entities.dto;

public class GradeSubjectDTO {

	private Integer grade;
	
	private String semester;
	
	private String subjectName;
	
	public GradeSubjectDTO() {}

	public GradeSubjectDTO(Integer grade, boolean firstSemester, String subjectName) {
		this.grade = grade;
		if (firstSemester) {
			semester = "Prvo polugodište";
		} else {
			semester = "Drugo polugodište";
		}
		this.subjectName = subjectName;
	}

	public Integer getGrade() {
		return grade;
	}

	public void setGrade(Integer grade) {
		this.grade = grade;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public String getSemester() {
		return semester;
	}

	public void setSemester(String semester) {
		this.semester = semester;
	}
	
}
