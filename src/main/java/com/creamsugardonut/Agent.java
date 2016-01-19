package com.creamsugardonut;

import java.lang.instrument.Instrumentation;

public class Agent {

	public static void premain(String agentArgs, Instrumentation inst) {
		inst.addTransformer(new SpringDispatcherServletTransformer());
	}
}