package com.dong.dongaicodegenerator.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.dong.dongaicodegenerator.exception.BusinessException;
import com.dong.dongaicodegenerator.exception.ErrorCode;
import com.dong.dongaicodegenerator.exception.ThrowUtils;
import com.dong.dongaicodegenerator.service.ProjectDownloadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

@Service
@Slf4j
public class ProjectDownloadServiceImpl implements ProjectDownloadService {

    public static final Set<String> IGNORED_NAMES = Set.of(
            "node_modules",
            ".git",
            "dist",
            "build",
            ".DS_store",
            ".env",
            "target",
            ".mvn",
            ".idea",
            ".vscode"
    );

    public static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".log",
            ".tmp",
            ".temp",
            ".cache"
    );

    /**
     * 检查给定的路径是否在允许下载的范围内。
     *
     * @param projectRoot 项目根路径
     * @param fullPath    要检查的完整路径
     * @return 如果路径允许下载，则返回 true；否则返回 false。
     */
    @Override
    public boolean isPathAllowed(Path projectRoot, Path fullPath) {
        Path relativized = projectRoot.relativize(fullPath);
        for (Path part : relativized) {
            String partName = part.toString();
            if (IGNORED_NAMES.contains(partName)) {
                return false;
            }
            if (IGNORED_EXTENSIONS.stream().anyMatch(partName::endsWith)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 下载项目文件 zip
     * @param projectPath
     * @param downloadFileName
     * @param response
     */
    @Override
    public void downloadProjectAsZip(String projectPath, String downloadFileName
            , HttpServletResponse response) {
        ThrowUtils.throwIf(StrUtil.isBlank(projectPath), ErrorCode.PARAMS_ERROR
                , "项目路径不可为空");
        ThrowUtils.throwIf(StrUtil.isBlank(downloadFileName), ErrorCode.PARAMS_ERROR, "文件名称为空");
        File projectDir = new File(projectPath);
        ThrowUtils.throwIf(!projectDir.exists(), ErrorCode.OPERATION_ERROR, "项目目录不存在");
        ThrowUtils.throwIf(!projectDir.isDirectory(), ErrorCode.OPERATION_ERROR);
        log.info("下载项目文件 {} -> {}.zip", projectPath, downloadFileName);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/zip");
        response.addHeader("Content-Disposition",
                String.format("attachment; filename=\"%s.zip\"", downloadFileName));
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File projectFile) {
                return isPathAllowed(projectDir.toPath(), projectFile.toPath());
            }
        };
        try {
            ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8
                    , false, fileFilter, projectDir);
            log.info("项目打包下载完成: {}", downloadFileName);
        } catch (Exception e) {
            log.error("项目打包下载异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "项目打包下载失败");
        }
    }
}
