package com.dong.dongaicodegenerator.parser;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.dong.dongaicodegenerator.ai.model.HtmlCodeResult;
import com.dong.dongaicodegenerator.ai.model.MultiFileCodeResult;
import com.dong.dongaicodegenerator.exception.BusinessException;
import com.dong.dongaicodegenerator.exception.ErrorCode;
import com.dong.dongaicodegenerator.model.enums.CodeGenTypeEnum;

/**
 * 代码解析器执行器
 * 根据代码生成类型选择相应的代码解析器进行解析
 */
public class CodeParserExecutor {

    private static final CodeParser<HtmlCodeResult> HTML_CODE_PARSER = new HtmlCodeParser();
    private static final CodeParser<MultiFileCodeResult> MULTI_FILE_CODE_PARSER = new MultiFileCodeParser();

    /**
     * 执行代码解析
     * @param content
     * @param codeGenTypeEnum
     * @return
     */
    public static Object executeCodeParser(String content, CodeGenTypeEnum codeGenTypeEnum) {
        if (StrUtil.isEmpty(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (ObjectUtil.isNull(codeGenTypeEnum)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "代码生成类型不能为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> HTML_CODE_PARSER.parseCode(content);
            case MULTI_FILE -> MULTI_FILE_CODE_PARSER.parseCode(content);
            default ->
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "不支持的代码生成类型：" + codeGenTypeEnum);
        };
    }
}
