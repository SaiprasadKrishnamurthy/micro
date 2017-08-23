package com.sai.rulebase.vertx;

import com.sai.rulebase.entity.RuleFlow;
import com.sai.rulebase.repository.TransactionalDataRepository;
import com.sai.rules.rulebase.RuleExecutionContext;
import com.sai.rules.rulebase.SpelUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * Created by saipkri on 18/08/17.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class ResponseBuilderVerticle extends AbstractVerticle {

    private final TransactionalDataRepository transactionalDataRepository;

    @Autowired
    public ResponseBuilderVerticle(final TransactionalDataRepository transactionalDataRepository) {
        this.transactionalDataRepository = transactionalDataRepository;
    }

    @Override
    public void start(final Future<Void> startFuture) throws Exception {
        super.start(startFuture);
        getVertx().eventBus().consumer(ResponseBuilderVerticle.class.getName(), this::exec);
    }

    private void exec(final Message<Object> msg) {
        String payload = msg.body().toString();
        String transactionId = payload.split("\\|")[0];

        RuleExecutionContext<?> ruleExecutionContext = transactionalDataRepository.contextFor(transactionId);
        RuleFlow ruleFlow = transactionalDataRepository.flowFor(transactionId);

        if (StringUtils.isNotBlank(ruleFlow.getPostExecutionCallback())) {
            Object response = SpelUtils.invoke(ruleExecutionContext, ruleFlow.getPostExecutionCallback());
            transactionalDataRepository.saveResponse(ruleExecutionContext, response);
        } else {
            transactionalDataRepository.saveResponse(ruleExecutionContext, ruleExecutionContext);
        }
        getVertx().eventBus().send("DONE|" + ruleExecutionContext.getId(), ""); // Don't need to pass anything around.
    }
}
