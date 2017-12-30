package com.standardcheckout.web.stripe.spring;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import com.standardcheckout.web.helper.EnvironmentHelper;
import com.standardcheckout.web.stripe.ChargeDetails;
import com.standardcheckout.web.stripe.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.oauth.OAuthException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.Token;
import com.stripe.model.oauth.TokenResponse;
import com.stripe.net.OAuth;
import com.stripe.net.RequestOptions;

@Service
public class StripeServiceImpl implements StripeService {

	@PostConstruct
	public void setupClientDefaults() {
		//Stripe.apiKey = getApiKey(); // TODO CHANGE BACK
		//Stripe.clientId = getClientId(); // TODO CHANGE BACK
		Stripe.apiKey = "sk_test_Ty4j6Dg2VN8pomFxTE55TDUR";
		Stripe.clientId = "ca_C1utFg1gieuA6KjrwjfOjnHqiYdjOcmv";
	}

	@Override
	public Customer createCustomer(Map<String, Object> customer) {
		try {
			return Customer.create(customer, requestOptions());
		} catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException | APIException exception) {
			exception.printStackTrace(); // TODO error handling
			return null;
		}
	}

	@Override
	public Customer getCustomer(String customerId) {
		try {
			return Customer.retrieve(customerId, requestOptions());
		} catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException | APIException exception) {
			exception.printStackTrace(); // TODO error handling
			return null;
		}
	}

	@Override
	public String getClientId() {
		return EnvironmentHelper.getVariable("STRIPE_CLIENT_ID").orElse(Stripe.clientId);
	}

	@Override
	public String getApiKey() {
		return EnvironmentHelper.getVariable("STRIPE_KEY").orElse(Stripe.apiKey);
	}

	@Override
	public String createConnectUrl(String session) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("state", session);
		parameters.put("response_type", "code");
		parameters.put("scope", "read_write");
		try {
			return OAuth.authorizeURL(parameters, requestOptions());
		} catch (AuthenticationException | InvalidRequestException exception) {
			exception.printStackTrace(); // TODO error handling
			return null;
		}
	}

	@Override
	public boolean updateCustomer(Customer customer, Map<String, Object> patch) {
		try {
			return customer.update(patch, requestOptions()) != null;
		} catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException
				| APIException exception) {
			exception.printStackTrace();
			return false;
		}
	}

	@Override
	public Charge charge(String customerId, ChargeDetails details) {
		RequestOptions options = requestOptions()
				.toBuilder()
				.setStripeAccount(details.getMerchantId())
				.build();

		String chargeToken = chargeToken(options, customerId);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("source", chargeToken == null ? customerId : chargeToken);
		parameters.put("currency", "usd");
		parameters.put("amount", bigDecimalToStripe(details.getAmount()));
		parameters.put("application_fee", bigDecimalToStripe(fee(details.getAmount())));
		String descriptor = details.getItemName() + " on " + details.getServerName();
		parameters.put("description", descriptor);
		parameters.put("statement_descriptor", descriptor);

		try {
			return Charge.create(parameters, options);
		} catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException
				| APIException exception) {
			exception.printStackTrace(); // TODO error handling
			return null;
		}
	}

	private BigDecimal fee(BigDecimal cost) {
		BigDecimal percent = cost.multiply(BigDecimal.valueOf(0.03D));
		BigDecimal cents = BigDecimal.valueOf(0.1D);
		return percent.max(cents).min(cost.multiply(BigDecimal.valueOf(0.3D)));
	}

	private String bigDecimalToStripe(BigDecimal value) {
		return value.setScale(2, RoundingMode.HALF_UP).movePointRight(2).toBigIntegerExact().toString();
	}

	private String chargeToken(RequestOptions options, String customerId) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("customer", customerId);

		try {
			return Token.create(parameters, options).getId();
		} catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException
				| APIException exception) {
			exception.printStackTrace(); // TODO error handling
			return null;
		}
	}

	@Override
	public String createUserId(String oauthCode) {
		TokenResponse token = token(oauthCode, "authorization_code");
		return token == null ? null : token.getStripeUserId();
	}

	private TokenResponse token(String oauthCode, String grantType) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("grant_type", grantType);
		parameters.put("code", oauthCode);
		try {
			return OAuth.token(parameters, requestOptions());
		} catch (AuthenticationException | InvalidRequestException | APIConnectionException | APIException
				| OAuthException exception) {
			exception.printStackTrace(); // TODO error handling
			return null;
		}
	}

	private RequestOptions requestOptions() {
		return RequestOptions.builder()
				.setApiKey(getApiKey())
				.build();
	}

}
