package com.standardcheckout.web.stripe;

import java.util.List;

import com.stripe.model.Card;
import com.stripe.model.Customer;
import com.stripe.model.ExternalAccount;
import com.stripe.model.ExternalAccountCollection;

public class CustomerHelper {

	public static Card getCardOnFile(Customer customer) {
		if (customer == null) {
			return null;
		}

		ExternalAccountCollection sources = customer.getSources();
		if (sources == null) {
			return null;
		}

		List<ExternalAccount> accounts = sources.getData();
		for (ExternalAccount account : accounts) {
			if (account instanceof Card) {
				return (Card) account;
			}
		}
		return null;
	}

	private CustomerHelper() {
	}

}
