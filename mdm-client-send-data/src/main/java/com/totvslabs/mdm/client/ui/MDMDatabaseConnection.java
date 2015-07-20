package com.totvslabs.mdm.client.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.totvslabs.mdm.client.pojoTSA.MasterConfigurationData;
import com.totvslabs.mdm.client.ui.events.ChangeTabDispatcher;
import com.totvslabs.mdm.client.ui.events.ChangeTabEvent;
import com.totvslabs.mdm.restclient.MDMRestAuthentication;
import com.totvslabs.mdm.restclient.MDMRestConnectionFactory;

public class MDMDatabaseConnection extends PanelAbstract {
	private static final long serialVersionUID = 1L;

	private JLabel labelMDMServerURL;
	private JTextField textMDMServerURL;

	private JLabel labelMDMTenantId;
	private JTextField textMDMTenantId;

	private JLabel labelMDMDatasourceId;
	private JTextField textMDMDatasourceId;

	private JLabel labelMDMUserName;
	private JTextField textMDMUserName;

	private JLabel labelMDMPassword;
	private JTextField textMDMPassword;

	private JButton buttonConnectDisconnect;

	public MDMDatabaseConnection(){
		super(1, 16, " MDM Database Parameters");

		this.labelMDMServerURL = new JLabel("Server URL: ");
		this.textMDMServerURL = new JTextField("https://totvslabs.fluigdata.com/mdm/", 20);
		this.labelMDMTenantId = new JLabel("Subdomain: ");
		this.textMDMTenantId = new JTextField("totvslabs");
		this.labelMDMDatasourceId = new JLabel("Datasource ID: ");
		this.textMDMDatasourceId = new JTextField("0a0829172fc2433c9aa26460c31b78f0");
		this.labelMDMUserName = new JLabel("User: ");
		this.textMDMUserName = new JTextField("admin@totvslabs.com", 20);
		this.labelMDMPassword = new JLabel("Password: ");
		this.textMDMPassword = new JPasswordField("Foobar1!", 20);
		this.buttonConnectDisconnect = new JButton("Connect!");

		this.initializeLayout();
	}

	public void initializeLayout() {
		this.add(this.labelMDMServerURL);
		this.add(this.textMDMServerURL);
		this.add(this.labelMDMTenantId);
		this.add(this.textMDMTenantId);
		this.add(this.labelMDMDatasourceId);
		this.add(this.textMDMDatasourceId);
		this.add(this.labelMDMUserName);
		this.add(this.textMDMUserName);
		this.add(this.labelMDMPassword);
		this.add(this.textMDMPassword);

		this.add(new JLabel());
		this.add(this.buttonConnectDisconnect);

		this.buttonConnectDisconnect.addActionListener(new ConnectClick());
	}

	class ConnectClick implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			MDMRestAuthentication.getInstance(textMDMServerURL.getText(), textMDMTenantId.getText(), textMDMDatasourceId.getText(), textMDMUserName.getText(), textMDMPassword.getText());
			MDMRestConnectionFactory.getConnection(textMDMServerURL.getText());

			ChangeTabDispatcher.getInstance().fireChangeTabEvent(new ChangeTabEvent(1));
		}
	}

	@Override
	public void fillComponents(MasterConfigurationData masterConfigurationData) {
		if(masterConfigurationData != null) {
		}
	}

	@Override
	public void fillData(MasterConfigurationData masterConfigurationData) {
		if(masterConfigurationData != null) {
			masterConfigurationData.setTechnicalInformationTSAServer(this.textMDMServerURL.getText());
			masterConfigurationData.setTechnicalInformationTSAUserName(this.textMDMUserName.getText());
			masterConfigurationData.setTechnicalInformationTSAPassword(this.textMDMPassword.getText());
		}
	}

	public JTextField getTextMDMServerURL() {
		return textMDMServerURL;
	}

	public JTextField getTextMDMUserName() {
		return textMDMUserName;
	}

	public JTextField getTextMDMPassword() {
		return textMDMPassword;
	}
}
