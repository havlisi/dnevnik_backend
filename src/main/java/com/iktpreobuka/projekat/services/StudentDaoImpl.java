package com.iktpreobuka.projekat.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.projekat.entities.GradeEntity;
import com.iktpreobuka.projekat.entities.ParentEntity;
import com.iktpreobuka.projekat.entities.StudentEntity;
import com.iktpreobuka.projekat.entities.TeacherEntity;
import com.iktpreobuka.projekat.entities.TeacherSubject;
import com.iktpreobuka.projekat.entities.UserEntity;
import com.iktpreobuka.projekat.entities.dto.UserDTO;
import com.iktpreobuka.projekat.repositories.GradeRepository;
import com.iktpreobuka.projekat.repositories.ParentRepository;
import com.iktpreobuka.projekat.repositories.StudentRepository;
import com.iktpreobuka.projekat.repositories.TeacherSubjectRepository;
import com.iktpreobuka.projekat.repositories.UserRepository;
import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.utils.ErrorMessageHelper;
import com.iktpreobuka.projekat.utils.RESTError;
import com.iktpreobuka.projekat.utils.UserCustomValidator;

@Service
public class StudentDaoImpl {

	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private ParentRepository parentRepository;

	@Autowired
	private TeacherSubjectRepository teacherSubjectRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private GradeRepository gradeRepository;
	
	@Autowired
	UserCustomValidator userValidator;
		
	@JsonView(Views.Admin.class)
	protected final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
	

	public ResponseEntity<?> getStudentByFirstName(String firstName, Authentication authentication) {
		
		String signedInUserEmail = authentication.getName();
		UserEntity currentUser = userRepository.findByEmail(signedInUserEmail);
		
		List<StudentEntity> allStudents = studentRepository.findByFirstName(firstName);
		logger.info("Found students with name - " + firstName + " in the database.");

		if (allStudents.isEmpty()) {
	        logger.error("No students found in the database with name: " + firstName);
			return new ResponseEntity<RESTError>(new RESTError(1, "No students found"), HttpStatus.NOT_FOUND);
		}

		if (currentUser.getRole().equals("ROLE_TEACHER")) {
			TeacherEntity teacher = (TeacherEntity) currentUser;
			
			List<StudentEntity> teachersStudents = new ArrayList<StudentEntity>();
			for (StudentEntity student : allStudents) {
				for (TeacherSubject teacherSubject : student.getTeacherSubjects()) {
					for (TeacherSubject teacherSubject2 : teacher.getTeacherSubject()) {
						if (teacherSubject.getId().equals(teacherSubject2.getId())) {
							teachersStudents.add(student);
						}
					}
				}
			}
			return new ResponseEntity<List<StudentEntity>>(teachersStudents, HttpStatus.OK);

		}
		return new ResponseEntity<List<StudentEntity>>(allStudents, HttpStatus.OK);
	}
	
	
	public ResponseEntity<?> createStudent(UserDTO newUser, BindingResult result) {
	
		if (result.hasErrors()) {
	        logger.error("Sent incorrect parameters.");
			return new ResponseEntity<>(ErrorMessageHelper.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		} else {
	        logger.info("Validating if the users password matches the confirming password");
			userValidator.validate(newUser, result);
		}
		
		UserEntity existingUserWithEmail = userRepository.findByEmail(newUser.getEmail());
        logger.info("Finding out whether there's a user with the same email.");

		if (existingUserWithEmail != null) {
	        logger.error("There is a user with the same email.");
			return new ResponseEntity<RESTError>(new RESTError(1, "Email already exists"), HttpStatus.CONFLICT);
		}

		UserEntity existingUserWithUsername = userRepository.findByUsername(newUser.getUsername()).orElse(null);
        logger.info("Finding out whether there's a user with the same username.");

		if (existingUserWithUsername != null) {
	        logger.error("There is a user with the same username.");
			return new ResponseEntity<RESTError>(new RESTError(2, "Username already exists"), HttpStatus.CONFLICT);
		}
		
		StudentEntity newStudent = new StudentEntity();

		newStudent.setFirstName(newUser.getFirstName());
		newStudent.setLastName(newUser.getLastName());
		newStudent.setUsername(newUser.getUsername());
		newStudent.setEmail(newUser.getEmail());
		newStudent.setPassword(newUser.getPassword());

		newStudent.setRole("ROLE_STUDENT");        
		logger.info("Setting users role.");

		studentRepository.save(newStudent);
        logger.info("Saving student to the database");

		return new ResponseEntity<StudentEntity>(newStudent, HttpStatus.CREATED);
	}
	
	public ResponseEntity<?> setStudentsParent(Integer parents_id, Integer students_id) {
		StudentEntity student = studentRepository.findById(students_id).orElse(null);
		ParentEntity parent = parentRepository.findById(parents_id).orElse(null);

		if (student == null) {
	        logger.error("There is no student found with " + students_id);
			return new ResponseEntity<RESTError>(new RESTError(1, "No student found"), HttpStatus.NOT_FOUND);
		}

		if (parent == null) {
	        logger.error("There is no parent found with " + parents_id);
			return new ResponseEntity<RESTError>(new RESTError(2, "No parent found"), HttpStatus.NOT_FOUND);
		}

		student.setParent(parent);
        logger.info("Setting students parent");

		studentRepository.save(student);
        logger.info("Saving student to the database");

		return new ResponseEntity<StudentEntity>(student, HttpStatus.OK);
	}
	
	public ResponseEntity<?> setStudentsTeachingSubj(Integer teachSubj_id, Integer students_id) {
		
		StudentEntity student = studentRepository.findById(students_id).orElse(null);
		TeacherSubject teacherSubject = teacherSubjectRepository.findById(teachSubj_id).orElse(null);

		if (teacherSubject == null) {
	        logger.error("There is no teaching subject found with " + teachSubj_id);
			return new ResponseEntity<RESTError>(new RESTError(1, "No teaching subject with " + teachSubj_id + " ID found"),
					HttpStatus.NOT_FOUND);
		}

		if (student == null) {
	        logger.error("There is no student found with " + students_id);
			return new ResponseEntity<RESTError>(new RESTError(2, "No student with " + students_id + " ID found"),
					HttpStatus.NOT_FOUND);
		}

		Set<TeacherSubject> teacherSubjects = student.getTeacherSubjects();
		
		teacherSubjects.add(teacherSubject);
		student.setTeacherSubjects(teacherSubjects);
        logger.info("Setting students teaching subject");

		studentRepository.save(student);
        logger.info("Saving student to the database");

		return new ResponseEntity<StudentEntity>(student, HttpStatus.OK);
	}
	
	public ResponseEntity<?> updateStudent(UserDTO updatedUser, BindingResult result, Integer id) {

		if (result.hasErrors()) {
	        logger.info("Validating users input parameters");
			return new ResponseEntity<>(ErrorMessageHelper.createErrorMessage(result), HttpStatus.BAD_REQUEST);
		} else {
	        logger.info("Validating if the users password matches the confirming password");
			userValidator.validate(updatedUser, result);
		}
		
		StudentEntity student = studentRepository.findById(id).orElse(null);

		if (student == null) {
	        logger.error("There is no teacher found with " + id);
			return new ResponseEntity<RESTError>(new RESTError(1, "No student found"), HttpStatus.NOT_FOUND);
		}

		student.setFirstName(updatedUser.getFirstName());
		student.setLastName(updatedUser.getLastName());
		student.setUsername(updatedUser.getUsername());
		student.setEmail(updatedUser.getEmail());

		studentRepository.save(student);
        logger.info("Saving student to the database");

		return new ResponseEntity<StudentEntity>(student, HttpStatus.OK);
	}
	
	public ResponseEntity<?> deleteStudentByID(Integer id) {
		Optional<StudentEntity> student = studentRepository.findById(id);

		if (student.isPresent()) {

			for (GradeEntity grade : student.get().getGrades()) {
				gradeRepository.delete(grade);
			}
			
			studentRepository.delete(student.get());
	        logger.info("Deleting the student from the database");
			return new ResponseEntity<>("Student with ID " + id + " has been successfully deleted.", HttpStatus.OK);
		} else {
	        logger.error("There is no student found with " + id);
			return new ResponseEntity<RESTError>(new RESTError(1, "No student found"), HttpStatus.NOT_FOUND);
		}
	}
	
	public ResponseEntity<?> deleteStudentByUsername(String username) {
		Optional<StudentEntity> student = studentRepository.findByUsername(username);

		if (student.isPresent()) {
			
			for (GradeEntity grade : student.get().getGrades()) {
				gradeRepository.delete(grade);
			}
			
			studentRepository.delete(student.get());
	        logger.info("Deleting the student from the database");
			return new ResponseEntity<>("Student with " + username + " username has been successfully deleted.",
					HttpStatus.OK);
		} else {
	        logger.error("There is no student found with " + username);
			return new ResponseEntity<RESTError>(new RESTError(1, "No student found"), HttpStatus.NOT_FOUND);
		}

	}
	
	public ResponseEntity<?> deleteStudentsParent(Integer parents_id, Integer students_id) {
		StudentEntity student = studentRepository.findById(students_id).orElse(null);

		if (student == null) {
	        logger.error("No student found with id " + students_id);
			return new ResponseEntity<RESTError>(new RESTError(1, "No student found"), HttpStatus.NOT_FOUND);
		}

		ParentEntity parent = student.getParent();

		if (parent == null || !parent.getId().equals(parents_id)) {
	        logger.error("No parent found with id " + parents_id + " for student with id " + students_id);
			return new ResponseEntity<RESTError>(new RESTError(1, "No parent found with id " + parents_id 
					+ " for student with id " + students_id), HttpStatus.NOT_FOUND);
		}
		
		if (student.getParent().equals(parent)) {
			parentRepository.delete(parent);
	        logger.info("Deleting parent with id " + parents_id + " from student with id " + students_id);

		}
		
		studentRepository.save(student);
        logger.info("Saving student");
		
		return new ResponseEntity<>("Parent with id " + parents_id + " was successfully removed from "
				+ "student with id " + students_id, HttpStatus.OK);
	}
	
}
