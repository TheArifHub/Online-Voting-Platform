package com.arif.online_voting_system.helper;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import com.arif.online_voting_system.dto.Voter;
import org.thymeleaf.context.Context;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.validation.Valid;
@Service
public class MyMailSender {
	
	@Autowired
	JavaMailSender mailSender;
	
	@Autowired
	TemplateEngine templateEngine;

	public void sendOtp(@Valid Voter voter) throws UnsupportedEncodingException, MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		
		helper.setFrom("yourmail@gmail.com", "Online-Voting-System");
		helper.setTo(voter.getEmail());
		helper.setSubject("OTP For Creating Account With Us");
		
		Context context = new Context();
		context.setVariable("x", voter);
		
		helper.setText(templateEngine.process("otp-template.html", context),true);
		mailSender.send(message);
	}
	
}
