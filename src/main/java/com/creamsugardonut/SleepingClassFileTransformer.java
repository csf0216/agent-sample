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

    private String httpInfo;
    private String collectorCode;

    public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] byteCode = classfileBuffer;
        if (className.equals("org/springframework/web/servlet/DispatcherServlet")) {
            try {
                ClassPool cp = ClassPool.getDefault();
                cp.importPackage("java.util");
                cp.importPackage("java.net");
                cp.importPackage("java.io");
                CtClass cc = cp.get("org.springframework.web.servlet.DispatcherServlet");
                CtMethod m = cc.getDeclaredMethod("doService");

                m.insertAfter(getHttpInfoCode());

                String collectorUrl = System.getProperty("collectorUrl");
                if (collectorUrl != null) {
                    m.insertAfter(getCollectorCode(collectorUrl));
                }

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
        sb.append(
                "String url = \"Agent URI: \" + request.getRequestURL() + \"?\"+ request.getQueryString();");
        sb.append("Enumeration headerValues = request.getHeaderNames();");
        sb.append("while (headerValues.hasMoreElements()) {");
        sb.append("String headerName = (String) headerValues.nextElement();");
        sb.append(
                "System.out.println(\"Agent Header: \" + headerName + \" = :\" + request.getHeader(headerName));");
        sb.append("}");
        sb.append("int statusCode = response.getStatus();");
        return sb.toString();
    }

    public String getCollectorCode(String collectorUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("URL oracle = new URL(\"" + collectorUrl + "\");");
        sb.append("URLConnection yc = oracle.openConnection();");
        sb.append("BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));");
        sb.append("String inputLine;");
        sb.append("while ((inputLine = in.readLine()) != null) {");
        sb.append("System.out.println(inputLine); }");
        sb.append("in.close();");
        return sb.toString();
    }
}