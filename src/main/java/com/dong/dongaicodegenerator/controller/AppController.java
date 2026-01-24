package com.dong.dongaicodegenerator.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.dong.dongaicodegenerator.annotation.AuthCheck;
import com.dong.dongaicodegenerator.common.BaseResponse;
import com.dong.dongaicodegenerator.common.DeleteRequest;
import com.dong.dongaicodegenerator.common.ResultUtils;
import com.dong.dongaicodegenerator.constant.AppConstant;
import com.dong.dongaicodegenerator.constant.UserConstant;
import com.dong.dongaicodegenerator.exception.BusinessException;
import com.dong.dongaicodegenerator.exception.ErrorCode;
import com.dong.dongaicodegenerator.exception.ThrowUtils;
import com.dong.dongaicodegenerator.model.dto.*;
import com.dong.dongaicodegenerator.model.entity.User;
import com.dong.dongaicodegenerator.model.enums.CodeGenTypeEnum;
import com.dong.dongaicodegenerator.model.enums.UserRoleEnum;
import com.dong.dongaicodegenerator.model.vo.AppVO;
import com.dong.dongaicodegenerator.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.dong.dongaicodegenerator.model.entity.App;
import com.dong.dongaicodegenerator.service.AppService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 应用 控制层。
 *
 * @author <a href="https://github.com/dong749">Dong</a>
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;
    @Resource
    private UserService userService;


    /**
     * 添加应用。
     *
     * @param appAddRequest 应用添加请求
     * @param request       HTTP 请求
     * @return 应用 ID
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(appAddRequest), ErrorCode.PARAMS_ERROR);
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
        User loginUser = userService.getLoginUser(request);
        // 构造需要存储的对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 初始默认多文件生成
        app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());
        // 取初始提示词的前12位为默认App名称
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        boolean saved = appService.save(app);
        ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "应用保存失败");
        return ResultUtils.success(app.getId());
    }


    /**
     * 更新应用。
     *
     * @param appUpdateRequest 应用更新请求
     * @param request          HTTP 请求
     * @return 是否更新成功
     */
    @PostMapping("/updateApp")
    public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(appUpdateRequest), ErrorCode.PARAMS_ERROR);
        Long appId = appUpdateRequest.getId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用不存在");
        User loginUser = userService.getLoginUser(request);
        App oldApp = appService.getById(appId);
        if (ObjectUtil.isNull(oldApp)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        if (!loginUser.getId().equals(oldApp.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该应用");
        }
        App app = new App();
        app.setId(appId);
        app.setAppName(appUpdateRequest.getAppName());
        app.setEditTime(LocalDateTime.now());
        boolean updated = appService.updateById(app);
        ThrowUtils.throwIf(!updated, ErrorCode.SYSTEM_ERROR, "应用更新失败");
        return ResultUtils.success(true);
    }

    /**
     * 删除应用。
     *
     * @param deleteRequest 删除请求
     * @param request       HTTP 请求
     * @return 是否删除成功
     */
    @DeleteMapping("/delete")
    public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (ObjectUtil.isNull(deleteRequest) || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        Long appId = deleteRequest.getId();
        App oldApp = appService.getById(appId);
        ThrowUtils.throwIf(ObjectUtil.isNull(oldApp), ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        User loginUser = userService.getLoginUser(request);
        if (!loginUser.getId().equals(oldApp.getUserId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该应用");
        }
        boolean removed = appService.removeById(appId);
        ThrowUtils.throwIf(!removed, ErrorCode.SYSTEM_ERROR, "应用删除失败");
        return ResultUtils.success(true);
    }


    /**
     * 根据 ID 获取 AppVO 应用详情
     *
     * @param id 应用 ID
     * @return 应用视图对象
     */
    @GetMapping("/get/vo")
    public BaseResponse<AppVO> getAppVOById(Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不合法");
        App app = appService.getById(id);
        ThrowUtils.throwIf(ObjectUtil.isNull(app), ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        AppVO appVO = appService.getAppVO(app);
        return ResultUtils.success(appVO);
    }


    /**
     * 分页获取当前用户的 AppVO 列表。
     *
     * @param appQueryRequest 应用查询请求
     * @param request         HTTP 请求
     * @return 应用视图对象分页列表
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppVO>> listMyAppVOPage(@RequestBody AppQueryRequest appQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(appQueryRequest), ErrorCode.PARAMS_ERROR, "参数错误");
        User loginUser = userService.getLoginUser(request);
        int pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.OPERATION_ERROR, "单页数据量过大");
        appQueryRequest.setUserId(loginUser.getId());
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> page = appService.page(Page.of(appQueryRequest.getPageNum()
                , appQueryRequest.getPageSize()), queryWrapper);
        Page<AppVO> appVOPage = new Page<>(appQueryRequest.getPageNum(), pageSize, page.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(page.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }


    /**
     * 分页获取高质量 AppVO 列表。
     *
     * @param appQueryRequest 应用查询请求
     * @return 应用视图对象分页列表
     */
    @PostMapping("/good/list/page/vo")
    public BaseResponse<Page<AppVO>> listHighQualityAppVOPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(appQueryRequest), ErrorCode.PARAMS_ERROR, "参数错误");
        int pageSize = appQueryRequest.getPageSize();
        int pageNum = appQueryRequest.getPageNum();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.OPERATION_ERROR, "单页数据量过大");
        appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> page = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, page.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(page.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 管理员删除应用。
     *
     * @param deleteRequest 删除请求
     * @return 是否删除成功
     */
    @DeleteMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> adminDeleteApp(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(deleteRequest), ErrorCode.PARAMS_ERROR, "参数错误");
        if (deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        App app = appService.getById(deleteRequest.getId());
        if (ObjectUtil.isNull(app)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        boolean removed = appService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!removed, ErrorCode.SYSTEM_ERROR, "应用删除失败");
        return ResultUtils.success(true);
    }


    /**
     * 管理员更新应用。
     *
     * @param appAdminUpdateRequest 应用更新请求
     * @return 是否更新成功
     */
    @PostMapping("/admin/updateApp")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> adminUpdateApp(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(appAdminUpdateRequest), ErrorCode.PARAMS_ERROR);
        Long appId = appAdminUpdateRequest.getId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用不存在");
        App oldApp = appService.getById(appId);
        ThrowUtils.throwIf(ObjectUtil.isNull(oldApp), ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        App app = new App();
        BeanUtil.copyProperties(appAdminUpdateRequest, app);
        app.setEditTime(LocalDateTime.now());
        boolean updated = appService.updateById(app);
        ThrowUtils.throwIf(!updated, ErrorCode.SYSTEM_ERROR, "应用更新失败");
        return ResultUtils.success(true);
    }


    /**
     * 管理员分页获取应用列表
     *
     * @param appQueryRequest 查询请求
     * @return 应用列表
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppVO>> listAppVOByPageByAdmin(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryRequest);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return ResultUtils.success(appVOPage);
    }


    /**
     * 管理员根据 id 获取应用详情
     *
     * @param id 应用 id
     * @return 应用详情
     */
    @GetMapping("/admin/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AppVO> getAppVOByIdByAdmin(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(appService.getAppVO(app));
    }


    /**
     * 使用 AI 模型进行代码生成对话，返回流式响应。
     *
     * @param prompt  用户提示词
     * @param appId   应用 ID
     * @param request HTTP 请求
     * @return 流式字符串响应
     */
    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatWithModelForGenerateCode(@RequestParam String prompt
            , @RequestParam Long appId, HttpServletRequest request) {
        ThrowUtils.throwIf(StrUtil.isBlank(prompt), ErrorCode.PARAMS_ERROR, "提示词不能为空");
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        User loginUser = userService.getLoginUser(request);
        Flux<String> generatedCodeFlux = appService.chatWithModelForGenerateCode(prompt, appId, loginUser);
        return generatedCodeFlux.map(new Function<String, ServerSentEvent<String>>() {
            @Override
            public ServerSentEvent<String> apply(String codeChunk) {
                Map<String, String> wrapper = Map.of("d", codeChunk);
                String jsonStr = JSONUtil.toJsonStr(wrapper);
                return ServerSentEvent.<String>builder()
                        .data(jsonStr)
                        .build();
            }
        }).concatWith(Mono.just(
                ServerSentEvent.<String>builder()
                        .event("done")
                        .data("")
                        .build(
        )));
    }

    /**
     * 应用部署
     *
     * @param appDeployRequest 部署请求
     * @param request          请求
     * @return 部署 URL
     */
    @PostMapping("/deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = appDeployRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务部署应用
        String deployUrl = appService.deployApp(appId, loginUser);
        return ResultUtils.success(deployUrl);
    }




    /**
     * 保存应用。
     *
     * @param app 应用
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("save")
    public boolean save(@RequestBody App app) {
        return appService.save(app);
    }

    /**
     * 根据主键删除应用。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable Long id) {
        return appService.removeById(id);
    }

    /**
     * 根据主键更新应用。
     *
     * @param app 应用
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    public boolean update(@RequestBody App app) {
        return appService.updateById(app);
    }

    /**
     * 查询所有应用。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    public List<App> list() {
        return appService.list();
    }

    /**
     * 根据主键获取应用。
     *
     * @param id 应用主键
     * @return 应用详情
     */
    @GetMapping("getInfo/{id}")
    public App getInfo(@PathVariable Long id) {
        return appService.getById(id);
    }

    /**
     * 分页查询应用。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("page")
    public Page<App> page(Page<App> page) {
        return appService.page(page);
    }

}
