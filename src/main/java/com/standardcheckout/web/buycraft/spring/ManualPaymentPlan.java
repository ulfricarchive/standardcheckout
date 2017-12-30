package com.standardcheckout.web.buycraft.spring;

import java.math.BigDecimal;
import java.util.List;

import com.ulfric.buycraft.model.ManualPayment;

public class ManualPaymentPlan {

	public List<ManualPayment> getPayments() {
		return payments;
	}

	public void setPayments(List<ManualPayment> payments) {
		this.payments = payments;
	}

	public BigDecimal getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(BigDecimal totalCost) {
		this.totalCost = totalCost;
	}

	private List<ManualPayment> payments;
	private BigDecimal totalCost;

}
