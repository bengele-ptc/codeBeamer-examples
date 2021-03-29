package com.intland.codebeamer.examples;

import org.springframework.stereotype.Component;

import com.intland.codebeamer.event.BaseEvent;
import com.intland.codebeamer.event.TrackerItemListener;
import com.intland.codebeamer.event.util.VetoException;
import com.intland.codebeamer.manager.util.ActionData;
import com.intland.codebeamer.persistence.dto.TrackerItemDto;
import com.intland.codebeamer.persistence.dto.TrackerTypeDto;

@Component
public class ExampleListener implements TrackerItemListener {

	private static final int APPROVED_STATUS = 7;

	@SuppressWarnings("rawtypes")
	@Override
	public void trackerItemUpdated(BaseEvent<TrackerItemDto, TrackerItemDto, ActionData> event) throws VetoException {
		processEvent(event);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void trackerItemCreated(BaseEvent<TrackerItemDto, TrackerItemDto, ActionData> event) throws VetoException {
		processEvent(event);
	}

	@SuppressWarnings("rawtypes")
	private void processEvent(BaseEvent<TrackerItemDto, TrackerItemDto, ActionData> event) {
		TrackerItemDto item = event.getSource();
		boolean isRootTestRun = item.getTracker().isA(TrackerTypeDto.TESTRUN) && item.getParent() == null;
		if(isRootTestRun && item.getStatus().getId() == APPROVED_STATUS) {
			item.setDescription(item.getDescription() + " Added by Listener to Approved Test Run");
		}
	}

	
}
