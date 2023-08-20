package com.luoguan.luoguancodesandbox.security;

import cn.hutool.core.io.FileUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class TestSecurity {
	public static void main(String[] args) {
		System.setSecurityManager(new MySecurityManage());

		List<String> strings =  FileUtil.readLines("D:\\CODE\\WS\\luoguan-code-sandbox\\src\\main\\java\\com\\luoguan\\luoguancodesandbox\\model\\ExcuteMessage.java", StandardCharsets.UTF_8);

		System.out.println(strings);

	}
}
