package com.dong.dongaicodegenerator.core;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.dong.dongaicodegenerator.ai.AiCodeGeneratorService;
import com.dong.dongaicodegenerator.ai.AiCodeGeneratorServiceFactory;
import com.dong.dongaicodegenerator.ai.model.HtmlCodeResult;
import com.dong.dongaicodegenerator.ai.model.MultiFileCodeResult;
import com.dong.dongaicodegenerator.exception.BusinessException;
import com.dong.dongaicodegenerator.exception.ErrorCode;
import com.dong.dongaicodegenerator.model.enums.CodeGenTypeEnum;
import com.dong.dongaicodegenerator.core.parser.CodeParserExecutor;
import com.dong.dongaicodegenerator.core.saver.CodeFileSaverExecutor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.function.Consumer;

/**
 * 代码生成门面类，组合代码生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    /**
     * 生成代码并保存文件的统一入口
     * @param prompt
     * @param codeGenTypeEnum
     * @return
     */
    public File generateCodeAndSave(String prompt, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (StrUtil.isBlank(prompt) || ObjectUtil.isNull(codeGenTypeEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数异常，提示词或代码生成类型不能为空");
        }
        // 根据 appId 获取对应的 AiCodeGeneratorService 服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(prompt);
                yield CodeFileSaverExecutor.saveCodeFileExecutor(htmlCodeResult, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE ->  {
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFleCode(prompt);
                yield CodeFileSaverExecutor.saveCodeFileExecutor(multiFileCodeResult, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "不支持的代码生成类型：" + codeGenTypeEnum);
            }
        };
    }

    /**
     * 流式生成代码并保存文件的统一入口
     * @param prompt
     * @param codeGenTypeEnum
     * @return
     */
    public Flux<String> generateCodeAndSaveWithStream(String prompt, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (StrUtil.isBlank(prompt) || ObjectUtil.isNull(codeGenTypeEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数异常，提示词或代码生成类型不能为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        if (codeGenTypeEnum.equals(CodeGenTypeEnum.HTML)) {
            Flux<String> htmlCodeStream = aiCodeGeneratorService.generateHtmlCodeStream(prompt);
            return processCodeGenerationStream(htmlCodeStream, CodeGenTypeEnum.HTML, appId);
        } else if (codeGenTypeEnum.equals(CodeGenTypeEnum.MULTI_FILE)) {
            Flux<String> multiFileCodeStream = aiCodeGeneratorService.generateMultiFileCodeStream(prompt);
            return processCodeGenerationStream(multiFileCodeStream, CodeGenTypeEnum.MULTI_FILE, appId);
        } else {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "不支持的代码生成类型：" + codeGenTypeEnum);
        }
    }

    /**
     * 通用处理代码生成流
     * @param codeGenerationFlux
     * @param codeGenTypeEnum
     * @return
     */
    private Flux<String> processCodeGenerationStream(Flux<String> codeGenerationFlux, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (ObjectUtil.isNull(codeGenTypeEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码生成类型不能为空");
        }
        StringBuilder codeBuilder = new StringBuilder();
        return codeGenerationFlux
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        codeBuilder.append(s);
                    }
                })
                .doOnComplete(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String completedCode = codeBuilder.toString();
                            Object codeResult = CodeParserExecutor.executeCodeParser(completedCode, codeGenTypeEnum);
                            File codeFile = CodeFileSaverExecutor.saveCodeFileExecutor(codeResult, codeGenTypeEnum, appId);
                            log.info("代码文件保存成功：" + codeFile.getAbsolutePath());
                        } catch (Exception e) {
                            log.error("代码文件保存失败", e);
                        }
                    }
                });
    }

//    /**
//     * 生成代码功能的统一入口，根据类型生成并保存文件
//     * @param userMessage
//     * @param codeGenTypeEnum
//     * @return
//     */
//    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
//        ThrowUtils.throwIf(ObjectUtil.isNull(codeGenTypeEnum), ErrorCode.PARAMS_ERROR, "生成类型不能为空");
//        return switch (codeGenTypeEnum) {
//            case HTML -> generateAndSaveHtmlCode(userMessage);
//            case MULTI_FILE -> generateAndSaveMultiFleCode(userMessage);
//            default -> {
//                throw new BusinessException(ErrorCode.OPERATION_ERROR, "生成类型错误");
//            }
//        };
//    }
//
//    /**
//     * 流式生成代码功能的统一入口，根据类型生成并保存文件
//     * @param userMessage
//     * @param codeGenTypeEnum
//     * @return
//     */
//    public Flux<String> generateAndSaveCodeWithStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
//        ThrowUtils.throwIf(ObjectUtil.isNull(codeGenTypeEnum), ErrorCode.PARAMS_ERROR, "生成类型不能为空");
//        if (codeGenTypeEnum.equals(CodeGenTypeEnum.HTML)) {
//            return generateHtmlCodeWithStream(userMessage);
//        } else if (codeGenTypeEnum.equals(CodeGenTypeEnum.MULTI_FILE)) {
//            return generateMultiFileCodeWithStream(userMessage);
//        } else {
//            throw new BusinessException(ErrorCode.OPERATION_ERROR, "生成类型错误");
//        }
//    }



//    private File generateAndSaveMultiFleCode(String userMessage) {
//        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFleCode(userMessage);
//        return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
//    }
//
//    private File generateAndSaveHtmlCode(String userMessage) {
//        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
//        return CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
//    }


//    /**
//     * 流式生成 HTML 代码并保存
//     * @param prompt
//     * @return
//     */
//    private Flux<String> generateHtmlCodeWithStream(String prompt) {
//        ThrowUtils.throwIf(StrUtil.isEmpty(prompt), ErrorCode.PARAMS_ERROR, "提示词不能为空");
//        Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(prompt);
//        // 用户不断拼接流式调用 AI 返回的代码，所用代码生成完以后再进行保存
//        StringBuilder codeBuilder = new StringBuilder();
//        return result.doOnNext(new Consumer<String>() {
//            @Override
//            public void accept(String s) {
//                codeBuilder.append(s);
//            }
//        }).doOnComplete(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    String completedHtmlCode = codeBuilder.toString();
//                    HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completedHtmlCode);
//                    File codeFile = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
//                    log.info("代码文件保存成功" + codeFile.getAbsolutePath());
//                } catch (Exception e) {
//                    log.error("代码文件保存失败", e);
//                }
//            }
//        });
//    }
//
//    /**
//     * 流式生成多文件代码并保存
//     * @param prompt
//     * @return
//     */
//    private Flux<String> generateMultiFileCodeWithStream(String prompt) {
//        ThrowUtils.throwIf(StrUtil.isEmpty(prompt), ErrorCode.PARAMS_ERROR, "提示词不能为空");
//        Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(prompt);
//        StringBuilder codeBuilder = new StringBuilder();
//        return result.doOnNext(new Consumer<String>() {
//            @Override
//            public void accept(String s) {
//                codeBuilder.append(s);
//            }
//        }).doOnComplete(new Runnable() {
//            @Override
//            public void run() {
//                String completedCode = codeBuilder.toString();
//                try {
//                    MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(completedCode);
//                    File multiFile = CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
//                    log.info("多文件代码保存成功：" + multiFile.getAbsolutePath());
//                } catch (Exception e) {
//                    log.error("多文件代码保存失败", e);
//                    throw new RuntimeException(e);
//                }
//            }
//        });
//    }
}
