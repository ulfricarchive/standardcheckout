package com.standardcheckout.web.vaadin.addons;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

public class Tip extends HorizontalLayout {

	public Tip(String message) {
		Label tip = new Label(message);
		tip.addStyleName(ValoTheme.LABEL_SMALL);
		tip.addStyleName(ValoTheme.LABEL_LIGHT);
		addComponent(tip);
		setComponentAlignment(tip, Alignment.TOP_CENTER);
	}

}
