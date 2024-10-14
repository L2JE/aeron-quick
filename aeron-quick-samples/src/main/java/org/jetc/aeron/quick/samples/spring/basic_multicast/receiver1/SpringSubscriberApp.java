package org.jetc.aeron.quick.samples.spring.basic_multicast.receiver1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringSubscriberApp {
    private static final Logger log = LoggerFactory.getLogger(SpringSubscriberApp.class);

    public static void main(String[] args) {
        setMockSysProps();
        SpringApplication.run(SpringSubscriberApp.class, args);
    }

    private static void setMockSysProps() {
        System.setProperty("aeron.quick.serviceWithSubscriber1.actionToTrigger.channel", "aeron:udp?endpoint=localhost:12001|control-mode=dynamic|control=localhost:13000");
        System.setProperty("aeron.quick.serviceWithSubscriber1.actionToTrigger.stream", "101");
        System.setProperty("server.port", "8082");
    }
}
