package com.dong.dongaicodegenerator.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.dong.dongaicodegenerator.constant.UserConstant;
import com.dong.dongaicodegenerator.exception.BusinessException;
import com.dong.dongaicodegenerator.exception.ErrorCode;
import com.dong.dongaicodegenerator.exception.ThrowUtils;
import com.dong.dongaicodegenerator.model.dto.UserQueryRequest;
import com.dong.dongaicodegenerator.model.enums.UserRoleEnum;
import com.dong.dongaicodegenerator.model.vo.LoginUserVO;
import com.dong.dongaicodegenerator.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.dong.dongaicodegenerator.model.entity.User;
import com.dong.dongaicodegenerator.mapper.UserMapper;
import com.dong.dongaicodegenerator.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.dong.dongaicodegenerator.constant.UserConstant.USER_LOGIN_STATE;

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

    @Override
    public LoginUserVO loginUserVO(User user) {
        if (ObjectUtil.isNull(user)) {
            return null;
        }
        return BeanUtil.copyProperties(user, LoginUserVO.class);
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String password, HttpServletRequest request) {
        // 校验参数
        if (StrUtil.hasBlank(userAccount, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不应小于4位");
        }
        if (password.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度应该大于8位");
        }
        // 密码加密在与数据库中的密码进行校验
        String encryptPassword = getEncryptPassword(password);
        // 查数据库，用户是否存在
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq(User::getUserAccount, userAccount);
        queryWrapper.eq(User::getUserPassword, encryptPassword);
        User user = this.mapper.selectOneByQuery(queryWrapper);
        ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCode.NOT_FOUND_ERROR, "用户不存在或者密码错误");
        // 记录用户的登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.loginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 判断当前用户是否登录
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (ObjectUtil.isNull(user)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        Long userId = user.getId();
        user = this.getById(userId);
        ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        return user;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 判断当前用户是否登录
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (ObjectUtil.isNull(user)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未登录");
        }
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (ObjectUtil.isNull(user)) {
            return null;
        }
        return BeanUtil.copyProperties(user, UserVO.class);
    }

    @Override
    public List<UserVO> getUserVOList(List<User> users) {
        if (CollectionUtil.isEmpty(users)) {
            return new ArrayList<>();
        }
        return users.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userAccount = userQueryRequest.getUserAccount();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        return QueryWrapper.create()
                // 1. ID 只要不为 null 就拼接条件
                .eq(User::getId, id, id != null)
                // 2. 字符串类型，通常建议既不为 null 也不为空串 ("") 时才拼接
                .eq(User::getUserRole, userRole, StrUtil.isNotBlank(userRole))
                .like(User::getUserAccount, userAccount, StrUtil.isNotBlank(userAccount))
                .like(User::getUserName, userName, StrUtil.isNotBlank(userName))
                .like(User::getUserProfile, userProfile, StrUtil.isNotBlank(userProfile))
                // 3. 排序字段的处理
                // 只有当 sortField 有值时才进行排序
                .orderBy(sortField, "ascend".equals(sortOrder));
    }
}
