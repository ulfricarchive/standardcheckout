package com.standardcheckout.web.webstore;

import java.util.Set;
import java.util.UUID;

import com.standardcheckout.web.security.PasswordProtected;

public class MinecraftCustomer extends PasswordProtected {

	private UUID mojangId;
	private String stripeId;
	private Set<UUID> authorizedWebstores;

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

	public Set<UUID> getAuthorizedWebstores() {
		return authorizedWebstores;
	}

	public void setAuthorizedWebstores(Set<UUID> authorizedWebstores) {
		this.authorizedWebstores = authorizedWebstores;
	}

}
