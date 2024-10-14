package org.jetc.aeron.quick.samples.spring.basic_multicast.sender;

import org.jetc.aeron.quick.AeronQuickFactory;
import org.jetc.aeron.quick.annotations.AeronQuickSender;
import org.jetc.aeron.quick.messaging.publication.OfferingResultSideActions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SenderConfiguration {

    @Bean
    public AeronQuickFactory quickFactory(){
        return AeronQuickFactory.builder().removeAeronDirOnShutdown(true).build();
    }

    @Bean
    @AeronQuickSender
    public SenderContract aeronQuickPublisher(AeronQuickFactory quickFactory){
        return quickFactory.getSender(SenderContract.class, "aeronQuickPublisher", OfferingResultSideActions::logResultConfig);
    }
}
