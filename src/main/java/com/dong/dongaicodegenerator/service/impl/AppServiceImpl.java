package com.dong.dongaicodegenerator.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.dong.dongaicodegenerator.constant.AppConstant;
import com.dong.dongaicodegenerator.core.AiCodeGeneratorFacade;
import com.dong.dongaicodegenerator.exception.BusinessException;
import com.dong.dongaicodegenerator.exception.ErrorCode;
import com.dong.dongaicodegenerator.exception.ThrowUtils;
import com.dong.dongaicodegenerator.model.dto.AppQueryRequest;
import com.dong.dongaicodegenerator.model.entity.User;
import com.dong.dongaicodegenerator.model.enums.ChatHistoryMessageTypeEnum;
import com.dong.dongaicodegenerator.model.enums.CodeGenTypeEnum;
import com.dong.dongaicodegenerator.model.vo.AppVO;
import com.dong.dongaicodegenerator.model.vo.UserVO;
import com.dong.dongaicodegenerator.service.ChatHistoryService;
import com.dong.dongaicodegenerator.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.dong.dongaicodegenerator.model.entity.App;
import com.dong.dongaicodegenerator.mapper.AppMapper;
import com.dong.dongaicodegenerator.service.AppService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/dong749">Dong</a>
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    @Resource
    private UserService userService;
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;
    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * 根据 App 实体获取 AppVO。
     *
     * @param app 应用实体
     * @return 应用视图对象
     */
    @Override
    public AppVO getAppVO(App app) {
        if (ObjectUtil.isNull(app)) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }


    /**
     * 获取查询包装器
     *
     * @param appQueryRequest 应用查询请求
     * @return 查询包装器
     */
    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(ObjectUtil.isNull(appQueryRequest), ErrorCode.PARAMS_ERROR);
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        return QueryWrapper.create()
                .eq(App::getId, id, ObjectUtil.isNotNull(id))
                .like(App::getAppName, appName, appName != null)
                .like(App::getCover, cover, cover != null)
                .like(App::getInitPrompt, initPrompt, initPrompt != null)
                .eq(App::getCodeGenType, codeGenType, codeGenType != null)
                .eq(App::getDeployKey, deployKey, deployKey != null)
                .eq(App::getPriority, priority, ObjectUtil.isNotNull(priority))
                .eq(App::getUserId, userId, ObjectUtil.isNotNull(userId))
                .orderBy(sortField, "ascend".equals(sortOrder));
    }


    /**
     * 根据 App 列表获取 AppVO 对象列表。
     *
     * @param appList 应用实体列表
     * @return 应用视图对象列表
     */
    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (ObjectUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        Set<Long> userIdsSet = appList.stream().map(new Function<App, Long>() {
            @Override
            public Long apply(App app) {
                return app.getUserId();
            }
        }).collect(Collectors.toSet());
        List<User> users = userService.listByIds(userIdsSet);
        Map<Long, UserVO> userVOMap = users.stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(new Function<App, AppVO>() {
            @Override
            public AppVO apply(App app) {
                AppVO appVO = new AppVO();
                BeanUtil.copyProperties(app, appVO);
                Long userId = app.getUserId();
                if (userId != null) {
                    appVO.setUser(userVOMap.get(userId));
                }
                return appVO;
            }
        }).collect(Collectors.toList());
    }


    /**
     * 调用 AI 模型进行代码生成
     *
     * @param prompt    提示词
     * @param appId     应用 ID
     * @param loginUser 登录用户
     * @return 代码生成结果流
     */
    @Override
    public Flux<String> chatWithModelForGenerateCode(String prompt, Long appId, User loginUser) {
        ThrowUtils.throwIf(StrUtil.isBlank(prompt), ErrorCode.PARAMS_ERROR, "提示词不能为空");
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        App app = this.getById(appId);
        ThrowUtils.throwIf(ObjectUtil.isNull(app), ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该应用");
        }
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        ThrowUtils.throwIf(ObjectUtil.isNull(codeGenTypeEnum), ErrorCode.PARAMS_ERROR, "不支持的代码生成类型：" + codeGenType);
        // 调用 AI 之前先保存用户消息
        chatHistoryService.addChatHistory(appId, prompt, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
        // 调用 AI 进行代码生成，并以流式方式返回结果
        Flux<String> contentFlux = aiCodeGeneratorFacade.generateCodeAndSaveWithStream(prompt, codeGenTypeEnum, appId);
        // 收集 AI 的完整相应内容，并且在流处理完成后保存 AI 回复的消息
        StringBuilder aiResponseBuilder = new StringBuilder();
        return contentFlux.doOnNext(new Consumer<String>() {
            @Override
            public void accept(String s) {
                // 实时收集 AI 响应内容
                aiResponseBuilder.append(s);
            }
        }).doOnComplete(new Runnable() {
            // 流式返回完成后，保存 AI 回复的消息
            @Override
            public void run() {
                String aiResponseBuilderString = aiResponseBuilder.toString();
                chatHistoryService.addChatHistory(appId, aiResponseBuilderString, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
            }
        }).doOnError(new Consumer<Throwable>() {
            // 即使流式返回发生错误时，也需要保存 AI 回复的消息
            @Override
            public void accept(Throwable throwable) {
                String errorMessage = "AI 回复生成失败，错误信息：" + throwable.getMessage();
                chatHistoryService.addChatHistory(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
            }
        });
    }


    /**
     * 部署应用
     *
     * @param appId     应用 ID
     * @param loginUser 登录用户
     * @return 部署访问地址
     */
    @Override
    public String deployApp(Long appId, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        App app = this.getById(appId);
        ThrowUtils.throwIf(ObjectUtil.isNull(app), ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该应用");
        }
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        File sourceFile = new File(sourceDirPath);
        if (!sourceFile.exists() || !sourceFile.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码文件不存在，无法部署");
        }
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            // 从code_output目录复制文件到code_deploy目录
            File deployFile = new File(deployDirPath);
            FileUtil.copyContent(sourceFile, deployFile, true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用部署失败，发生异常：" + e.getMessage());
        }
        App updatedApp = new App();
        updatedApp.setId(appId);
        updatedApp.setDeployKey(deployKey);
        updatedApp.setDeployedTime(LocalDateTime.now());
        boolean updated = this.updateById(updatedApp);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "应用部署信息更新失败");
        return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }

    /**
     * 覆盖原本 Mybatis-Plus 原本的方法，根据 ID 删除应用，同时删除相关联的聊天记录
     * @param id
     * @return
     */
    @Override
    @Transactional
    public boolean removeById(Serializable id) {
        if (id == null) {
            return false;
        }
        long appId = Long.parseLong(id.toString());
        if (appId <= 0) {
            return false;
        }
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            log.error("删除应用关联的聊天记录失败，应用 ID：" + appId, e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除应用关联的聊天记录失败");
        }
        return super.removeById(id);
    }
}
