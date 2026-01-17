package com.dong.dongaicodegenerator.service.impl;

import cn.hutool.core.util.StrUtil;
import com.dong.dongaicodegenerator.exception.BusinessException;
import com.dong.dongaicodegenerator.exception.ErrorCode;
import com.dong.dongaicodegenerator.model.enums.UserRoleEnum;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.dong.dongaicodegenerator.model.entity.User;
import com.dong.dongaicodegenerator.mapper.UserMapper;
import com.dong.dongaicodegenerator.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/dong749">Dong</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService {

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 检验参数
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不应小于4位");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度应该大于8位");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入密码不一致");
        }
        // 查询用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(User::getUserAccount, userAccount);
        long count = this.mapper.selectCountByQuery(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户已注册");
        }
        // 密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 创建用户
        User user = new User();
        String randomName = UUID.randomUUID().toString();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName(randomName);
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
         if (!saveResult) {
             throw new BusinessException(ErrorCode.OPERATION_ERROR, "注册失败");
         }
        return user.getId();
    }

    @Override
    public String getEncryptPassword(String password) {
        final String SALT = "dong";
        return DigestUtils.md5DigestAsHex((password + SALT).getBytes(StandardCharsets.UTF_8));
    }
}
