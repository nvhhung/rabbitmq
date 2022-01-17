package com.mservice.test.B;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.rabbitmq.RabbitMQClient;

public class Publisher extends AbstractVerticle {
  public static final String QUEUE_NAME = "dev_hung-rabbit-test";
  private RabbitMQClient client;
  public Publisher(RabbitMQClient client) {
    this.client = client;
  }

  @Override
  public void start() throws Exception {
    final EventBus eventBus = vertx.eventBus();
    eventBus.consumer("2000", receivedMessage -> {
      Buffer replyQueue = Buffer.buffer(receivedMessage.body().toString());
      System.out.println("message " + replyQueue);
      client.basicPublish("", QUEUE_NAME, replyQueue, pubResult -> {
        if (pubResult.succeeded()) {
          System.out.println("Message published from B to A! " + replyQueue);
        } else {
          pubResult.cause().printStackTrace();
        }
      });
    });
  }
}
