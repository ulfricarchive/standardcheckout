package com.standardcheckout.web.ui;

import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;

@Title("Standard Checkout")
@SpringUI(path = "test")
public class TestUI extends ScoUI {

	@Override
	protected void init(VaadinRequest request) {
		super.init(request);
	}

}
