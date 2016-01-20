package com.creamsugardonut;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Enumeration;

import javassist.*;

/**
 * Created by lks21c on 16. 1. 14.
 */
public class SpringDispatcherServletTransformer implements ClassFileTransformer {

	private static final String TAG_NAME = "SpringDispatcherServletTransformer";

	public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		byte[] byteCode = classfileBuffer;

		String springWebMvcPath = System.getProperty("springWebMvcPath");

		if (className.equals("org/springframework/web/servlet/DispatcherServlet")) {
			try {
				ClassPool cp = ClassPool.getDefault();

				cp.importPackage("java.util");
				cp.importPackage("java.net");
				cp.importPackage("java.io");

				URL url = loader.getResource("");
				String springWebappPath = url.getPath();

				if (springWebappPath.contains("WEB-INF/classes/")) {
					springWebappPath = springWebappPath.replace("WEB-INF/classes/", "WEB-INF/lib/*");
					System.out.println(TAG_NAME + ": " + springWebappPath + "added");
					cp.insertClassPath(springWebappPath);
				} else {
					if (springWebMvcPath != null) {
						cp.insertClassPath(springWebMvcPath);
					} else {
						throw new IllegalArgumentException("Please add springWebMvcPath as JAVA argument.");
					}
				}

				CtClass cc = cp.get("org.springframework.web.servlet.DispatcherServlet");
				CtMethod m = cc.getDeclaredMethod("doService");

				m.insertAfter(getHttpInfoCode() + AgentToCollectorSender.getCollectorCode());

				byteCode = cc.toBytecode();
				cc.detach();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if ("javax/servlet/http/HttpServletRequest".equals(className)) {
			URL url = loader.getResource("");
			ClassPool cp = ClassPool.getDefault();
			try {
				String tomcatLibPath = url.getPath() + "*";
				System.out.println(TAG_NAME + ": " + tomcatLibPath + "added");
				cp.insertClassPath(tomcatLibPath);
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
		}
		return byteCode;
	}

	public String getHttpInfoCode() {
		StringBuilder sb = new StringBuilder();
		sb.append("String ip = request.getRemoteAddr();");
		sb.append("String method = request.getMethod();");
		sb.append("String url = request.getRequestURL().toString();");
		sb.append("String param = \"\";");
		sb.append("if (request.getQueryString() != null) {");
		sb.append("param = request.getQueryString().toString();");
		sb.append("}");
		sb.append("Enumeration headerValues = request.getHeaderNames();");
		sb.append("StringBuilder sb = new StringBuilder();");
		sb.append("sb.append(\"'header' : [\");");
		sb.append("boolean loopEntered = false;");
		sb.append("while (headerValues.hasMoreElements()) {");
		sb.append("if (loopEntered) {");
		sb.append("sb.append(\",\");");
		sb.append("}");
		sb.append("String headerName = (String) headerValues.nextElement();");
		sb.append("sb.append(\"{'\" + headerName + \"': '\" + request.getHeader(headerName) + \"'}\");");
		sb.append("loopEntered = true;");
		sb.append("}");
		sb.append("sb.append(\"]\");");
		sb.append("int statusCode = response.getStatus();");
		sb.append("System.out.println(ip);");
		return sb.toString();
	}
}