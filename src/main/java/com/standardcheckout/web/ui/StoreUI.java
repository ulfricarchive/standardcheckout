package com.standardcheckout.web.ui;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.vaadin.textfieldformatter.CreditCardFieldFormatter;

import com.standardcheckout.web.helper.EnvironmentHelper;
import com.standardcheckout.web.helper.StringHelper;
import com.standardcheckout.web.mojang.MojangService;
import com.standardcheckout.web.stripe.CardType;
import com.standardcheckout.web.stripe.Country;
import com.standardcheckout.web.stripe.StripeService;
import com.standardcheckout.web.vaadin.addons.CardTypeResource;
import com.standardcheckout.web.vaadin.addons.CountryFlagResource;
import com.standardcheckout.web.vaadin.addons.Tip;
import com.standardcheckout.web.webstore.CustomersService;
import com.standardcheckout.web.webstore.MinecraftCustomer;
import com.standardcheckout.web.webstore.Webstore;
import com.standardcheckout.web.webstore.WebstoreService;
import com.stripe.model.Customer;
import com.vaadin.annotations.Title;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@Title("Standard Checkout")
@SpringUI
public class StoreUI extends ScoUI {

	private Webstore webstore;

	@Inject
	private MojangService mojang;

	@Inject
	private StripeService stripe;

	@Inject
	private PasswordEncoder encoder;

	@Inject
	private WebstoreService webstores;

	@Inject
	private CustomersService customers;

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
				+ "The application is currently in an invite-only BETA. "
				+ "If you'd like to add SCO to your server's store, "
				+ "send us an email at billing@ulfric.com");
		label.setSizeFull();
		label.addStyleName(ValoTheme.LABEL_LARGE);
		center.addComponent(label);

		questionAndAnswer("Pricing", "3% or $0.10 from each transaction, whichever is higher, capped at 30% of the transaction. " +
				"Examples: $10 purchase = $0.30 fee.  $1 purchase = $0.10. $0.15 purchase = $0.045 (rounded up to $0.05) fee.");
		questionAndAnswer("How is card data stored?", "Card data is not stored on our servers. We use Stripe for tokenization.");
		questionAndAnswer("Do you compete with Buycraft?", "No. SCO is an extension to the Buycraft platform - it's a dependency.");
		questionAndAnswer("How does it work?", "Say you want to sell an item in-game, like a crate key. " +
				"Ideally the purchasing player would just click the key in the crate, and maybe a confirmation button. " +
				"Our mission is to make that happen. The first time a player makes a purchase through SCO, they'll be " +
				"directed to your webstore to add their card details. Then, back in-game, they can make all the purchases " +
				"they want using their linked card. If the player already added their card on another server, they'll " +
				"just need to authorize yours through your SCO webstore.");
	}

	private void questionAndAnswer(String question, String answer) {
		Label answerLabel = new Label(answer);
		answerLabel.setVisible(false);

		Button button = new Button(question);
		button.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);

		button.addClickListener(click -> answerLabel.setVisible(!answerLabel.isVisible()));

		sendComponentMiddle(button);
		sendComponentMiddle(answerLabel);

		answerLabel.setWidth("90%");
	}

	private void flowUsername() {
		requestInput("username", "Minecraft Username", "Notch", 16, usernameField -> {
			UUID user = attemptToGetUniqueId(usernameField);
			if (user == null) {
				return null;
			}

			return () -> flowPassword(user);
		});
	}

	private void flowPassword(UUID user) {
		MinecraftCustomer player = customers.getCustomerByMojangId(user);
		if (player == null || StringUtils.isEmpty(player.getPassword())) {
			requestPassword("Create a Standard Checkout Password",
					"Passwords must be at between 8 and 128 characters in length. This should NOT be the same as your Minecraft password!",
					true, passwordField -> {
						String value = passwordField.getValue();
						if (!StringHelper.isInBounds(value, 8, 128)) {
							sendError(passwordField, "Passwords must be between 8 and 128 characters");
							return null;
						}

						MinecraftCustomer createdPlayer = new MinecraftCustomer();
						createdPlayer.setMojangId(user);
						createdPlayer.setPassword(encoder.encode(value));
						createdPlayer.setBillingEnabled(true);
						createdPlayer.setReceiveEmails(true);
						customers.saveCustomer(createdPlayer);

						return () -> flowLoggedIn(createdPlayer);
					});

			return;
		}

		if (player.getMojangId() == null) {
			player.setMojangId(user);
			customers.saveCustomer(player);
		}

		requestPassword("Standard Checkout Password", false, passwordField -> {
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

		flowAuthorize(player);
		// TODO
	}

	private void flowEnterCreditCard(MinecraftCustomer player) {
		TextField nameField = new TextField("Full Name");
		nameField.setPlaceholder("John Smith");
		nameField.setId("ccname");
		nameField.setMaxLength(100);
		nameField.setRequiredIndicatorVisible(true);
		sendComponentMiddle(nameField);

		TextField emailField = new TextField("Email");
		emailField.setPlaceholder("hello@example.com");
		emailField.setId("email");
		emailField.setMaxLength(100);
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
		cardNumberField.setValueChangeMode(ValueChangeMode.EAGER);
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
		zipCodeField.setMaxLength(100);
		zipCodeField.setId("zipcode");
		zipCodeField.setWidth("100%");
		zipCodeField.setRequiredIndicatorVisible(true);

		locationFields.addComponents(countryField, zipCodeField);
		locationFields.setExpandRatio(countryField, 5);
		locationFields.setExpandRatio(zipCodeField, 3);

		sendComponentMiddle(locationFields);

		CheckBox termsOfServiceField = sendTermsOfService(EnvironmentHelper.readFileOnClassPath(resources, "static/terms.txt"));

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
			if (cardMonth.length() == 1) {
				cardMonth = "0" + cardMonth;
			}

			String cardYear = yearField.getValue();
			if (StringUtils.isEmpty(cardYear)) {
				sendError(yearField, "You must enter your card expiration year");
				return;
			}
			if (cardYear.length() != 2 && cardYear.length() != 4) {
				sendError(yearField, "You must enter a valid card expiration year");
				return;
			}

			String cvv = cvvField.getValue();
			if (StringUtils.isEmpty(cvv)) {
				sendError(cvvField, "You must enter your card security code");
				return;
			}

			Country country = countryField.getValue();
			if (country == null) {
				sendError(countryField, "You must enter your country");
				return;
			}

			String zip = zipCodeField.getValue();
			if (StringUtils.isEmpty(zip)) {
				sendError(zipCodeField, "You must enter your zip code");
				return;
			}

			click.getButton().setEnabled(false);

			Map<String, Object> customerData = new HashMap<>();

			customerData.put("description", "Mojang UUID " + player.getMojangId());

			Map<String, String> metadata = new HashMap<>();
			metadata.put("mojangUuid", player.getMojangId().toString());
			customerData.put("metadata", metadata);

			customerData.put("email", email);

			Map<String, Object> source = new HashMap<>();
			source.put("object", "card");
			source.put("number", cardNumber);
			source.put("exp_year", cardYear);
			source.put("exp_month", cardMonth);
			source.put("address_country", country);
			source.put("address_zip", zip);
			source.put("cvc", cvv);
			source.put("name", name);
			customerData.put("source", source);

			Customer customer = stripe.createCustomer(customerData);

			if (customer == null) {
				click.getButton().setEnabled(true);
				sendError(zipCodeField, "You must enter a valid debit or credit card");
				return;
			}

			player.setStripeId(customer.getId());
			customers.saveCustomer(player);
			flowAuthorize(player);
		});
		save.setDisableOnClick(false);
		sendComponentUpper(new Tip("Your card data will be stored with Stripe"));
	}

	private void flowAuthorize(MinecraftCustomer customer) {
		clearCenter();

		Set<UUID> authorizedWebstores = customer.getAuthorizedWebstores() == null ? new HashSet<>() : customer.getAuthorizedWebstores();

		if (authorizedWebstores.contains(webstore.getAuthorizationId())) {
			flowDashboard(customer);
			return;
		}

		String name = webstore.getFriendlyName();
		if (StringUtils.isEmpty(name)) {
			name = webstore.getStoreId();
		}

		Label explanation = new Label("Would you like to authorize in-game payments to " + name + "? You'll receive an email whenever you're charged.");
		explanation.addStyleName(ValoTheme.LABEL_LARGE);
		sendComponentMiddle(explanation);

		CheckBox terms = StringUtils.isEmpty(webstore.getTermsOfService()) ? null : sendTermsOfService(webstore.getTermsOfService());

		sendFriendlyButtonMiddle("Authorize", click -> {
			if (terms != null) {
				if (!Boolean.TRUE.equals(terms.getValue())) {
					sendError(terms, "You must agree to the Terms of Service");
					return;
				}
			}

			authorizedWebstores.add(webstore.getAuthorizationId());
			customer.setAuthorizedWebstores(authorizedWebstores);
			customers.saveCustomer(customer);
			flowDashboard(customer);
			Notification notification = sendSuccessNotice("Account created! You can head back to the game.");
			notification.setDelayMsec(-1);
		});
	}

	private void flowDashboard(MinecraftCustomer player) {
		clearCenter();

		Customer customer = stripe.getCustomer(player.getStripeId());
		if (customer == null) {
			Label error = new Label("There was an error loading your dashboard. Send an email to billing@ulfric.com if this persists.");
			sendComponentMiddle(error);
			return;
		}

		Label profile = new Label("Profile");
		profile.addStyleName(ValoTheme.LABEL_LARGE);
		sendComponentMiddle(profile);

		if (customer.getAccountBalance() != null) {
			BigDecimal balance = BigDecimal.valueOf(customer.getAccountBalance()).movePointLeft(2);
			if (BigDecimal.ZERO.compareTo(balance) != 0) {
				Label balanceLabel = new Label("Account Balance: " + DecimalFormat.getCurrencyInstance().format(balance));
				sendComponentMiddle(balanceLabel);
			}
		}

		TextField emailField = new TextField("Email");
		emailField.setMaxLength(100);
		emailField.setValue(customer.getEmail());
		sendComponentMiddle(emailField);

		CheckBox subscribedField = new CheckBox("Receive emails from StandardCheckout");
		subscribedField.setValue(player.getReceiveEmails());
		sendComponentMiddle(subscribedField);

		// TODO allow removing authorized servers
		// TODO card management

		CheckBox billingField = new CheckBox("Account active");
		billingField.setValue(player.getBillingEnabled());
		sendComponentMiddle(billingField);

		sendFriendlyButtonMiddle("Update", click -> {
			if (!Objects.equals(emailField.getValue(), customer.getEmail())) {
				if (StringUtils.isEmpty(emailField.getValue())) {
					sendError(emailField, "Your email must not be blank");
					return;
				}

				Map<String, Object> patch = new HashMap<>();
				patch.put("email", emailField.getValue());
				if (stripe.updateCustomer(customer, patch)) {
					sendError(emailField, "You must enter a valid email");
					return;
				}
			}

			player.setBillingEnabled(Boolean.TRUE.equals(billingField.getValue()));
			player.setReceiveEmails(Boolean.TRUE.equals(subscribedField.getValue()));
			customers.saveCustomer(player);

			sendSuccessNotice("Account updated");
		});
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
