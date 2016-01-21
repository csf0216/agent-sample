package com.creamsugardonut;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.security.ProtectionDomain;

import javassist.*;

/**
 * Created by lks21c on 16. 1. 14.
 */
public class SpringDispatcherServletTransformer implements ClassFileTransformer {

	private static final String TAG_NAME = "SpringDispatcherServletTransformer";

	public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		byte[] byteCode = classfileBuffer;

		// System.out.println(className);

		if (className.equals("org/springframework/web/servlet/DispatcherServlet")) {
			try {
				ClassPool cp = ClassPool.getDefault();

				addDefaultJavaPackages(cp);
				addSpringDependencyJars(loader, cp);

				CtClass cc = cp.get("org.springframework.web.servlet.DispatcherServlet");
				CtMethod m = cc.getDeclaredMethod("doService");
				m.insertAfter(getHttpInfoCode() + AgentToCollectorSender.getCollectorCode());

				m = cc.getDeclaredMethod("doDispatch");
				m.insertAfter("System.out.println(\"doDispatch\");");

				m = cc.getDeclaredMethod("processDispatchResult");
				m.insertAfter("System.out.println(\"processDispatchResult\");");

				m = cc.getDeclaredMethod("render");
				StringBuilder sb = new StringBuilder();
				sb.append("System.out.println(\"dispatchers render\");");
				m.insertBefore(sb.toString());

				byteCode = cc.toBytecode();
				cc.detach();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if ("javax/servlet/http/HttpServletRequest".equals(className)) {
			URL url = loader.getResource("");
			ClassPool cp = ClassPool.getDefault();
			try {
				String tomcatLibPath;
				if (url.getPath().contains(".jar!/")) {
					tomcatLibPath = url.getPath().replace("file:", "").replace(".jar!/", ".jar");
				} else {
					tomcatLibPath = url.getPath() + "*";
				}
				System.out.println(TAG_NAME + ": " + tomcatLibPath + " added");
				cp.insertClassPath(tomcatLibPath);
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
		} else if ("org/springframework/web/servlet/view/AbstractView".equals(className)) {
			try {
				System.out.println(TAG_NAME + " org/springframework/web/servlet/view/AbstractView");
				ClassPool cp = ClassPool.getDefault();

				addDefaultJavaPackages(cp);
				addSpringDependencyJars(loader, cp);

				CtClass cc = cp.get("org.springframework.web.servlet.view.AbstractView");
				CtMethod m = cc.getDeclaredMethod("render");
				StringBuilder sb = new StringBuilder();

				sb.append("System.out.println(\"abstract render\");");
				// sb.append("Set keys = model.keySet();");
				// sb.append("Iterator iterator = keys.iterator();");
				// sb.append("while (iterator.hasNext()) {");
				// sb.append("System.out.println(iterator.next().toString());");
				// sb.append("}");
				m.insertBefore(sb.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("org/springframework/web/servlet/view/json/AbstractJackson2View".equals(className)) {
			try {
				System.out.println(TAG_NAME + " org.springframework.web.servlet.view.json.AbstractJackson2View");
				ClassPool cp = ClassPool.getDefault();

				addDefaultJavaPackages(cp);
				addSpringDependencyJars(loader, cp);

				CtClass cc = cp.get("org.springframework.web.servlet.view.json.AbstractJackson2View");
				CtMethod m = cc.getDeclaredMethod("writeContent");
				StringBuilder sb = new StringBuilder();

				sb.append("System.out.println(\"AbstractJackson2View writeContent\");");
				// sb.append("Set keys = model.keySet();");
				// sb.append("Iterator iterator = keys.iterator();");
				// sb.append("while (iterator.hasNext()) {");
				// sb.append("System.out.println(iterator.next().toString());");
				// sb.append("}");
				m.insertBefore(sb.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return byteCode;
	}

	private void addDefaultJavaPackages(ClassPool cp) {
		cp.importPackage("java.util");
		cp.importPackage("java.net");
		cp.importPackage("java.io");
	}

	private void addSpringDependencyJars(ClassLoader loader, ClassPool cp) throws NotFoundException {
		URL url = loader.getResource("");
		String springWebappPath = url.getPath();
		if (springWebappPath.contains("WEB-INF/classes/")) {
			springWebappPath = springWebappPath.replace("WEB-INF/classes/", "WEB-INF/lib/*");
			System.out.println(TAG_NAME + ": " + springWebappPath + "added");
			cp.insertClassPath(springWebappPath);
		}
	}

	public String getHttpInfoCode() {
		StringBuilder sb = new StringBuilder();
		sb.append("System.out.println(\"DoService invoked.\");");
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