package org.jetc.aeron.quick.annotations.processors;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.jetc.aeron.quick.annotations.testing.JavacTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import javax.tools.StandardLocation;
import java.io.IOException;

class AeronQuickReceiverProcessorTest extends JavacTest {

    @Override
    protected String getTestName() {
        return "AeronQuickReceiverProcessorTest";
    }

    @Test
    void generates_adapter_class_for_receiver_with_primitive_parameters() throws IOException {
        String targetClassName = "AeronQuickGeneralServiceServer";
        Compilation compilation = withAeronQuickInDefaultClasspath(Compiler.javac())
                .withProcessors(new AeronQuickReceiverProcessor())
                .compile(
                        JavaFileObjects.forSourceString("org.jetc.aeron.quick.samples.general.AeronQuickGeneralServiceServer", """
                                package org.jetc.aeron.quick.samples.general;
                                import java.lang.annotation.ElementType;
                                import java.lang.annotation.Retention;
                                import java.lang.annotation.RetentionPolicy;
                                import java.lang.annotation.Target;
                                import org.jetc.aeron.quick.annotations.AeronQuickReceiver;
                                import org.jetc.aeron.quick.annotations.QuickContractEndpoint;
                        
                                @AeronQuickReceiver(name = "exampleReceiver")
                                class AeronQuickGeneralServiceServer implements AeronGeneralServiceContract {
                                    @Override
                                    public void notifyOperationDone(char extraData, int param2){}
                                    @Override
                                    public long nonAdaptedMethod(){return 0;}
                                }
                                interface AeronGeneralServiceContract {
                                    @QuickContractEndpoint
                                    void notifyOperationDone(char extraData, int param2);
                                    long nonAdaptedMethod();
                                }
                        """)
                );

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation).generatedFile(StandardLocation.SOURCE_OUTPUT,"org/jetc/aeron/quick/samples/general/"+targetClassName+"_Adapter.java");
        CompilationSubject.assertThat(compilation).generatedFile(StandardLocation.CLASS_OUTPUT,"org/jetc/aeron/quick/samples/general/"+targetClassName+"_Adapter.class");
        Assertions.assertEquals(
    """
            package org.jetc.aeron.quick.samples.general;
            import org.jetc.aeron.quick.server.precompile.ReceiverAdapterBase;
            import org.jetc.aeron.quick.messaging.ReceiverBindingProvider;
            import org.jetc.aeron.quick.messaging.fragment_handling.ContextualHandler;
            import org.jetc.aeron.quick.messaging.subscription.SubscriptionMeta;
            import com.fasterxml.jackson.core.JsonProcessingException;
            import com.fasterxml.jackson.databind.ObjectMapper;
            import io.aeron.Publication;
            import io.aeron.logbuffer.Header;
            import org.agrona.DirectBuffer;
            import org.agrona.ExpandableDirectByteBuffer;
            import org.agrona.MutableDirectBuffer;
            import org.agrona.BitUtil;
            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;
            import java.nio.ByteOrder;
            import java.util.HashMap;
            import java.util.List;
            import java.util.Map;
                                    
            class AeronQuickGeneralServiceServer_Adapter implements ReceiverAdapterBase<AeronQuickGeneralServiceServer>{
                private static final Logger log = LoggerFactory.getLogger(AeronQuickGeneralServiceServer_Adapter.class);
                private static final String PROPS_SUFFIX = "aeron.quick.exampleReceiver.";
                private record Binding(String methodName, int fragmentLimit, ContextualHandler handler){}
                private final List<Binding> bindingsToCompute;
                                    
                private static String getPropForMethod(String method, String prop){
                        String value = System.getProperty(PROPS_SUFFIX + method + "." + prop);
                        if(value == null || value.isBlank())
                            value = System.getProperty(PROPS_SUFFIX + prop);
                        return value;
                }
                                    
                @Override
                public ReceiverBindingProvider getBindings() {
                    ReceiverBindingProvider computedBindings = new ReceiverBindingProvider(new HashMap<>());
                                    
                    for (Binding binding : this.bindingsToCompute){
                        boolean isRepeatedBinding = computedBindings.setBinding(
                                getPropForMethod(binding.methodName() ,"channel"),
                                Integer.parseInt(getPropForMethod(binding.methodName(), "stream")),
                                new SubscriptionMeta(binding.handler(), binding.fragmentLimit())
                        ) != null;
                                    
                        if(isRepeatedBinding)
                            throw new IllegalStateException("Only unique channel-stream pairs are allowed. Check properties for: %s".formatted(PROPS_SUFFIX + binding.methodName()));
                    }
                                    
                    return computedBindings;
                }
                public AeronQuickGeneralServiceServer_Adapter(AeronQuickGeneralServiceServer server){
                    MutableDirectBuffer rspBuffer = new ExpandableDirectByteBuffer(256);
                    bindingsToCompute = List.of(
                        new Binding(
                            "notifyOperationDone",
                            3,
                            aeron -> (DirectBuffer buffer, int offset, int length, Header header) -> {
                                server.notifyOperationDone(
                                    buffer.getChar(offset, ByteOrder.LITTLE_ENDIAN),
                                    buffer.getInt(offset + BitUtil.SIZE_OF_CHAR, ByteOrder.LITTLE_ENDIAN)
                                );
                            }
                        )
                                    
                    );
                }
            }""",
            compilation.generatedFiles().getLast().getCharContent(false).toString()
        );
    }
}