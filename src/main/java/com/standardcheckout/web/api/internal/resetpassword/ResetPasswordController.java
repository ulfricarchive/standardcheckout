package com.standardcheckout.web.api.internal.resetpassword;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.standardcheckout.web.webstore.CustomersRepository;
import com.standardcheckout.web.webstore.MinecraftCustomer;
import com.ulfric.buycraft.sco.model.StandardCheckoutResetRequest;

@RestController
@RequestMapping("/internal/resetpassword") // TODO use jwt instead of the 'admin token' system to authenticate
public class ResetPasswordController {

	@Value("${ADMIN_TOKEN}")
	private String adminToken;

	@Inject
	private CustomersRepository customers;

	@PostMapping(consumes = "application/json")
	public ResponseEntity<Void> charge(@RequestBody StandardCheckoutResetRequest charge) {
		if (adminToken == null || !Objects.equals(charge.getScoToken(), adminToken)) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		UUID mojangId = Objects.requireNonNull(charge.getMojangId(), "mojangId");
		MinecraftCustomer customer = customers.getCustomerByMojangId(mojangId);
		if (customer == null) {
			return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
		}

		customer.setPasswordResetToken(charge.getToken());
		customers.saveCustomer(customer);

		return new ResponseEntity<>(HttpStatus.OK);
	}

}
