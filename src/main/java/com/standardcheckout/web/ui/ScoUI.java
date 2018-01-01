package com.standardcheckout.web.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;

import org.springframework.util.StringUtils;

import com.standardcheckout.web.vaadin.addons.EnterShortcut;
import com.vaadin.annotations.Theme;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.UserError;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

@Theme("standardcheckout")
public abstract class ScoUI extends UI {

	private static final int GRID_SIZE = 3;

	protected GridLayout root;
	protected VerticalLayout center;

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

	protected void requestPassword(String title, boolean needsConfirmation, Function<PasswordField, Runnable> listener) {
		requestPassword(title, null, needsConfirmation, listener);
	}

	protected void requestPassword(String title, String hint, boolean needsConfirmation, Function<PasswordField, Runnable> listener) {
		PasswordField field = new PasswordField(title);
		sendComponentMiddle(field);
		if (!StringUtils.isEmpty(hint)) {
			Label hintLabel = new Label(hint);
			hintLabel.setHeight("50%");
			sendComponentUpper(hintLabel);
		}
		PasswordField confirmation = needsConfirmation ? new PasswordField("Confirm password") : null;
		if (needsConfirmation) {
			confirmation.setVisible(false);
			sendComponentMiddle(confirmation);
			field.setValueChangeMode(ValueChangeMode.EAGER);
			field.addValueChangeListener(change -> confirmation.setVisible(true));
		}
		Button continueButton = sendContinueButton(click -> {
			if (needsConfirmation && confirmation.isVisible()) {
				if (!Objects.equals(field.getValue(), confirmation.getValue())) {
					sendError(confirmation, "Password and confirmation must match");
					click.getButton().setEnabled(true);
					return;
				}
			}
			Runnable next = listener.apply(field);
			if (next != null) {
				clearCenter();
				next.run();
			} else {
				click.getButton().setEnabled(true);
			}
		});
		if (needsConfirmation) {
			confirmation.addShortcutListener(new EnterShortcut("Continue", continueButton::click));
		}
		field.addShortcutListener(new EnterShortcut("Continue", () -> {
			if (needsConfirmation) {
				if (!confirmation.isVisible()) {
					confirmation.setVisible(true);
				}
				confirmation.focus();
			} else {
				continueButton.click();
			}
		}));
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

		CheckBox termsOfServiceField = new CheckBox("I agree to the Terms and Conditions");
		termsOfServiceField.addValueChangeListener(change -> {
			if (!Boolean.TRUE.equals(change.getOldValue())) {
				addWindow(termsOfService);
				termsOfService.focus();
			}
		});

		sendComponentMiddle(termsOfServiceField);
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
