package com.standardcheckout.web.ui;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.servlet.http.Cookie;

import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import com.standardcheckout.web.helper.StringHelper;
import com.standardcheckout.web.security.PasswordProtected;
import com.standardcheckout.web.vaadin.addons.EnterShortcut;
import com.standardcheckout.web.vaadin.addons.PasswordRequest;
import com.vaadin.annotations.Theme;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.UserError;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.Position;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.wcs.wcslib.vaadin.widget.recaptcha.ReCaptcha;
import com.wcs.wcslib.vaadin.widget.recaptcha.shared.ReCaptchaOptions;

@Theme("standardcheckout")
public abstract class ScoUI extends UI {

	private static final int GRID_SIZE = 3;

	protected GridLayout root;
	protected VerticalLayout center;

	@Value("${RECAPTCHA_PUBLIC}")
	private String recaptchaPublic;

	@Value("$RECAPTCHA_PRIVATE")
	private String recaptchaPrivate;

	@Inject
	private PasswordEncoder encoder;

	@Override
	protected void init(VaadinRequest request) {
		createRoot();
		createGrid();
		center = getGridSection(1, 1);
	}

	private void createRoot() {
		root = new GridLayout(GRID_SIZE, GRID_SIZE);
		root.setSizeFull();
		setContent(root);
	}

	private void createGrid() {
		for (int x = 0; x < GRID_SIZE; x++) {
			for (int z = 0; z < GRID_SIZE; z++) {
				createGridEntry(x, z);
			}
		}
	}

	private void createGridEntry(int x, int z) {
		VerticalLayout buttonLayout = new VerticalLayout();
		root.addComponent(buttonLayout, x, z);
		root.setComponentAlignment(buttonLayout, Alignment.MIDDLE_CENTER);
	}

	protected void clearCenter() {
		center.removeAllComponents();
	}

	protected VerticalLayout getGridSection(int x, int z) {
		return (VerticalLayout) root.getComponent(x, z);
	}

	protected void requestInput(String parameter, String title, int maxLength, Function<TextField, Runnable> listener) {
		requestInput(parameter, title, null, maxLength, listener);
	}

	protected void requestInput(String parameter, String title, String placeholder, int maxLength, Function<TextField, Runnable> listener) {
		TextField field = new TextField(title);
		field.setMaxLength(maxLength);

		String existingValue = getOption(parameter);
		if (!StringUtils.isEmpty(existingValue)) {
			field.setValue(existingValue);
			Runnable next = listener.apply(field);
			if (next != null) {
				next.run();
				return;
			}
			field.setValue(null);
		}

		if (!StringUtils.isEmpty(placeholder)) {
			field.setPlaceholder(placeholder);
		}
		sendComponentMiddle(field);
		Button button = sendContinueButton(click -> {
			Runnable next = listener.apply(field);
			if (next != null) {
				clearCenter();
				next.run();
			} else {
				click.getButton().setEnabled(true);
			}
		});
		field.addShortcutListener(new EnterShortcut("Continue", button::click));
	}

	protected void requestPassword(PasswordRequest request) { // TODO warn if capslock is enabled
		HorizontalLayout passwordFields = new HorizontalLayout();
		passwordFields.setSpacing(false);

		MutableObject<TextField> passwordField = new MutableObject<>(new PasswordField(request.getTitle()));
		passwordField.getValue().setWidth("100%");
		passwordField.getValue().setMaxLength(128);

		Button showButton = new Button("SHOW");
		showButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);

		MutableObject<Button> continueButton = new MutableObject<>();
		showButton.addClickListener(click -> {
			TextField newPasswordField;
			boolean shouldShow = click.getButton().getCaption().equals("HIDE");
			if (shouldShow) {
				click.getButton().setCaption("SHOW");
				newPasswordField = new PasswordField(request.getTitle());
			} else {
				click.getButton().setCaption("HIDE");
				newPasswordField = new TextField(request.getTitle());
			}
			newPasswordField.setValue(passwordField.getValue().getValue());
			newPasswordField.setWidth("100%");
			newPasswordField.setMaxLength(128);
			passwordFields.replaceComponent(passwordField.getValue(), newPasswordField);
			passwordField.setValue(newPasswordField);
			newPasswordField.addShortcutListener(new EnterShortcut("Continue", continueButton.getValue()::click));
			//passwordFields.setExpandRatio(newPasswordField, 1000F);
		});

		passwordFields.addComponents(passwordField.getValue(), showButton);
		passwordFields.setComponentAlignment(showButton, Alignment.BOTTOM_CENTER);

		passwordFields.setExpandRatio(passwordField.getValue(), 1000F);
		passwordFields.setExpandRatio(showButton, 1F);

		sendComponentMiddle(passwordFields);

		if (request.getHint() != null) {
			Label hintLabel = new Label(request.getHint());
			hintLabel.addStyleName(ValoTheme.LABEL_SMALL);
			hintLabel.addStyleName(ValoTheme.LABEL_LIGHT);
			sendComponentUpper(hintLabel);
		}

		MutableObject<ReCaptcha> captcha = new MutableObject<>();

		continueButton.setValue(sendContinueButton(click -> {
			click.getButton().setEnabled(false);
			try {
				String password = passwordField.getValue().getValue();
				String error = getPasswordError(password);
				if (error != null) {
					sendError(passwordField.getValue(), error);
					return;
				}

				ReCaptcha recaptcha = captcha.getValue();
				if (recaptcha != null && !recaptcha.validate()) {
					sendError("You must fill in the captcha");
					recaptcha.reload();
					return;
				}

				PasswordProtected account = request.getAccount().get();

				int attempts = account.getFailedAttempts() == null ? 0 : account.getFailedAttempts();
				if (attempts >= 3 && recaptcha == null) {
					recaptcha = new ReCaptcha(recaptchaPrivate, new ReCaptchaOptions() {{
						sitekey = recaptchaPublic;
						theme = "light";
					}});
					captcha.setValue(recaptcha);
					center.addComponentAsFirst(recaptcha);
					center.setComponentAlignment(recaptcha, Alignment.MIDDLE_CENTER);
				}

				if (!StringUtils.isEmpty(account.getPassword())) {
					if (!encoder.matches(password, account.getPassword())) {
						account.setFailedAttempts(++attempts);
						sendError(passwordField.getValue(), "Your password is incorrect");
						request.getCallback().accept(account, false);
						return;
					}
				} else {
					account.setPassword(encoder.encode(password));
				}

				account.setLastLogin(Instant.now());
				account.setFailedAttempts(0);
				request.getCallback().accept(account, true);
			} finally {
				click.getButton().setEnabled(true);
			}
		}));

		passwordField.getValue().addShortcutListener(new EnterShortcut("Continue", continueButton.getValue()::click));

		if (request.getShowReset()) { // TODO allow webstores to reset their passwords by signing into their stripe account?
			Button resetPassword = new Button("Reset your password");
			resetPassword.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
			resetPassword.addStyleName(ValoTheme.BUTTON_SMALL);
			resetPassword.addClickListener(click -> {
				Window resetPasswordWindow = new Window("Password Reset");
				resetPasswordWindow.setModal(true);
				resetPasswordWindow.setResizable(false);
				resetPasswordWindow.center();
				VerticalLayout subContent = new VerticalLayout();
				resetPasswordWindow.setContent(subContent);

				Label explanationLabel1 = new Label("To reset your password, join the Minecraft server:");
				explanationLabel1.setWidth("380px");

				TextField ipField = new TextField();
				ipField.setEnabled(false);
				ipField.setValue("verify.standardcheckout.com");
				ipField.addStyleName("serverip");
				ipField.addStyleName(ValoTheme.TEXTFIELD_ALIGN_CENTER);
				ipField.setSizeFull();

				Label explanationLabel2 = new Label("You'll be given a code to reset your password");
				explanationLabel2.addStyleName(ValoTheme.LABEL_LIGHT);
				explanationLabel2.setWidth("380px");

				TextField resetCode = new TextField("Password reset code");
				resetCode.setPlaceholder("You get this from the Minecraft server");
				resetCode.setSizeFull();

				PasswordField newPassword = new PasswordField("New password");
				newPassword.setSizeFull();

				Button resetPasswordContinue = new Button("Continue");
				resetPasswordContinue.setSizeFull();
				resetPasswordContinue.addStyleName(ValoTheme.BUTTON_FRIENDLY);
				resetPasswordContinue.addClickListener(confirmClick -> { // TODO add captcha to reset
					PasswordProtected account = request.getAccount().get();
					if (account == null || account.getPasswordResetToken() == null) {
						sendError(ipField, "You must join the StandardCheckout server to reset your password");
						return;
					}

					if (!Objects.equals(account.getPasswordResetToken().getCode(), resetCode.getValue())) {
						sendError(resetCode, "You must enter a valid reset code");
						return;
					}

					Long timestamp = account.getPasswordResetToken().getTimestamp();
					if (timestamp != null && Instant.ofEpochMilli(timestamp).plus(10, ChronoUnit.MINUTES).isBefore(Instant.now())) {
						sendError(resetCode, "Your reset code is expired. It must be used within 10 minutes!");
						return;
					}

					String value = newPassword.getValue();
					if (!StringHelper.isInBounds(value, 8, 128)) {
						sendError(newPassword, "Passwords must be between 8 and 128 characters");
						return;
					}

					account.setPasswordResetToken(null);
					account.setPassword(encoder.encode(value));
					account.setLastLogin(Instant.now());

					resetPasswordWindow.close();
					clearCenter();

					request.getCallback().accept(account, true);
				});

				subContent.addComponents(explanationLabel1, ipField, explanationLabel2, resetCode, newPassword, resetPasswordContinue);

				addWindow(resetPasswordWindow);
				resetPasswordWindow.focus();
			});
			sendComponentUpper(resetPassword);
		}
	}

	private String getPasswordError(String password) {
		if (StringUtils.isEmpty(password)) {
			return "You must enter a password";
		}

		if (!StringHelper.isInBounds(password, 8, 128)) {
			return "Passwords must be between 8 and 128 characters";
		}

		return null;
	}

	protected Button sendContinueButton(Button.ClickListener listener) {
		return sendFriendlyButton("Continue", listener);
	}

	protected Button sendFriendlyButton(String name, Button.ClickListener listener) {
		Button button = new Button(name, listener);
		button.setDisableOnClick(true);
		button.setStyleName(ValoTheme.BUTTON_FRIENDLY);
		button.setHeight("50%");
		sendComponentUpper(button);
		return button;
	}

	protected Button sendFriendlyButtonMiddle(String name, Button.ClickListener listener) {
		Button button = new Button(name, listener);
		button.setDisableOnClick(true);
		button.addStyleName(ValoTheme.BUTTON_FRIENDLY);
		button.setHeight("50%");
		sendComponentMiddle(button);
		return button;
	}

	protected void sendComponentUpper(Component component) {
		component.setWidth("65%");
		center.addComponent(component);
		center.setComponentAlignment(component, Alignment.TOP_CENTER);
	}

	protected void sendComponentMiddle(Component component) {
		component.setWidth("65%");
		center.addComponent(component);
		center.setComponentAlignment(component, Alignment.MIDDLE_CENTER);
	}

	protected void sendComponentLower(Component component) {
		component.setWidth("65%");
		center.addComponent(component);
		center.setComponentAlignment(component, Alignment.BOTTOM_CENTER);
	}

	protected void sendError(AbstractComponent component, String error) {
		ErrorMessage message = new UserError(error);
		component.setComponentError(message);

		sendError(error);
	}

	protected void sendError(String error) {
		Notification notification = new Notification(error, Notification.Type.ERROR_MESSAGE);
		notification.setPosition(Position.TOP_CENTER);
		notification.setDelayMsec((int) TimeUnit.SECONDS.toMillis(5));
		notification.show(getPage());
	}

	protected String getOption(String name) {
		String option = getUrlParameter(name);
		if (StringUtils.isEmpty(option)) {
			option = getCookie(name);
			if (StringUtils.isEmpty(option)) {
				return null;
			}
		}
		return option;
	}

	protected String getUrlParameter(String name) {
		String query = getPage().getLocation().getQuery();
		if (query == null) {
			return null;
		}
		Map<String, String> parameters = new HashMap<>();
		// TODO optimize
		for (String element : query.split(Pattern.quote("&"))) {
			String[] parts = element.split(Pattern.quote("="));
			String key = parts[0];
			String value = parts[1];
			parameters.put(key, value);
		}
		return parameters.get(name);
	}

	protected String getCookie(String name) {
		name = "standardcheckout_" + name;
		Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
		if (cookies == null) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (Objects.equals(name, cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}

	protected void setCookie(String name, String value) {
		Cookie cookie = new Cookie("standardcheckout_" + name, value);
		cookie.setMaxAge((int) TimeUnit.MINUTES.toSeconds(15));
		cookie.setPath("/");
		VaadinService.getCurrentResponse().addCookie(cookie);
	}

	protected void deleteCookie(String name) {
		Cookie cookie = new Cookie("standardcheckout_" + name, null);
		cookie.setMaxAge(0);
		cookie.setPath("/");
		VaadinService.getCurrentResponse().addCookie(cookie);
	}

	protected CheckBox sendTermsOfService(String terms) {
		Window termsOfService = new Window("Terms of Service");
		termsOfService.setHeight("75%");
		termsOfService.setModal(true);
		termsOfService.setResizable(false);
		VerticalLayout subContent = new VerticalLayout();
		termsOfService.setContent(subContent);

		for (String part : terms.split(Pattern.quote("\n"))) {
			if (part.trim().isEmpty()) {
				continue;
			}
			Label termsLabel = new Label(part);
			termsLabel.setWidth("500px");
			subContent.addComponent(termsLabel);
		}

		termsOfService.center();
		termsOfService.addBlurListener(blur -> termsOfService.close());

		CssLayout termsOfServiceFields = new CssLayout();
		termsOfServiceFields.setStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP);
		CheckBox termsOfServiceField = new CheckBox("I agree to the ");
		Button button = new Button(" Terms of Service", click -> {
			addWindow(termsOfService);
			termsOfService.focus();
		});
		button.addStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
		button.addStyleName("termsbutton");
		termsOfServiceFields.addComponents(termsOfServiceField, button);

		sendComponentMiddle(termsOfServiceFields);
		return termsOfServiceField;
	}

	protected Notification sendSuccessNotice(String message) {
		Notification notification = new Notification(message, Notification.Type.ASSISTIVE_NOTIFICATION);
		notification.setPosition(Position.TOP_CENTER);
		notification.setDelayMsec((int) TimeUnit.SECONDS.toMillis(3));
		notification.show(getPage());
		return notification;
	}

}
