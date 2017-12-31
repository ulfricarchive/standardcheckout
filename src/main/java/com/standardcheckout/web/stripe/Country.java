package com.standardcheckout.web.stripe;

public enum Country {

	UNITED_STATES("United States", "US"),
	AUSTRALIA("Australia", "AU"),
	AUSTRIA("Austria", "AT"),
	BELGIUM("Belgium", "BE"),
	BRAZIL("Brazil", "BR"),
	CANADA("Canada", "CA"),
	DENMARK("Denmark", "DK"),
	FINLAND("Finland", "FI"),
	FRANCE("France", "FR"),
	GERMANY("Germany", "DE"),
	HONG_KONG("Hong Kong", "HK"),
	IRELAND("Ireland", "IE"),
	//ITALY("Italy", "IT"),
	JAPAN("Japan", "JP"),
	LUXEMBOURG("Luxembourg", "LU"),
	//MEXICO("Mexico", "MX"),
	NETHERLANDS("Netherlands", "NL"),
	NEW_ZEALAND("New Zealand", "NZ"),
	NORWAY("Norway", "NO"),
	//PORTUGAL("Portugal", "PT"),
	SINGAPORE("Singapore", "SG"),
	SPAIN("Spain", "ES"),
	SWEDEN("Sweden", "SE"),
	UNITED_KINGDOM("United Kingdom", "UK");

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
