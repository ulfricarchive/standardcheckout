package com.standardcheckout.web.vaadin.addons;

import com.standardcheckout.web.stripe.Country;
import com.vaadin.server.ThemeResource;

public class CountryFlagResource extends ThemeResource {

	public CountryFlagResource(Country country) {
		super("flags/" + country.getTwoLetter().toLowerCase() + ".svg");
	}

}
