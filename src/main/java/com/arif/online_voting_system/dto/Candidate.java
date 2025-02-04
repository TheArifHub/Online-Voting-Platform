package com.arif.online_voting_system.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
public class Candidate {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Size(min = 3, max = 30, message = "Enter between 3-30 characters")
	@Pattern(regexp = "^[A-Za-z\\s]+$", message = "Name can only contain letters and spaces")
	private String fullname;

	@Email(message = "Enter a proper email")
	@NotEmpty(message = "* It is a required field")
	private String email;

	@NotEmpty(message = "Political Party is required")
	private String party;

	@NotEmpty(message = "Constituency is required")
	private String constituency;

	@Size(min = 10, max = 500, message = "Manifesto should be between 10 and 500 characters")
	private String manifesto;

	@Size(min = 8, message = "Password must be at least 8 characters")
	@Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$", message = "Password must include one uppercase, one lowercase, one number, and one special character")
	@NotEmpty(message = "* It is a required field")
	private String password;

	@Transient
	private String confirmPassword;
	
	@Transient
	private MultipartFile profilePic;

	private String profilePicUrl;

	@Column(nullable = false)
	private int otp;

	@Column(nullable = false)
	private boolean verified;
	
	@Column(name = "status")
	private String status; 
	
	@Column(name = "vote_count", nullable = false)
	private int voteCount = 0;




}
