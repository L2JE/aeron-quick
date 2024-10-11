package org.jetc.aeron.quick.annotations.testing;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class JavacTest {
    private static final String TEST_RESOURCES = "src/test/resources";
    private static final String EXTERNAL_LIBS_DIR = "java/libs";
    private static final String TEST_SOURCES_DIR = "java/compilerTargets/";
    protected abstract String getTestName();

    public JavaFileObject getSourceFile(String resourceName){
        return JavaFileObjects.forResource(TEST_SOURCES_DIR + getTestName() + "/"+ resourceName+".java");
    }

    public static Compiler withAeronQuickInDefaultClasspath(Compiler compiler){
        List<File> finalCP = new LinkedList<>();
        Compiler.javac().classPath().ifPresent(finalCP::addAll);

        File[] filesToInclude = Paths.get(TEST_RESOURCES, EXTERNAL_LIBS_DIR).toFile().listFiles((dir, name) -> name.startsWith("aeron-quick-base") && name.endsWith("-fat.jar") || name.startsWith("aeron-quick-annotations"));

        if(filesToInclude != null)
            Collections.addAll(finalCP, filesToInclude);

        return compiler.withClasspath(finalCP);
    }

    public static void assertFileContentEquals(Compilation compilation, String actualFileNameEnd, String expectedContent){
        compilation.generatedFiles().forEach(file -> {
            if(file.getName().endsWith(actualFileNameEnd)) {
                try {
                    assertEquals(expectedContent, file.getCharContent(false).toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
