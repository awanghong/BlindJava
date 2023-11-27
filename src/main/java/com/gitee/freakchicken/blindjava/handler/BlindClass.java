package com.gitee.freakchicken.blindjava.handler;

import com.gitee.freakchicken.blindjava.util.ConfigUtil;
import com.gitee.freakchicken.blindjava.util.FileUtil;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BlindClass {

    private static final Logger log = LoggerFactory.getLogger(BlindClass.class);

    private static final String step2 = ConfigUtil.targetDir + "step2" + File.separator;

    private static final String step3 = ConfigUtil.targetDir + "step3" + File.separator;

    static Map<String, String> classMap = new HashMap<>();

    private static boolean m_RN1AFnxu(String p_6zaNadB8) {
        for (String lf_A35JQosp : ConfigUtil.excludeDirList) {
            String lf_g31F1mtX = step2 + lf_A35JQosp;
            if (p_6zaNadB8.startsWith(lf_g31F1mtX)) {
                return true;
            }
        }
        return false;
    }

    public static void blindClass() throws IOException {
        Collection<File> fileCollection = FileUtils.listFiles(new File(step2), null, true);
        FileUtils.deleteDirectory(new File(step3));
        FileUtils.forceMkdir(new File(step3));
        fileCollection.stream().filter(fileSingle -> !m_RN1AFnxu(fileSingle.getPath()) && fileSingle.isFile()).forEach(file -> {
            String filePath = file.getPath();
            String step2Path = filePath.substring(step2.length());
            String step3Path = step3 + step2Path;
            JavaParser javaParser = new JavaParser();
            List<String> list = new ArrayList<>();
            File lf_2uPfo6mg = new File(step3Path);
            log.info(step3Path);
            try {
                FileUtils.forceMkdirParent(lf_2uPfo6mg);
                if (file.getName().endsWith(".java")) {
                    ParseResult<CompilationUnit> parse = javaParser.parse(file);
                    if (parse.isSuccessful()) {
                        Optional<CompilationUnit> parseResult = parse.getResult();
                        CompilationUnit compilationUnit = parseResult.get();
                        NodeList<TypeDeclaration<?>> types = compilationUnit.getTypes();
                        TypeDeclaration<?> typeDeclaration = types.getFirst().get();
                        NodeList<AnnotationExpr> annotations = typeDeclaration.getAnnotations();
                        list = annotations.stream().map(annotationExpr -> annotationExpr.getNameAsString()).collect(Collectors.toList());
                    }
                    Map<String, String> classNameAndPackageName = FileUtil.obtainClassNameAndPackageName(file);
                    // 
                    if ((!ConfigUtil.checkPackage(classNameAndPackageName.get("packageName"))) && (!ConfigUtil.checkClass(classNameAndPackageName.get("className"))) && (!ConfigUtil.m_6gZPmnqh(list))) {
                        String className = classNameAndPackageName.get("className");
                        String packageName = classNameAndPackageName.get("packageName");
                        classMap.put(className, packageName + ".C_" + RandomStringUtils.random(8, true, true));
                    } else {
                        log.info("excluded: " + step3Path);
                        IOUtils.copy(new FileInputStream(file), new FileOutputStream(lf_2uPfo6mg));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        fileCollection.stream().filter(fileSingle -> !m_RN1AFnxu(fileSingle.getPath()) && fileSingle.isFile()).forEach(file -> {
            String filePath = file.getPath();
            String step2Path = filePath.substring(step2.length());
            String step2Path1 = step2Path.substring(step2Path.lastIndexOf('.') + 1);
            String step3Path = BlindClass.step3 + step2Path;
            File lf_hMW7iGcr = new File(step3Path);
            try {
                FileUtils.forceMkdirParent(lf_hMW7iGcr);
                if (file.getName().endsWith(".java")) {
                    Map<String, String> classNameAndPackageName = FileUtil.obtainClassNameAndPackageName(file);
                    System.out.println(classNameAndPackageName.get("packageName") + ":" + classNameAndPackageName.get("className"));
                    if ((!ConfigUtil.checkPackage(classNameAndPackageName.get("packageName"))) && (!ConfigUtil.checkClass(classNameAndPackageName.get("className")))) {
                        String classSwitchResult = classSwitch(file);
                        if (classMap.containsKey(classNameAndPackageName.get("className"))) {
                            String className = classMap.get(classNameAndPackageName.get("className"));
                            String className1 = className.substring(className.lastIndexOf(".") + 1);
                            String classNameJava = step3Path.substring(0, step3Path.lastIndexOf("\\") + 1) + className1 + ".java";
                            IOUtils.write(classSwitchResult, new FileOutputStream(new File(classNameJava)), Charsets.UTF_8);
                        } else
                            IOUtils.write(classSwitchResult, new FileOutputStream(lf_hMW7iGcr), Charsets.UTF_8);
                    } else {
                        log.info("excluded: " + step3Path);
                    }
                } else {
                    if (ConfigUtil.fileList.contains(step2Path1)) {
                        String result = IOUtils.toString(new FileInputStream(file), Charsets.UTF_8);
                        for (String className : classMap.keySet()) {
                            String relaceName = classMap.get(className);
                            result = result.replace(className, relaceName);
                        }
                        IOUtils.write(result, new FileOutputStream(lf_hMW7iGcr), Charsets.UTF_8);
                    } else {
                        IOUtils.copy(new FileInputStream(file), new FileOutputStream(lf_hMW7iGcr));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        log.info("类名混淆成功");
    }

    private static String classSwitch(File file) throws IOException {
        JavaParser javaParser = new JavaParser();
        ParseResult<CompilationUnit> parse = javaParser.parse(file);
        if (parse.isSuccessful()) {
            Optional<CompilationUnit> parseResult = parse.getResult();
            CompilationUnit compilationUnit = parseResult.get();
            PackageDeclaration packageDeclaration = compilationUnit.getPackageDeclaration().get();
            String packageDeclarationNameAsString = packageDeclaration.getNameAsString();
            NodeList<ImportDeclaration> imports = compilationUnit.getImports();
            List<String> importsNameAsString = imports.stream().map(importDeclaration -> importDeclaration.getNameAsString()).collect(Collectors.toList());
            List<Node> childNodes = compilationUnit.getChildNodes();
            compilationUnit.getTypes().stream().forEach(typeDeclaration -> {
                List<ConstructorDeclaration> constructors = typeDeclaration.getConstructors();
                if (constructors.size() > 0) {
                    String typeDeclarationNameAsString = typeDeclaration.getNameAsString();
                    if (classMap.containsKey(packageDeclarationNameAsString + "." + typeDeclarationNameAsString)) {
                        String className = classMap.get(packageDeclarationNameAsString + "." + typeDeclarationNameAsString);
                        String className1 = className.substring(className.lastIndexOf(".") + 1);
                        constructors.stream().forEach(constructorDeclaration -> {
                            constructorDeclaration.setName(className1);
                        });
                    }
                }
            });
            compilationUnit.getTypes().stream().forEach(typeDeclaration -> {
                String typeName = typeDeclaration.getNameAsString();
                if (classMap.containsKey(packageDeclarationNameAsString + "." + typeName)) {
                    String className = classMap.get(packageDeclarationNameAsString + "." + typeName);
                    String className1 = className.substring(className.lastIndexOf(".") + 1);
                    typeDeclaration.setName(className1);
                }
            });
            recursionClassNode(childNodes, importsNameAsString, packageDeclarationNameAsString);
            imports.stream().forEach(importDeclaration -> {
                String importDeclarationNameAsString = importDeclaration.getNameAsString();
                if (classMap.containsKey(importDeclarationNameAsString)) {
                    importDeclaration.setName(classMap.get(importDeclarationNameAsString));
                }
            });
            return compilationUnit.toString();
        }
        return null;
    }

    private static void recursionClassNode(List<Node> childNodes, List<String> importsNameAsString, String packageDeclarationNameAsString) {
        for (Node node : childNodes) {
            if (node instanceof ClassOrInterfaceType) {
                ClassOrInterfaceType classOrInterfaceType = (ClassOrInterfaceType) node;
                String className = classOrInterfaceType.getNameAsString();
                Optional<String> first = importsNameAsString.stream().filter(importName -> importName.substring(importName.lastIndexOf(".") + 1).equals(className)).findFirst();
                if (first.isPresent()) {
                    String importFirst = first.get();
                    if (classMap.containsKey(importFirst)) {
                        String classFirst = classMap.get(importFirst);
                        String classTypeName = classFirst.substring(classFirst.lastIndexOf(".") + 1);
                        classOrInterfaceType.setName(classTypeName);
                    }
                } else {
                    String packageName = packageDeclarationNameAsString + "." + className;
                    if (classMap.containsKey(packageName)) {
                        String classFirst = classMap.get(packageName);
                        String classTypeName = classFirst.substring(classFirst.lastIndexOf(".") + 1);
                        classOrInterfaceType.setName(classTypeName);
                    }
                }
            } else if (node instanceof NameExpr) {
                Optional<Node> optionalNode = node.getParentNode();
                if (optionalNode.isPresent()) {
                    Node parentNode = optionalNode.get();
                    if (parentNode instanceof MethodCallExpr) {
                        NameExpr nameExpr = (NameExpr) node;
                        String methodName = nameExpr.getNameAsString();
                        Optional<String> importOptional = importsNameAsString.stream().filter(importName -> importName.substring(importName.lastIndexOf(".") + 1).equals(methodName)).findFirst();
                        if (importOptional.isPresent()) {
                            String importFirst = importOptional.get();
                            if (classMap.containsKey(importFirst)) {
                                String classFirst = classMap.get(importFirst);
                                String className = classFirst.substring(classFirst.lastIndexOf(".") + 1);
                                nameExpr.setName(className);
                            }
                        } else {
                            String packageName = packageDeclarationNameAsString + "." + methodName;
                            if (classMap.containsKey(packageName)) {
                                String classFirst = classMap.get(packageName);
                                String className = classFirst.substring(classFirst.lastIndexOf(".") + 1);
                                nameExpr.setName(className);
                            }
                        }
                    } else if (parentNode instanceof FieldAccessExpr) {
                        NameExpr nameExpr = (NameExpr) node;
                        String nameAsString = nameExpr.getNameAsString();
                        Optional<String> importOptional = importsNameAsString.stream().filter(importsName -> importsName.substring(importsName.lastIndexOf(".") + 1).equals(nameAsString)).findFirst();
                        if (importOptional.isPresent()) {
                            String importName = importOptional.get();
                            if (classMap.containsKey(importName)) {
                                String classFirst = classMap.get(importName);
                                String className = classFirst.substring(classFirst.lastIndexOf(".") + 1);
                                nameExpr.setName(className);
                            }
                        } else {
                            String packageName = packageDeclarationNameAsString + "." + nameAsString;
                            if (classMap.containsKey(packageName)) {
                                String classFirst = classMap.get(packageName);
                                String className = classFirst.substring(classFirst.lastIndexOf(".") + 1);
                                nameExpr.setName(className);
                            }
                        }
                    }
                }
            }
            if (node.getChildNodes().size() > 0) {
                recursionClassNode(node.getChildNodes(), importsNameAsString, packageDeclarationNameAsString);
            }
        }
    }

    public static void main(String[] p_zUAwpxFK) throws IOException {
        blindClass();
    }
}
