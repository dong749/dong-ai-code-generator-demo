package com.dong.dongaicodegenerator.core.saver;


import cn.hutool.core.util.ObjectUtil;
import com.dong.dongaicodegenerator.ai.model.HtmlCodeResult;
import com.dong.dongaicodegenerator.exception.BusinessException;
import com.dong.dongaicodegenerator.exception.ErrorCode;
import com.dong.dongaicodegenerator.model.enums.CodeGenTypeEnum;

import java.io.File;

public class HtmlCodeSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {

    @Override
    protected File doSaveCodeFile(HtmlCodeResult result, CodeGenTypeEnum codeGenTypeEnum) {
        boolean flag = paraValidation(result);
        if (!flag) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存 HTML 代码文件参数异常");
        }
        if (ObjectUtil.isNull(codeGenTypeEnum)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "代码类型为空");
        }
        String uniqueDir = buildUniqueDir(CodeGenTypeEnum.HTML);
        writeToFile(uniqueDir, "index.html", result.getHtmlCode());
        return new File(uniqueDir);
    }
}
