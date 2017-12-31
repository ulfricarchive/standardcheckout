package com.standardcheckout.web.webstore;

import java.math.BigDecimal;
import java.util.UUID;

import com.standardcheckout.web.security.PasswordProtected;

public class Webstore extends PasswordProtected {

	private String storeId;
	private String theme;
	private String logoUrl;
	private String stripeId;
	private String stripeSession;
	private UUID authorizationId;
	private String termsOfService;
	private String friendlyName;
	private String token;
	private BigDecimal specialFeePercentage;
	private BigDecimal specialFeeCents;

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

	public UUID getAuthorizationId() {
		return authorizationId;
	}

	public void setAuthorizationId(UUID authorizationId) {
		this.authorizationId = authorizationId;
	}

	public String getTermsOfService() {
		return termsOfService;
	}

	public void setTermsOfService(String termsOfService) {
		this.termsOfService = termsOfService;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public BigDecimal getSpecialFeePercentage() {
		return specialFeePercentage;
	}

	public void setSpecialFeePercentage(BigDecimal specialFeePercentage) {
		this.specialFeePercentage = specialFeePercentage;
	}

	public BigDecimal getSpecialFeeCents() {
		return specialFeeCents;
	}

	public void setSpecialFeeCents(BigDecimal specialFeeCents) {
		this.specialFeeCents = specialFeeCents;
	}

	public String getStripeId() {
		return stripeId;
	}

	public void setStripeId(String stripeId) {
		this.stripeId = stripeId;
	}

}
