package org.example;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.lang.reflect.Method;

public class DynamicCodeExecutor {

    public String executeGeneratedCode(String code) throws Exception {
        JavaFileObject file = new JavaSourceFromString("DynamicClass", code);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        File tempDirectory = Files.createTempDirectory("compiled").toFile();

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, java.util.Collections.singleton(tempDirectory));

        compiler.getTask(null, fileManager, null, null, null, java.util.Arrays.asList(file)).call();

        ClassLoader classLoader = new CustomClassLoader(tempDirectory);
        Class<?> cls = classLoader.loadClass("DynamicClass");
        Object instance = cls.getDeclaredConstructor().newInstance();

        Method method = cls.getMethod("execute");
        Object result = method.invoke(instance);

        fileManager.close();
        deleteDirectory(tempDirectory);

        return result != null ? result.toString() : "Method executed successfully";
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File f : files) {
                deleteDirectory(f);
            }
        }
        directory.delete();
    }

    static class JavaSourceFromString extends SimpleJavaFileObject {
        final String code;

        JavaSourceFromString(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    static class CustomClassLoader extends ClassLoader {
        private final File directory;

        public CustomClassLoader(File directory) {
            this.directory = directory;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                String classPath = name.replace('.', '/') + ".class";
                File classFile = new File(directory, classPath);

                if (!classFile.exists()) {
                    throw new ClassNotFoundException("Class file not found: " + classFile.getPath());
                }

                byte[] classData = Files.readAllBytes(classFile.toPath());
                return defineClass(name, classData, 0, classData.length);
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        }
    }
}

