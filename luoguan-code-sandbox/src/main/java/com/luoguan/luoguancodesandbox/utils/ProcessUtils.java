package com.luoguan.luoguancodesandbox.utils;

import com.luoguan.luoguancodesandbox.model.ExcuteMessage;
import org.springframework.util.StopWatch;

import java.io.BufferedReader;
import java.io.InputStreamReader;

// 进程工具类，控制台程序
public class ProcessUtils {
	public static ExcuteMessage runProcessAndGetMessage(Process runProcess, String opName)

	{
		ExcuteMessage executeMessage = new ExcuteMessage();

		try {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			int exitValue = runProcess.waitFor();
			executeMessage.setExitValue(exitValue);
			if (exitValue == 0) {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
				StringBuilder compileOutputStringBuilder = new StringBuilder();
				String compileOutputLine;
				while ((compileOutputLine = bufferedReader.readLine()) != null) {
					compileOutputStringBuilder.append(compileOutputLine);
				}
				executeMessage.setMessage(opName + compileOutputStringBuilder.toString());
			} else {
				// 异常退出
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
				StringBuilder errorOutputStringBuilder = new StringBuilder();
				String errorOutputLine;
				while ((errorOutputLine = bufferedReader.readLine()) != null) {
					errorOutputStringBuilder.append(errorOutputLine);
					;
				}
				executeMessage.setErrorMessage(opName + errorOutputStringBuilder.toString());
			}
			stopWatch.stop();
			executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return executeMessage;
	}
}
