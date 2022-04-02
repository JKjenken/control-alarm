package com.example.controlalarm.websocket.config;

import com.example.controlalarm.websocket.mq.MessageReceiver;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DigestUtils;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * 缓存配置-使用Lettuce客户端，自动注入配置的方式
 * @author linjiankai
 */
@Configuration
@EnableCaching //启用缓存
public class StringRedisConfig implements CachingConfigurer {


    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;
    @Value("${spring.redis.password}")
    private String password;
    @Value("${spring.redis.lettuce.pool.max-wait}")
    private int maxWait;
    @Value("${spring.redis.lettuce.pool.max-idle}")
    private int maxIdle;
    @Value("${spring.redis.lettuce.pool.max-active}")
    private int maxActive;
    @Value("${spring.redis.timeout}")
    private long timeout;
    @Value("${spring.redis.message.ship-alarm-user}")
    private String alarmUser;
    @Value("${spring.redis.message.ship-alarm-group}")
    private String alarmGroup;
    @Value("${spring.redis.message.ship-alarm-all}")
    private String alarmAll;

//    @Value("${spring.redis.cluster.nodes}")
//    private String nodes;
//    @Value("${spring.redis.cluster.password}")
//    private String clusterPassword;
//    @Value("${spring.redis.cluster.max-redirects}")
//    private int maxRedirects;

    /**
     * redis 集群配置
     * @return
     */
//    @Bean
//    public RedisClusterConfiguration redisClusterConfiguration(){
//        RedisClusterConfiguration configuration = new RedisClusterConfiguration(Arrays.asList(nodes));
//        configuration.setMaxRedirects(maxRedirects);
//        configuration.setPassword(RedisPassword.of(clusterPassword));
//
//        return configuration;
//    }

    /**
     * 缓存配置管理器
     */
//    @Bean
//    public CacheManager cacheManager() {
//        //以锁写入的方式创建RedisCacheWriter对象
//        RedisCacheWriter writer = RedisCacheWriter.lockingRedisCacheWriter(factory);
//        /*
//        设置CacheManager的Value序列化方式为JdkSerializationRedisSerializer,
//        但其实RedisCacheConfiguration默认就是使用
//        StringRedisSerializer序列化key，
//        JdkSerializationRedisSerializer序列化value,
//        所以以下注释代码就是默认实现，没必要写，直接注释掉
//         */
//        // RedisSerializationContext.SerializationPair pair = RedisSerializationContext.SerializationPair.fromSerializer(new JdkSerializationRedisSerializer(this.getClass().getClassLoader()));
//        // RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith(pair);
//        //创建默认缓存配置对象
//        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
//        RedisCacheManager cacheManager = new RedisCacheManager(writer, config);
//        return cacheManager;
//    }

    /**
     * redis每个库的操作助手
     *
     */
    @Bean("redisTemplate")
    public RedisTemplate<String, String> redisTemplate() {
        return getTemplate(createLettuceConnectionFactory(RedisDbIndex.INDEX_0.value));
    }

    /**
     * 使用Jackson序列化对象
     */
    @Bean
    public Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer(){
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(objectMapper);

        return serializer;
    }

    /**
     * 消息监听器(监听单对单发送消息)
     */
    @Bean
    MessageListenerAdapter messageListenerAdapter(MessageReceiver messageReceiver, Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer){
        //消息接收者以及对应的默认处理方法
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(messageReceiver, "receiveMessage");
        //消息的反序列化方式
        messageListenerAdapter.setSerializer(jackson2JsonRedisSerializer);

        return messageListenerAdapter;
    }

    /**
     * 消息监听器(监听分组发送消息)
     */
    @Bean
    MessageListenerAdapter messageListenerAdapter2(MessageReceiver messageReceiver, Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer){
        //消息接收者以及对应的默认处理方法
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(messageReceiver, "receiveGroupMessage");
        //消息的反序列化方式
        messageListenerAdapter.setSerializer(jackson2JsonRedisSerializer);

        return messageListenerAdapter;
    }

    /**
     * 消息监听器(监听向所有用户发送消息)
     */
    @Bean
    MessageListenerAdapter messageListenerAdapter3(MessageReceiver messageReceiver, Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer){
        //消息接收者以及对应的默认处理方法
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(messageReceiver, "receiveAllMessage");
        //消息的反序列化方式
        messageListenerAdapter.setSerializer(jackson2JsonRedisSerializer);

        return messageListenerAdapter;
    }

    /**
     * 消息监听容器
     */
    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory
            , MessageListenerAdapter messageListenerAdapter
            , MessageListenerAdapter messageListenerAdapter2
            , MessageListenerAdapter messageListenerAdapter3){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        //添加消息监听器(订阅单对单发送消息的通道)
        container.addMessageListener(messageListenerAdapter, new PatternTopic(alarmUser));
        //添加消息监听器(订阅分组发送消息的通道)
        container.addMessageListener(messageListenerAdapter2, new PatternTopic(alarmGroup));
        //添加消息监听器(订阅向所有用户发送消息的通道)
        container.addMessageListener(messageListenerAdapter3, new PatternTopic(alarmAll));

        return container;
    }

    private RedisTemplate<String, String> getTemplate(LettuceConnectionFactory factory) {
        //创建Redis缓存操作助手RedisTemplate对象
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(factory);
        //以下代码为将RedisTemplate的Value序列化方式由JdkSerializationRedisSerializer更换为Jackson2JsonRedisSerializer
        //此种序列化方式结果清晰、容易阅读、存储字节少、速度快，所以推荐更换
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        //StringRedisTemplate是RedisTempLate<String, String>的子类
        return template;
    }

    private LettuceConnectionFactory createLettuceConnectionFactory(int dbIndex){

        //redis配置
        RedisStandaloneConfiguration redisConfiguration = new
                RedisStandaloneConfiguration(host,port);
        redisConfiguration.setDatabase(dbIndex);
        redisConfiguration.setPassword(RedisPassword.of(password));

        //连接池配置
        GenericObjectPoolConfig genericObjectPoolConfig =
                new GenericObjectPoolConfig();
        genericObjectPoolConfig.setMaxIdle(maxIdle);
        genericObjectPoolConfig.setMinIdle(0);
        genericObjectPoolConfig.setMaxTotal(maxActive);

        //redis客户端配置
        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder
                builder =  LettucePoolingClientConfiguration.builder().
                commandTimeout(Duration.ofMillis(timeout));

        builder.shutdownTimeout(Duration.ofMillis(timeout));
        builder.poolConfig(genericObjectPoolConfig);
        LettuceClientConfiguration lettuceClientConfiguration = builder.build();

        //根据配置和客户端配置创建连接
        LettuceConnectionFactory lettuceConnectionFactory = new
                LettuceConnectionFactory(redisConfiguration,lettuceClientConfiguration);
        lettuceConnectionFactory .afterPropertiesSet();

        return lettuceConnectionFactory;
    }

}
