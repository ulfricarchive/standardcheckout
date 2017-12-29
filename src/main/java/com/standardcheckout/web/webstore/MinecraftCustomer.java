package com.standardcheckout.web.webstore;

import java.util.UUID;

import com.standardcheckout.web.security.PasswordProtected;

public class MinecraftCustomer extends PasswordProtected {

	private UUID mojangId;
	private String stripeId;

	public UUID getMojangId() {
		return mojangId;
	}

	public void setMojangId(UUID mojangId) {
		this.mojangId = mojangId;
	}

	public String getStripeId() {
		return stripeId;
	}

	public void setStripeId(String stripeId) {
		this.stripeId = stripeId;
	}

}
