package com.axonivy.connector.mailstore.enums;

import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

/**
 * Enum representing the method of moving emails in an IMAP mailbox.
 *
 * <p>
 * There are two supported methods:
 * <ul>
 *   <li>{@link #APPEND} - Use the {@code appendMessages} function to move emails.</li>
 *   <li>{@link #COPY} - Use the {@code copyMessages} function to move emails.</li>
 * </ul>
 * </p>
 */
public enum MailMovingMethod {
	APPEND, COPY;

	/**
	 * Parses the input string and returns the corresponding {@code MailMovingMethod}.
	 * The comparison is case-insensitive and trims any leading/trailing whitespace.
	 *
	 * @param name the name of the mail moving method to parse
	 * @return the matching {@code MailMovingMethod}, or {@code APPEND} if no match is found
	 */
	public static MailMovingMethod from(String name) {
		return Stream.of(MailMovingMethod.values())
				.filter(method -> StringUtils.equalsIgnoreCase(method.name(), StringUtils.trim(name))).findAny().orElse(APPEND);
	}
}
