package com.dong.dongaicodegenerator.ai;

import com.dong.dongaicodegenerator.ai.model.HtmlCodeResult;
import com.dong.dongaicodegenerator.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;

public interface AiCodeGeneratorService {

    /**
     * 根据提示词生成HTML代码
     * @param prompt
     * @return
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String prompt);


    /**
     * 根据提示词生成多文件的代码
     * @param prompt
     * @return
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFleCode(String prompt);
}
