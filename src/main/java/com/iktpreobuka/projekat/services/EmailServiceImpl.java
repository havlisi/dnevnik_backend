package com.iktpreobuka.projekat.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonView;
import com.iktpreobuka.projekat.entities.StudentEntity;
import com.iktpreobuka.projekat.entities.TeacherSubject;
import com.iktpreobuka.projekat.entities.dto.EmailDTO;
import com.iktpreobuka.projekat.security.Views;

@Service
public class EmailServiceImpl implements EmailService{

	@Autowired
	public JavaMailSender emailSender;
	
	@JsonView(Views.Admin.class)
	protected final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void sendSimpleMessage(EmailDTO email) {
		SimpleMailMessage message = new SimpleMailMessage();
		
		message.setTo(email.getTo());
		message.setSubject(email.getSubject());
		message.setText(email.getText());
		
		emailSender.send(message);
	}

	public void messageToParents(TeacherSubject teachingSubject, StudentEntity student, Integer gradeValue) {
		
		EmailDTO email = new EmailDTO();

		//email.setTo(student.getParent().getEmail());
		email.setTo("isidorahavlovic@gmail.com");
		logger.info("Setting up parents email address");
		
		email.setSubject("Ocena - " + teachingSubject.getSubject().getSubjectName());
		logger.info("Setting up subject of the email with teaching subject in the title");

		email.setText("Poštovani " + student.getParent().getLastName() + " " + student.getParent().getFirstName() + 
				",\n\nObaveštavam Vas da je Vaše dete " + student.getFirstName() + " dobilo " + gradeValue +
				" iz predmeta " + teachingSubject.getSubject().getSubjectName() + ".\n\nSrdačan pozdrav,\n" + 
				teachingSubject.getTeacher().getLastName() + " " + teachingSubject.getTeacher().getFirstName() + 
				"\n\nOsnovna škola 'Svetozar Marković Toza', Novi Sad");
		logger.info("Setting up message of the email with information about grade, to which student is given, which subject and who has given it");

		
		sendSimpleMessage(email);
	}
	
}
