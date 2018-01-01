package com.standardcheckout.web.ui;

import com.standardcheckout.web.stripe.CardType;
import com.standardcheckout.web.vaadin.addons.CardTypeResource;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@Title("Standard Checkout")
@SpringUI(path = "test")
public class TestUI extends ScoUI {

	@Override
	protected void init(VaadinRequest request) {
		super.init(request);

		Panel panel = new Panel();
		HorizontalLayout content = new HorizontalLayout();

		CardType cardType = CardType.VISA;
		String cardNumber = "•••• 1234";
		String expires = "01/20";

		Image cardTypeLabel = new Image();
		cardTypeLabel.setSource(new CardTypeResource(cardType));
		cardTypeLabel.setWidth("80px");
		cardTypeLabel.setHeight("80px");
		content.addComponent(cardTypeLabel);

		VerticalLayout cardData = new VerticalLayout();
		MarginInfo margin = new MarginInfo(false, false, false, false);
		cardData.setMargin(margin);
		cardData.setSpacing(false);
		Label cardOnFileTitle = new Label("Card on File");
		cardOnFileTitle.addStyleName(ValoTheme.LABEL_LARGE);
		cardData.addComponent(cardOnFileTitle);
		Label cardNumberLabel = new Label(cardNumber);
		cardNumberLabel.addStyleName(ValoTheme.LABEL_LARGE);
		cardNumberLabel.addStyleName(ValoTheme.LABEL_LIGHT);
		cardData.addComponent(cardNumberLabel);
		Label expiresLabel = new Label("Expires " + expires);
		expiresLabel.addStyleName(ValoTheme.LABEL_SMALL);
		expiresLabel.addStyleName(ValoTheme.LABEL_LIGHT);
		cardData.addComponent(expiresLabel);
		content.addComponent(cardData);

		panel.setContent(content);
		sendComponentMiddle(panel);
		panel.setWidth("206px");
	}

}
