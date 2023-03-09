package com.iktpreobuka.projekat.controllers;

import java.util.List;
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
import com.iktpreobuka.projekat.entities.dto.GradeDTO;
import com.iktpreobuka.projekat.repositories.GradeRepository;
import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.services.GradeDaoImpl;
import com.iktpreobuka.projekat.utils.RESTError;

@RestController
@RequestMapping(path = "/api/project/grade")
public class GradeController {

	@Autowired
	private GradeRepository gradeRepository;
	
	@Autowired
	private GradeDaoImpl gradeDaoImpl;
	
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
		return gradeDaoImpl.getGradesByStudentUsername(username, authentication);
	}
	
	@Secured({"ROLE_STUDENT", "ROLE_ADMIN", "ROLE_TEACHER", "ROLE_PARENT"})
	@RequestMapping(method = RequestMethod.GET, value = "allGrades/by_studentUsername/for-subject")
	public ResponseEntity<?> getGradesByStudentUsernameForSubject(@RequestParam String username,
			@RequestParam String subject_name, Authentication authentication) {
		return gradeDaoImpl.getGradesByStudentUsernameForSubject(username, subject_name, authentication);
	}
	
	@Secured({"ROLE_STUDENT", "ROLE_ADMIN", "ROLE_TEACHER", "ROLE_PARENT"})
	@RequestMapping(method = RequestMethod.GET, value = "allGrades/by_studentId")
	public ResponseEntity<?> getGradesByStudentIdForSubject(@RequestParam Integer id,
			@RequestParam String subject_name, Authentication authentication) {
		return gradeDaoImpl.getGradesByStudentIdForSubject(id, subject_name, authentication);
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
		return gradeDaoImpl.createGrade(newGradeDTO, result, authentication);
	}

	@Secured({"ROLE_ADMIN", "ROLE_TEACHER"})
	@RequestMapping(method = RequestMethod.PUT, value = "/updateGrade")
	public ResponseEntity<?> updateGrade(@Valid @RequestBody GradeDTO updateGradeDTO, BindingResult result, 
			@RequestParam Integer grade_id, Authentication authentication) {
		return gradeDaoImpl.updateGrade(updateGradeDTO, result, grade_id, authentication);
	}

	@Secured({ "ROLE_ADMIN", "ROLE_TEACHER" })
	@RequestMapping(method = RequestMethod.DELETE, value = "/deleteGrade/grade/{grade_id}/teachSubj/{teachsubj_id}")
	public ResponseEntity<?> deleteGrade(@PathVariable Integer grade_id, @PathVariable Integer teachsubj_id, 
			@RequestParam Integer teacher_id, Authentication authentication) {
		return gradeDaoImpl.deleteGrade(grade_id, teachsubj_id, teacher_id, authentication);
	}
	
}
