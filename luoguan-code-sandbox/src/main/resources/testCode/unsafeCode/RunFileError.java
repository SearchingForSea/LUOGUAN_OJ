package com.luoguan.luoguancodesandbox.unsafe;

import java.io.*;


public class RunFileError {
	public static void main(String[] args) throws IOException, InterruptedException {
		String userDir = System.getProperty("user.dir");
		String filePath = userDir + File.separator + "src/main/resources/木马程序.bat";
		Process process = Runtime.getRuntime().exec(filePath);
		process.waitFor();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		// 逐行读取
		String compileOutputLine;
		while ((compileOutputLine = bufferedReader.readLine()) != null) {
			System.out.println(compileOutputLine);
		}
		System.out.println("执行完毕.");
	}
}