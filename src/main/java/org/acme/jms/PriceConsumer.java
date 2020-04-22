package org.acme.jms;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

/**
 * A bean consuming prices from the JMS queue.
 */
@ApplicationScoped
public class PriceConsumer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(PriceConsumer.class);

    @Inject
    ConnectionFactory connectionFactory;

    private final ExecutorService scheduler = Executors.newSingleThreadExecutor();

    private volatile String lastPrice;

    public String getLastPrice() {
        return lastPrice;
    }

    void onStart(@Observes StartupEvent ev) {
        LOG.info("On Start Begin");
        scheduler.submit(this);
        LOG.info("Submitted");
    }

    void onStop(@Observes ShutdownEvent ev) {
        scheduler.shutdown();
    }

    @Override
    public void run() {
        LOG.info("Running");
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            LOG.info("Context creation returned");

            JMSConsumer consumer = context.createConsumer(context.createQueue("prices"));
            LOG.info("Consumer creation returned");

            while (true) {
                Message message = consumer.receive();
                if (message == null) {
                    LOG.info("Receive returned null");
                    // receive returns `null` if the JMSConsumer is closed
                    return;
                }
                LOG.info("Receive returned message");
                lastPrice = message.getBody(String.class);
                LOG.info("Price = " + lastPrice);
            }
        } catch (JMSException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }
}
