package com.dong.dongaicodegenerator.core.saver;

import cn.hutool.core.util.ObjectUtil;
import com.dong.dongaicodegenerator.ai.model.MultiFileCodeResult;
import com.dong.dongaicodegenerator.exception.BusinessException;
import com.dong.dongaicodegenerator.exception.ErrorCode;
import com.dong.dongaicodegenerator.model.enums.CodeGenTypeEnum;

import java.io.File;

public class MultiFileCodeSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult>{

    @Override
    protected File doSaveCodeFile(MultiFileCodeResult result, CodeGenTypeEnum codeGenTypeEnum) {
        boolean flag = paraValidation(result);
        if (!flag) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存多文件代码文件参数异常");
        }
        if (ObjectUtil.isNull(codeGenTypeEnum)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "代码类型为空");
        }
        String uniqueDir = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE);
        writeToFile(uniqueDir, "index.html", result.getHtmlCode());
        writeToFile(uniqueDir, "style.css", result.getCssCode());
        writeToFile(uniqueDir, "script.js", result.getJsCode());
        return new File(uniqueDir);
    }
}
