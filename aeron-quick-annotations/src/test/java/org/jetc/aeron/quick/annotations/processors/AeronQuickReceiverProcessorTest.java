package org.jetc.aeron.quick.annotations.processors;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.jetc.aeron.quick.annotations.testing.JavacTest;
import org.junit.jupiter.api.Test;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;

import static org.jetc.aeron.quick.annotations.processors.aeron_adapter.AdapterConfiguration.RECEIVER_SUFFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AeronQuickReceiverProcessorTest extends JavacTest {

    @Override
    protected String getTestName() {
        return "AeronQuickReceiverProcessorTest";
    }

    @Test
    void generates_adapter_primitive_params_only_adapt_only_method_marked_with_QuickContractMethod() throws IOException {
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
                                import org.jetc.aeron.quick.annotations.AeronQuickContractEndpoint;
                        
                                @AeronQuickReceiver
                                class AeronQuickGeneralServiceServer implements AeronGeneralServiceContract {
                                    @Override
                                    public void notifyOperationDone(char extraData, int param2){}
                                    @Override
                                    public long nonAdaptedMethod(){return 0;}
                                }
                                interface AeronGeneralServiceContract {
                                    @AeronQuickContractEndpoint
                                    void notifyOperationDone(char extraData, int param2);
                                    long nonAdaptedMethod();
                                }
                        """)
                );

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation).generatedFile(StandardLocation.SOURCE_OUTPUT,"org/jetc/aeron/quick/samples/general/"+targetClassName+RECEIVER_SUFFIX+".java");
        CompilationSubject.assertThat(compilation).generatedFile(StandardLocation.CLASS_OUTPUT,"org/jetc/aeron/quick/samples/general/"+targetClassName+RECEIVER_SUFFIX+".class");
        assertFileContentEquals(compilation, RECEIVER_SUFFIX + ".java", """
                package org.jetc.aeron.quick.samples.general;
                import org.agrona.BitUtil;
                import org.jetc.aeron.quick.AeronQuickContext;
                import org.jetc.aeron.quick.messaging.BindingAppender;
                import org.jetc.aeron.quick.messaging.serialization.ObjectStringMapper;
                import java.nio.ByteOrder;
                import org.jetc.aeron.quick.messaging.subscription.SubscriptionMeta;
                        
                public class AeronQuickGeneralServiceServer_RAdapter extends org.jetc.aeron.quick.peers.receiver.SequentialAdapter<AeronQuickGeneralServiceServer>{                    
                    @Override
                    protected void registerBindings(BindingAppender<SubscriptionMeta> bindings, AeronQuickContext ctx, ObjectStringMapper mapper, String receiverName, AeronQuickGeneralServiceServer server) {
                    
                        bindings.addBinding(
                            ctx.getProperty(receiverName, "notifyOperationDone", "channel"),
                            ctx.getIntProperty(receiverName, "notifyOperationDone", "stream"),
                            new SubscriptionMeta(
                                (buffer, offset, length, header) -> {
                                    char param0 = buffer.getChar(offset, ByteOrder.LITTLE_ENDIAN);
                                    int param1 = buffer.getInt(offset + BitUtil.SIZE_OF_CHAR, ByteOrder.LITTLE_ENDIAN);
                                    server.notifyOperationDone(param0, param1);
                                }, 3
                            )
                        );
                    }
                }""");
    }

    @Test
    void generates_adapter_primitive_params_only_adapt_only_method_marked_with_QuickContractMethod_concurrent() {
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
                                        import org.jetc.aeron.quick.annotations.AeronQuickContractEndpoint;
                                                        
                                        @AeronQuickReceiver(concurrent = true)
                                        class AeronQuickGeneralServiceServer implements AeronGeneralServiceContract {
                                            @Override
                                            public void notifyOperationDone(char extraData, int param2){}
                                            @Override
                                            public long nonAdaptedMethod(){return 0;}
                                        }
                                        interface AeronGeneralServiceContract {
                                            @AeronQuickContractEndpoint
                                            void notifyOperationDone(char extraData, int param2);
                                            long nonAdaptedMethod();
                                        }
                                """)
                );

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation).generatedFile(StandardLocation.SOURCE_OUTPUT, "org/jetc/aeron/quick/samples/general/" + targetClassName + RECEIVER_SUFFIX + ".java");
        CompilationSubject.assertThat(compilation).generatedFile(StandardLocation.CLASS_OUTPUT, "org/jetc/aeron/quick/samples/general/" + targetClassName + RECEIVER_SUFFIX + ".class");
        assertFileContentEquals(compilation, RECEIVER_SUFFIX + ".java", """
                package org.jetc.aeron.quick.samples.general;
                import org.agrona.BitUtil;
                import org.jetc.aeron.quick.AeronQuickContext;
                import org.jetc.aeron.quick.messaging.BindingAppender;
                import org.jetc.aeron.quick.messaging.serialization.ObjectStringMapper;
                import java.nio.ByteOrder;
                import org.jetc.aeron.quick.messaging.subscription.ConcurrentSubscriptionMeta;
                import org.jetc.aeron.quick.utils.events.MethodExecutor;
                        
                public class AeronQuickGeneralServiceServer_RAdapter extends org.jetc.aeron.quick.peers.receiver.ConcurrentAdapter<AeronQuickGeneralServiceServer>{
                    @Override
                    protected void registerBindings(BindingAppender<ConcurrentSubscriptionMeta<AeronQuickGeneralServiceServer>> bindings, AeronQuickContext ctx, ObjectStringMapper mapper, String receiverName, AeronQuickGeneralServiceServer server) {
                    
                        bindings.addBinding(
                            ctx.getProperty(receiverName, "notifyOperationDone", "channel"),
                            ctx.getIntProperty(receiverName, "notifyOperationDone", "stream"),
                            new ConcurrentSubscriptionMeta<>(
                                (event, sequence, buffer, offset, length) -> {
                                    char param0 = buffer.getChar(offset, ByteOrder.LITTLE_ENDIAN);
                                    int param1 = buffer.getInt(offset + BitUtil.SIZE_OF_CHAR, ByteOrder.LITTLE_ENDIAN);
                                    event.setExecutor(new MethodExecutor0(param0, param1));
                                }, 3
                            )
                        );
                    }
                    public record MethodExecutor0(char param0, int param1) implements MethodExecutor<AeronQuickGeneralServiceServer>{
                        @Override
                        public void runMethod(AeronQuickGeneralServiceServer target) {
                            target.notifyOperationDone(param0, param1);
                        }
                    }
                }""");
    }

    @Test
    void generates_adapter_only_primitive_params_adapt_all_methods_in_interface_marked_with_AeronQuickContract() throws IOException {
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
                                import org.jetc.aeron.quick.annotations.AeronQuickContract;
                        
                                @AeronQuickReceiver
                                class AeronQuickGeneralServiceServer implements AeronGeneralServiceContract {
                                    @Override
                                    public void notifyOperationDone(char extraData, int param2){}
                                    @Override
                                    public long otherAdaptedMethod(){return 0;}
                                }
                                @AeronQuickContract
                                interface AeronGeneralServiceContract {
                                    void notifyOperationDone(char extraData, int param2);
                                    long otherAdaptedMethod();
                                }
                        """)
                );

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation).generatedFile(StandardLocation.SOURCE_OUTPUT,"org/jetc/aeron/quick/samples/general/"+targetClassName+RECEIVER_SUFFIX+".java");
        CompilationSubject.assertThat(compilation).generatedFile(StandardLocation.CLASS_OUTPUT,"org/jetc/aeron/quick/samples/general/"+targetClassName+RECEIVER_SUFFIX+".class");
        assertFileContentEquals(compilation, RECEIVER_SUFFIX+".java", """
                package org.jetc.aeron.quick.samples.general;
                import org.agrona.BitUtil;
                import org.jetc.aeron.quick.AeronQuickContext;
                import org.jetc.aeron.quick.messaging.BindingAppender;
                import org.jetc.aeron.quick.messaging.serialization.ObjectStringMapper;
                import java.nio.ByteOrder;
                import org.jetc.aeron.quick.messaging.subscription.SubscriptionMeta;
                                
                public class AeronQuickGeneralServiceServer_RAdapter extends org.jetc.aeron.quick.peers.receiver.SequentialAdapter<AeronQuickGeneralServiceServer>{
                    @Override
                    protected void registerBindings(BindingAppender<SubscriptionMeta> bindings, AeronQuickContext ctx, ObjectStringMapper mapper, String receiverName, AeronQuickGeneralServiceServer server) {
                                
                        bindings.addBinding(
                            ctx.getProperty(receiverName, "notifyOperationDone", "channel"),
                            ctx.getIntProperty(receiverName, "notifyOperationDone", "stream"),
                            new SubscriptionMeta(
                                (buffer, offset, length, header) -> {
                                    char param0 = buffer.getChar(offset, ByteOrder.LITTLE_ENDIAN);
                                    int param1 = buffer.getInt(offset + BitUtil.SIZE_OF_CHAR, ByteOrder.LITTLE_ENDIAN);
                                    server.notifyOperationDone(param0, param1);
                                }, 3
                            )
                        );
                        bindings.addBinding(
                            ctx.getProperty(receiverName, "otherAdaptedMethod", "channel"),
                            ctx.getIntProperty(receiverName, "otherAdaptedMethod", "stream"),
                            new SubscriptionMeta(
                                (buffer, offset, length, header) -> {
                                    server.otherAdaptedMethod();
                                }, 3
                            )
                        );
                    }
                }""");
    }

    @Test
    void generates_adapter_only_primitive_params_adapt_all_methods_in_same_class_marked_with_AeronQuickContract() throws IOException {
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
                                import org.jetc.aeron.quick.annotations.AeronQuickContract;
                        
                                @AeronQuickContract
                                @AeronQuickReceiver
                                class AeronQuickGeneralServiceServer {
                                    public void notifyOperationDone(char extraData, int param2){}
                                    public long otherAdaptedMethod(){return 0;}
                                }
                        """)
                );

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation).generatedFile(StandardLocation.SOURCE_OUTPUT,"org/jetc/aeron/quick/samples/general/"+targetClassName+RECEIVER_SUFFIX+".java");
        CompilationSubject.assertThat(compilation).generatedFile(StandardLocation.CLASS_OUTPUT,"org/jetc/aeron/quick/samples/general/"+targetClassName+RECEIVER_SUFFIX+".class");
        assertFileContentEquals(compilation, RECEIVER_SUFFIX+".java",
        """
        package org.jetc.aeron.quick.samples.general;
        import org.agrona.BitUtil;
        import org.jetc.aeron.quick.AeronQuickContext;
        import org.jetc.aeron.quick.messaging.BindingAppender;
        import org.jetc.aeron.quick.messaging.serialization.ObjectStringMapper;
        import java.nio.ByteOrder;
        import org.jetc.aeron.quick.messaging.subscription.SubscriptionMeta;
                
        public class AeronQuickGeneralServiceServer_RAdapter extends org.jetc.aeron.quick.peers.receiver.SequentialAdapter<AeronQuickGeneralServiceServer>{
            @Override
            protected void registerBindings(BindingAppender<SubscriptionMeta> bindings, AeronQuickContext ctx, ObjectStringMapper mapper, String receiverName, AeronQuickGeneralServiceServer server) {
                
                bindings.addBinding(
                    ctx.getProperty(receiverName, "notifyOperationDone", "channel"),
                    ctx.getIntProperty(receiverName, "notifyOperationDone", "stream"),
                    new SubscriptionMeta(
                        (buffer, offset, length, header) -> {
                            char param0 = buffer.getChar(offset, ByteOrder.LITTLE_ENDIAN);
                            int param1 = buffer.getInt(offset + BitUtil.SIZE_OF_CHAR, ByteOrder.LITTLE_ENDIAN);
                            server.notifyOperationDone(param0, param1);
                        }, 3
                    )
                );
                bindings.addBinding(
                    ctx.getProperty(receiverName, "otherAdaptedMethod", "channel"),
                    ctx.getIntProperty(receiverName, "otherAdaptedMethod", "stream"),
                    new SubscriptionMeta(
                        (buffer, offset, length, header) -> {
                            server.otherAdaptedMethod();
                        }, 3
                    )
                );
            }
        }"""
        );
    }

    @Test
    void generates_adapter_string_param_adapt_single_method_marked_with_AeronQuickContractEndpoint_in_same_class() throws IOException {
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
                                import org.jetc.aeron.quick.annotations.AeronQuickContractEndpoint;
                        
                                @AeronQuickReceiver
                                class AeronQuickGeneralServiceServer {
                                    @AeronQuickContractEndpoint
                                    public void methodWStringParam(String extraData, int param2){}
                                }
                        """)
                );

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation).generatedFile(StandardLocation.SOURCE_OUTPUT,"org/jetc/aeron/quick/samples/general/"+targetClassName+RECEIVER_SUFFIX+".java");
        CompilationSubject.assertThat(compilation).generatedFile(StandardLocation.CLASS_OUTPUT,"org/jetc/aeron/quick/samples/general/"+targetClassName+RECEIVER_SUFFIX+".class");
        assertFileContentEquals(compilation, RECEIVER_SUFFIX+".java", """
                package org.jetc.aeron.quick.samples.general;
                import org.agrona.BitUtil;
                import org.jetc.aeron.quick.AeronQuickContext;
                import org.jetc.aeron.quick.messaging.BindingAppender;
                import org.jetc.aeron.quick.messaging.serialization.ObjectStringMapper;
                import java.nio.ByteOrder;
                import org.jetc.aeron.quick.messaging.subscription.SubscriptionMeta;
                                
                public class AeronQuickGeneralServiceServer_RAdapter extends org.jetc.aeron.quick.peers.receiver.SequentialAdapter<AeronQuickGeneralServiceServer>{
                    @Override
                    protected void registerBindings(BindingAppender<SubscriptionMeta> bindings, AeronQuickContext ctx, ObjectStringMapper mapper, String receiverName, AeronQuickGeneralServiceServer server) {
                                
                        bindings.addBinding(
                            ctx.getProperty(receiverName, "methodWStringParam", "channel"),
                            ctx.getIntProperty(receiverName, "methodWStringParam", "stream"),
                            new SubscriptionMeta(
                                (buffer, offset, length, header) -> {
                                    java.lang.String param0 = buffer.getStringUtf8(offset, ByteOrder.LITTLE_ENDIAN);
                                    int param1 = buffer.getInt(offset + param0.length() + BitUtil.SIZE_OF_INT, ByteOrder.LITTLE_ENDIAN);
                                    server.methodWStringParam(param0, param1);
                                }, 3
                            )
                        );
                    }
                }""");
    }

    @Test
    void generates_adapter_only_primitive_params_adapt_single_method_marked_with_AeronQuickContractEndpoint_in_same_class() throws IOException {
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
                                import org.jetc.aeron.quick.annotations.AeronQuickContractEndpoint;
                        
                                @AeronQuickReceiver
                                class AeronQuickGeneralServiceServer {
                                    @AeronQuickContractEndpoint
                                    public void notifyOperationDone(char extraData, int param2){}
                                    public long otherMethod(){return 0;}
                                }
                        """)
                );

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation).generatedFile(StandardLocation.SOURCE_OUTPUT,"org/jetc/aeron/quick/samples/general/"+targetClassName+RECEIVER_SUFFIX+".java");
        CompilationSubject.assertThat(compilation).generatedFile(StandardLocation.CLASS_OUTPUT,"org/jetc/aeron/quick/samples/general/"+targetClassName+RECEIVER_SUFFIX+".class");
        assertFileContentEquals(compilation, RECEIVER_SUFFIX+".java", """
                package org.jetc.aeron.quick.samples.general;
                import org.agrona.BitUtil;
                import org.jetc.aeron.quick.AeronQuickContext;
                import org.jetc.aeron.quick.messaging.BindingAppender;
                import org.jetc.aeron.quick.messaging.serialization.ObjectStringMapper;
                import java.nio.ByteOrder;
                import org.jetc.aeron.quick.messaging.subscription.SubscriptionMeta;
                                
                public class AeronQuickGeneralServiceServer_RAdapter extends org.jetc.aeron.quick.peers.receiver.SequentialAdapter<AeronQuickGeneralServiceServer>{
                    @Override
                    protected void registerBindings(BindingAppender<SubscriptionMeta> bindings, AeronQuickContext ctx, ObjectStringMapper mapper, String receiverName, AeronQuickGeneralServiceServer server) {
                                
                        bindings.addBinding(
                            ctx.getProperty(receiverName, "notifyOperationDone", "channel"),
                            ctx.getIntProperty(receiverName, "notifyOperationDone", "stream"),
                            new SubscriptionMeta(
                                (buffer, offset, length, header) -> {
                                    char param0 = buffer.getChar(offset, ByteOrder.LITTLE_ENDIAN);
                                    int param1 = buffer.getInt(offset + BitUtil.SIZE_OF_CHAR, ByteOrder.LITTLE_ENDIAN);
                                    server.notifyOperationDone(param0, param1);
                                }, 3
                            )
                        );
                    }
                }""");
    }

    @Test
    void generates_adapter_pojo_param_adapt_single_method_marked_with_AeronQuickContractEndpoint_in_same_class() throws IOException {
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
                                import org.jetc.aeron.quick.annotations.AeronQuickContractEndpoint;
                        
                                @AeronQuickReceiver
                                class AeronQuickGeneralServiceServer {
                                    @AeronQuickContractEndpoint
                                    public void notifyOperationDone(char extraData, POJOExample pojoExample, int param2){}
                                    public long otherMethod(){return 0;}
                                }
                                record POJOExample(String fieldExample, float valueExample){}
                        """)
                );

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation).generatedFile(StandardLocation.SOURCE_OUTPUT,"org/jetc/aeron/quick/samples/general/"+targetClassName+RECEIVER_SUFFIX+".java");
        CompilationSubject.assertThat(compilation).generatedFile(StandardLocation.CLASS_OUTPUT,"org/jetc/aeron/quick/samples/general/"+targetClassName+RECEIVER_SUFFIX+".class");
        assertFileContentEquals(compilation, RECEIVER_SUFFIX+".java", """
                package org.jetc.aeron.quick.samples.general;
                import org.agrona.BitUtil;
                import org.jetc.aeron.quick.AeronQuickContext;
                import org.jetc.aeron.quick.messaging.BindingAppender;
                import org.jetc.aeron.quick.messaging.serialization.ObjectStringMapper;
                import java.nio.ByteOrder;
                import org.jetc.aeron.quick.messaging.subscription.SubscriptionMeta;
                                
                public class AeronQuickGeneralServiceServer_RAdapter extends org.jetc.aeron.quick.peers.receiver.SequentialAdapter<AeronQuickGeneralServiceServer>{
                    @Override
                    protected void registerBindings(BindingAppender<SubscriptionMeta> bindings, AeronQuickContext ctx, ObjectStringMapper mapper, String receiverName, AeronQuickGeneralServiceServer server) {
                                
                        bindings.addBinding(
                            ctx.getProperty(receiverName, "notifyOperationDone", "channel"),
                            ctx.getIntProperty(receiverName, "notifyOperationDone", "stream"),
                            new SubscriptionMeta(
                                (buffer, offset, length, header) -> {
                                    char param0 = buffer.getChar(offset, ByteOrder.LITTLE_ENDIAN);
                                    java.lang.String param1_str = buffer.getStringUtf8(offset + BitUtil.SIZE_OF_CHAR, ByteOrder.LITTLE_ENDIAN);
                                    org.jetc.aeron.quick.samples.general.POJOExample param1 = mapper.deserialize(param1_str, org.jetc.aeron.quick.samples.general.POJOExample.class);
                                    int param2 = buffer.getInt(offset + BitUtil.SIZE_OF_CHAR + param1_str.length() + BitUtil.SIZE_OF_INT, ByteOrder.LITTLE_ENDIAN);
                                    server.notifyOperationDone(param0, param1, param2);
                                }, 3
                            )
                        );
                    }
                }""");
    }

    @Test
    void generates_adapter_on_method_compatible_with_fragment_handler() throws IOException {//TODO
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
                                import org.jetc.aeron.quick.annotations.AeronQuickContractEndpoint;
                                import io.aeron.logbuffer.Header;
                                import org.agrona.DirectBuffer;
                        
                                @AeronQuickReceiver
                                class AeronQuickGeneralServiceServer {
                                    @AeronQuickContractEndpoint
                                    public void fragmentHandlerCompatible(DirectBuffer buff, int offset, int len, Header head){}
                                }
                        """)
                );

        CompilationSubject.assertThat(compilation).succeeded();
        CompilationSubject.assertThat(compilation).generatedFile(StandardLocation.SOURCE_OUTPUT,"org/jetc/aeron/quick/samples/general/"+targetClassName+RECEIVER_SUFFIX+".java");
        CompilationSubject.assertThat(compilation).generatedFile(StandardLocation.CLASS_OUTPUT,"org/jetc/aeron/quick/samples/general/"+targetClassName+RECEIVER_SUFFIX+".class");
        assertFileContentEquals(compilation, RECEIVER_SUFFIX+".java", """
                package org.jetc.aeron.quick.samples.general;
                import org.agrona.BitUtil;
                import org.jetc.aeron.quick.AeronQuickContext;
                import org.jetc.aeron.quick.messaging.BindingAppender;
                import org.jetc.aeron.quick.messaging.serialization.ObjectStringMapper;
                import java.nio.ByteOrder;
                import org.jetc.aeron.quick.messaging.subscription.SubscriptionMeta;
                                
                public class AeronQuickGeneralServiceServer_RAdapter extends org.jetc.aeron.quick.peers.receiver.SequentialAdapter<AeronQuickGeneralServiceServer>{
                    @Override
                    protected void registerBindings(BindingAppender<SubscriptionMeta> bindings, AeronQuickContext ctx, ObjectStringMapper mapper, String receiverName, AeronQuickGeneralServiceServer server) {
                                
                        bindings.addBinding(
                            ctx.getProperty(receiverName, "fragmentHandlerCompatible", "channel"),
                            ctx.getIntProperty(receiverName, "fragmentHandlerCompatible", "stream"),
                            new SubscriptionMeta(
                                server::fragmentHandlerCompatible, 3
                            )
                        );
                    }
                }""");
    }

    @Test
    void throws_when_no_methods_to_adapt_in_class_marked_with_AeronQuickReceiver() {
        JavaFileObject receiverCode = JavaFileObjects.forSourceString("org.jetc.aeron.quick.samples.general.AeronQuickGeneralServiceServer", """
                                package org.jetc.aeron.quick.samples.general;
                                import java.lang.annotation.ElementType;
                                import java.lang.annotation.Retention;
                                import java.lang.annotation.RetentionPolicy;
                                import java.lang.annotation.Target;
                                import org.jetc.aeron.quick.annotations.AeronQuickReceiver;
                        
                                @AeronQuickReceiver
                                class AeronQuickGeneralServiceServer {
                                    public void notifyOperationDone(char extraData, int param2){}
                                    public long otherMethod(){return 0;}
                                }
                        """);
        Compiler compiler = withAeronQuickInDefaultClasspath(Compiler.javac()).withProcessors(new AeronQuickReceiverProcessor());

        Throwable thrown = assertThrows(RuntimeException.class, () -> compiler.compile(receiverCode));
        assertEquals(
                "There are no methods to bind in the receiver class: org.jetc.aeron.quick.samples.general.AeronQuickGeneralServiceServer. Please remove the annotation or mark at least one method as AeronQuickContractEndpoint",
                thrown.getCause().getCause().getMessage()
        );
    }

    @Test
    void throws_when_method_to_adapt_has_non_imported_type_on_parameter() {
        JavaFileObject receiverCode = JavaFileObjects.forSourceString("org.jetc.aeron.quick.samples.general.AeronQuickGeneralServiceServer", """
                                package org.jetc.aeron.quick.samples.general;
                                import java.lang.annotation.ElementType;
                                import java.lang.annotation.Retention;
                                import java.lang.annotation.RetentionPolicy;
                                import java.lang.annotation.Target;
                                import org.jetc.aeron.quick.annotations.AeronQuickReceiver;
                                import org.jetc.aeron.quick.annotations.AeronQuickContractEndpoint;
                        
                                @AeronQuickReceiver
                                class AeronQuickGeneralServiceServer {
                                    @AeronQuickContractEndpoint
                                    public void notifyOperationDone(char extraData, Example param2){}
                                    public long otherMethod(){return 0;}
                                }
                        """);
        Compiler compiler = withAeronQuickInDefaultClasspath(Compiler.javac()).withProcessors(new AeronQuickReceiverProcessor());

        Throwable thrown = assertThrows(RuntimeException.class, () -> compiler.compile(receiverCode));
        assertEquals(
                "There was an error while creating an adapter for org.jetc.aeron.quick.samples.general.AeronQuickGeneralServiceServer: org.jetc.aeron.quick.annotations.processors.utils.AdaptingError: Unsupported/Not-Resolved parameter type on method notifyOperationDone: Example param2. Should be an imported class, interface or primitive type",
                thrown.getCause().getMessage()
        );
    }

    @Test
    void throws_when_method_to_adapt_has_arg_of_primitive_array_no_byte_type() {
        JavaFileObject receiverCode = JavaFileObjects.forSourceString("org.jetc.aeron.quick.samples.general.AeronQuickGeneralServiceServer", """
                                package org.jetc.aeron.quick.samples.general;
                                import java.lang.annotation.ElementType;
                                import java.lang.annotation.Retention;
                                import java.lang.annotation.RetentionPolicy;
                                import java.lang.annotation.Target;
                                import org.jetc.aeron.quick.annotations.AeronQuickReceiver;
                                import org.jetc.aeron.quick.annotations.AeronQuickContractEndpoint;
                        
                                @AeronQuickReceiver
                                class AeronQuickGeneralServiceServer {
                                    @AeronQuickContractEndpoint
                                    public void notifyOperationDone(char extraData, int[] param2){}
                                    public long otherMethod(){return 0;}
                                }
                                record Example(String val1){}
                        """);
        Compiler compiler = withAeronQuickInDefaultClasspath(Compiler.javac()).withProcessors(new AeronQuickReceiverProcessor());

        Throwable thrown = assertThrows(RuntimeException.class, () -> compiler.compile(receiverCode));
        assertEquals(
                "There was an error while creating an adapter for org.jetc.aeron.quick.samples.general.AeronQuickGeneralServiceServer: org.jetc.aeron.quick.annotations.processors.utils.AdaptingError: Unsupported/Not-Resolved parameter type on method notifyOperationDone: int[] param2. Should be an imported class, interface or primitive type",
                thrown.getCause().getMessage()
        );
    }
}