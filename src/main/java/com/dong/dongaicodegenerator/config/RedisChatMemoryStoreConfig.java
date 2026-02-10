package com.dong.dongaicodegenerator.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedisChatMemoryStoreConfig {

        /**
        * Redis 主机地址
        */
        private String host;

        /**
        * Redis 端口号
        */
        private int port;

        /**
        * Redis 密码（如果有）
        */
        private String password;


        /**
        * Redis 连接超时时间（毫秒）
        */
        private long ttl;

        @Bean
        public RedisChatMemoryStore redisChatMemoryStore() {
            return RedisChatMemoryStore.builder()
                    .host(host)
                    .port(port)
                    .user("default") // RedisChatMemoryStore 需要一个用户标识，可以根据实际情况调整
                    .password(password)
                    .ttl(ttl)
                    .build();
        }
}
