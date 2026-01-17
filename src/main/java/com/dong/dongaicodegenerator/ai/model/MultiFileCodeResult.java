package com.dong.dongaicodegenerator.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Data
@Description("生成多个代码代码文件的结果")
public class MultiFileCodeResult {

    @Description("HTML代码")
    private String htmlCode;
    @Description("CSS代码")
    private String cssCode;
    @Description("JavaScript代码")
    private String jsCode;
    @Description("生成的代码的描述")
    private String description;
}
