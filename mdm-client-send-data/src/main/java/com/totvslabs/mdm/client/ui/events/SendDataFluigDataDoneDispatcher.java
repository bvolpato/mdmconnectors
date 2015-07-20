package com.totvslabs.mdm.client.ui.events;

import java.util.ArrayList;
import java.util.List;

public class SendDataFluigDataDoneDispatcher {
	private static SendDataFluigDataDoneDispatcher instance;
	private List<SendDataFluigDataDoneListener> listeners = new ArrayList<SendDataFluigDataDoneListener>();

	private SendDataFluigDataDoneDispatcher() {
	}

	public void addSendDataFluigDataDoneListener(SendDataFluigDataDoneListener listener) {
		this.listeners.add(listener);
	}

	public void removeSendDataFluigDataDoneListener(SendDataFluigDataDoneListener listener) {
		this.listeners.remove(listener);
	}

	public void fireSendDataFluigDataDoneEvent(SendDataFluigDataDoneEvent event) {
		for (SendDataFluigDataDoneListener sendDataFluigDataDoneListener : listeners) {
			sendDataFluigDataDoneListener.onSendDataFluigDataDone(event);
		}
	}

	public static SendDataFluigDataDoneDispatcher getInstance() {
		if(instance == null) {
			instance = new SendDataFluigDataDoneDispatcher();
		}

		return instance;
	}
}
