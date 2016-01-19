package com.creamsugardonut;

import java.io.IOException;

import javassist.CannotCompileException;

/**
 * Created by lks21c on 16. 1. 19.
 */
public class AgentToCollectorSender {

	public static String getCollectorCode() throws IOException, CannotCompileException {
		String collectorUrl = System.getProperty("collectorUrl");
		String collectorProtocol = System.getProperty("collectorProtocol", "http");

		StringBuilder sb = new StringBuilder();
		if ("http".equals(collectorProtocol)) {
			sb.append("StringBuilder queryBuilder = new StringBuilder();");
			sb.append("queryBuilder.append(\"{\");");
			sb.append("queryBuilder.append(\"'ip':\" + \"'\" + ip + \"',\");");
			sb.append("queryBuilder.append(\"'method':\" + \"'\" + method + \"',\");");
			sb.append("queryBuilder.append(\"'url':\" + \"'\" + url + \"',\");");
			sb.append("queryBuilder.append(\"'param':\" + \"'\" + param + \"',\");");
			sb.append("queryBuilder.append(\"'statusCode':\" + \"'\" + statusCode + \"',\");");
			sb.append("queryBuilder.append(sb.toString());");
			sb.append("queryBuilder.append(\"}\");");
			sb.append("URL url = new URL(\"" + collectorUrl + "\" + \"?query=\" + URLEncoder.encode(queryBuilder.toString()));");
			sb.append("URLConnection yc = url.openConnection();");
			sb.append("BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));");
			sb.append("String inputLine;");
			sb.append("while ((inputLine = in.readLine()) != null) {");
			sb.append("}");
			sb.append("in.close();");
		} else if ("udp".equals(collectorProtocol)) {
			// BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			// DatagramSocket clientSocket = new DatagramSocket();
			// InetAddress IPAddress = InetAddress.getByName("localhost");
			// byte[] sendData = new byte[1024];
			// byte[] receiveData = new byte[1024];
			// String sentence = inFromUser.readLine();
			// sendData = sentence.getBytes();
			// DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
			// clientSocket.send(sendPacket);
			// DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			// clientSocket.receive(receivePacket);
			// String modifiedSentence = new String(receivePacket.getData());
			// System.out.println("FROM SERVER:" + modifiedSentence);
			// clientSocket.close();
		}
		return sb.toString();
	}
}
