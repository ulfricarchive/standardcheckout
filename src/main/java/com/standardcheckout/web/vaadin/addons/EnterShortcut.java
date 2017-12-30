package com.standardcheckout.web.vaadin.addons;

import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;

import com.vaadin.event.ShortcutListener;

public class EnterShortcut extends ShortcutListener {

	private final Runnable action;

	public EnterShortcut(String caption, Runnable action) {
		super(caption, KeyCode.ENTER, ArrayUtils.EMPTY_INT_ARRAY);
		Objects.requireNonNull(action, "action");
		this.action = action;
	}

	@Override
	public void handleAction(Object sender, Object target) {
		action.run();
	}

}
