package com.standardcheckout.web.api.charge;

import java.math.BigDecimal;
import java.util.Objects;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.standardcheckout.web.buycraft.BuycraftService;
import com.standardcheckout.web.buycraft.spring.ManualPaymentPlan;
import com.standardcheckout.web.stripe.ChargeDetails;
import com.standardcheckout.web.stripe.StripeService;
import com.standardcheckout.web.webstore.CustomersService;
import com.standardcheckout.web.webstore.MinecraftCustomer;
import com.standardcheckout.web.webstore.Webstore;
import com.standardcheckout.web.webstore.WebstoreService;
import com.stripe.model.Charge;
import com.ulfric.buycraft.sco.model.StandardCheckoutError;
import com.ulfric.buycraft.sco.model.StandardCheckoutChargeRequest;
import com.ulfric.buycraft.sco.model.StandardCheckoutChargeResponse;

@RestController
@RequestMapping("/api/charge")
public class ChargeController {

	@Inject
	private BuycraftService buycraft;

	@Inject
	private StripeService stripe;

	@Inject
	private CustomersService customers;

	@Inject
	private WebstoreService webstores;

	@ResponseBody
	@PostMapping(produces = "application/json", consumes = "application/json")
	public StandardCheckoutChargeResponse charge(@RequestBody StandardCheckoutChargeRequest charge) {
		StandardCheckoutChargeResponse response = new StandardCheckoutChargeResponse();

		if (StringUtils.isEmpty(charge.getBuycraftToken())) {
			response.setError(StandardCheckoutError.MISSING_BUYCRAFT_TOKEN);
			return response;
		}

		if (StringUtils.isEmpty(charge.getScoToken())) {
			response.setError(StandardCheckoutError.MISSING_SCO_TOKEN);
			return response;
		}

		if (StringUtils.isEmpty(charge.getWebstoreId())) {
			response.setError(StandardCheckoutError.MISSING_WEBSTORE_ID);
			return response;
		}

		if (charge.getCart() == null) {
			response.setError(StandardCheckoutError.MISSING_CART);
			return response;
		}

		if (charge.getPurchaser() == null) {
			response.setError(StandardCheckoutError.MISSING_PURCHASER);
			return response;
		}

		Webstore webstore = webstores.getWebstore(charge.getWebstoreId());
		if (webstore == null) {
			response.setError(StandardCheckoutError.INVALID_WEBSTORE);
			return response;
		}

		if (!Objects.equals(webstore.getToken(), charge.getScoToken())) {
			response.setError(StandardCheckoutError.INCORRECT_SCO_TOKEN); // TODO rate limit
			return response;
		}

		MinecraftCustomer customer = customers.getCustomerByMojangId(charge.getPurchaser());
		if (customer == null ||
				customer.getAuthorizedWebstores() == null ||
				!customer.getAuthorizedWebstores().contains(webstore.getAuthorizationId())) {
			response.setRequiresAuthorization(true);
			return response;
		}

		ManualPaymentPlan plan = buycraft.asManualPayments(charge.getCart(), charge.getBuycraftToken());
		if (plan == null || BigDecimal.ZERO.compareTo(plan.getTotalCost()) == 0) {
			response.setError(StandardCheckoutError.INCORRECT_BUYCRAFT_TOKEN);
			return response;
		}

		ChargeDetails details = new ChargeDetails();
		details.setAmount(plan.getTotalCost());
		details.setServerName(StringUtils.isEmpty(webstore.getFriendlyName()) ? webstore.getStoreId() : webstore.getFriendlyName());
		details.setWebstore(webstore);
		details.setItemName(charge.getItemName());
		Charge stripeCharge = stripe.charge(customer.getStripeId(), details);
		if (stripeCharge == null) {
			response.setError(StandardCheckoutError.PAYMENT_FAILED);
			return response;
		}

		response.setState(true);
		return response;
	}

}
