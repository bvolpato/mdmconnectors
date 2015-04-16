package com.totvslabs.mdm.restclient;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.JerseyClientBuilder;

import com.google.gson.Gson;
import com.totvslabs.mdm.restclient.command.CommandPostStagingC;
import com.totvslabs.mdm.restclient.command.ICommand;
import com.totvslabs.mdm.restclient.vo.EnvelopeVO;

public class MDMRestConnection {
	private Client client;
	private String mdmURL;

	public MDMRestConnection(String mdmURL) {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws CertificateException {
			}

			public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1) throws CertificateException {
			}
		} };

		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

		SSLContext sc = null;

		try {
			sc = SSLContext.getInstance("SSL");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		try {
			sc.init(null, trustAllCerts, new SecureRandom());
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}

		this.client = new JerseyClientBuilder().sslContext(sc).hostnameVerifier(hv).build(); 

		this.mdmURL = mdmURL;
	}

	public EnvelopeVO executeCommand(ICommand command) {
		if(command instanceof CommandPostStagingC) {
			this.client.register(GZipReaderInterceptor.class);
			this.client.register(GZipWriterInterceptor.class);
		}

		Gson gson = new Gson();

		Map<String, String> parametersHeader = command.getParametersHeader();
		Map<String, String> parameterPath = command.getParameterPath();

		Set<String> keySetPath = parameterPath != null ? parameterPath.keySet() : null;
		Set<String> keySetHeader = parametersHeader != null ? parametersHeader.keySet() : null;

		WebTarget webResource = this.client.target(mdmURL + command.getCommandURL());

		if(keySetPath != null) {
			for (String string : keySetPath) {
				webResource = webResource.queryParam(string, parameterPath.get(string));
			}
		}

		Builder request = webResource.request(MediaType.APPLICATION_JSON);

		if(keySetHeader != null) {
			for (String string : keySetHeader) {
				request = request.header(string, parametersHeader.get(string));
			}
		}

		if(command instanceof CommandPostStagingC) {
			request = request.header("HttpHeaders.ACCEPT_ENCODING", "gzip");
		}

		Response response = null;

		switch(command.getType()) {
			case GET:
				response = request.accept(MediaType.APPLICATION_JSON).get();
				break;

			case POST:
				String type = MediaType.APPLICATION_JSON;
				String additionalInformation = "";

				if(command instanceof CommandPostStagingC) {
					request.header(HttpHeaders.ACCEPT_ENCODING, "gzip");
					additionalInformation = " - COMPRESS";
				}

				long initialTimeConvertJson = System.currentTimeMillis();
				String string = command.getData().toString();
				System.out.println("Time to convert the OBJECT to JSON: " + (System.currentTimeMillis() - initialTimeConvertJson) );
				long initialTime = System.currentTimeMillis();
				System.out.println("Time BEFORE send the data: " + System.currentTimeMillis());
				response = request.accept(type).post(Entity.entity(string, type));
				System.out.println("Time AFTER send the data: " + System.currentTimeMillis());
				System.out.println("Time to execute the service ('" + command.getCommandURL() + "'" + additionalInformation + "): " + (System.currentTimeMillis() - initialTime) );
				break;
		}

		if (response.getStatus() != 200) {
			System.out.println(command.getData().toString());
			throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		}

		EnvelopeVO envelopeVO = gson.fromJson(response.readEntity(String.class), EnvelopeVO.class);
//		Type mapType = new TypeToken<List<GenericVO>>() {}.getType();
//		List<GenericVO> genericVO = gson.fromJson(jsonObject.get("hits"), mapType);

		return envelopeVO;
	}
}
