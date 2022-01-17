package com.mservice.test.B;

import com.mservice.test.B.Publisher;
import io.vertx.core.Vertx;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;

public class DeployB {
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
        vertx.deployVerticle(new Consumer(client));
        System.out.println("RabbitMQ successfully connected!");
      } else {
        System.out.println("Fail to connect to RabbitMQ " + asyncResult.cause().getMessage());
      }
    });
  }
}
