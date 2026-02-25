package com.dong.dongaicodegenerator.service;

import jakarta.servlet.http.HttpServletResponse;

import java.nio.file.Path;

public interface ProjectDownloadService {

    /**
     * 检查给定的路径是否在允许下载的范围内。
     *
     * @param projectRoot 项目根路径
     * @param fullPath    要检查的完整路径
     * @return 如果路径允许下载，则返回 true；否则返回 false。
     */
    boolean isPathAllowed(Path projectRoot, Path fullPath);

    /**
     * 下载项目文件 zip
     * @param projectPath
     * @param downloadFileName
     * @param response
     */
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}
