package com.standardcheckout.web;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

public class Tip extends HorizontalLayout {

	public Tip(String message) {
		Label tip = new Label(message);
		tip.setStyleName(ValoTheme.LABEL_SMALL);
		addComponent(tip);
		setComponentAlignment(tip, Alignment.TOP_CENTER);
	}

}
