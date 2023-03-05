package com.iktpreobuka.projekat.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.iktpreobuka.projekat.entities.StudentEntity;
import com.iktpreobuka.projekat.entities.TeacherSubject;
import com.iktpreobuka.projekat.entities.dto.EmailDTO;

@Service
public class EmailServiceImpl implements EmailService{

	@Autowired
	public JavaMailSender emailSender;
	
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

		email.setTo(student.getParent().getEmail());
		email.setSubject("Ocena - " + teachingSubject.getSubject().getSubjectName());
		email.setText("Poštovani " + student.getParent().getLastName() + " " + student.getParent().getFirstName() + 
				",\n\nObaveštavam Vas da je Vaše dete " + student.getFirstName() + " dobilo " + gradeValue +
				" iz predmeta " + teachingSubject.getSubject().getSubjectName() + ".\n\nSrdačan pozdrav,\n" + 
				teachingSubject.getTeacher().getLastName() + " " + teachingSubject.getTeacher().getFirstName() + 
				"\n\nOsnovna škola 'Svetozar Marković Toza', Novi Sad");
		
		sendSimpleMessage(email);
	}
	
}
