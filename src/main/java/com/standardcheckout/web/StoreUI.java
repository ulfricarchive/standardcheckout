package com.standardcheckout.web;

import java.util.UUID;

import javax.inject.Inject;

import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import com.standardcheckout.web.helper.EnvironmentHelper;
import com.standardcheckout.web.helper.StringHelper;
import com.standardcheckout.web.jclouds.BlobStoreService;
import com.standardcheckout.web.mojang.MojangService;
import com.standardcheckout.web.stripe.StripeService;
import com.standardcheckout.web.webstore.Webstore;
import com.standardcheckout.web.webstore.WebstoreService;
import com.vaadin.annotations.Title;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@Title("Standard Checkout")
@SpringUI
public class StoreUI extends ScoUI {

	private Webstore webstore;

	@Inject
	private BlobStoreService blobs;

	@Inject
	private MojangService mojang;

	@Inject
	private StripeService stripe;

	@Inject
	private PasswordEncoder encoder;

	@Inject
	private WebstoreService webstores;

	@Inject
	private ResourceLoader resources;

	@Override
	protected void init(VaadinRequest request) {
		super.init(request);

		String storeId = getOption("webstore");
		if (!StringUtils.isEmpty(storeId)) {
			webstore = webstores.getWebstore(storeId);
			if (webstore != null) {
				String logoUrl = webstore.getLogoUrl();
				if (!StringUtils.isEmpty(logoUrl)) {
					Image logo = new Image();
					logo.setSource(new ExternalResource(logoUrl));
					logo.setAlternateText(webstore.getStoreId());
					VerticalLayout top = getGridSection(1, 0);
					top.addComponent(logo);
					top.setComponentAlignment(logo, Alignment.TOP_CENTER);
				}

				flowUsername();
				return;
			}
			// TODO handle missing webstores
		}

		flowHome();
	}

	private void flowHome() {
		Label label = new Label("StandardCheckout is made by Ulfric, LLC. "
				+ "We're currently in BETA. "
				+ "If you'd like to add StandardCheckout to your server's store, "
				+ "send us an email at billing@ulfric.com");
		label.setSizeFull();
		center.addComponent(label);
	}

	private void flowUsername() {
		requestInput("username", "Minecraft Username", "Notch", usernameField -> {
			UUID user = attemptToGetUniqueId(usernameField);
			if (user == null) {
				return null;
			}

			return () -> flowPassword(user);
		});
	}

	private void flowPassword(UUID user) {
		String file = "customers/" + user.toString();
		MinecraftCustomer player = blobs.get(file, MinecraftCustomer.class);
		if (player == null || StringUtils.isEmpty(player.getPassword())) {
			requestPassword("Create a Standard Checkout Password",
					"Passwords must be at between 8 and 128 characters in length. This should NOT be the same as your Minecraft password!",
					passwordField -> {
						String value = passwordField.getValue();
						if (!StringHelper.isInBounds(value, 8, 128)) {
							sendError(passwordField, "Passwords must be between 8 and 128 characters");
							return null;
						}

						MinecraftCustomer createdPlayer = new MinecraftCustomer();
						createdPlayer.setMojangId(user);
						createdPlayer.setPassword(encoder.encode(value));
						blobs.put(file, createdPlayer);

						return () -> flowLoggedIn(createdPlayer);
					});

			return;
		}

		if (player.getMojangId() == null) {
			player.setMojangId(user);
			blobs.put(file, player);
		}

		requestPassword("Standard Checkout Password", passwordField -> {
			String value = passwordField.getValue();
			if (StringUtils.isEmpty(value)) {
				sendError(passwordField, "Your password is required");
				return null;
			}

			if (!encoder.matches(value, player.getPassword())) {
				// TODO error, captcha?
				sendError(passwordField, "Your password is incorrect");
				return null;
			}

			return () -> flowLoggedIn(player);
		});
	}

	private void flowLoggedIn(MinecraftCustomer player) {
		if (StringUtils.isEmpty(player.getStripeId())) {
			flowEnterCreditCard(player);
			return;
		}
		// TODO
	}

	private void flowEnterCreditCard(MinecraftCustomer player) {
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

		TextField cardNumberField = new TextField("Card Number");
		cardNumberField.setMaxLength(19);
		cardNumberField.setPlaceholder("4242 4242 4242 4242");
		cardNumberField.setId("card-number");
		cardNumberField.setRequiredIndicatorVisible(true);
		sendComponentMiddle(cardNumberField);

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

		sendFriendlyButtonMiddle("Save", click -> {
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

			
		});
		sendComponentUpper(new Tip("Your card data will be stored with Stripe"));
	}

	private UUID attemptToGetUniqueId(TextField field) {
		String value = field.getValue();
		if (StringUtils.isEmpty(value)) {
			sendError(field, "You must enter a username");
			return null;
		}

		value = value.trim();
		if (value.isEmpty()) {
			sendError(field, "You must enter a username other than whitespace");
			return null;
		}

		UUID user = mojang.getUniqueIdFromName(value);
		if (user == null) {
			sendError(field, "You must enter a valid username");
			return null;
		}
		return user;
	}

}
