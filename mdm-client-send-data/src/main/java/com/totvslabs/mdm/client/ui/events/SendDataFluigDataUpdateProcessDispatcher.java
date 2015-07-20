package com.totvslabs.mdm.client.ui.events;

import java.util.ArrayList;
import java.util.List;

public class SendDataFluigDataUpdateProcessDispatcher {
	private static SendDataFluigDataUpdateProcessDispatcher instance;
	private List<SendDataFluigDataUpdateProcessListener> listeners = new ArrayList<SendDataFluigDataUpdateProcessListener>();

	private SendDataFluigDataUpdateProcessDispatcher() {
	}

	public void addSendDataFluigDataUpdateProcessListener(SendDataFluigDataUpdateProcessListener listener) {
		this.listeners.add(listener);
	}

	public void removeSendDataFluigDataUpdateProcessListener(SendDataFluigDataUpdateProcessListener listener) {
		this.listeners.remove(listener);
	}

	public void fireSendDataFluigDataUpdateProcessEvent(SendDataFluigDataUpdateProcessEvent event) {
		for (SendDataFluigDataUpdateProcessListener sendDataFluigDataDoneListener : listeners) {
			sendDataFluigDataDoneListener.onSendDataFluigDataUpdateProcess(event);
		}
	}

	public static SendDataFluigDataUpdateProcessDispatcher getInstance() {
		if(instance == null) {
			instance = new SendDataFluigDataUpdateProcessDispatcher();
		}

		return instance;
	}
}
