package com.dong.dongaicodegenerator.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.dong.dongaicodegenerator.constant.AppConstant;
import com.dong.dongaicodegenerator.exception.BusinessException;
import com.dong.dongaicodegenerator.exception.ErrorCode;
import com.dong.dongaicodegenerator.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

public abstract class CodeFileSaverTemplate<T> {

    // 文件保存的根目录
    // private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/temp/code_output";
    // 文件保存根目录
    protected static final String FILE_SAVE_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;


    /**
     * 具体保存代码文件的实现方法
     * @param result
     * @param codeGenTypeEnum
     */
    protected abstract File doSaveCodeFile(T result, CodeGenTypeEnum codeGenTypeEnum, Long appId);

    /**
     * 参数校验
     * @param result
     * @return
     */
    protected boolean paraValidation(T result) {
        return !ObjectUtil.isNull(result);
    }

    /**
     * 保存代码文件
     * @param result
     * @param codeGenTypeEnum
     * @return
     */
    public final File saveCodeFile(T result, Long appId, CodeGenTypeEnum codeGenTypeEnum) {
        if (!paraValidation(result)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "保存代码文件参数异常");
        }
        String dirPath = buildUniqueDir(codeGenTypeEnum, appId);
        doSaveCodeFile(result, codeGenTypeEnum, appId);
        return new File(dirPath);
    }

    /**
     * 构建代码文件存储路径以及唯一的代码目录名称
     * @param
     * @return
     */
    protected String buildUniqueDir(CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        String dirName = StrUtil.format("{}_{}", codeGenTypeEnum.getValue(), appId);
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + dirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 保存代码文件
     * @param dirpath
     * @param fileName
     * @param content
     */
    protected void writeToFile(String dirpath, String fileName, String content) {
        if (StrUtil.isNotBlank(content)) {
            String filePath = dirpath + File.separator + fileName;
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }
    }
}
