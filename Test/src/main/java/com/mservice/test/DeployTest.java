package com.mservice.test;

import io.vertx.core.Vertx;

public class DeployTest {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new MainVerticle());
  }
}
