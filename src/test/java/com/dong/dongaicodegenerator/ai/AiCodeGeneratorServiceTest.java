package com.dong.dongaicodegenerator.ai;

import com.dong.dongaicodegenerator.ai.model.HtmlCodeResult;
import com.dong.dongaicodegenerator.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode("做一个程序员旭东的博客，代码不超过20行");
        Assertions.assertNotNull(htmlCodeResult);
    }

    @Test
    void generateMultiFleCode() {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFleCode("做一个程序员旭东的留言板，代码不超过20行");
        Assertions.assertNotNull(multiFileCodeResult);
    }
}