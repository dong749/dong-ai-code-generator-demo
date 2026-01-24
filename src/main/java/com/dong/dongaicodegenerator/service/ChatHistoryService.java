package com.dong.dongaicodegenerator.service;

import com.dong.dongaicodegenerator.model.dto.ChatHistoryQueryRequest;
import com.dong.dongaicodegenerator.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.dong.dongaicodegenerator.model.entity.ChatHistory;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/dong749">Dong</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加聊天记录
     *
     * @param appId          应用ID
     * @param messageContent 消息内容
     * @param messageType    消息类型
     * @return 是否添加成功
     */
    boolean addChatHistory(Long appId, String messageContent, String messageType, Long userId);

    /**
     * 根据应用 ID 删除聊天记录
     *
     * @param appId 应用ID
     * @return 是否删除成功
     */
    boolean deleteByAppId(Long appId);

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 分页获取应用聊天记录
     *
     * @param appId          应用ID
     * @param pageSize       每页大小
     * @param lastCreateTime 上次创建时间
     * @param loginUser     登录用户
     * @return 聊天记录分页
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);
}
