package com.dong.dongaicodegenerator.service;

import cn.hutool.http.server.HttpServerRequest;
import com.dong.dongaicodegenerator.model.dto.UserQueryRequest;
import com.dong.dongaicodegenerator.model.vo.LoginUserVO;
import com.dong.dongaicodegenerator.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.dong.dongaicodegenerator.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com/dong749">Dong</a>
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 加密
     * @param password
     * @return
     */
    String getEncryptPassword(String password);


    /**
     * 获取脱敏的已经登陆的用户信息
     *
     * @param user
     * @return
     */
    LoginUserVO loginUserVO(User user);


    /**
     * 用户登录
     *
     * @param userAccount
     * @param password
     * @param request
     * @return
     */
    LoginUserVO userLogin(String userAccount, String password, HttpServletRequest request);


    /**
     * 获取当前登录的用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 用户退出登录
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏后的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 分页查询获取脱敏后的用户列表
     *
     * @param users
     * @return
     */
    List<UserVO> getUserVOList(List<User> users);

    /**
     * 根据查询条件构造数据查询的 QueryWrapper 对象
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);
}
