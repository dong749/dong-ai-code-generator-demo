package com.dong.dongaicodegenerator.core.saver;

import cn.hutool.core.util.ObjectUtil;
import com.dong.dongaicodegenerator.ai.model.HtmlCodeResult;
import com.dong.dongaicodegenerator.ai.model.MultiFileCodeResult;
import com.dong.dongaicodegenerator.exception.BusinessException;
import com.dong.dongaicodegenerator.exception.ErrorCode;
import com.dong.dongaicodegenerator.exception.ThrowUtils;
import com.dong.dongaicodegenerator.model.enums.CodeGenTypeEnum;

import java.io.File;

public class CodeFileSaverExecutor {

    private static final HtmlCodeSaverTemplate HTML_CODE_SAVER_TEMPLATE = new HtmlCodeSaverTemplate();
    private static final MultiFileCodeSaverTemplate MULTI_FILE_CODE_SAVER_TEMPLATE = new MultiFileCodeSaverTemplate();

    public static File saveCodeFileExecutor(Object result, CodeGenTypeEnum codeGenTypeEnum) {
        ThrowUtils.throwIf(ObjectUtil.isNull(codeGenTypeEnum), ErrorCode.OPERATION_ERROR, "代码文件保存类型异常");
        switch (codeGenTypeEnum) {
            case HTML:
                return HTML_CODE_SAVER_TEMPLATE.saveCodeFile((HtmlCodeResult) result, codeGenTypeEnum);
            case MULTI_FILE:
                return MULTI_FILE_CODE_SAVER_TEMPLATE.saveCodeFile((MultiFileCodeResult) result, codeGenTypeEnum);
            default:
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "代码文件保存类型未支持");
        }
    }
}
