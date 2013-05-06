package com.gigaspaces.analytics;

import org.openspaces.events.EventTemplateProvider;

import com.j_spaces.core.client.SQLQuery;

public class ProcessedEventFilter implements EventTemplateProvider {
	public SQLQuery<Event> getTemplate() {
		final boolean processed = true;
		return 	new SQLQuery<Event>(Event.class,"processed = ?", processed);
	}

}	
