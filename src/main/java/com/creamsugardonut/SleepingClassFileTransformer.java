package com.creamsugardonut;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Created by lks21c on 16. 1. 14.
 */
public class SleepingClassFileTransformer implements ClassFileTransformer {

	public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		byte[] byteCode = classfileBuffer;
		if (className.equals("org/springframework/web/servlet/DispatcherServlet")) {
			try {
				ClassPool cp = ClassPool.getDefault();
				cp.importPackage("java.util.Enumeration");
				CtClass cc = cp.get("org.springframework.web.servlet.DispatcherServlet");
				CtMethod m = cc.getDeclaredMethod("doService");

                System.out.println("transform");

				StringBuilder sb = new StringBuilder();
				sb.append(
						"System.out.println(\"Agent URI: \" + request.getRequestURL() + \"?\"+ request.getQueryString());");
				sb.append("Enumeration headerValues = request.getHeaderNames();");
				sb.append("while (headerValues.hasMoreElements()) {");
				sb.append("String headerName = (String) headerValues.nextElement();");
				sb.append(
						"System.out.println(\"Agent Header: \" + headerName + \" = :\" + request.getHeader(headerName));");
				sb.append("}");

				m.insertAfter(sb.toString());

				byteCode = cc.toBytecode();
				cc.detach();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return byteCode;
	}
}