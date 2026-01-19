package com.dong.dongaicodegenerator.parser;

import cn.hutool.core.util.StrUtil;
import com.dong.dongaicodegenerator.ai.model.HtmlCodeResult;
import com.dong.dongaicodegenerator.exception.ErrorCode;
import com.dong.dongaicodegenerator.exception.ThrowUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlCodeParser implements CodeParser<HtmlCodeResult>{

    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*(.*?)\\s*```", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * 根据正则匹配并提取代码内容
     * @param content
     * @param pattern
     * @return
     */
    private String extractCodeByPattern(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @Override
    public HtmlCodeResult parseCode(String content) {
        ThrowUtils.throwIf(StrUtil.isEmpty(content), ErrorCode.PARAMS_ERROR);
        HtmlCodeResult htmlCodeResult = new HtmlCodeResult();
        String htmlCode = extractCodeByPattern(content, HTML_CODE_PATTERN);
        if (htmlCode != null && StrUtil.isNotEmpty(htmlCode.trim())) {
            htmlCodeResult.setHtmlCode(htmlCode.trim());
        } else {
            htmlCodeResult.setHtmlCode(content.trim());
        }
        return htmlCodeResult;
    }
}
