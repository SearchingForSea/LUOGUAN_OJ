package com.luoguan.luoguancodesandbox.model;

import lombok.Data;

@Data
public class ExcuteMessage {
	private int exitValue;
	private String message;
	private String errorMessage;
	private Long time;
}
