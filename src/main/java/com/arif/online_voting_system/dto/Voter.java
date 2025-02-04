package com.arif.online_voting_system.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Entity
@Data
public class Voter {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Size(min = 3, max = 30, message = "Enter between 3-30 characters")
	@Pattern(regexp = "^[A-Za-z\\s]+$", message = "Name can only contain letters and spaces")
	private String fullname;

	@Email(message = "Enter a proper email")
	@NotEmpty(message = "* It is a required field")
	private String email;

	@Pattern(regexp = "^[A-Za-z0-9]{6,12}$", message = "Voter ID must be alphanumeric and 6-12 characters long")
	@NotEmpty(message = "* It is a required field")
	private String voterid;

	@Size(min = 8, message = "Password must be at least 8 characters")
	@Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$", message = "Password must include one uppercase, one lowercase, one number, and one special character")
	@NotEmpty(message = "* It is a required field")
	private String password;

	@Transient
	private String confirmpassword;

	@Column(nullable = false)
	private int otp;

	@Column(nullable = false)
	private boolean verified;

	@Column(name = "has_voted", nullable = false)
	private boolean hasVoted = false;

}
