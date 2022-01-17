package com.mservice.test.A;

import com.rabbitmq.client.AMQP;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Publisher extends AbstractVerticle {
  private RabbitMQClient client;
  private static final String QUEUE_NAME = "dev_hung-rabbit-test";
  private static final String QUEUE_NAME_REPLY = "dev_hung-rabbit-test-reply";
  public Publisher(RabbitMQClient client) {
    this.client = client;
  }
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Router router = Router.router(vertx);
//    Random generator = new Random();
//    int varRandom = generator.nextInt();
    Map<String, RoutingContext> mapReplyAPI = new HashMap<>();
    router.route().handler(BodyHandler.create());
    router.route().handler(CorsHandler.create("*").allowedMethod(HttpMethod.GET).allowedMethod(HttpMethod.POST));
    router.get("/test").handler(this::test);
    router.post("/operation").handler(ctx -> {
      String idRoute = ctx.toString().substring(ctx.toString().indexOf('@'));
      mapReplyAPI.put(idRoute, ctx);
      publishMessageToRabbitMQ(ctx, idRoute);
    });
    Map<String, AsyncResult<RabbitMQConsumer>> mapReply = new HashMap<>();
    client.basicConsumer(QUEUE_NAME_REPLY, rabbitMQConsumerAsyncResult -> {
      mapReply.put("reply", rabbitMQConsumerAsyncResult);
      if (mapReply.get("reply").succeeded()) {
        mapReply.get("reply").result().handler(message -> {
          mapReplyAPI.get(message.properties().getCorrelationId()).response()
            .end(Json.encodePrettily(message.body().toJson()));
        });
      }
      else {
        rabbitMQConsumerAsyncResult.cause().printStackTrace();
      }
    });
    vertx.createHttpServer().requestHandler(router).listen(8001, httpServerAsyncResult -> {
      if (httpServerAsyncResult.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server on port 8001");
      } else {
        startPromise.fail(httpServerAsyncResult.cause());
      }
    });
  }

  private void test(RoutingContext routingContext) {
    routingContext.response()
      .setStatusCode(200)
      .end(Json.encodePrettily("123123"));
  }

  private void publishMessageToRabbitMQ(RoutingContext routingContext, String idRoute ) {
    Buffer message = Buffer.buffer(routingContext.getBodyAsString());
    com.rabbitmq.client.BasicProperties properties = new AMQP.BasicProperties.Builder()
      .replyTo(QUEUE_NAME_REPLY)
      .correlationId(idRoute)
      .build();
    client.basicPublish("", QUEUE_NAME, properties ,message, pubResult -> {
        Map<String, AsyncResult<Void>> mapReply = new HashMap<>();
        mapReply.put("reply", pubResult);
        if (mapReply.get("reply").succeeded()) {
          System.out.println("Message " + message);
          System.out.println("Properties " + properties);
        } else {
          pubResult.cause().printStackTrace();
        }
    });
  }
}
