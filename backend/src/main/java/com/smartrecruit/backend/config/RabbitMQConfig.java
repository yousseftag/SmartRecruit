package com.smartrecruit.backend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  public static final String CV_QUEUE = "cv.processing.queue";
  public static final String CV_ROUTING_KEY = "cv.routing.key";

  public static final String OFFER_QUEUE = "offer.processing.queue";
  public static final String OFFER_ROUTING_KEY = "offer.routing.key";

  public static final String EXCHANGE = "ai.exchange";

  @Bean
  public Queue cvQueue() {
    return new Queue(CV_QUEUE, true); // durable
  }

  @Bean
  public Queue offerQueue() {
    return new Queue(OFFER_QUEUE, true); // durable
  }

  @Bean
  public DirectExchange exchange() {
    return new DirectExchange(EXCHANGE);
  }

  @Bean
  public Binding cvBinding(Queue cvQueue, DirectExchange exchange) {
    return BindingBuilder.bind(cvQueue).to(exchange).with(CV_ROUTING_KEY);
  }

  @Bean
  public Binding offerBinding(Queue offerQueue, DirectExchange exchange) {
    return BindingBuilder.bind(offerQueue).to(exchange).with(OFFER_ROUTING_KEY);
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
