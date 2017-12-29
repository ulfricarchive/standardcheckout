package com.standardcheckout.web.stripe;

public enum Country {

	UNITED_STATES("United States", "US"),
	AUSTRALIA("Australia", "AU");

	private final String friendly;
	private final String twoLetter;

	Country(String friendly, String twoLetter) {
		this.friendly = friendly;
		this.twoLetter = twoLetter;
	}

	public final String getFriendlyName() {
		return friendly;
	}

	public String getTwoLetter() {
		return twoLetter;
	}

}
