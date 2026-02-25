package com.dong.dongaicodegenerator.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.dong.dongaicodegenerator.exception.ErrorCode;
import com.dong.dongaicodegenerator.exception.ThrowUtils;
import com.dong.dongaicodegenerator.manager.CosManager;
import com.dong.dongaicodegenerator.service.ScreenshotService;
import com.dong.dongaicodegenerator.utils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private CosManager cosManager;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR, "网页 URL 不能为空");
        log.info("开始生成网页截图，URL: {}", webUrl);
        String compressedImagePath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        ThrowUtils.throwIf(StrUtil.isBlank(compressedImagePath), ErrorCode.OPERATION_ERROR
                , "生成网页截图失败");
        log.info("网页截图生成成功，路径: {}", compressedImagePath);
        try {
            String url = uploadScreenshotToCos(compressedImagePath);
            ThrowUtils.throwIf(StrUtil.isBlank(url), ErrorCode.OPERATION_ERROR
                    , "上传截图到 COS 失败");
            log.info("截图上传COS成功，访问URL: {}", url);
            return url;
        } catch (Exception e) {

        } finally {
            cleanLocalFile(compressedImagePath);
        }
        return "";
    }

    private String generateCosKey(String fileName) {
        String datePath = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd/"));
        return String.format("screenshots/%s%s", datePath, fileName);
    }

    private void cleanLocalFile(String filePath) {
        File localFile = new File(filePath);
        if (localFile.exists()) {
            File parentFile = localFile.getParentFile();
            FileUtil.del(parentFile);
            log.info("已删除本地临时文件: {}", parentFile.getAbsolutePath());
        }
    }

    private String uploadScreenshotToCos(String localFilePath) {
        if (StrUtil.isBlank(localFilePath)) {
            return null;
        }
        File screenFile = new File(localFilePath);
        if (!screenFile.exists()) {
            log.info("本地截图文件不存在，路径: {}", localFilePath);
            return null;
        }
        String filaName = UUID.randomUUID().toString().substring(0, 8) + "_compressed.jpg";
        String cosKey = generateCosKey(filaName);
        return cosManager.uploadFile(cosKey, screenFile);
    }
}
