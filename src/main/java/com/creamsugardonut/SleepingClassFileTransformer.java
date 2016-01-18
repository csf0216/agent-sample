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
		if (className.equals("com/creamsugardonut/Sleeping")) {
			try {
				ClassPool cp = ClassPool.getDefault();
				CtClass cc = cp.get("com.creamsugardonut.Sleeping");
				CtMethod m = cc.getDeclaredMethod("randomSleep");

				m.addLocalVariable("elapsedTime", CtClass.longType);
				m.insertBefore("elapsedTime = System.currentTimeMillis();");
				m.insertAfter("{elapsedTime = System.currentTimeMillis() - elapsedTime;"
						+ "System.out.println(\"메서드 실행 시간 in ms: \" + elapsedTime);}");
				byteCode = cc.toBytecode();
				cc.detach();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return byteCode;
	}
}