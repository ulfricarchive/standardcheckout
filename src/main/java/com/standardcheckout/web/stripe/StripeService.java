package com.standardcheckout.web.stripe;

import java.util.Map;

import com.stripe.model.Customer;

public interface StripeService {

	String getClientId();

	String getApiKey();

	String createConnectUrl(String session);

	String createUserId(String oauthCode);

	Customer createCustomer(Map<String, Object> customer);

}
