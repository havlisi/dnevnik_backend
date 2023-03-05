package com.iktpreobuka.projekat.controllers;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.iktpreobuka.projekat.entities.GradeEntity;
import com.iktpreobuka.projekat.entities.StudentEntity;
import com.iktpreobuka.projekat.entities.TeacherEntity;
import com.iktpreobuka.projekat.entities.TeacherSubject;
import com.iktpreobuka.projekat.repositories.GradeRepository;
import com.iktpreobuka.projekat.repositories.StudentRepository;
import com.iktpreobuka.projekat.repositories.TeacherRepository;
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

	@Autowired
	private TeacherRepository teacherRepository;
	
	
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getAllGrades() {
		List<GradeEntity> grades = (List<GradeEntity>) gradeRepository.findAll();

		if (grades.isEmpty()) {
			return new ResponseEntity<>("No grades found", HttpStatus.NOT_FOUND);
		} else {
			return new ResponseEntity<>(grades, HttpStatus.OK);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "allGrades/by_studentName")
	public ResponseEntity<?> getGradesByStudentName(@RequestParam String studentFName,
			@RequestParam String studentLName) {
		Optional<StudentEntity> student = studentRepository.findByFirstNameAndLastName(studentFName, studentLName);
		
		if (student.isPresent()) {
			List<GradeEntity> grades = student.get().getGrades();

			if (grades.isEmpty()) {
				return new ResponseEntity<>("Grades for student " + studentFName + " " + studentLName + " not found",
						HttpStatus.NOT_FOUND);
			} else {
				return new ResponseEntity<>(grades, HttpStatus.OK);
			}
		}
		return new ResponseEntity<>("Student " + studentFName + " " + studentLName + " not found",
				HttpStatus.NOT_FOUND);
	}

	// @Secured({ "ROLE_ADMIN", "ROLE_TEACHER" })
	@RequestMapping(method = RequestMethod.POST, value = "/newGrade/student/{student_id}/teachsubj/{teachsubj_id}")
	public ResponseEntity<?> createGrade(@PathVariable Integer student_id, @PathVariable Integer teachsubj_id,
			@RequestParam boolean firstSemester, @RequestParam Integer gradeValue) {
		StudentEntity student = studentRepository.findById(student_id).orElse(null);
		TeacherSubject teachingSubject = teacherSubjectRepository.findById(teachsubj_id).orElse(null);

		if (student == null) {
			return new ResponseEntity<>("No student found", HttpStatus.NOT_FOUND);
		}
		
		if (teachingSubject == null) {
			return new ResponseEntity<>("No teaching subject found", HttpStatus.NOT_FOUND);
		}

		boolean daLiSeTeachingSubjectNalaziUListi = false;

		for (TeacherSubject teacherSubject : student.getTeacherSubjects()) {
			if (teacherSubject.getId().equals(teachingSubject.getId())) {
				daLiSeTeachingSubjectNalaziUListi = true;
			}
		}
		if (!daLiSeTeachingSubjectNalaziUListi) {
			return new ResponseEntity<>("Student " + student.getFirstName() + " " + student.getLastName()
			+ " is not taking the class that this teacher is teaching.", HttpStatus.NOT_FOUND);
		}

		GradeEntity newGrade = new GradeEntity();

		newGrade.setFirstSemester(firstSemester);
		newGrade.setTeacherSubject(teachingSubject);
		newGrade.setStudent(student);
		newGrade.setGrade(gradeValue);

		student.getGrades().add(newGrade);
		teachingSubject.getGrades().add(newGrade);
		
		gradeRepository.save(newGrade);
		teacherSubjectRepository.save(teachingSubject);
		studentRepository.save(student);
		return new ResponseEntity<>(student, HttpStatus.CREATED);
	}

	// @Secured({ "ROLE_ADMIN", "ROLE_TEACHER" })
	@RequestMapping(method = RequestMethod.PUT, value = "/updateGrade/student/{student_id}/teachsubj/{teachsubj_id}/grade/{grade_id}")
	public ResponseEntity<?> updateGrade(@PathVariable Integer student_id, @PathVariable Integer teachsubj_id,
			@PathVariable Integer grade_id, @RequestParam boolean firstSemester, @RequestParam Integer gradeValue) {
		StudentEntity student = studentRepository.findById(student_id).get();
		TeacherSubject teachingSubject = teacherSubjectRepository.findById(teachsubj_id).get();
		GradeEntity grade = gradeRepository.findById(grade_id).get();

		if (grade == null) {
			return new ResponseEntity<>("There is no grade with this id " + grade_id, HttpStatus.NOT_FOUND);
		}

		if (grade.getTeacherSubject().getId().equals(teachingSubject.getId())) {
			return new ResponseEntity<>("Grade with this id " + grade_id 
					+ " doesn't exist for this teaching subject", HttpStatus.NOT_FOUND);
		}

		if (grade.getStudent().getId().equals(student.getId())) {
			return new ResponseEntity<>("Grade with this id " + grade_id 
					+ " doesn't exist for this student", HttpStatus.NOT_FOUND);
		}

		grade.setGrade(gradeValue);
		grade.setFirstSemester(firstSemester);

		gradeRepository.save(grade);
		return new ResponseEntity<>(grade, HttpStatus.OK);
	}

//	@Secured({ "ROLE_ADMIN", "ROLE_TEACHER" })
	@RequestMapping(method = RequestMethod.DELETE, value = "/deleteGrade/grade/{grade_id}/teachSubj/{teachsubj_id}")
	public ResponseEntity<?> deleteGrade(@PathVariable Integer grade_id, @PathVariable Integer teachsubj_id,
			@RequestParam Integer teacher_id) {
		GradeEntity grade = gradeRepository.findById(grade_id).orElse(null);
		TeacherSubject teachingSubject = teacherSubjectRepository.findById(teachsubj_id).orElse(null);
		TeacherEntity teacher = teacherRepository.findById(teacher_id).orElse(null);

		if (grade == null) {
			return new ResponseEntity<>("Grade with this id " + grade_id 
					+ " doesn't exist", HttpStatus.NOT_FOUND);
		} 
		
		if (teachingSubject == null) {
			return new ResponseEntity<>("No teaching subject found", HttpStatus.NOT_FOUND);
		}
		
		if (!teachingSubject.getGrades().contains(grade)) {
			return new ResponseEntity<>("The grade with " + grade_id + " ID doesn't exist in"
					+ " teaching subject with " + teachsubj_id + " ID.", HttpStatus.NOT_FOUND);
		}
		
		//token autentifikacija ipak
		if (!teachingSubject.getTeacher().equals(teacher)) {
			return new ResponseEntity<>("The teacher with " + teacher_id + " ID doesn't teach"
					+ " teaching subject with " + teachsubj_id + " ID.", HttpStatus.FORBIDDEN);
		}
		
		// servis pa udji u ovo deleteGrade(); i izvrsi pre ovo dole
		
		gradeRepository.delete(grade);
	    teachingSubject.getGrades().remove(grade);
		teacherSubjectRepository.save(teachingSubject);
		return new ResponseEntity<>(grade, HttpStatus.OK);
	}
}
