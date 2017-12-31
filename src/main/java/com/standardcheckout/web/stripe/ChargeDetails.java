package com.standardcheckout.web.stripe;

import java.math.BigDecimal;

import com.standardcheckout.web.webstore.Webstore;

public class ChargeDetails {

	private Webstore webstore;
	private BigDecimal amount;
	private String serverName;
	private String itemName;

	public Webstore getWebstore() {
		return webstore;
	}

	public void setWebstore(Webstore webstore) {
		this.webstore = webstore;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

}
