package com.iktpreobuka.projekat.controllers;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.iktpreobuka.projekat.entities.GradeEntity;
import com.iktpreobuka.projekat.entities.StudentEntity;
import com.iktpreobuka.projekat.entities.TeacherSubject;
import com.iktpreobuka.projekat.repositories.GradeRepository;
import com.iktpreobuka.projekat.repositories.StudentRepository;
import com.iktpreobuka.projekat.repositories.TeacherSubjectRepository;

@RestController
@RequestMapping(path = "/api/project/grade")
public class GradeController {

	@Autowired
	private GradeRepository gradeRepository;

	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private TeacherSubjectRepository teacherSubjectRepository;

	@RequestMapping(method = RequestMethod.GET)
	public Iterable<GradeEntity> getAllGrades() {
		return gradeRepository.findAll();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "allGrades/by_studentName")
	public Iterable<GradeEntity> getGradesByStudentName(@RequestParam String studentFName, @RequestParam String studentLName) {
		StudentEntity student = studentRepository.findByFirstNameAndLastName(studentFName, studentLName);
		return student.getGrades();
	}
	
	//TODO dodati svuda https / eo

	//@Secured({ "ROLE_ADMIN", "ROLE_TEACHER" })
	@RequestMapping(method = RequestMethod.POST, value = "/newGrade/{student_id}/{teachsubj_id}")
	public GradeEntity createGrade(@PathVariable Integer student_id, @PathVariable Integer teachsubj_id,
			@RequestParam boolean firstSemester, @RequestParam Integer gradeValue) {
		StudentEntity student = studentRepository.findById(student_id).get();
		TeacherSubject teachingSubject = teacherSubjectRepository.findById(teachsubj_id).get();
		
		if (student == null || teachingSubject == null ) {
			return null;
		}
		
		boolean daLiSeTeachingSubjectNalaziUListi = false;
		
		for (TeacherSubject teacherSubject : student.getTeacherSubjects()) {
			if (teacherSubject.getId().equals(teachingSubject.getId())) {
				daLiSeTeachingSubjectNalaziUListi = true;
			}
		}
		if (!daLiSeTeachingSubjectNalaziUListi) {
			System.out.println("Student " + student.getFirstName() + " " + student.getLastName()
			+ " is not taking the class that this teacher is teaching.");
			return null;
		}
		
		GradeEntity newGrade = new GradeEntity();
		
		newGrade.setFirstSemester(firstSemester);
		newGrade.setTeacherSubject(teachingSubject);
		newGrade.setStudent(student);
		newGrade.setGrade(gradeValue);

		student.getGrades().add(newGrade); // ako ne radi, do ovoga je		
		studentRepository.save(student);
		return newGrade;
	}

	//@Secured({ "ROLE_ADMIN", "ROLE_TEACHER" })
	@RequestMapping(method = RequestMethod.PUT, value = "/updateGrade/{student_id}/{teachsubj_id}/{grade_id}")
	public GradeEntity updateGrade(@PathVariable Integer student_id, @PathVariable Integer teachsubj_id,
			@PathVariable Integer grade_id, @RequestParam boolean firstSemester, @RequestParam Integer gradeValue) {
		StudentEntity student = studentRepository.findById(student_id).get();
		TeacherSubject teachingSubject = teacherSubjectRepository.findById(teachsubj_id).get();
		GradeEntity grade = gradeRepository.findById(grade_id).get();

		if (grade == null) {
			System.out.println("There is no grade with this id " + grade_id);
			return null;
		}
		
		if (grade.getTeacherSubject().getId().equals(teachingSubject.getId())) {
			System.out.println("Grade with this id " + grade_id + " doesn't exist for this teaching subject");
			return null;
		}
		
		if (grade.getStudent().getId().equals(student.getId())) {
				System.out.println("Grade with this id " + grade_id + " doesn't exist for this student");
				return null;
			}
		
		grade.setGrade(gradeValue);
		grade.setFirstSemester(firstSemester);

		gradeRepository.save(grade);
		return grade;
	}
	
//	@Secured({ "ROLE_ADMIN", "ROLE_TEACHER" })
	@RequestMapping(method = RequestMethod.DELETE, value = "/deleteGrade/{grade_id}")
	public GradeEntity deleteGrade(@PathVariable Integer grade_id) {
		GradeEntity grade = gradeRepository.findById(grade_id).get();
		
		if (grade == null) {
			return null;
		}
		
		// TODO proveriti da li ta ocena pripada tom nastavniku
		// tj da li taj nastavnik sme da je brise ocenu
		
		gradeRepository.delete(grade);
		return grade;
	}
}
