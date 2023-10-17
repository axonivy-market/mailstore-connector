package com.axonivy.connector.mailstore.demo;

import java.util.Comparator;

import javax.mail.Message;

import ch.ivyteam.ivy.environment.Ivy;

public class MessageComparator implements Comparator<Message> {
	@Override
	public int compare(Message o1, Message o2) {

		try {
			return o1.getSentDate().compareTo(o2.getSentDate());
		} catch (Exception e) {
			Ivy.log().warn(e);
		}
		return 0;
	}
}
