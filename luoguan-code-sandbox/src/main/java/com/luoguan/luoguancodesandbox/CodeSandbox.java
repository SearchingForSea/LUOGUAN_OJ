package com.luoguan.luoguancodesandbox;

import com.luoguan.luoguancodesandbox.model.ExecuteCodeRequest;
import com.luoguan.luoguancodesandbox.model.ExecuteCodeResponse;

/**
 * 代码沙箱接口定义
 */
public interface CodeSandbox {

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
