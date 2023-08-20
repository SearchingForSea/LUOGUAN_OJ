package com.luoguan.luoguancodesandbox.unsafe;

import java.util.ArrayList;
import java.util.List;

// 无限占用空间
public class MemoryError {
	public static void main(String[] args) {
		List<byte[]> bytes = new ArrayList<byte[]>();
		while (true) {
			bytes.add(new byte[10000]);
		}
	}
}
