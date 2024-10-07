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
                                            @AeronQuickSender(name = "senderExample")
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
    }

}