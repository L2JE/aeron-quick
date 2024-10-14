package org.jetc.aeron.quick.annotations.processors;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.jetc.aeron.quick.annotations.processors.aeron_adapter.AdapterConfiguration;
import org.jetc.aeron.quick.annotations.testing.JavacTest;
import org.junit.jupiter.api.Test;
import javax.tools.StandardLocation;
import java.io.IOException;

class AeronQuickSenderProcessorTest extends JavacTest {

    @Override
    protected String getTestName() {
        return "AeronQuickSenderProcessorTest";
    }

    @Test
    void generates_adapter_primitive_params_only_adapt_only_method_marked_with_QuickContractMethod() throws IOException {
        String resultClassName = "AeronGeneralServiceContract" + AdapterConfiguration.SENDER_SUFFIX;
        Compilation compilation = withAeronQuickInDefaultClasspath(Compiler.javac())
                .withProcessors(new AeronQuickSenderProcessor())
                .compile(
                        JavaFileObjects.forSourceString("org.jetc.aeron.quick.samples.annotation_receiver_and_sender.ClassWithSender", """
                                        package org.jetc.aeron.quick.samples.annotation_receiver_and_sender;
                                        import org.jetc.aeron.quick.AeronQuickFactory;
                                        import org.jetc.aeron.quick.annotations.AeronQuickSender;
                                        import org.jetc.aeron.quick.annotations.AeronQuickContract;
                                        public class ClassWithSender {
                                            private static final AeronQuickFactory factory = AeronQuickFactory.builder().build();
                                            @AeronQuickSender
                                            private AeronGeneralServiceContract senderInstanceField;
                                        }
                                        @AeronQuickContract
                                        interface AeronGeneralServiceContract {
                                            long duplicateNumber(long target);
                                        }
                                """)
                );

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation).generatedFile(StandardLocation.SOURCE_OUTPUT, "org/jetc/aeron/quick/samples/annotation_receiver_and_sender/" + resultClassName +".java");
        CompilationSubject.assertThat(compilation).generatedFile(StandardLocation.CLASS_OUTPUT, "org/jetc/aeron/quick/samples/annotation_receiver_and_sender/" + resultClassName + ".class");
        assertFileContentEquals(compilation, resultClassName + ".java",
        """
                package org.jetc.aeron.quick.samples.annotation_receiver_and_sender;
                import io.aeron.Publication;
                import org.agrona.BitUtil;
                import org.agrona.BufferUtil;
                import org.agrona.ExpandableDirectByteBuffer;
                import org.agrona.MutableDirectBuffer;
                import org.agrona.concurrent.UnsafeBuffer;
                import org.jetc.aeron.quick.AeronQuickContext;
                import org.jetc.aeron.quick.messaging.publication.PublicationOfferingStrategy;
                import org.jetc.aeron.quick.messaging.serialization.ObjectStringMapper;
                import org.jetc.aeron.quick.peers.sender.SenderConfiguration;
                import java.nio.ByteOrder;
                        
                public class AeronGeneralServiceContract_SAdapter implements org.jetc.aeron.quick.peers.sender.SenderAdapter<AeronGeneralServiceContract>, AeronGeneralServiceContract{
                    private ObjectStringMapper mapper;
                    private PublicationOfferingStrategy offeringStrategy;
                    private final Publication[] publications = new Publication[1];
                    private final MutableDirectBuffer[] buffers = new MutableDirectBuffer[]{
                        new UnsafeBuffer(BufferUtil.allocateDirectAligned(256, 64))
                    };
                    @Override
                    public void configure(SenderConfiguration config) {
                        final AeronQuickContext ctx = config.getContext();
                        String senderName = config.getComponentName();
                        mapper = ctx.getObjectMapper();
                        offeringStrategy = config.getOfferingStrategy();
                        final String[] methods = new String[]{"duplicateNumber"};
                        for(int i = 0; i < publications.length; i++){
                            publications[i] = ctx.getAeron().addExclusivePublication(
                                ctx.getProperty(senderName, methods[i], "channel"),
                                ctx.getIntProperty(senderName, methods[i], "stream")
                            );
                        }
                        
                    }
                    @Override
                    public AeronGeneralServiceContract getAdapted() { return this; }
                    @Override
                    public long duplicateNumber(long target){
                        MutableDirectBuffer buffer = buffers[0];
                        buffer.putLong(0, target, ByteOrder.LITTLE_ENDIAN);
                        offeringStrategy.offerMessage(publications[0], buffer, 0, 0 + BitUtil.SIZE_OF_LONG);
                        return 0;
                    }
                        
                }""");
    }

}