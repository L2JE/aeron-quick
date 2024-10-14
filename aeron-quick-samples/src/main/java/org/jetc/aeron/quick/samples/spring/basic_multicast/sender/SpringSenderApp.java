package org.jetc.aeron.quick.samples.spring.basic_multicast.sender;

import org.jetc.aeron.quick.samples.basic_multicast.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;

@SpringBootApplication
public class SpringSenderApp {
    private static final Logger log = LoggerFactory.getLogger(SpringSenderApp.class);

    public static void main(String[] args) {
        setMockSysProps();
        SpringApplication.run(SpringSenderApp.class, args);
    }

    private static void setMockSysProps() {
        System.setProperty("aeron.quick.aeronQuickPublisher.userAdded.channel", "aeron:udp?control-mode=dynamic|control=localhost:13000");
        System.setProperty("aeron.quick.aeronQuickPublisher.userAdded.stream", "101");
        System.setProperty("server.port", "8081");
    }

    @Bean
    public CommandLineRunner startupCmdRunner(SenderContract aeronQuickPublisher) {
        return args -> {
            final int MAX_MESSAGES = 10000;
            final Duration idleTime = Duration.ofSeconds(2);
            for (int i = 0; i < MAX_MESSAGES; i++) {
                log.warn("Publishing user %s ".formatted(i));
                aeronQuickPublisher.userAdded(
                        new User(i, "user-" + i, new Date(1990, Calendar.NOVEMBER, i + 1)),
                        LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
                );

                Thread.sleep(idleTime);
            }
        };
    }
}
