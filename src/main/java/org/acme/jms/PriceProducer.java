package org.acme.jms;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

/**
 * A bean producing random prices every 5 seconds and sending them to the prices JMS queue.
 */
@ApplicationScoped
public class PriceProducer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(PriceProducer.class);

    @Inject
    ConnectionFactory connectionFactory;

    private final Random random = new Random();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    void onStart(@Observes StartupEvent ev) {
        LOG.info("On Start Begin");
        scheduler.scheduleWithFixedDelay(this, 0L, 5L, TimeUnit.SECONDS);
        LOG.info("Scheduled");
    }

    void onStop(@Observes ShutdownEvent ev) {
        scheduler.shutdown();
    }

    @Override
    public void run() {
        LOG.info("Running");
        try {
            try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
                LOG.info("Context creation returned");
                JMSProducer producer = context.createProducer();
                LOG.info("Producer creation returned");
                producer.send(context.createQueue("prices"), Integer.toString(random.nextInt(100)));
                LOG.info("Send returned");
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
        LOG.info("Context closed");
    }
}
