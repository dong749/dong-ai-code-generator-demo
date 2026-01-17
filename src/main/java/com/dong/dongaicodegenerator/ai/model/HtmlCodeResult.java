package com.dong.dongaicodegenerator.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Data
@Description("生成的 HTML 代码文件的结果")
public class HtmlCodeResult {

    @Description("HTML代码")
    private String htmlCode;

    @Description("生成的代码的描述")
    private String description;
}
