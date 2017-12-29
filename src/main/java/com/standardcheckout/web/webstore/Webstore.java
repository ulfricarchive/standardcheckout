package com.standardcheckout.web.webstore;

import com.standardcheckout.web.security.PasswordProtected;

public class Webstore extends PasswordProtected {

	public String getStripeId() {
		return stripeId;
	}

	public void setStripeId(String stripeId) {
		this.stripeId = stripeId;
	}

	private String storeId;
	private String theme;
	private String logoUrl;
	private String stripeId;
	private String stripeSession;

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public String getStripeSession() {
		return stripeSession;
	}

	public void setStripeSession(String stripeSession) {
		this.stripeSession = stripeSession;
	}

	public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

}
