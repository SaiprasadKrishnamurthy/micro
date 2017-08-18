package com.sai.rulebase.scratchpad;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.UUID;

/**
 * Created by saipkri on 18/08/17.
 */
public class Ver {

    public static class RuleExecutor extends AbstractVerticle {

        public int x;

        @Override
        public void start(final Future<Void> startFuture) throws Exception {
            super.start(startFuture);
            getVertx().eventBus().consumer("process", msg -> System.out.println("Rule processing: " + this + " -- " + (x++) + " --- " + msg.body()));
        }
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(RuleExecutor.class.getName(), new DeploymentOptions().setInstances(2).setWorker(true));

        vertx.setPeriodic(1000, l -> vertx.eventBus().send("process", UUID.randomUUID() + "|" + l));


    }
}
