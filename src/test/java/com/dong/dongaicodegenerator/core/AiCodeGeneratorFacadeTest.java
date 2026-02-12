package com.dong.dongaicodegenerator.core;

import com.dong.dongaicodegenerator.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

//    @Test
//    void generateAndSaveCode() {
//        File file = aiCodeGeneratorFacade.generateAndSaveCode("生成一个登录界面, 不超过20行代码", CodeGenTypeEnum.MULTI_FILE);
//        Assertions.assertNotNull(file);
//    }
//
//    @Test
//    void generateAndSaveCodeStream() {
//        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeWithStream("任务记录网站", CodeGenTypeEnum.MULTI_FILE);
//        // 阻塞等待所有数据收集完成
//        List<String> result = codeStream.collectList().block();
//        // 验证结果
//        Assertions.assertNotNull(result);
//        String completeContent = String.join("", result);
//        Assertions.assertNotNull(completeContent);
//    }


    @Test
    void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateCodeAndSave("任务记录网站", CodeGenTypeEnum.MULTI_FILE, 1L);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateAndSaveCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateCodeAndSaveWithStream("任务记录网站", CodeGenTypeEnum.MULTI_FILE, 1L);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }

    @Test
    void generateVueProjectCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateCodeAndSaveWithStream(
                "简单的任务记录网站，总代码量不超过 200 行",
                CodeGenTypeEnum.VUE_PROJECT, 1L);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }

}