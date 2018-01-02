package com.standardcheckout.web.security;

import java.time.Instant;

import com.ulfric.buycraft.sco.model.ResetToken;

public class PasswordProtected {

	private String password;
	private Instant created;
	private ResetToken passwordResetToken;
	private Boolean accountDisabled;

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

	public ResetToken getPasswordResetToken() {
		return passwordResetToken;
	}

	public void setPasswordResetToken(ResetToken passwordResetToken) {
		this.passwordResetToken = passwordResetToken;
	}

	public Boolean getAccountDisabled() {
		return accountDisabled;
	}

	public void setAccountDisabled(Boolean accountDisabled) {
		this.accountDisabled = accountDisabled;
	}

}
