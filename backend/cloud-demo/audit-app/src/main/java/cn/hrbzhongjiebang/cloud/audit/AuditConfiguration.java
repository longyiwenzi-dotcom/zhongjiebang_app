package cn.hrbzhongjiebang.cloud.audit;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditConfiguration {
    static final String EXCHANGE = "zjb.house.events";
    static final String QUEUE = "zjb.audit.house-viewed";
    @Bean TopicExchange houseEventsExchange() { return new TopicExchange(EXCHANGE, true, false); }
    @Bean Queue houseViewedQueue() { return QueueBuilder.durable(QUEUE).build(); }
    @Bean Binding houseViewedBinding(Queue queue, TopicExchange exchange) { return BindingBuilder.bind(queue).to(exchange).with("house.viewed"); }
    @Bean Jackson2JsonMessageConverter rabbitJsonConverter() { return new Jackson2JsonMessageConverter(); }
}
