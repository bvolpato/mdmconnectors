package com.totvslabs.mdm.client.util;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.totvslabs.mdm.client.pojo.JDBCConnectionParameter;
import com.totvslabs.mdm.client.pojo.JDBCFieldVO;
import com.totvslabs.mdm.client.pojo.JDBCTableVO;
import com.totvslabs.mdm.client.ui.JDBCEntities;
import com.totvslabs.mdm.client.ui.events.LogManagerDispatcher;
import com.totvslabs.mdm.client.ui.events.SendDataFluigDataDoneDispatcher;
import com.totvslabs.mdm.client.ui.events.SendDataFluigDataDoneEvent;
import com.totvslabs.mdm.client.ui.events.SendDataFluigDataUpdateProcessDispatcher;
import com.totvslabs.mdm.client.ui.events.SendDataFluigDataUpdateProcessEvent;
import com.totvslabs.mdm.restclient.MDMRestConnection;
import com.totvslabs.mdm.restclient.MDMRestConnectionFactory;
import com.totvslabs.mdm.restclient.command.CommandPostSchema;
import com.totvslabs.mdm.restclient.command.CommandPostStaging;
import com.totvslabs.mdm.restclient.command.CommandPostStagingC;

public class ThreadExportData implements Runnable {
	private JDBCEntities panelJDBCEntities;
	private JDBCTableVO tableVO;
	private JDBCConnectionParameter param;
	private String mdmServerURL;
	private String tenantId;
	private String datasourceId;
	private boolean justExportJSonData = Boolean.FALSE;

	public ThreadExportData(JDBCTableVO tableVO, JDBCConnectionParameter param, JDBCEntities panelJDBCEntities) {
		this(null, null, null, tableVO, param, panelJDBCEntities);
		this.justExportJSonData = Boolean.TRUE;
	}

	public ThreadExportData(String mdmServerURL, String tenantId, String datasourceId, JDBCTableVO tableVO, JDBCConnectionParameter param, JDBCEntities panelJDBCEntities) {
		this.panelJDBCEntities = panelJDBCEntities;
		this.param = param;
		this.mdmServerURL = mdmServerURL;
		this.tenantId = tenantId;
		this.datasourceId = datasourceId;
		this.tableVO = tableVO;
	}

	@Override
	public void run() {
		DateFormat df = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");

		LogManagerDispatcher.getInstance().register("Starting the process now: " + df.format(Calendar.getInstance().getTime()));
		StringBuffer totalDataJSon = new StringBuffer();
		Integer batchSize = 100;
		Integer totalRecords = tableVO.getTotalRecords();

		try {
			batchSize = Integer.parseInt(panelJDBCEntities.getTextBatchSize().getText());
		}
		catch(NumberFormatException e) {
		}

		MDMRestConnection connection = MDMRestConnectionFactory.getConnection(mdmServerURL);

		Long totalTimeSchema = System.currentTimeMillis();
		JsonObject schema = new JsonObject();
		JsonObject schemas = new JsonObject();
		schema.add("properties", schemas);

		List<JDBCFieldVO> fields = this.tableVO.getFields();

		for (JDBCFieldVO jdbcFieldVO : fields) {
			JsonObject fieldDetail = new JsonObject();

			String type = jdbcFieldVO.getType();

			if(type.equals(String.class.getCanonicalName())) {
				type = "string";
			}
			else if(type.equals(Boolean.class.getCanonicalName())) {
				type = "boolean";
			}
			else if(type.equals(Short.class.getCanonicalName())) {
				type = "short";
			}
			else if(type.equals(Integer.class.getCanonicalName())) {
				type = "integer";
			}
			else if(type.equals(Float.class.getCanonicalName())) {
				type = "float";
			}
			else if(type.equals(Double.class.getCanonicalName())) {
				type = "double";
			}
			else if(type.equals(Byte.class.getCanonicalName())) {
				type = "byte";
			}
			else if(type.equals(java.sql.Date.class.getCanonicalName())) {
				type = "date";
			}
			else if(type.equals(BigDecimal.class.getCanonicalName())) {
				type = "double";
			}
			else if(type.equals(Long.class.getCanonicalName())) {
				type = "integer";
			}

			fieldDetail.addProperty("type", type);

			schemas.add(jdbcFieldVO.getName(), fieldDetail);
		}

		if(!this.justExportJSonData) {
			CommandPostSchema schemaCommand = new CommandPostSchema(tenantId, datasourceId, panelJDBCEntities.getTextTemplateName().getText(), schema.toString());
			
			try {
				connection.executeCommand(schemaCommand);
				/* See the result, what happen??? */
				LogManagerDispatcher.getInstance().register("Sent the schema for the type '" + panelJDBCEntities.getTextTemplateName().getText() + "' in " + (totalTimeSchema - System.currentTimeMillis()) + "ms.");
			}
			catch(Exception e) {
				System.err.println("Error sending schema: " + e.getMessage());
				e.printStackTrace();
			}
			
			LogManagerDispatcher.getInstance().register("I am going to send " + totalRecords + " records...");
		}

		for(int totalDataSend=0; totalDataSend<totalRecords;) {
			JsonArray lote = JDBCConnectionFactory.loadData(param, tableVO, totalDataSend, batchSize);

			if(this.justExportJSonData) {
				totalDataJSon.append(lote.toString() + "\n");
				totalDataSend += lote.size();
			}
			else {
				CommandPostStaging staging = null;
				
				if(panelJDBCEntities.getCheckBoxCompress().isSelected()) {
					staging = new CommandPostStagingC(tenantId, datasourceId, panelJDBCEntities.getTextTemplateName().getText(), lote);
				}
				else {
					staging = new CommandPostStaging(tenantId, datasourceId, panelJDBCEntities.getTextTemplateName().getText(), lote);
				}

				long initialTime = System.currentTimeMillis();
				long endTime = initialTime;
				
				String additionalInformation = "";
				
				if(staging instanceof CommandPostStagingC) {
					additionalInformation = " (compressed)";
				}

				try {
					connection.executeCommand(staging);
					endTime = System.currentTimeMillis();
					totalDataSend += lote.size();
				}
				catch(Exception e) {
					System.err.println("Error: " + e.getMessage());
					e.printStackTrace();
				}

				double n1 = totalDataSend;
				double n2 = totalRecords;
				double result = n1 / n2;
				DecimalFormat decF = new DecimalFormat("0.00");

				LogManagerDispatcher.getInstance().register("Sent " + lote.size() + additionalInformation + " records in " + (endTime - initialTime) + "ms, " + decF.format(result*100) + "% completed (" + totalDataSend + " in total).");
			}

			ProcessTypeEnum processType = ProcessTypeEnum.SEND_DATA;

			if(this.justExportJSonData) {
				processType = ProcessTypeEnum.EXPORT_DATA;
			}

			SendDataFluigDataUpdateProcessDispatcher.getInstance().fireSendDataFluigDataUpdateProcessEvent(new SendDataFluigDataUpdateProcessEvent(totalDataSend, totalRecords, processType));
		}

		LogManagerDispatcher.getInstance().register("Finished the process now: " + df.format(Calendar.getInstance().getTime()) + "\n\n");

		ProcessTypeEnum processTypeEnum = ProcessTypeEnum.SEND_DATA;

		if(justExportJSonData) {
			processTypeEnum = ProcessTypeEnum.EXPORT_DATA;
		}

		SendDataFluigDataDoneDispatcher.getInstance().fireSendDataFluigDataDoneEvent(new SendDataFluigDataDoneEvent(processTypeEnum, totalDataJSon.toString()));
	}
}
