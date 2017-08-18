package com.sai.rulebase.vertx;

import com.sai.rulebase.config.RulebaseConfig;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.spi.VerticleFactory;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by saipkri on 14/04/17.
 */
@Component
@Configuration
@Data
public class Bootstrap {

    @Autowired
    private final ApplicationContext applicationContext;

    @Autowired
    private RulebaseConfig rulebaseConfig;

    private Vertx vertx;

    @PostConstruct
    public void onstartup() throws Exception {
        vertx = Vertx.vertx();
        VerticleFactory verticleFactory = applicationContext.getBean(SpringVerticleFactory.class);
        vertx.registerVerticleFactory(verticleFactory);
        vertx.deployVerticle(verticleFactory.prefix() + ":" + RuleExecutorVerticle.class.getName(), new DeploymentOptions().setInstances(rulebaseConfig.getRuleExecutorInstances()));
    }
}
