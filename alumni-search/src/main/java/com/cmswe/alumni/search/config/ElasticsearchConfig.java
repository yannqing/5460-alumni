package com.cmswe.alumni.search.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.time.Duration;

/**
 * Elasticsearch 配置类
 *
 * @author CNI Alumni System
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.cmswe.alumni.search.repository")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUris;

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    @Value("${spring.elasticsearch.connection-timeout:10s}")
    private Duration connectionTimeout;

    @Value("${spring.elasticsearch.socket-timeout:30s}")
    private Duration socketTimeout;

    @Override
    public ClientConfiguration clientConfiguration() {
        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder = ClientConfiguration.builder()
                .connectedTo(elasticsearchUris.replace("http://", ""));

        // 如果配置了用户名和密码，则添加基本认证
        if (username != null && !username.isEmpty()) {
            return builder.withBasicAuth(username, password)
                    .withConnectTimeout(connectionTimeout)
                    .withSocketTimeout(socketTimeout)
                    .build();
        }

        return builder.withConnectTimeout(connectionTimeout)
                .withSocketTimeout(socketTimeout)
                .build();
    }
}
