package com.dong.dongaicodegenerator.parser;

import cn.hutool.core.util.StrUtil;
import com.dong.dongaicodegenerator.ai.model.MultiFileCodeResult;
import com.dong.dongaicodegenerator.exception.BusinessException;
import com.dong.dongaicodegenerator.exception.ErrorCode;
import com.dong.dongaicodegenerator.exception.ThrowUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiFileCodeParser implements CodeParser<MultiFileCodeResult>{

    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*(.*?)\\s*```", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*(.*?)\\s*```", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*(.*?)\\s*```", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

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
    public MultiFileCodeResult parseCode(String content) {
        ThrowUtils.throwIf(StrUtil.isEmpty(content), ErrorCode.PARAMS_ERROR);
        MultiFileCodeResult multiFileCodeResult = new MultiFileCodeResult();
        String htmlCode = extractCodeByPattern(content, HTML_CODE_PATTERN);
        if (htmlCode != null && StrUtil.isNotEmpty(htmlCode.trim())) {
            multiFileCodeResult.setHtmlCode(htmlCode.trim());
        } else {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未找到 HTML 代码块");
        }
        String cssCode = extractCodeByPattern(content, CSS_CODE_PATTERN);
        if (cssCode != null && StrUtil.isNotEmpty(cssCode.trim())) {
            multiFileCodeResult.setCssCode(cssCode.trim());
        } else {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未找到 CSS 代码块");
        }
        String jsCode = extractCodeByPattern(content, JS_CODE_PATTERN);
        if (jsCode != null && StrUtil.isNotEmpty(jsCode.trim())) {
            multiFileCodeResult.setJsCode(jsCode.trim());
        } else {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未找到 JS 代码块");
        }
        return multiFileCodeResult;
    }
}
