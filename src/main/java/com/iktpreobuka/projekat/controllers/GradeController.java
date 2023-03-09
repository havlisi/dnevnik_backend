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
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.projekat.entities.GradeEntity;
import com.iktpreobuka.projekat.entities.ParentEntity;
import com.iktpreobuka.projekat.entities.StudentEntity;
import com.iktpreobuka.projekat.entities.TeacherEntity;
import com.iktpreobuka.projekat.entities.TeacherSubject;
import com.iktpreobuka.projekat.entities.UserEntity;
import com.iktpreobuka.projekat.entities.dto.GradeDTO;
import com.iktpreobuka.projekat.entities.dto.GradeSubjectDTO;
import com.iktpreobuka.projekat.repositories.GradeRepository;
import com.iktpreobuka.projekat.repositories.StudentRepository;
import com.iktpreobuka.projekat.repositories.TeacherRepository;
import com.iktpreobuka.projekat.repositories.TeacherSubjectRepository;
import com.iktpreobuka.projekat.repositories.UserRepository;
import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.services.EmailServiceImpl;
import com.iktpreobuka.projekat.services.GradeDaoImpl;
import com.iktpreobuka.projekat.utils.ErrorMessageHelper;
import com.iktpreobuka.projekat.utils.RESTError;

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
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private GradeDaoImpl gradeDaoImpl;
	
	@Autowired
	private EmailServiceImpl emailServiceImpl;
	
	@JsonView(Views.Admin.class)
	protected final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
	
	@Secured("ROLE_ADMIN")
	@JsonView(Views.Admin.class)
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

	@Secured({"ROLE_STUDENT", "ROLE_ADMIN", "ROLE_TEACHER", "ROLE_PARENT"})
	@RequestMapping(method = RequestMethod.GET, value = "allGrades/by_studentUsername")
	public ResponseEntity<?> getGradesByStudentUsername(@RequestParam String username,
			Authentication authentication) {
		String signedInUserEmail = authentication.getName();
		UserEntity currentUser = userRepository.findByEmail(signedInUserEmail);
		
		Optional<StudentEntity> student = studentRepository.findByUsername(username);
		
		if (!student.isPresent()) {
			logger.error("Student " + username + " not found");
			RESTError error = new RESTError(1, "Student " + username + " not found");
			return new ResponseEntity<RESTError>(error, HttpStatus.NOT_FOUND);
		}
		
		List<GradeEntity> grades = student.get().getGrades();

		if (grades.isEmpty()) {
			logger.error("Grades for student " + username + " not found");
			return new ResponseEntity<RESTError>(new RESTError(2, "Grades for student " 
					+ username + " not found"), HttpStatus.NOT_FOUND);
		}
		
		List<GradeSubjectDTO> gradesWithSubjects = new ArrayList<GradeSubjectDTO>();
		for (GradeEntity grade : grades) {
			GradeSubjectDTO gradeSubject = new GradeSubjectDTO(grade.getGrade(), grade.isFirstSemester(), 
					grade.getTeacherSubject().getSubject().getSubjectName());
			gradesWithSubjects.add(gradeSubject);
		}
		
		if (currentUser.getRole().equals("ROLE_TEACHER")) {
			logger.info("Logged in user is a teacher.");
		    TeacherEntity teacher = (TeacherEntity) currentUser;
		    boolean isTeachingStudent = false;
		    for (TeacherSubject teachingSubject : teacher.getTeacherSubject()) {
		        if (teachingSubject.getStudents().contains(student.get())) {
					logger.info("Correct! Student is taking this teaching subject");
		            isTeachingStudent = true;
		        }
		    }
		    if (isTeachingStudent) {
				logger.info("Teacher is looking at students grades.");
		        return new ResponseEntity<List<GradeSubjectDTO>>(gradesWithSubjects, HttpStatus.OK);
		    } else {
		        logger.error("Teacher is unauthorized to looked at " + username + " grades.");
		        RESTError error = new RESTError(3, "Teacher is unauthorized to looked at " + username + " grades.");
		        return new ResponseEntity<RESTError>(error, HttpStatus.UNAUTHORIZED);
		    }
		}
		
		if (currentUser.getRole().equals("ROLE_PARENT")) {
			logger.info("Logged in user is a students parent.");
			ParentEntity parent = (ParentEntity) currentUser;
		    boolean isParentOfStudent = false;
		    for (StudentEntity child : parent.getStudent()) {
		    	if (child.getId().equals(student.get().getId())) {
					logger.info("Correct! This is a parent to this student");
		    		isParentOfStudent = true;
		        }
		    }
		    if (isParentOfStudent) {
				logger.info("Parent is looking at childs grades.");
		        return new ResponseEntity<List<GradeSubjectDTO>>(gradesWithSubjects, HttpStatus.OK);
		    } else {
				logger.error("Parent is unauthorized to looked at " + username + " grades.");
				RESTError error = new RESTError(4, "Parent is unauthorized to looked at " + username + " grades.");
		        return new ResponseEntity<RESTError>(error, HttpStatus.UNAUTHORIZED);
			}
		}
		
		if (currentUser.getRole().equals("ROLE_STUDENT")) {
			logger.info("Logged in user is a student.");
			StudentEntity loggedStudent = (StudentEntity) currentUser;
			if (loggedStudent.getId().equals(student.get().getId())) {
				logger.info("Student is looking at its own grades.");
		        return new ResponseEntity<List<GradeSubjectDTO>>(gradesWithSubjects, HttpStatus.OK);
			} else {
				logger.error("Student is unauthorized to looked at " + username + " grades.");
				RESTError error = new RESTError(5, "Student is unauthorized to looked at " + username + " grades.");
		        return new ResponseEntity<RESTError>(error, HttpStatus.UNAUTHORIZED);
			}
		}

		if (currentUser.getRole().equals("ROLE_ADMIN")) {
		    return new ResponseEntity<List<GradeSubjectDTO>>(gradesWithSubjects, HttpStatus.OK);
		}

		return new ResponseEntity<RESTError>(new RESTError(6, "Unauthorized access"), HttpStatus.UNAUTHORIZED);
	}
	
	
	@Secured({"ROLE_STUDENT", "ROLE_ADMIN", "ROLE_TEACHER", "ROLE_PARENT"})
	@RequestMapping(method = RequestMethod.GET, value = "allGrades/by_studentId")
	public ResponseEntity<?> getGradesByStudentIdForSubject(@RequestParam Integer id,
			@RequestParam String subject_name, Authentication authentication) {
		
		String signedInUserEmail = authentication.getName();
		UserEntity currentUser = userRepository.findByEmail(signedInUserEmail);
		
		Optional<StudentEntity> student = studentRepository.findById(id);
		
		if (!student.isPresent()) {
			logger.error("Student with " + id + " ID not found");
			RESTError error = new RESTError(1, "Student with " + id + " ID not found");
			return new ResponseEntity<RESTError>(error, HttpStatus.NOT_FOUND);
		}
		
		List<GradeEntity> grades = student.get().getGrades();

		if (grades.isEmpty()) {
			logger.error("Grades for student " + id + " not found");
			return new ResponseEntity<RESTError>(new RESTError(2, "Grades for student with " 
					+ id + " ID not found"), HttpStatus.NOT_FOUND);
		}
		
		List<GradeSubjectDTO> gradesWithSubjects = new ArrayList<GradeSubjectDTO>();
		for (GradeEntity grade : grades) {
			if (grade.getTeacherSubject().getSubject().getSubjectName().equals(subject_name)) {
				GradeSubjectDTO gradeSubject = new GradeSubjectDTO(grade.getGrade(), grade.isFirstSemester(), 
						grade.getTeacherSubject().getSubject().getSubjectName());
				gradesWithSubjects.add(gradeSubject);
			}
		}
		
		if (currentUser.getRole().equals("ROLE_TEACHER")) {
			logger.info("Logged in user is a teacher.");
		    TeacherEntity teacher = (TeacherEntity) currentUser;
		    boolean isTeachingStudent = false;
		    for (TeacherSubject teachingSubject : teacher.getTeacherSubject()) {
		        if (teachingSubject.getStudents().contains(student.get())) {
					logger.info("Correct! Student is taking this teaching subject");
		            isTeachingStudent = true;
		        }
		    }
		    if (isTeachingStudent) {
				logger.info("Teacher is looking at students grades.");
		        return new ResponseEntity<List<GradeSubjectDTO>>(gradesWithSubjects, HttpStatus.OK);
		    } else {
		        logger.error("Teacher is unauthorized to looked at " + id + " grades.");
		        RESTError error = new RESTError(3, "Teacher is unauthorized to looked at " + id + " grades.");
		        return new ResponseEntity<RESTError>(error, HttpStatus.UNAUTHORIZED);
		    }
		}
		
		if (currentUser.getRole().equals("ROLE_PARENT")) {
			logger.info("Logged in user is a students parent.");
			ParentEntity parent = (ParentEntity) currentUser;
		    boolean isParentOfStudent = false;
		    for (StudentEntity child : parent.getStudent()) {
		    	if (child.getId().equals(student.get().getId())) {
					logger.info("Correct! This is a parent to this student");
		    		isParentOfStudent = true;
		        }
		    }
		    if (isParentOfStudent) {
				logger.info("Parent is looking at childs grades.");
		        return new ResponseEntity<List<GradeSubjectDTO>>(gradesWithSubjects, HttpStatus.OK);
		    } else {
				logger.error("Parent is unauthorized to looked at " + id + " grades.");
				RESTError error = new RESTError(4, "Parent is unauthorized to looked at " + id + " grades.");
		        return new ResponseEntity<RESTError>(error, HttpStatus.UNAUTHORIZED);
			}
		}
		
		if (currentUser.getRole().equals("ROLE_STUDENT")) {
			logger.info("Logged in user is a student.");
			StudentEntity loggedStudent = (StudentEntity) currentUser;
			if (loggedStudent.getId().equals(student.get().getId())) {
				logger.info("Student is looking at its own grades.");
		        return new ResponseEntity<List<GradeSubjectDTO>>(gradesWithSubjects, HttpStatus.OK);
			} else {
				logger.error("Student is unauthorized to looked at " + id + " grades.");
				RESTError error = new RESTError(5, "Student is unauthorized to looked at " + id + " grades.");
		        return new ResponseEntity<RESTError>(error, HttpStatus.UNAUTHORIZED);
			}
		}

		if (currentUser.getRole().equals("ROLE_ADMIN")) {
		    return new ResponseEntity<List<GradeSubjectDTO>>(gradesWithSubjects, HttpStatus.OK);
		}

		return new ResponseEntity<RESTError>(new RESTError(6, "Unauthorized access"), HttpStatus.UNAUTHORIZED);
	}
	
	@Secured({"ROLE_STUDENT", "ROLE_ADMIN", "ROLE_TEACHER", "ROLE_PARENT"})
	@RequestMapping(method = RequestMethod.GET, value = "allGrades/by_studentUsername/for-subject")
	public ResponseEntity<?> getGradesByStudentUsernameForSubject(@RequestParam String username,
			@RequestParam String subject_name, Authentication authentication) {
		
		List<GradeSubjectDTO> gradesWithSubjects = new ArrayList<GradeSubjectDTO>();
		for (GradeSubjectDTO gradeSubject : (List<GradeSubjectDTO>)(getGradesByStudentUsername(username, authentication).getBody())) {
			if (gradeSubject.getSubjectName().equals(subject_name)) {
				gradesWithSubjects.add(gradeSubject);
			}
		}		
		return new ResponseEntity<List<GradeSubjectDTO>>(gradesWithSubjects, HttpStatus.OK);
	}
			
	@Secured({"ROLE_STUDENT", "ROLE_ADMIN", "ROLE_TEACHER", "ROLE_PARENT"})
	@RequestMapping(method = RequestMethod.GET, value = "/semester")
	public ResponseEntity<?> findSubjectGradeBySemester(@RequestParam Integer userId, @RequestParam Integer tsId,
			@RequestParam Integer sbId, @RequestParam boolean firstsemester, Authentication authentication) {
		return gradeDaoImpl.findGradesBySemester(userId, tsId, sbId, firstsemester, authentication);
	}
	
	@Secured({"ROLE_STUDENT", "ROLE_ADMIN", "ROLE_TEACHER", "ROLE_PARENT"})
	@RequestMapping(method = RequestMethod.GET, value = "/finalGrade")
	public ResponseEntity<?> findGradeFinalGrade(@RequestParam Integer userId, @RequestParam Integer tsId,
			@RequestParam Integer sbId, Authentication authentication) {
		return gradeDaoImpl.findFinalGrades(userId, tsId, sbId, authentication);
	}

	@Secured({"ROLE_ADMIN", "ROLE_TEACHER"})
	@RequestMapping(method = RequestMethod.POST, value = "/newGrade")
	public ResponseEntity<?> createGrade(@Valid @RequestBody GradeDTO newGradeDTO, 
			BindingResult result, Authentication authentication) {
		
		if(result.hasErrors()) {
	        logger.error("Sent incorrect parameters.");
			return new ResponseEntity<>(ErrorMessageHelper.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		}
		
		String signedInUserEmail = authentication.getName();
		UserEntity currentUser = userRepository.findByEmail(signedInUserEmail);
		
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
		
        logger.info("Checking which user is logged in.");
		
		if (currentUser.getRole().equals("ROLE_TEACHER")) {
			if (!teachingSubject.getTeacher().getId().equals(currentUser.getId())) {
				logger.error("Unauthorized teacher tried to give grade.");
				return new ResponseEntity<RESTError>(new RESTError(4, "Teacher is not authorized to grade this student."), HttpStatus.UNAUTHORIZED);
			}
		}

		if (currentUser.getRole().equals("ROLE_ADMIN")) {
			logger.info("Admin " + currentUser.getFirstName() + " " + currentUser.getLastName() + " added new grade.");
		} else {
			logger.error("Unauthorized user tried to give grade.");
			return new ResponseEntity<RESTError>(new RESTError(5, "User is not authorized to grade students."), HttpStatus.UNAUTHORIZED);
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

	@Secured({"ROLE_ADMIN", "ROLE_TEACHER"})
	@RequestMapping(method = RequestMethod.PUT, value = "/updateGrade")
	public ResponseEntity<?> updateGrade(@Valid @RequestBody GradeDTO updateGradeDTO, BindingResult result, 
			@RequestParam Integer grade_id, Authentication authentication) {
		
		String signedInUserEmail = authentication.getName();
		UserEntity currentUser = userRepository.findByEmail(signedInUserEmail);
		
		StudentEntity student = studentRepository.findById(updateGradeDTO.getStudent_id()).get();
		TeacherSubject teachingSubject = teacherSubjectRepository.findById(updateGradeDTO.getTeachsubj_id()).get();
		GradeEntity grade = gradeRepository.findById(grade_id).get();

		if(result.hasErrors()) {
			return new ResponseEntity<>(ErrorMessageHelper.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		}
		
		if (grade == null) {
	        logger.error("There is no grade found with " + grade_id);
			return new ResponseEntity<RESTError>(new RESTError(1, "There is no grade with this id " + grade_id), HttpStatus.NOT_FOUND);
		}

		if (!grade.getTeacherSubject().getId().equals(teachingSubject.getId())) {
	        logger.error("Grade with this id " + grade_id + " doesn't exist for this teaching subject");

			return new ResponseEntity<RESTError>(new RESTError(2,  "Grade with this id " + grade_id 
					+ " doesn't exist for this teaching subject"), HttpStatus.NOT_FOUND);
		}
		
		if (!grade.getStudent().getId().equals(student.getId())) {
	        logger.error("Grade with this id " + grade_id + " doesn't exist for this student");
			return new ResponseEntity<RESTError>(new RESTError(3, "Grade with this id " + grade_id
					+ " doesn't exist for this student"), HttpStatus.NOT_FOUND);
		}
		
        logger.info("Checking which user is logged in.");

		if (currentUser.getRole().equals("ROLE_TEACHER")) {
			if (!teachingSubject.getTeacher().getId().equals(currentUser.getId())) {
				logger.error("Unauthorized teacher tried to update a grade.");
				return new ResponseEntity<RESTError>(new RESTError(4, "Teacher is not authorized to add/update grade for this student."), HttpStatus.UNAUTHORIZED);
			}
		}
		
		if (currentUser.getRole().equals("ROLE_ADMIN")) {
			logger.info("Admin " + currentUser.getFirstName() + " " + currentUser.getLastName() + " can update a grade.");
		} else {
			logger.error("Unauthorized user tried to give grade.");
			return new ResponseEntity<RESTError>(new RESTError(5, "User is not authorized to add/update grade."), HttpStatus.UNAUTHORIZED);
		}

		grade.setGrade(updateGradeDTO.getGradeValue());
		grade.setFirstSemester(updateGradeDTO.isFirstSemester());

		gradeRepository.save(grade);
        logger.info("Saving grade to the database");

		return new ResponseEntity<GradeEntity>(grade, HttpStatus.OK);
	}

	@Secured({ "ROLE_ADMIN", "ROLE_TEACHER" })
	@RequestMapping(method = RequestMethod.DELETE, value = "/deleteGrade/grade/{grade_id}/teachSubj/{teachsubj_id}")
	public ResponseEntity<?> deleteGrade(@PathVariable Integer grade_id, @PathVariable Integer teachsubj_id, 
			@RequestParam Integer teacher_id, Authentication authentication) {
		
		String signedInUserEmail = authentication.getName();
		UserEntity currentUser = userRepository.findByEmail(signedInUserEmail);
		
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
		
		if (teacher == null) {
	        logger.error("There is no teacher found with " + teacher_id);
			return new ResponseEntity<RESTError>(new RESTError(3, "No teacher found with " + teacher_id), 
					HttpStatus.NOT_FOUND);
		}
		
		if (!teachingSubject.getGrades().contains(grade)) {
	        logger.error("The grade with " + grade_id + " ID doesn't exist in" + " teaching subject with " + teachsubj_id + " ID.");
			return new ResponseEntity<RESTError>(new RESTError(4, "The grade with " + grade_id 
					+ " ID doesn't exist in" + " teaching subject with " + teachsubj_id + " ID."), 
					HttpStatus.NOT_FOUND);
			}
		
		logger.info("Checking which user is logged in.");

		if (currentUser.getRole().equals("ROLE_TEACHER")) {
			if (!teachingSubject.getTeacher().getId().equals(currentUser.getId())) {
				logger.error("Unauthorized teacher tried to delete a grade.");
				return new ResponseEntity<RESTError>(new RESTError(5, "Teacher is not authorized to delete grade."
						+ " for this student."), HttpStatus.UNAUTHORIZED);
			}
		}
		
		if (currentUser.getRole().equals("ROLE_ADMIN")) {
			logger.info("Admin " + currentUser.getFirstName() + " " + currentUser.getLastName() + " can delete a grade.");
		} else {
			logger.error("Unauthorized user tried to give grade.");
			return new ResponseEntity<RESTError>(new RESTError(6, "User is not authorized to delete grade."), HttpStatus.UNAUTHORIZED);
		}
				
		gradeRepository.delete(grade);
        logger.info("Deleting grade from the database");

	    teachingSubject.getGrades().remove(grade);
        logger.info("Removing grade from the teaching subject");

		teacherSubjectRepository.save(teachingSubject);        
		logger.info("Saving the teaching subject");

		return new ResponseEntity<GradeEntity>(grade, HttpStatus.OK);
	}
}
