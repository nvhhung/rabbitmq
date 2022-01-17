package com.mservice.test.A;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;

public class DeployA {
  private static final String QUEUE_NAME_REPLY = "dev_hung-rabbit-test-reply";
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    RabbitMQOptions config = new RabbitMQOptions();
// Each parameter is optional
// The default parameter with be used if the parameter is not set
    config.setUser("test");
    config.setPassword("test");
    config.setHost("172.16.9.166");
    config.setPort(5672);
    RabbitMQClient client = RabbitMQClient.create(vertx, config);
// Connect
    client.start(asyncResult -> {
      if (asyncResult.succeeded()) {
        vertx.deployVerticle(new Publisher(client));
        System.out.println("RabbitMQ successfully connected!");
        createQueueReply(client);
      } else {
        System.out.println("Fail to connect to RabbitMQ " + asyncResult.cause().getMessage());
      }
    });
  }

  private static void createQueueReply(RabbitMQClient client) {
    JsonObject configQueueReply = new JsonObject();
    configQueueReply.put("x-message-ttl", 10_000L);
// para thứ 3 đại diện cho queue auto-delete vì thế tải phải truyền cho nó true, false đại diện cho long to live
    client.queueDeclare(QUEUE_NAME_REPLY, true, true, true, configQueueReply, queueResult -> {
      if (queueResult.succeeded()) {
        System.out.println("Queue reply declared!");
      } else {
        System.err.println("Queue failed to be declared!");
        queueResult.cause().printStackTrace();
      }
    });

  }

}
