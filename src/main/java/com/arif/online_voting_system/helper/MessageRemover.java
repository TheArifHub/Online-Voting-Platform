package com.arif.online_voting_system.helper;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class MessageRemover {
	public void remove() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		HttpSession session = request.getSession();

		session.removeAttribute("error");
		session.removeAttribute("success");
	}
}

