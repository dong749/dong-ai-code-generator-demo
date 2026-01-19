package com.dong.dongaicodegenerator.core;

import cn.hutool.core.util.StrUtil;
import com.dong.dongaicodegenerator.ai.model.HtmlCodeResult;
import com.dong.dongaicodegenerator.ai.model.MultiFileCodeResult;
import com.dong.dongaicodegenerator.exception.ErrorCode;
import com.dong.dongaicodegenerator.exception.ThrowUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码解析器
 * 提供静态方法解析不同类型的代码内容
 */
public class CodeParser {

    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*(.*?)\\s*```", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*(.*?)\\s*```", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*(.*?)\\s*```", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);


    /**
     * 根据正则匹配并提取代码内容
     * @param content
     * @param pattern
     * @return
     */
    private static String extractCodeByPattern(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }


    /**
     * 解析 HTML 代码内容
     * @param content
     * @return
     */
    public static HtmlCodeResult parseHtmlCode(String content) {
        HtmlCodeResult htmlCodeResult = new HtmlCodeResult();
        ThrowUtils.throwIf(StrUtil.isEmpty(content), ErrorCode.OPERATION_ERROR, "代码内容不能为空");
        String htmlCode = extractCodeByPattern(content, HTML_CODE_PATTERN);
        if (htmlCode != null && StrUtil.isNotEmpty(htmlCode.trim())) {
            htmlCodeResult.setHtmlCode(htmlCode.trim());
        } else {
            htmlCodeResult.setHtmlCode(content.trim());
        }
        return htmlCodeResult;
    }

    /**
     * 解析多文件代码内容
     * @param content
     * @return
     */
    public static MultiFileCodeResult parseMultiFileCode(String content) {
        MultiFileCodeResult multiFileCodeResult = new MultiFileCodeResult();
        ThrowUtils.throwIf(StrUtil.isEmpty(content), ErrorCode.OPERATION_ERROR, "代码内容不能为空");

        String htmlCode = extractCodeByPattern(content, HTML_CODE_PATTERN);
        if (htmlCode != null && StrUtil.isNotEmpty(htmlCode.trim())) {
            multiFileCodeResult.setHtmlCode(htmlCode.trim());
        }

        String cssCode = extractCodeByPattern(content, CSS_CODE_PATTERN);
        if (cssCode != null && StrUtil.isNotEmpty(cssCode.trim())) {
            multiFileCodeResult.setCssCode(cssCode.trim());
        }

        String jsCode = extractCodeByPattern(content, JS_CODE_PATTERN);
        if (jsCode != null && StrUtil.isNotEmpty(jsCode.trim())) {
            multiFileCodeResult.setJsCode(jsCode.trim());
        }

        return multiFileCodeResult;
    }
}
