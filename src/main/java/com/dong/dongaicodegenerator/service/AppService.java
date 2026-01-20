package com.dong.dongaicodegenerator.service;

import com.dong.dongaicodegenerator.model.dto.AppAddRequest;
import com.dong.dongaicodegenerator.model.dto.AppQueryRequest;
import com.dong.dongaicodegenerator.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.dong.dongaicodegenerator.model.entity.App;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/dong749">Dong</a>
 */
public interface AppService extends IService<App> {

    /**
     * 根据 App 实体获取 AppVO。
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    /**
     * 获取查询包装器
     *
     * @param appQueryRequest 应用查询请求
     * @return 查询包装器
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);


    /**
     * 根据 App 列表获取 AppVO 列表。
     *
     * @param appList 应用实体列表
     * @return 应用视图对象列表
     */
    List<AppVO> getAppVOList(List<App> appList);
}
