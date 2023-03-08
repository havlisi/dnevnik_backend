package com.iktpreobuka.projekat.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.projekat.entities.GradeEntity;
import com.iktpreobuka.projekat.utils.ErrorMessageHelper;
import com.iktpreobuka.projekat.entities.StudentEntity;
import com.iktpreobuka.projekat.entities.TeacherEntity;
import com.iktpreobuka.projekat.entities.TeacherSubject;
import com.iktpreobuka.projekat.entities.dto.GradeSubjectDTO;
import com.iktpreobuka.projekat.entities.dto.GradeDTO;
import com.iktpreobuka.projekat.repositories.GradeRepository;
import com.iktpreobuka.projekat.repositories.StudentRepository;
import com.iktpreobuka.projekat.repositories.TeacherRepository;
import com.iktpreobuka.projekat.repositories.TeacherSubjectRepository;
import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.services.EmailServiceImpl;
import com.iktpreobuka.projekat.services.GradeDaoImpl;
import com.iktpreobuka.projekat.utils.RESTError;

@RestController

//Ako je korisnik administrator može ima potpun pristup svim podacima u sistemu.
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
	
	@Autowired
	private GradeDaoImpl gradeDaoImpl;
	
	@Autowired
	private EmailServiceImpl emailServiceImpl;
	
	@JsonView(Views.Admin.class)
	protected final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
	
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<?> getAllGrades() {
		List<GradeEntity> grades = (List<GradeEntity>) gradeRepository.findAll();

		if (grades.isEmpty()) {
	        logger.error("No grades found in the database.");
			return new ResponseEntity<RESTError>(new RESTError(1, "No grades found"), HttpStatus.NOT_FOUND);
		} else {
	        logger.info("Found grade(s) in the database");
			return new ResponseEntity<List<GradeEntity>>(grades, HttpStatus.OK);
		}
	}

	//ucenik moze da vidi secured ucenik

	@Secured({"ROLE_STUDENT", "ROLE_ADMIN", "ROLE_TEACHER", "ROLE_PARENT"})
	@RequestMapping(method = RequestMethod.GET, value = "allGrades/by_studentName")
	public ResponseEntity<?> getGradesByStudentName(@RequestParam String studentFName,
			@RequestParam String studentLName) {
		Optional<StudentEntity> student = studentRepository.findByFirstNameAndLastName(studentFName, studentLName);
		
		if (student.isPresent()) {
			// TODO proveriti da l je ulogovan korisnik roditelj tom studentu, nastavnik koji mu predaje, admin ili taj student
			List<GradeEntity> grades = student.get().getGrades();

			if (grades.isEmpty()) {
				logger.error("Grades for student " + studentFName + " " + studentLName + " not found");
				return new ResponseEntity<RESTError>(new RESTError(1, "Grades for student " 
						+ studentFName + " " + studentLName + " not found"), HttpStatus.NOT_FOUND);
			} else {
				List<GradeSubjectDTO> gradesWithSubjects = new ArrayList<GradeSubjectDTO>();
				for (GradeEntity grade : grades) {
					GradeSubjectDTO gradeSubject = new GradeSubjectDTO(grade.getGrade(), grade.isFirstSemester(), 
							grade.getTeacherSubject().getSubject().getSubjectName());
					gradesWithSubjects.add(gradeSubject);
				}
				
				return new ResponseEntity<List<GradeSubjectDTO>>(gradesWithSubjects, HttpStatus.OK);
			}
		}
		
		logger.info("Student " + studentFName + " " + studentLName + " not found");
		RESTError error = new RESTError(2, "Student " + studentFName + " " + studentLName + " not found");
		return new ResponseEntity<RESTError>(error, HttpStatus.NOT_FOUND);
	}
	
	
	//Ako je korisnik roditelj, može da vidi sve ocene svih učenika vezanih za sebe.
	
	//finalna ocena svega ikad
	
	@RequestMapping(method = RequestMethod.GET, value = "/semester")
	public ResponseEntity<?> findSubjectGradeBySemester(@RequestParam Integer userId, @RequestParam Integer tsId,
			@RequestParam Integer sbId, @RequestParam boolean firstsemester) {
		return gradeDaoImpl.findGradesBySemester(userId, tsId, sbId, firstsemester);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/finalGrade")
	public ResponseEntity<?> findGradeFinalGrade(@RequestParam Integer userId, @RequestParam Integer tsId,
			@RequestParam Integer sbId) {
		return gradeDaoImpl.findFinalGrades(userId, tsId, sbId);
	}

	// @Secured({ "ROLE_ADMIN", "ROLE_TEACHER" })
//	Ako je korisnik nastavnik, može da vidi sve ocene svih svojih predmeta za učenike
//	i predmete kojima predaje i da ih menja, briše, ili dodaje nove uz mogućnost
//	pretrage.
	@RequestMapping(method = RequestMethod.POST, value = "/newGrade")
	public ResponseEntity<?> createGrade(@Valid @RequestBody GradeDTO newGradeDTO, BindingResult result) {
		
		if(result.hasErrors()) {
	        logger.error("Sent incorrect parameters.");
			return new ResponseEntity<>(ErrorMessageHelper.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		}
		
		StudentEntity student = studentRepository.findById(newGradeDTO.getStudent_id()).orElse(null);
		TeacherSubject teachingSubject = teacherSubjectRepository.findById(newGradeDTO.getTeachsubj_id()).orElse(null);

		if (student == null) {
	        logger.error("There is no student found with " + newGradeDTO.getStudent_id());
			return new ResponseEntity<RESTError>(new RESTError(1, "No student found"), HttpStatus.NOT_FOUND);
		}
		
		if (teachingSubject == null) {
	        logger.error("There is no teaching subject found with " + newGradeDTO.getTeachsubj_id());
			return new ResponseEntity<RESTError>(new RESTError(2, "No teaching subject found"), HttpStatus.NOT_FOUND);
		}

		boolean daLiSeTeachingSubjectNalaziUListi = false;

		for (TeacherSubject teacherSubject : student.getTeacherSubjects()) {
			if (teacherSubject.getId().equals(teachingSubject.getId())) {
		        logger.info("Checking if student studies the teaching subject with " + newGradeDTO.getTeachsubj_id() + " ID.");
				daLiSeTeachingSubjectNalaziUListi = true;
			}
		}
		if (!daLiSeTeachingSubjectNalaziUListi) {
	        logger.error("Student isn't taking the class with " + newGradeDTO.getTeachsubj_id() + " ID.");
			return new ResponseEntity<RESTError>(new RESTError(3, "Student " + student.getFirstName() + " " + student.getLastName()
			+ " is not taking the class that this teacher is teaching."), HttpStatus.NOT_FOUND);
		}

		GradeEntity newGrade = new GradeEntity();

		newGrade.setFirstSemester(newGradeDTO.isFirstSemester());
		newGrade.setTeacherSubject(teachingSubject);
		newGrade.setStudent(student);
		newGrade.setGrade(newGradeDTO.getGradeValue());

		student.getGrades().add(newGrade);
		teachingSubject.getGrades().add(newGrade);
		
        logger.info("Teacher " + teachingSubject.getTeacher().getFirstName() + " " + teachingSubject.getTeacher().getLastName()
        		+ " gave " + student.getFirstName() + " " + student.getLastName() + " " + newGradeDTO.getGradeValue() 
        		+ " from " + teachingSubject.getSubject().getSubjectName().toLowerCase() + ".");
		
		gradeRepository.save(newGrade);
		teacherSubjectRepository.save(teachingSubject);
		studentRepository.save(student);
		
		emailServiceImpl.messageToParents(teachingSubject, student, newGradeDTO.getGradeValue());
		
		return new ResponseEntity<StudentEntity>(student, HttpStatus.CREATED);
	}

	// @Secured({ "ROLE_ADMIN", "ROLE_TEACHER" })
	@RequestMapping(method = RequestMethod.PUT, value = "/updateGrade")
	public ResponseEntity<?> updateGrade(@Valid @RequestBody GradeDTO updateGradeDTO, BindingResult result) {
		StudentEntity student = studentRepository.findById(updateGradeDTO.getStudent_id()).get();
		TeacherSubject teachingSubject = teacherSubjectRepository.findById(updateGradeDTO.getTeachsubj_id()).get();
		GradeEntity grade = gradeRepository.findById(updateGradeDTO.getGrade_id()).get();

		if(result.hasErrors()) {
			return new ResponseEntity<>(ErrorMessageHelper.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		}
		
		if (grade == null) {
	        logger.error("There is no grade found with " + updateGradeDTO.getGrade_id());
			return new ResponseEntity<RESTError>(new RESTError(1, "There is no grade with this id " + updateGradeDTO.getGrade_id()), HttpStatus.NOT_FOUND);
		}

		if (!grade.getTeacherSubject().getId().equals(teachingSubject.getId())) {
	        logger.error("Grade with this id " + updateGradeDTO.getGrade_id() + " doesn't exist for this teaching subject");

			return new ResponseEntity<RESTError>(new RESTError(2,  "Grade with this id " + updateGradeDTO.getGrade_id() 
					+ " doesn't exist for this teaching subject"), HttpStatus.NOT_FOUND);
		}

		if (!grade.getStudent().getId().equals(student.getId())) {
	        logger.error("Grade with this id " + updateGradeDTO.getGrade_id() + " doesn't exist for this student");
			return new ResponseEntity<RESTError>(new RESTError(3, "Grade with this id " + updateGradeDTO.getGrade_id()
					+ " doesn't exist for this student"), HttpStatus.NOT_FOUND);
		}

		grade.setGrade(updateGradeDTO.getGradeValue());
		grade.setFirstSemester(updateGradeDTO.isFirstSemester());

		gradeRepository.save(grade);
        logger.info("Saving grade to the database");

		return new ResponseEntity<GradeEntity>(grade, HttpStatus.OK);
	}

//	@Secured({ "ROLE_ADMIN", "ROLE_TEACHER" })
	@RequestMapping(method = RequestMethod.DELETE, value = "/deleteGrade/grade/{grade_id}/teachSubj/{teachsubj_id}")
	public ResponseEntity<?> deleteGrade(@PathVariable Integer grade_id, @PathVariable Integer teachsubj_id, @RequestParam Integer teacher_id) {
		GradeEntity grade = gradeRepository.findById(grade_id).orElse(null);
		TeacherSubject teachingSubject = teacherSubjectRepository.findById(teachsubj_id).orElse(null);
		TeacherEntity teacher = teacherRepository.findById(teacher_id).orElse(null);

		if (grade == null) {
	        logger.error("There is no grade found with " + grade_id);
			return new ResponseEntity<RESTError>(new RESTError(1, "Grade with this id " + grade_id 
					+ " doesn't exist"), HttpStatus.NOT_FOUND);
		} 
		
		if (teachingSubject == null) {
	        logger.error("There is no teaching subject found with " + teachsubj_id);
			return new ResponseEntity<RESTError>(new RESTError(2, "No teaching subject found with " + teachsubj_id), 
					HttpStatus.NOT_FOUND);
		}
		
		if (!teachingSubject.getGrades().contains(grade)) {
	        logger.error("The grade with " + grade_id + " ID doesn't exist in" + " teaching subject with " + teachsubj_id + " ID.");
			return new ResponseEntity<RESTError>(new RESTError(3, "The grade with " + grade_id 
					+ " ID doesn't exist in" + " teaching subject with " + teachsubj_id + " ID."), 
					HttpStatus.NOT_FOUND);
			}
		
		//token autentifikacija ipak
		if (!teachingSubject.getTeacher().equals(teacher)) {
			return new ResponseEntity<RESTError>(new RESTError(2, "The teacher with " + teacher_id 
					+ " ID doesn't teach" + " teaching subject with " + teachsubj_id + " ID."), 
					HttpStatus.FORBIDDEN);
		}
		
		// servis pa udji u ovo deleteGrade(); i izvrsi pre ovo dole
		
		gradeRepository.delete(grade);
        logger.info("Deleting grade from the database");

	    teachingSubject.getGrades().remove(grade);
        logger.info("Removing grade from the teaching subject");

		teacherSubjectRepository.save(teachingSubject);        
		logger.info("Saving the teaching subject");

		return new ResponseEntity<GradeEntity>(grade, HttpStatus.OK);
	}
}
