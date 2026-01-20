package com.dong.dongaicodegenerator.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.dong.dongaicodegenerator.exception.ErrorCode;
import com.dong.dongaicodegenerator.exception.ThrowUtils;
import com.dong.dongaicodegenerator.model.dto.AppQueryRequest;
import com.dong.dongaicodegenerator.model.entity.User;
import com.dong.dongaicodegenerator.model.vo.AppVO;
import com.dong.dongaicodegenerator.model.vo.UserVO;
import com.dong.dongaicodegenerator.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.dong.dongaicodegenerator.model.entity.App;
import com.dong.dongaicodegenerator.mapper.AppMapper;
import com.dong.dongaicodegenerator.service.AppService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/dong749">Dong</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    @Resource
    private UserService userService;

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


}
