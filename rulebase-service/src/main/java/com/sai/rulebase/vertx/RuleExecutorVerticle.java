package com.sai.rulebase.vertx;

import com.sai.rulebase.entity.Rule;
import com.sai.rulebase.model.DefaultRuleExecutor;
import com.sai.rulebase.model.RuleExecutor;
import com.sai.rulebase.repository.TransactionalDataRepository;
import com.sai.rules.rulebase.RuleExecutionContext;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * Created by saipkri on 18/08/17.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class RuleExecutorVerticle extends AbstractVerticle {

    private final TransactionalDataRepository transactionalDataRepository;

    @Autowired
    public RuleExecutorVerticle(final TransactionalDataRepository transactionalDataRepository) {
        this.transactionalDataRepository = transactionalDataRepository;
    }

    @Override
    public void start(final Future<Void> startFuture) throws Exception {
        super.start(startFuture);
        getVertx().eventBus().consumer(RuleExecutorVerticle.class.getName(), this::exec);
    }

    private void exec(final Message<Object> msg) {
        String payload = msg.body().toString();
        String transactionId = payload.split("\\|")[0];
        String ruleName = payload.split("\\|")[1];

        System.out.println("\t\t " + ruleName);
        List<Rule> nextRules = transactionalDataRepository.nextRulesFor(transactionId, ruleName);

        boolean hasAllPredecessorsFinishedExecution = transactionalDataRepository.hasAllPredecessorsFinishedExecution(transactionId, ruleName);

        RuleExecutionContext<?> ruleExecutionContext = transactionalDataRepository.contextFor(transactionId);
        Rule rule = transactionalDataRepository.ruleFor(ruleName);
        RuleExecutor ruleExecutor = new DefaultRuleExecutor(rule, vertx);

        if (ruleExecutor.evaluate(ruleExecutionContext)) {
            ruleExecutor.execute(ruleExecutionContext);
        }
        // Fire the next rules in the pipeline.
        if (nextRules.isEmpty()) {
            getVertx().eventBus().send(ResponseBuilderVerticle.class.getName(), transactionId + "|" + ""); // Don't need to pass anything around.
        } else if (hasAllPredecessorsFinishedExecution) {
            nextRules
                    .forEach(next -> vertx.eventBus().send(RuleExecutorVerticle.class.getName(), transactionId + "|" + next.getName(),
                            new DeliveryOptions().setSendTimeout(next.getTimeoutSecs() * 1000),
                            result -> {
                                if (result.failed()) {
                                    System.out.println(" Timed out : "+next);
                                    ruleExecutionContext.getErroredRules().put(next.getName(), result.cause().toString());
                                }
                            }));
        } else {
            System.out.println("\t\t --- " + ruleName + "I'm still waiting, have more job to do ---- ");
        }
    }
}
