package com.iktpreobuka.projekat.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.projekat.entities.GradeEntity;
import com.iktpreobuka.projekat.entities.ParentEntity;
import com.iktpreobuka.projekat.entities.StudentEntity;
import com.iktpreobuka.projekat.entities.SubjectEntity;
import com.iktpreobuka.projekat.entities.TeacherEntity;
import com.iktpreobuka.projekat.entities.TeacherSubject;
import com.iktpreobuka.projekat.entities.UserEntity;
import com.iktpreobuka.projekat.entities.dto.GradeDTO;
import com.iktpreobuka.projekat.entities.dto.GradeSubjectDTO;
import com.iktpreobuka.projekat.repositories.GradeRepository;
import com.iktpreobuka.projekat.repositories.StudentRepository;
import com.iktpreobuka.projekat.repositories.SubjectRepository;
import com.iktpreobuka.projekat.repositories.TeacherRepository;
import com.iktpreobuka.projekat.repositories.TeacherSubjectRepository;
import com.iktpreobuka.projekat.repositories.UserRepository;
import com.iktpreobuka.projekat.security.Views;
import com.iktpreobuka.projekat.utils.ErrorMessageHelper;
import com.iktpreobuka.projekat.utils.RESTError;

@Service
public class GradeDaoImpl implements GradeDao {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private TeacherSubjectRepository teacherSubjectRepository;
	
	@Autowired
	private SubjectRepository subjectRepository;
	
	@Autowired
	private GradeRepository gradeRepository;

	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private TeacherRepository teacherRepository;
	
	@Autowired
	private EmailServiceImpl emailServiceImpl;
	
	@PersistenceContext
	private EntityManager em;
	
	@JsonView(Views.Admin.class)
	protected final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	
	public ResponseEntity<?> getGradesByStudentUsername(String username, Authentication authentication) {
		
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
	

	public ResponseEntity<?> getGradesByStudentUsernameForSubject(String username, String subject_name, Authentication authentication) {
		List<GradeSubjectDTO> gradesWithSubjects = new ArrayList<GradeSubjectDTO>();
		
		for (GradeSubjectDTO gradeSubject : (List<GradeSubjectDTO>)(getGradesByStudentUsername(username, authentication).getBody())) {
			if (gradeSubject.getSubjectName().equals(subject_name)) {
				gradesWithSubjects.add(gradeSubject);
			}
		}		
		return new ResponseEntity<List<GradeSubjectDTO>>(gradesWithSubjects, HttpStatus.OK);
	}
	

	public ResponseEntity<?> getGradesByStudentIdForSubject(Integer id, String subject_name, Authentication authentication) {
		
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
	
	
	@Override
	public ResponseEntity<?> findGradesBySemester(Integer userId, Integer tsId, 
			Integer sbId, boolean firstsemester, Authentication authentication) {
		
		String signedInUserEmail = authentication.getName();
		UserEntity currentUser = userRepository.findByEmail(signedInUserEmail);
		
		Optional<UserEntity> user = userRepository.findById(userId);
		Optional<TeacherSubject> teacherSubject = teacherSubjectRepository.findById(tsId);
		Optional<SubjectEntity> subject = subjectRepository.findById(sbId);
		
		if (user == null) {
			logger.error("User with " + userId + " doesn't exist.");
			return new ResponseEntity<RESTError>(new RESTError(1, "No user found"), HttpStatus.NOT_FOUND);
		}

		if (teacherSubject == null) {
			logger.error("Teaching subject with " + tsId + " doesn't exist.");
			return new ResponseEntity<RESTError>(new RESTError(2, "No teaching subject found"), HttpStatus.NOT_FOUND);
		}
		
		if (subject == null) {
			logger.error("Subject with " + sbId + " doesn't exist.");
			return new ResponseEntity<RESTError>(new RESTError(3, "No subject found"), HttpStatus.NOT_FOUND);
		}
		
		if (userId == null || tsId == null || sbId == null) {
            logger.error("No grade found with these parameters.");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
					"One or more required parameters are missing.");
		}
		
		String hql = "";
		
	    if (firstsemester == true) {
            logger.info("Finding " + user.get().getFirstName() + " " + user.get().getLastName() + " final grade for " 
            		+ subject.get().getSubjectName() + " for first semester");
            
	        hql = "SELECT u.firstName, u.lastName, sb.subjectName, "
	            + "ROUND(AVG(g.grade),2) " 
	            + "FROM UserEntity u "
	            + "JOIN GradeEntity g ON u.id = g.student " 
	            + "JOIN TeacherSubject ts ON g.teacherSubject = ts.id "
	            + "JOIN SubjectEntity sb ON ts.subject = sb.id " 
	            + "WHERE u.id = :userId " + "AND ts.id = :tsId "
	            + "AND sb.id = :sbId " + "AND g.firstSemester = :firstsemester "
	            + "GROUP BY g.student, sb.subjectName";
	    } else {
	    	logger.info("Finding " + user.get().getFirstName() + " " + user.get().getLastName() + " final grade for " 
            		+ subject.get().getSubjectName() + " for second semester");
	    	
	        hql = "SELECT u.firstName, u.lastName, sb.subjectName, "
	            + "ROUND(AVG(g.grade),2) " 
	            + "FROM UserEntity u "
	            + "JOIN GradeEntity g ON u.id = g.student " 
	            + "JOIN TeacherSubject ts ON g.teacherSubject = ts.id "
	            + "JOIN SubjectEntity sb ON ts.subject = sb.id " 
	            + "WHERE u.id = :userId " + "AND ts.id = :tsId "
	            + "AND sb.id = :sbId " + "AND g.firstSemester = :firstsemester "
	            + "GROUP BY g.student, sb.subjectName";
	    }
		
		Query query = em.createQuery(hql);
		query.setParameter("userId", userId);
		query.setParameter("tsId", tsId);
		query.setParameter("sbId", sbId);
		query.setParameter("firstsemester", firstsemester);
		
		List<GradeEntity> result = query.getResultList();
		
		logger.info("Checking who is logged in user.");

		if (currentUser.getRole().equals("ROLE_TEACHER")) {
			logger.info("Logged in user is a teacher.");
		    TeacherEntity teacher = (TeacherEntity) currentUser;
		    boolean isTeachingStudent = false;
		    for (TeacherSubject teachingSubject : teacher.getTeacherSubject()) {
		        if (teachingSubject.getStudents().contains(user.get())) {
					logger.info("Correct! Student is taking this teaching subject");
		            isTeachingStudent = true;
		        }
		    }
		    if (isTeachingStudent) {
				logger.info("Teacher is looking at students grades.");
		        return new ResponseEntity<List<GradeEntity>>(result, HttpStatus.OK);
		    } else {
		        logger.error("Teacher is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
		        RESTError error = new RESTError(4, "Teacher is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
		        return new ResponseEntity<RESTError>(error, HttpStatus.UNAUTHORIZED);
		    }
		}
		
		if (currentUser.getRole().equals("ROLE_PARENT")) {
			logger.info("Logged in user is a students parent.");
			ParentEntity parent = (ParentEntity) currentUser;
		    boolean isParentOfStudent = false;
		    for (StudentEntity child : parent.getStudent()) {
		    	if (child.getId().equals(user.get().getId())) {
					logger.info("Correct! This is a parent to this student");
		    		isParentOfStudent = true;
		        }
		    }
		    if (isParentOfStudent) {
				logger.info("Parent is looking at childs grades.");
		        return new ResponseEntity<List<GradeEntity>>(result, HttpStatus.OK);
		    } else {
				logger.error("Parent is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
				RESTError error = new RESTError(5, "Parent is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
		        return new ResponseEntity<RESTError>(error, HttpStatus.UNAUTHORIZED);
			}
		}
		
		if (currentUser.getRole().equals("ROLE_STUDENT")) {
			logger.info("Logged in user is a student.");
			StudentEntity loggedStudent = (StudentEntity) currentUser;
			if (loggedStudent.getId().equals(user.get().getId())) {
				logger.info("Student is looking at its own grades.");
		        return new ResponseEntity<List<GradeEntity>>(result, HttpStatus.OK);
			} else {
				logger.error("Student is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
				RESTError error = new RESTError(6, "Student is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
		        return new ResponseEntity<RESTError>(error, HttpStatus.UNAUTHORIZED);
			}
		}
		
		if (currentUser.getRole().equals("ROLE_ADMIN")) {
			logger.info("Admin is looking at students grades.");
			return new ResponseEntity<List<GradeEntity>>(result, HttpStatus.OK);
		}

		logger.error("Unauthorized access");
		return new ResponseEntity<RESTError>(new RESTError(7, "Unauthorized access"), HttpStatus.UNAUTHORIZED);
	}
	
	
	@Override
	public ResponseEntity<?> findFinalGrades(Integer userId, Integer tsId, Integer sbId, Authentication authentication) {
		
		String signedInUserEmail = authentication.getName();
		UserEntity currentUser = userRepository.findByEmail(signedInUserEmail);
		
		Optional<UserEntity> user = userRepository.findById(userId);
		Optional<TeacherSubject> teacherSubject = teacherSubjectRepository.findById(tsId);
		Optional<SubjectEntity> subject = subjectRepository.findById(sbId);
		
		if (user == null) {
			logger.error("User with " + userId + " doesn't exist.");
			return new ResponseEntity<RESTError>(new RESTError(1, "No user found"), HttpStatus.NOT_FOUND);
		}

		if (teacherSubject == null) {
			logger.error("Teaching subject with " + tsId + " doesn't exist.");
			return new ResponseEntity<RESTError>(new RESTError(2, "No teaching subject found"), HttpStatus.NOT_FOUND);
		}
		
		if (subject == null) {
			logger.error("Subject with " + sbId + " doesn't exist.");
			return new ResponseEntity<RESTError>(new RESTError(3, "No subject found"), HttpStatus.NOT_FOUND);
		}
		
		if (userId == null || tsId == null || sbId == null) {
            logger.error("No grade found with these parameters.");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
					"One or more required parameters are missing.");
		}
		
		logger.info("Finding " + user.get().getFirstName() + user.get().getLastName() + " final grade for " 
        		+ subject.get().getSubjectName());
		
		String hql  = "SELECT u.firstName, u.lastName, sb.subjectName, "
	            + "ROUND(AVG(g.grade),2) " 
	            + "FROM UserEntity u "
	            + "JOIN GradeEntity g ON u.id = g.student " 
	            + "JOIN TeacherSubject ts ON g.teacherSubject = ts.id "
	            + "JOIN SubjectEntity sb ON ts.subject = sb.id " 
	            + "WHERE u.id = :userId " + "AND ts.id = :tsId "
	            + "AND sb.id = :sbId "
	            + "GROUP BY g.student, sb.subjectName";
	    
		
		Query query = em.createQuery(hql);
		query.setParameter("userId", userId);
		query.setParameter("tsId", tsId);
		query.setParameter("sbId", sbId);
		
		List<GradeEntity> result = query.getResultList();
		
		logger.info("Checking who is logged in user.");

		if (currentUser.getRole().equals("ROLE_TEACHER")) {
			logger.info("Logged in user is a teacher.");
		    TeacherEntity teacher = (TeacherEntity) currentUser;
		    boolean isTeachingStudent = false;
		    for (TeacherSubject teachingSubject : teacher.getTeacherSubject()) {
		        if (teachingSubject.getStudents().contains(user.get())) {
					logger.info("Correct! Student is taking this teaching subject");
		            isTeachingStudent = true;
		        }
		    }
		    if (isTeachingStudent) {
				logger.info("Teacher is looking at students grades.");
		        return new ResponseEntity<List<GradeEntity>>(result, HttpStatus.OK);
		    } else {
		        logger.error("Teacher is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
		        RESTError error = new RESTError(4, "Teacher is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
		        return new ResponseEntity<RESTError>(error, HttpStatus.UNAUTHORIZED);
		    }
		}
		
		if (currentUser.getRole().equals("ROLE_PARENT")) {
			logger.info("Logged in user is a students parent.");
			ParentEntity parent = (ParentEntity) currentUser;
		    boolean isParentOfStudent = false;
		    for (StudentEntity child : parent.getStudent()) {
		    	if (child.getId().equals(user.get().getId())) {
					logger.info("Correct! This is a parent to this student");
		    		isParentOfStudent = true;
		        }
		    }
		    if (isParentOfStudent) {
				logger.info("Parent is looking at childs grades.");
		        return new ResponseEntity<List<GradeEntity>>(result, HttpStatus.OK);
		    } else {
				logger.error("Parent is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
				RESTError error = new RESTError(5, "Parent is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
		        return new ResponseEntity<RESTError>(error, HttpStatus.UNAUTHORIZED);
			}
		}
		
		if (currentUser.getRole().equals("ROLE_STUDENT")) {
			logger.info("Logged in user is a student.");
			StudentEntity loggedStudent = (StudentEntity) currentUser;
			if (loggedStudent.getId().equals(user.get().getId())) {
				logger.info("Student is looking at its own grades.");
		        return new ResponseEntity<List<GradeEntity>>(result, HttpStatus.OK);
			} else {
				logger.error("Student is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
				RESTError error = new RESTError(6, "Student is unauthorized to looked at " + user.get().getFirstName() + " " + user.get().getLastName() + " grades.");
		        return new ResponseEntity<RESTError>(error, HttpStatus.UNAUTHORIZED);
			}
		}
		
		if (currentUser.getRole().equals("ROLE_ADMIN")) {
			logger.info("Admin is looking at students grades.");
			return new ResponseEntity<List<GradeEntity>>(result, HttpStatus.OK);
		}
		
		logger.error("Unauthorized access");
		return new ResponseEntity<RESTError>(new RESTError(7, "Unauthorized access"), HttpStatus.UNAUTHORIZED);
	}
	
	
	public ResponseEntity<?> createGrade(GradeDTO newGradeDTO, BindingResult result, Authentication authentication) {
		
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

        logger.debug(currentUser.getRole());
        
		if (currentUser.getRole().equals("ROLE_ADMIN")) {
			logger.info("Admin " + currentUser.getFirstName() + " " + currentUser.getLastName() + " is adding a new grade.");
		} else if (currentUser.getRole().equals("ROLE_TEACHER")) {
			TeacherEntity teacher = (TeacherEntity) currentUser;
			boolean smeDaGaOceni = false;
			for (TeacherSubject teacherSubject : student.getTeacherSubjects()) {
				for (TeacherSubject teacherSubject2 : teacher.getTeacherSubject()) {
					if (teacherSubject.getId().equals(teacherSubject2.getId())) {
						smeDaGaOceni = true;
						break;
					}
				}
			}
			if (!smeDaGaOceni) {
				logger.error("Unauthorized teacher tried to give grade.");
				return new ResponseEntity<RESTError>(new RESTError(4, "Teacher is not authorized to grade this student."), HttpStatus.UNAUTHORIZED);
			}
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

	
	public ResponseEntity<?> updateGrade(GradeDTO updateGradeDTO, BindingResult result, 
			Integer grade_id, Authentication authentication) {
		
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

        if (currentUser.getRole().equals("ROLE_ADMIN")) {
			logger.info("Admin " + currentUser.getFirstName() + " " + currentUser.getLastName() + " can update a grade.");
		} else if (currentUser.getRole().equals("ROLE_TEACHER")) {
			if (!teachingSubject.getTeacher().getId().equals(currentUser.getId())) {
				logger.error("Unauthorized teacher tried to update a grade.");
				return new ResponseEntity<RESTError>(new RESTError(4, "Teacher is not authorized to add/update grade for this student."), HttpStatus.UNAUTHORIZED);
			}
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
	
	public ResponseEntity<?> deleteGrade(Integer grade_id, Integer teachsubj_id, 
			Integer teacher_id, Authentication authentication) {
		
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

		if (currentUser.getRole().equals("ROLE_ADMIN")) {
			logger.info("Admin " + currentUser.getFirstName() + " " + currentUser.getLastName() + " can delete a grade.");
		} else if (currentUser.getRole().equals("ROLE_TEACHER")) {
			if (!teachingSubject.getTeacher().getId().equals(currentUser.getId())) {
				logger.error("Unauthorized teacher tried to delete a grade.");
				return new ResponseEntity<RESTError>(new RESTError(5, "Teacher is not authorized to delete grade."
						+ " for this student."), HttpStatus.UNAUTHORIZED);
			}
		} else {
			logger.error("Unauthorized user tried to give grade.");
			return new ResponseEntity<RESTError>(new RESTError(5, "User is not authorized to delete students."), HttpStatus.UNAUTHORIZED);
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
