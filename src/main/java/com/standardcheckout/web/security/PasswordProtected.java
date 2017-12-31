package com.standardcheckout.web.security;

import java.time.Instant;

public class PasswordProtected {

	private String password;
	private Instant created;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Instant getCreated() {
		return created;
	}

	public void setCreated(Instant created) {
		this.created = created;
	}

}
