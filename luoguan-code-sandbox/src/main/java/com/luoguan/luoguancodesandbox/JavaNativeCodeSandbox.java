package com.luoguan.luoguancodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.luoguan.luoguancodesandbox.model.ExcuteMessage;
import com.luoguan.luoguancodesandbox.model.ExecuteCodeRequest;
import com.luoguan.luoguancodesandbox.model.ExecuteCodeResponse;
import com.luoguan.luoguancodesandbox.model.JudgeInfo;
import com.luoguan.luoguancodesandbox.security.DefaultSecurityManager;
import com.luoguan.luoguancodesandbox.utils.ProcessUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class JavaNativeCodeSandbox implements CodeSandbox {
	private static final String GLOBAL_DIR_NAME = "tmpCode";
	private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

	private static final long TIME_OUT = 5000L;

	private static final List<String> blackList = Arrays.asList("Files", "exec");

	private static final WordTree WORD_TREE;

	static {
		// 初始化字典树
		WORD_TREE = new WordTree();
		WORD_TREE.addWords(blackList);
	}

	public static void main(String[] args) {
		JavaNativeCodeSandbox javaNativeCodeSandbox = new JavaNativeCodeSandbox();
		ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
//		String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
//		String code = ResourceUtil.readStr("testCode/unsafeCode/SleepError.java", StandardCharsets.UTF_8);
//		String code = ResourceUtil.readStr("testCode/unsafeCode/MemoryError.java", StandardCharsets.UTF_8);
		String code = ResourceUtil.readStr("testCode/unsafeCode/ReadFileError.java", StandardCharsets.UTF_8);
		executeCodeRequest.setCode(code);
		executeCodeRequest.setLanguage("java");
		executeCodeRequest.setInputList(Arrays.asList("1 2", "1 3"));
		ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
		System.out.println(executeCodeResponse);
	}

	public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {

		List<String> inputList = executeCodeRequest.getInputList();
		String code = executeCodeRequest.getCode();
		String language = executeCodeRequest.getLanguage();

		String userDir = System.getProperty("user.dir");
		String globelCodePathName = userDir + File.separator + GLOBAL_DIR_NAME;

		FoundWord foundWord = WORD_TREE.matchWord(code);
		if (foundWord != null) {
			System.out.println(foundWord.getFoundWord());
			return null;
		}

		// 判断全局代码目录下是否存在该文件。没有则创建
		if (!FileUtil.exist(globelCodePathName)) {
			FileUtil.mkdir(globelCodePathName);
		}

		// 用户代码隔离存放
		String userCodeParentPath = globelCodePathName + File.separator + UUID.randomUUID();
		String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
		File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

		// 编译代码
		String compiled = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
		try {
			Process compileProcess = Runtime.getRuntime().exec(compiled);
			ExcuteMessage excuteMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
			System.out.println(excuteMessage);
		} catch (IOException e) {
			return getResponse(e);
		}

		// 运行代码
		List<ExcuteMessage> executeMessageList = new ArrayList<>();
		for (String inputArgs : inputList) {
			// -Xmx256m -Xms（初始堆空间大小）
			String runCmd = String.format("java -Xmx256m -Dfile.encoding=uft-8 -cp %s Main %s", userCodeParentPath, inputArgs);


			try {
				Process runProcess = Runtime.getRuntime().exec(runCmd);
				// 超时控制
				new Thread(() -> {
					try {
						Thread.sleep(TIME_OUT);
						System.out.println("超时了");
						runProcess.destroy();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}).start();
				ExcuteMessage excuteMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
				System.out.println(excuteMessage);
				executeMessageList.add(excuteMessage);
			} catch (IOException e) {
				return getResponse(e);
			}
		}

		// 整理输出
		ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
		List<String> outputList = new ArrayList<>();
		// 取用时最大值
		long maxTime = 0L;
		for (ExcuteMessage executeMessage : executeMessageList) {
			String errorMessage = executeMessage.getErrorMessage();
			if (StrUtil.isNotBlank(errorMessage)) {
				executeCodeResponse.setMessage(errorMessage);
				// 执行中存在错误
				executeCodeResponse.setStatus(3);
				break;
			}
			Long time = executeMessage.getTime();
			if (time != null) {
				maxTime = Math.max(maxTime, time);
			}
			outputList.add(executeMessage.getErrorMessage());
		}
		if (outputList.size() == executeMessageList.size()) {
			executeCodeResponse.setStatus(1);
		}
		executeCodeResponse.setOutputList(outputList);
		// 正常运行完成
		JudgeInfo judgeInfo = new JudgeInfo();
		// 借助第三方库
//		judgeInfo.setMemory();
		judgeInfo.setTime(maxTime);
		executeCodeResponse.setJudgeInfo(judgeInfo);

		// 文件清理
		if (userCodeFile.getParentFile() != null) {
			boolean del = FileUtil.del(userCodeParentPath);
			System.out.println("删除" + (del ? "成功" : "失败"));
		}

		// 错误处理,提升健壮性，封装一个错误处理方法，程序一场出现时直接返回程序响应

		return executeCodeResponse;
	}

	// 获取错误响应
	private ExecuteCodeResponse getResponse(Throwable e) {
		ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
		executeCodeResponse.setOutputList(new ArrayList<String>());
		executeCodeResponse.setMessage(e.getMessage());
		// 表示代码沙箱错误
		executeCodeResponse.setStatus(2);
		executeCodeResponse.setJudgeInfo(new JudgeInfo());
		return executeCodeResponse;
	}
}
