package com.dong.dongaicodegenerator.core;

import cn.hutool.core.util.ObjectUtil;
import com.dong.dongaicodegenerator.ai.AiCodeGeneratorService;
import com.dong.dongaicodegenerator.ai.model.HtmlCodeResult;
import com.dong.dongaicodegenerator.ai.model.MultiFileCodeResult;
import com.dong.dongaicodegenerator.exception.BusinessException;
import com.dong.dongaicodegenerator.exception.ErrorCode;
import com.dong.dongaicodegenerator.exception.ThrowUtils;
import com.dong.dongaicodegenerator.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * 代码生成门面类，组合代码生成和保存功能
 */
@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 生成代码功能的统一入口，根据类型生成并保存文件
     * @param userMessage
     * @param codeGenTypeEnum
     * @return
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        ThrowUtils.throwIf(ObjectUtil.isNull(codeGenTypeEnum), ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFleCode(userMessage);
            default -> {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "生成类型错误");
            }
        };
    }

    private File generateAndSaveMultiFleCode(String userMessage) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFleCode(userMessage);
        return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
    }

    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
    }


}
