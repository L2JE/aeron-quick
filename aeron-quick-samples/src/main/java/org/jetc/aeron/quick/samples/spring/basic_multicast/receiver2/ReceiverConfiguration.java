package org.jetc.aeron.quick.samples.spring.basic_multicast.receiver2;

import org.agrona.concurrent.SleepingMillisIdleStrategy;
import org.jetc.aeron.quick.AeronQuickFactory;
import org.jetc.aeron.quick.peers.receiver.AeronQuickReceiverRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ReceiverConfiguration {
    @Bean
    public AeronQuickFactory quickFactory(){
        return AeronQuickFactory.builder().removeAeronDirOnShutdown(true).build();
    }

    @Bean
    public AeronQuickReceiverRunner<ServiceWithSubscriberExample> serviceWithSubscriberRunner(AeronQuickFactory quickFactory, ServiceWithSubscriberExample targetComponent){
        return quickFactory.getReceiver(targetComponent, "serviceWithSubscriber2", c -> c.setAgentIdleStrategy(new SleepingMillisIdleStrategy(1000*4))).start();
    }
}
