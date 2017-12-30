package com.standardcheckout.web.buycraft;

import java.util.List;

import com.standardcheckout.web.buycraft.spring.ManualPaymentPlan;
import com.ulfric.buycraft.model.Cart;

public interface BuycraftService {

	ManualPaymentPlan asManualPayments(Cart cart, String token);

	List<Boolean> process(ManualPaymentPlan plan, String token);

}
