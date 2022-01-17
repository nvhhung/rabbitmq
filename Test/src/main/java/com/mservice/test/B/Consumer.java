package com.mservice.test.B;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mservice.test.Operation;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQMessage;

import java.util.HashMap;
import java.util.Map;

public class Consumer extends AbstractVerticle {
  private RabbitMQClient client;
  public static final String QUEUE_NAME = "dev_hung-rabbit-test";
  public Consumer(RabbitMQClient client) {
    this.client = client;
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    getMessage();
  }

  private void getMessage() {
    // Create a stream of messages from a queue
    client.basicConsumer(QUEUE_NAME, rabbitMQConsumerAsyncResult -> {
      if (rabbitMQConsumerAsyncResult.succeeded()) {
        System.out.println("RabbitMQ consumer created !");
        RabbitMQConsumer mqConsumer = rabbitMQConsumerAsyncResult.result();
        mqConsumer.handler(message -> {
          System.out.println("Got message: " + message.body().toString());
          System.out.println("replyQueue: " + message.properties().getReplyTo());
          handlingMessage(message);
        });
      } else {
        rabbitMQConsumerAsyncResult.cause().printStackTrace();
      }
    });
  }
  private void handlingMessage(RabbitMQMessage message) {
    System.out.println("Message " + message.body().toJson());
    Operation  operation = JSON.parseObject(message.body().toString(), Operation.class);
    Integer result = operation.getLeftExpr() + operation.getRightExpr();
    Buffer replyQueueMsg = Buffer.buffer(result.toString());
    client.basicPublish("", message.properties().getReplyTo(),message.properties(), replyQueueMsg, pubResult -> {
      if (pubResult.succeeded()) {
        System.out.println("Message published from B to A: " + replyQueueMsg);
        System.out.println("Id route: " + message.properties().getCorrelationId());
      } else {
        pubResult.cause().printStackTrace();
      }
    });
  }
}
