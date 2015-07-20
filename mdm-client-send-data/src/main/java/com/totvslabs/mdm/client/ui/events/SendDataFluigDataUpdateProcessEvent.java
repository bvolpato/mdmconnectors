package com.totvslabs.mdm.client.ui.events;

import com.totvslabs.mdm.client.util.ProcessTypeEnum;

public class SendDataFluigDataUpdateProcessEvent {
	private Integer recordsSent;
	private Integer totalRecords;
	private ProcessTypeEnum processType;

	public SendDataFluigDataUpdateProcessEvent(Integer recordsSent, Integer totalRecords, ProcessTypeEnum processType) {
		super();
		this.recordsSent = recordsSent;
		this.totalRecords = totalRecords;
		this.processType = processType;
	}

	public Integer getRecordsSent() {
		return recordsSent;
	}

	public void setRecordsSent(Integer recordsSent) {
		this.recordsSent = recordsSent;
	}

	public Integer getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(Integer totalRecords) {
		this.totalRecords = totalRecords;
	}

	public ProcessTypeEnum getProcessType() {
		return processType;
	}

	public void setProcessType(ProcessTypeEnum processType) {
		this.processType = processType;
	}
}
