package com.dong.dongaicodegenerator.service;

import com.mybatisflex.core.service.IService;
import com.dong.dongaicodegenerator.model.entity.User;

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

}
