package com.creamsugardonut;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * Created by lks21c on 16. 1. 14.
 */
public class SpringDispatcherServletTransformer implements ClassFileTransformer {

	public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        //System.out.println("transformer stated.");
        byte[] byteCode = classfileBuffer;

        //System.out.println("class name = " + className);

		if (className.equals("org/springframework/web/servlet/DispatcherServlet")) {
            System.out.println("DispatcherServlet entered");
			try {
				ClassPool cp = ClassPool.getDefault();
				cp.importPackage("java.util");
				cp.importPackage("java.net");
				cp.importPackage("java.io");
				CtClass cc = cp.get("org.springframework.web.servlet.DispatcherServlet");
				CtMethod m = cc.getDeclaredMethod("doService");

                m.insertAfter(getHttpInfoCode() + AgentToCollectorSender.getCollectorCode());

				byteCode = cc.toBytecode();
				cc.detach();
			} catch (Exception ex) {
				ex.printStackTrace();
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