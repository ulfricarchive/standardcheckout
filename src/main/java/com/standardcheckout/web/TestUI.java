package com.standardcheckout.web;

import javax.inject.Inject;

import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;
import org.vaadin.textfieldformatter.CreditCardFieldFormatter;

import com.standardcheckout.web.helper.EnvironmentHelper;
import com.standardcheckout.web.stripe.CardType;
import com.standardcheckout.web.stripe.Country;
import com.standardcheckout.web.vaadin.addons.CardTypeResource;
import com.standardcheckout.web.vaadin.addons.CountryFlagResource;
import com.vaadin.annotations.Title;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@Title("Standard Checkout")
@SpringUI(path = "test")
public class TestUI extends ScoUI {

	@Inject
	private ResourceLoader resources;

	@Override
	protected void init(VaadinRequest request) {
		super.init(request);

		TextField nameField = new TextField("Full Name");
		nameField.setPlaceholder("John Smith");
		nameField.setId("ccname");
		nameField.setRequiredIndicatorVisible(true);
		sendComponentMiddle(nameField);

		TextField emailField = new TextField("Email");
		emailField.setPlaceholder("hello@example.com");
		emailField.setId("email");
		emailField.setRequiredIndicatorVisible(true);
		sendComponentMiddle(emailField);

		HorizontalLayout cardFields = new HorizontalLayout();
		cardFields.setMargin(false);
		cardFields.setSpacing(false);
		cardFields.addStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);

		TextField cardNumberField = new TextField("Card Number");
		cardNumberField.setWidth("100%");
		cardNumberField.setMaxLength(19);
		cardNumberField.setPlaceholder("•••• •••• •••• ••••");
		cardNumberField.setId("card-number");
		cardNumberField.setRequiredIndicatorVisible(true);
		cardNumberField.addStyleName("vertical");
		new CreditCardFieldFormatter(cardNumberField);

		Image cardTypeLabel = new Image();
		cardTypeLabel.setSource(new CardTypeResource(null));
		cardTypeLabel.setWidth("40px");
		cardTypeLabel.setHeight("40px");

		MutableObject<CardType> type = new MutableObject<>();
		cardNumberField.addValueChangeListener(ignore -> {
			CardType currentType = CardType.detect(cardNumberField.getValue());
			if (currentType == type.getValue()) {
				return;
			}

			type.setValue(currentType);
			cardTypeLabel.setSource(new CardTypeResource(currentType));
		});

		cardFields.addComponents(cardNumberField, cardTypeLabel);
		cardFields.setComponentAlignment(cardTypeLabel, Alignment.BOTTOM_CENTER);
		cardFields.setExpandRatio(cardNumberField, 9);
		cardFields.setExpandRatio(cardTypeLabel, 1);

		sendComponentMiddle(cardFields);

		HorizontalLayout extraCardFields = new HorizontalLayout();

		TextField monthField = new TextField("Month");
		monthField.setId("expiry-month");
		monthField.setPlaceholder("MM");
		monthField.setMaxLength(2);
		monthField.setWidth("100%");
		monthField.setRequiredIndicatorVisible(true);
		monthField.addStyleName(ValoTheme.TEXTFIELD_SMALL);

		TextField yearField = new TextField("Year");
		yearField.setId("expiry-year");
		yearField.setPlaceholder("YY");
		yearField.setMaxLength(2);
		yearField.setWidth("100%");
		yearField.setRequiredIndicatorVisible(true);
		yearField.addStyleName(ValoTheme.TEXTFIELD_SMALL);

		TextField cvvField = new TextField("CVV");
		cvvField.setId("cvv");
		cvvField.setPlaceholder("123");
		cvvField.setMaxLength(5);
		cvvField.setWidth("100%");
		cvvField.setRequiredIndicatorVisible(true);
		cvvField.addStyleName(ValoTheme.TEXTFIELD_SMALL);

		extraCardFields.addComponents(monthField, yearField, cvvField);
		extraCardFields.setExpandRatio(monthField, 1);
		extraCardFields.setExpandRatio(yearField, 1);
		extraCardFields.setExpandRatio(cvvField, 1);
		sendComponentMiddle(extraCardFields);

		HorizontalLayout locationFields = new HorizontalLayout();

		ComboBox<Country> countryField = new ComboBox<>("Country");
		countryField.setWidth("100%");
		countryField.setItems(Country.values());
		countryField.setRequiredIndicatorVisible(true);
		countryField.setItemCaptionGenerator(Country::getFriendlyName);
		countryField.setItemIconGenerator(CountryFlagResource::new);

		TextField zipCodeField = new TextField("Zip Code");
		zipCodeField.setId("zipcode");
		zipCodeField.setWidth("100%");
		zipCodeField.setRequiredIndicatorVisible(true);

		locationFields.addComponents(countryField, zipCodeField);
		locationFields.setExpandRatio(countryField, 5);
		locationFields.setExpandRatio(zipCodeField, 3);

		sendComponentMiddle(locationFields);

		VerticalLayout popupContent = new VerticalLayout();
		popupContent.setMargin(true);
		PopupView popup = new PopupView(null, popupContent);
		popup.setHideOnMouseOut(false);
		popup.setWidth("100%");
		getGridSection(1, 0).addComponent(popup);

		Button close = new Button(VaadinIcons.CLOSE);
		close.addStyleName(ValoTheme.BUTTON_DANGER);
		close.addClickListener(click -> popup.setPopupVisible(false));

		String terms = EnvironmentHelper.readFileOnClassPath(resources, "static/terms.txt");
		Label termsLabel = new Label(terms);
		termsLabel.setContentMode(ContentMode.PREFORMATTED);

		popupContent.addComponents(close, termsLabel);
		popupContent.setComponentAlignment(close, Alignment.MIDDLE_CENTER);

		CheckBox termsOfServiceField = new CheckBox("I agree to the Terms and Conditions");

		termsOfServiceField.addValueChangeListener(change -> {
			if (!Boolean.TRUE.equals(change.getOldValue())) {
				popup.setPopupVisible(true);
			}
		});

		sendComponentMiddle(termsOfServiceField);

		Button save = sendFriendlyButtonMiddle("Save", click -> {
			if (!Boolean.TRUE.equals(termsOfServiceField.getValue())) {
				sendError(termsOfServiceField, "You must agree to the Terms of Service");
				return;
			}

			String name = nameField.getValue();
			if (StringUtils.isEmpty(name)) {
				sendError(nameField, "You must enter your name");
				return;
			}

			String email = emailField.getValue();
			if (StringUtils.isEmpty(email)) {
				sendError(emailField, "You must enter your email");
				return;
			}

			String cardNumber = cardNumberField.getValue();
			if (cardNumber != null) {
				cardNumber = cardNumber.replace(" ", "");
			}
			if (StringUtils.isEmpty(cardNumber)) {
				sendError(cardNumberField, "You must enter your card number");
				return;
			}

			String cardMonth = monthField.getValue();
			if (StringUtils.isEmpty(cardMonth)) {
				sendError(monthField, "You must enter your card expiration month");
				return;
			}

			String cardYear = yearField.getValue();
			if (StringUtils.isEmpty(cardYear)) {
				sendError(monthField, "You must enter your card expiration year");
				return;
			}

			String cvv = cvvField.getValue();
			if (StringUtils.isEmpty(cvv)) {
				sendError(cvvField, "You must enter your card security code");
				return;
			}

			click.getButton().setEnabled(false);
			// TODO save logic
			click.getButton().setEnabled(true);
		});
		save.setDisableOnClick(false);
		sendComponentUpper(new Tip("Your card data will be stored with Stripe"));
	}

}
