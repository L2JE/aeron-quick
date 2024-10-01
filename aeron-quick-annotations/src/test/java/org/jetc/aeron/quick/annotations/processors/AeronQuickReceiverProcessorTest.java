package org.jetc.aeron.quick.annotations.processors;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.jetc.aeron.quick.annotations.testing.JavacTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
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
                                    
            public class AeronQuickGeneralServiceServer_Adapter implements ReceiverAdapterBase<AeronQuickGeneralServiceServer>{
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
                                char param0 = buffer.getChar(offset, ByteOrder.LITTLE_ENDIAN);
                                int param1 = buffer.getInt(offset + BitUtil.SIZE_OF_CHAR, ByteOrder.LITTLE_ENDIAN);
                                server.notifyOperationDone(param0, param1);
                            }
                        )
                                    
                    );
                }
            }""",
            compilation.generatedFiles().getLast().getCharContent(false).toString()
        );
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
                        
                                @AeronQuickReceiver(name = "receiverX")
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
        CompilationSubject.assertThat(compilation).generatedFile(StandardLocation.SOURCE_OUTPUT,"org/jetc/aeron/quick/samples/general/"+targetClassName+"_Adapter.java");
        CompilationSubject.assertThat(compilation).generatedFile(StandardLocation.CLASS_OUTPUT,"org/jetc/aeron/quick/samples/general/"+targetClassName+"_Adapter.class");
        Assertions.assertEquals(
                """
                        package org.jetc.aeron.quick.samples.general;
                        import org.jetc.aeron.quick.server.precompile.ReceiverAdapterBase;
                        import org.jetc.aeron.quick.messaging.ReceiverBindingProvider;
                        import org.jetc.aeron.quick.messaging.fragment_handling.ContextualHandler;
                        import org.jetc.aeron.quick.messaging.subscription.SubscriptionMeta;
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
                                                
                        public class AeronQuickGeneralServiceServer_Adapter implements ReceiverAdapterBase<AeronQuickGeneralServiceServer>{
                            private static final Logger log = LoggerFactory.getLogger(AeronQuickGeneralServiceServer_Adapter.class);
                            private static final String PROPS_SUFFIX = "aeron.quick.receiverX.";
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
                                            char param0 = buffer.getChar(offset, ByteOrder.LITTLE_ENDIAN);
                                            int param1 = buffer.getInt(offset + BitUtil.SIZE_OF_CHAR, ByteOrder.LITTLE_ENDIAN);
                                            server.notifyOperationDone(param0, param1);
                                        }
                                    ),
                                    new Binding(
                                        "otherAdaptedMethod",
                                        3,
                                        aeron -> (DirectBuffer buffer, int offset, int length, Header header) -> {
                                            server.otherAdaptedMethod();
                                        }
                                    ) 
                                           
                                );
                            }
                        }""",
                compilation.generatedFiles().getLast().getCharContent(false).toString()
        );
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
                                @AeronQuickReceiver(name = "receiverX")
                                class AeronQuickGeneralServiceServer {
                                    public void notifyOperationDone(char extraData, int param2){}
                                    public long otherAdaptedMethod(){return 0;}
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
                                                
                        public class AeronQuickGeneralServiceServer_Adapter implements ReceiverAdapterBase<AeronQuickGeneralServiceServer>{
                            private static final Logger log = LoggerFactory.getLogger(AeronQuickGeneralServiceServer_Adapter.class);
                            private static final String PROPS_SUFFIX = "aeron.quick.receiverX.";
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
                                            char param0 = buffer.getChar(offset, ByteOrder.LITTLE_ENDIAN);
                                            int param1 = buffer.getInt(offset + BitUtil.SIZE_OF_CHAR, ByteOrder.LITTLE_ENDIAN);
                                            server.notifyOperationDone(param0, param1);
                                        }
                                    ),
                                    new Binding(
                                        "otherAdaptedMethod",
                                        3,
                                        aeron -> (DirectBuffer buffer, int offset, int length, Header header) -> {
                                            server.otherAdaptedMethod();
                                        }
                                    ) 
                                           
                                );
                            }
                        }""",
                compilation.generatedFiles().getLast().getCharContent(false).toString()
        );
    }

    @Test
    void generates_adapter_string_param_adapt_single_method_marked_with_QuickContractEndpoint_in_same_class() throws IOException {
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
                        
                                @AeronQuickReceiver(name = "receiverX")
                                class AeronQuickGeneralServiceServer {
                                    @QuickContractEndpoint
                                    public void methodWStringParam(String extraData, int param2){}
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
                                                
                        public class AeronQuickGeneralServiceServer_Adapter implements ReceiverAdapterBase<AeronQuickGeneralServiceServer>{
                            private static final Logger log = LoggerFactory.getLogger(AeronQuickGeneralServiceServer_Adapter.class);
                            private static final String PROPS_SUFFIX = "aeron.quick.receiverX.";
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
                                        "methodWStringParam",
                                        3,
                                        aeron -> (DirectBuffer buffer, int offset, int length, Header header) -> {
                                            java.lang.String param0 = buffer.getStringUtf8(offset, ByteOrder.LITTLE_ENDIAN);
                                            int param1 = buffer.getInt(offset + param0.length() + BitUtil.SIZE_OF_INT, ByteOrder.LITTLE_ENDIAN);
                                            server.methodWStringParam(param0, param1);
                                        }
                                    )
                                           
                                );
                            }
                        }""",
                compilation.generatedFiles().getLast().getCharContent(false).toString()
        );
    }

    @Test
    void generates_adapter_only_primitive_params_adapt_single_method_marked_with_QuickContractEndpoint_in_same_class() throws IOException {
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
                        
                                @AeronQuickReceiver(name = "receiverX")
                                class AeronQuickGeneralServiceServer {
                                    @QuickContractEndpoint
                                    public void notifyOperationDone(char extraData, int param2){}
                                    public long otherMethod(){return 0;}
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
                                                
                        public class AeronQuickGeneralServiceServer_Adapter implements ReceiverAdapterBase<AeronQuickGeneralServiceServer>{
                            private static final Logger log = LoggerFactory.getLogger(AeronQuickGeneralServiceServer_Adapter.class);
                            private static final String PROPS_SUFFIX = "aeron.quick.receiverX.";
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
                                            char param0 = buffer.getChar(offset, ByteOrder.LITTLE_ENDIAN);
                                            int param1 = buffer.getInt(offset + BitUtil.SIZE_OF_CHAR, ByteOrder.LITTLE_ENDIAN);
                                            server.notifyOperationDone(param0, param1);
                                        }
                                    )
                                           
                                );
                            }
                        }""",
                compilation.generatedFiles().getLast().getCharContent(false).toString()
        );
    }

    @Test
    void generates_adapter_pojo_param_adapt_single_method_marked_with_QuickContractEndpoint_in_same_class() throws IOException {
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
                        
                                @AeronQuickReceiver(name = "receiverX")
                                class AeronQuickGeneralServiceServer {
                                    @QuickContractEndpoint
                                    public void notifyOperationDone(char extraData, POJOExample pojoExample, int param2){}
                                    public long otherMethod(){return 0;}
                                }
                                record POJOExample(String fieldExample, float valueExample){}
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
                                                
                        public class AeronQuickGeneralServiceServer_Adapter implements ReceiverAdapterBase<AeronQuickGeneralServiceServer>{
                            private static final Logger log = LoggerFactory.getLogger(AeronQuickGeneralServiceServer_Adapter.class);
                            private static final String PROPS_SUFFIX = "aeron.quick.receiverX.";
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
                                            char param0 = buffer.getChar(offset, ByteOrder.LITTLE_ENDIAN);
                                            java.lang.String param1_str = buffer.getStringUtf8(offset + BitUtil.SIZE_OF_CHAR, ByteOrder.LITTLE_ENDIAN);
                                            org.jetc.aeron.quick.samples.general.POJOExample param1 = mapToObject(param1_str, org.jetc.aeron.quick.samples.general.POJOExample.class);
                                            int param2 = buffer.getInt(offset + BitUtil.SIZE_OF_CHAR + param1_str.length() + BitUtil.SIZE_OF_INT, ByteOrder.LITTLE_ENDIAN);
                                            server.notifyOperationDone(param0, param1, param2);
                                        }
                                    )
                        
                                );
                            }
                            private static final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            private static <T> T mapToObject(String content, Class<T> targetClass){
                                try {
                                    return mapper.readValue(content, targetClass);
                                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }""",
                compilation.generatedFiles().getLast().getCharContent(false).toString()
        );
    }

    @Test
    void generates_adapter_on_method_compatible_with_contextual_fragment_handler() throws IOException {//TODO
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
                                import io.aeron.Aeron;
                                import io.aeron.logbuffer.FragmentHandler;
                        
                                @AeronQuickReceiver(name = "receiverX")
                                class AeronQuickGeneralServiceServer {
                                    @QuickContractEndpoint
                                    public FragmentHandler contextualFragmentHandlerCompatible(Aeron aeronArg){
                                        return (var1, var2, var3, var4) -> {};
                                    }
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
                                                
                        public class AeronQuickGeneralServiceServer_Adapter implements ReceiverAdapterBase<AeronQuickGeneralServiceServer>{
                            private static final Logger log = LoggerFactory.getLogger(AeronQuickGeneralServiceServer_Adapter.class);
                            private static final String PROPS_SUFFIX = "aeron.quick.receiverX.";
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
                                        "contextualFragmentHandlerCompatible",
                                        3,
                                        server::contextualFragmentHandlerCompatible
                                    )
                                           
                                );
                            }
                        }""",
                compilation.generatedFiles().getLast().getCharContent(false).toString()
        );
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
                                import org.jetc.aeron.quick.annotations.QuickContractEndpoint;
                                import io.aeron.logbuffer.Header;
                                import org.agrona.DirectBuffer;
                        
                                @AeronQuickReceiver(name = "receiverX")
                                class AeronQuickGeneralServiceServer {
                                    @QuickContractEndpoint
                                    public void fragmentHandlerCompatible(DirectBuffer buff, int offset, int len, Header head){}
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
                                                
                        public class AeronQuickGeneralServiceServer_Adapter implements ReceiverAdapterBase<AeronQuickGeneralServiceServer>{
                            private static final Logger log = LoggerFactory.getLogger(AeronQuickGeneralServiceServer_Adapter.class);
                            private static final String PROPS_SUFFIX = "aeron.quick.receiverX.";
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
                                        "fragmentHandlerCompatible",
                                        3,
                                        aeron -> server::fragmentHandlerCompatible
                                    )
                                           
                                );
                            }
                        }""",
                compilation.generatedFiles().getLast().getCharContent(false).toString()
        );
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
                        
                                @AeronQuickReceiver(name = "receiverX")
                                class AeronQuickGeneralServiceServer {
                                    public void notifyOperationDone(char extraData, int param2){}
                                    public long otherMethod(){return 0;}
                                }
                        """);
        Compiler compiler = withAeronQuickInDefaultClasspath(Compiler.javac()).withProcessors(new AeronQuickReceiverProcessor());

        Throwable thrown = assertThrows(RuntimeException.class, () -> compiler.compile(receiverCode));
        assertEquals(
                "There are no methods to bind in the receiver class: org.jetc.aeron.quick.samples.general.AeronQuickGeneralServiceServer. Please remove the annotation or mark at least one method as QuickContractEndpoint",
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
                                import org.jetc.aeron.quick.annotations.QuickContractEndpoint;
                        
                                @AeronQuickReceiver(name = "receiverX")
                                class AeronQuickGeneralServiceServer {
                                    @QuickContractEndpoint
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
                                import org.jetc.aeron.quick.annotations.QuickContractEndpoint;
                        
                                @AeronQuickReceiver(name = "receiverX")
                                class AeronQuickGeneralServiceServer {
                                    @QuickContractEndpoint
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