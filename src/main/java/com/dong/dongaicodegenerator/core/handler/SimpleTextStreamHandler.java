package com.dong.dongaicodegenerator.core.handler;

import com.dong.dongaicodegenerator.model.entity.User;
import com.dong.dongaicodegenerator.model.enums.ChatHistoryMessageTypeEnum;
import com.dong.dongaicodegenerator.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class SimpleTextStreamHandler {

    public Flux<String> simpleTextStreamHandler(Flux<String> originFlux
            , ChatHistoryService chatHistoryService
            , Long appId, User loginUser) {
        StringBuilder aiResponseBuilder = new StringBuilder();
        return originFlux.map(new Function<String, String>() {
            @Override
            public String apply(String s) {
                aiResponseBuilder.append(s);
                return s;
            }
        }).doOnComplete(new Runnable() {
            @Override
            public void run() {
                String aiResponseString = aiResponseBuilder.toString();
                chatHistoryService.addChatHistory(appId, aiResponseString
                        , ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
            }
        }).doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                String errorMessage = "AI 回复生成失败，错误信息：" + throwable.getMessage();
                chatHistoryService.addChatHistory(appId, errorMessage
                        , ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
            }
        });
    }
}
