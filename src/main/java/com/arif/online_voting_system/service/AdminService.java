package com.arif.online_voting_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.arif.online_voting_system.controller.AdminController;

import jakarta.servlet.http.HttpSession;

@Service
public class AdminService {
	@Autowired
	AdminController adminController;

	public String login(String adminname, String password, HttpSession session) {
		String name = adminController.getName();
		String pwd = adminController.getPwd();

		if (name.equals(adminname) && password.equals(pwd)) {
			session.setAttribute("admin", adminname);
			session.setAttribute("success", "Login Success as Admin");
			return "admin-home";
		}
		session.setAttribute("error", "Invalid Admin Credentials");
		return "redirect:/login";
	}
}
