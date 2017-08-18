package com.sai.rulebase.vertx;

import com.sai.rulebase.entity.Rule;
import com.sai.rulebase.model.DefaultRuleExecutor;
import com.sai.rulebase.model.RuleExecutor;
import com.sai.rulebase.repository.RuleFlowRepository;
import com.sai.rulebase.repository.RuleRepository;
import com.sai.rulebase.repository.TransactionalDataRepository;
import com.sai.rules.rulebase.RuleExecutionContext;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
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

    private final RuleRepository ruleRepository;
    private final TransactionalDataRepository transactionalDataRepository;
    private final RuleFlowRepository ruleFlowRepository;

    @Autowired
    public RuleExecutorVerticle(final RuleRepository ruleRepository, final TransactionalDataRepository transactionalDataRepository, final RuleFlowRepository ruleFlowRepository) {
        this.ruleRepository = ruleRepository;
        this.transactionalDataRepository = transactionalDataRepository;
        this.ruleFlowRepository = ruleFlowRepository;
    }

    @Override
    public void start(final Future<Void> startFuture) throws Exception {
        super.start(startFuture);
        getVertx().eventBus().consumer("EXEC", this::exec);
    }

    private void exec(final Message<Object> msg) {
        String payload = msg.body().toString();
        String transactionId = payload.split("\\|")[0];
        String ruleName = payload.split("\\|")[1];

        RuleExecutionContext<?> ruleExecutionContext = transactionalDataRepository.contextFor(transactionId);
        Rule rule = transactionalDataRepository.ruleFor(ruleName);
        RuleExecutor ruleExecutor = new DefaultRuleExecutor(rule);
        if (ruleExecutor.evaluate(ruleExecutionContext)) {
            ruleExecutor.execute(ruleExecutionContext);
        }
        // Fire the next rules in the pipeline.
        List<Rule> nextRules = transactionalDataRepository.nextRulesFor(transactionId, ruleName);
        if (nextRules.isEmpty()) {
            getVertx().eventBus().send("DONE|" + ruleExecutionContext.getId(), ""); // Don't need to pass anything around.
        } else {
            nextRules
                    .forEach(next -> vertx.eventBus().send("EXEC", transactionId + "|" + next.getName()));
        }
    }
}
