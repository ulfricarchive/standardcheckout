package com.standardcheckout.web.stripe;

import java.util.Map;

import com.stripe.model.Charge;
import com.stripe.model.Customer;

public interface StripeService {

	String getClientId();

	String getApiKey();

	String createConnectUrl(String session);

	String createUserId(String oauthCode);

	Customer createCustomer(Map<String, Object> customer);

	Customer getCustomer(String customerId);

	boolean updateCustomer(Customer customer, Map<String, Object> patch);

	Charge charge(String customerId, ChargeDetails details);

}
