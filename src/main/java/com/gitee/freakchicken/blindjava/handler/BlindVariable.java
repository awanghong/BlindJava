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
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BlindVariable {

    private static final Logger log = LoggerFactory.getLogger(BlindVariable.class);

    private static Map<String, Map<String, String>> classVariableMap = new HashMap<>();

    List<File> parsedFile = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        BlindVariable blindVariable = new BlindVariable();
        blindVariable.blindVariable();
    }

    public void blindVariable() throws IOException {
        obtainClassNameAndPackageName();
        Collection<File> fileCollection = FileUtils.listFiles(new File(ConfigUtil.sourceDir), null, true);
        String firstStep = ConfigUtil.targetDir + "step1" + File.separator;
        FileUtils.deleteDirectory(new File(firstStep));
        fileCollection.stream().filter(fileSingle -> !ConfigUtil.checkExcludePath(fileSingle.getPath()) && fileSingle.isFile()).forEach(file -> {
            String filePath = file.getPath();
            String substring = filePath.substring(ConfigUtil.sourceDir.length());
            File blindFile = new File(firstStep + substring);
            try {
                FileUtils.forceMkdirParent(blindFile);
                blindFile.createNewFile();
                if (file.getName().endsWith(".java")) {
                    Map<String, String> classNameAndPackageName = FileUtil.obtainClassNameAndPackageName(file);
                    log.info(classNameAndPackageName.get("packageName") + ":" + classNameAndPackageName.get("className"));
                    if ((!ConfigUtil.checkPackage(classNameAndPackageName.get("packageName"))) && (!ConfigUtil.checkClass(classNameAndPackageName.get("className")))) {
                        String parseResult = parseFile(file);
                        IOUtils.write(parseResult, new FileOutputStream(blindFile), Charsets.UTF_8);
                        parsedFile.add(blindFile);
                    } else {
                        log.info("excluded: " + firstStep);
                        IOUtils.copy(new FileInputStream(file), new FileOutputStream(blindFile));
                    }
                } else {
                    IOUtils.copy(new FileInputStream(file), new FileOutputStream(blindFile));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        log.info("变量名混淆成功");
    }

    private void obtainClassNameAndPackageName() throws IOException {
        Collection<File> fileCollection = FileUtils.listFiles(new File(ConfigUtil.sourceDir), null, true);
        String firstFile = ConfigUtil.targetDir + "step1" + File.separator;
        FileUtils.deleteDirectory(new File(firstFile));
        fileCollection.stream().filter(file -> !ConfigUtil.checkExcludePath(file.getPath()) && file.isFile()).forEach(singleFile -> {
            try {
                if (singleFile.getName().endsWith(".java")) {
                    Map<String, String> allClassPackage = FileUtil.obtainClassNameAndPackageName(singleFile);
                    log.info(allClassPackage.get("packageName") + ":" + allClassPackage.get("className"));
                    if ((!ConfigUtil.checkPackage(allClassPackage.get("packageName"))) && (!ConfigUtil.checkClass(allClassPackage.get("className")))) {
                        obtainClassVariable(singleFile);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void obtainClassVariable(File file) throws FileNotFoundException {
        String nameAsString = null;
        JavaParser javaParser = new JavaParser();
        ParseResult<CompilationUnit> parse = javaParser.parse(file);
        if (parse.isSuccessful()) {
            Optional<CompilationUnit> parseResult = parse.getResult();
            CompilationUnit compilationUnit = parseResult.get();
            List<Node> childNodes = compilationUnit.getChildNodes();
            for (Node node : childNodes) {
                if (node instanceof ImportDeclaration) {
                } else if (node instanceof PackageDeclaration) {
                    nameAsString = ((PackageDeclaration) node).getNameAsString();
                } else if (node instanceof ClassOrInterfaceDeclaration) {
                    ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) node;
                    String classOrInterfaceDeclarationNameAsString = classOrInterfaceDeclaration.getNameAsString();
                    List<FieldDeclaration> fields = classOrInterfaceDeclaration.getFields();
                    String allName = nameAsString + "." + classOrInterfaceDeclarationNameAsString;
                    Map<String, String> classMap = new HashMap<String, String>();
                    for (FieldDeclaration fieldDeclaration : fields) {
                        NodeList<VariableDeclarator> variables = fieldDeclaration.getVariables();
                        for (VariableDeclarator variableDeclarator : variables) {
                            String variableDeclaratorNameAsString = variableDeclarator.getNameAsString();
                            classMap.put(variableDeclaratorNameAsString, "gf_" + RandomStringUtils.random(8, true, true));
                        }
                    }
                    classVariableMap.put(allName, classMap);
                }
            }
        } else {
            throw new RuntimeException("Parse Error");
        }
    }

    public String parseFile(File file) throws FileNotFoundException {
        JavaParser javaParser = new JavaParser();
        ParseResult<CompilationUnit> parse = javaParser.parse(file);
        if (parse.isSuccessful()) {
            Optional<CompilationUnit> parseResult = parse.getResult();
            CompilationUnit compilationUnit = parseResult.get();
            PackageDeclaration packageDeclaration = compilationUnit.getPackageDeclaration().get();
            String packageName = packageDeclaration.getNameAsString();
            NodeList<ImportDeclaration> imports = compilationUnit.getImports();
            List<String> importList = imports.stream().map(importDeclaration -> importDeclaration.getNameAsString()).collect(Collectors.toList());
            List<Node> childNodes = compilationUnit.getChildNodes();
            StringBuffer stringBuffer = new StringBuffer();
            for (Node node : childNodes) {
                if (node instanceof ImportDeclaration) {
                } else if (node instanceof PackageDeclaration) {
                    // 
                } else if (node instanceof ClassOrInterfaceDeclaration) {
                    ClassOrInterfaceDeclaration classOrInterfaceDeclaration = (ClassOrInterfaceDeclaration) node;
                    String ns = classOrInterfaceDeclaration.getNameAsString();
                    List<Node> nodes = classOrInterfaceDeclaration.getChildNodes();
                    List<MethodDeclaration> methods = classOrInterfaceDeclaration.getMethods();
                    List<FieldDeclaration> fields = classOrInterfaceDeclaration.getFields();
                    String allPath = packageName + "." + ns;
                    if (classOrInterfaceDeclaration.isInterface()) {
                    } else {
                        for (MethodDeclaration methodDeclaration : methods) {
                            Map<String, String> methodMap = new HashMap<>();
                            NodeList<AnnotationExpr> annotations = methodDeclaration.getAnnotations();
                            List<String> lf_BS1OYtR2 = annotations.stream().map(annotationSingle -> annotationSingle.getNameAsString()).collect(Collectors.toList());
                            if (ConfigUtil.checkKmpPackage(packageName, allPath) || ConfigUtil.checkKmpMethodHasAnnotation(lf_BS1OYtR2)) {
                                // 
                            } else {
                                NodeList<Parameter> parameters = methodDeclaration.getParameters();
                                for (Parameter parameter : parameters) {
                                    String parameterNameAsString = parameter.getNameAsString();
                                    methodMap.put(parameterNameAsString, "p_" + RandomStringUtils.random(8, true, true));
                                    parameter.setName(methodMap.get(parameterNameAsString));
                                }
                            }
                            Optional<BlockStmt> body = methodDeclaration.getBody();
                            if (body.isPresent()) {
                                recursionNode(body.get().getChildNodes(), methodMap);
                            }
                        }
                        Map<String, String> classVariable = classVariableMap.get(allPath);
                        for (FieldDeclaration fieldDeclaration : fields) {
                            NodeList<VariableDeclarator> variables = fieldDeclaration.getVariables();
                            for (VariableDeclarator variableDeclarator : variables) {
                                String lf_ymcyxdNb = variableDeclarator.getNameAsString();
                                variableDeclarator.setName(classVariable.get(lf_ymcyxdNb));
                                // 
                                recursionNode(variableDeclarator.getChildNodes(), classVariable);
                            }
                        }
                        for (MethodDeclaration methodDeclaration : methods) {
                            Optional<BlockStmt> body = methodDeclaration.getBody();
                            if (body.isPresent()) {
                                recursionMethodNode(body.get().getChildNodes(), classVariable);
                            }
                        }
                        List<InitializerDeclaration> initializerDeclarations = nodes.stream().filter(singleNode -> singleNode instanceof InitializerDeclaration).map(item -> (InitializerDeclaration) item).collect(Collectors.toList());
                        for (InitializerDeclaration initializerDeclaration : initializerDeclarations) {
                            List<Node> declarationChildNodes = initializerDeclaration.getChildNodes();
                            recursionNode(declarationChildNodes, new HashMap<String, String>());
                            recursionMethodNode(declarationChildNodes, classVariable);
                        }
                        List<ConstructorDeclaration> constructorDeclarations = nodes.stream().filter(single -> single instanceof ConstructorDeclaration).map(item -> (ConstructorDeclaration) item).collect(Collectors.toList());
                        for (ConstructorDeclaration constructorDeclaration : constructorDeclarations) {
                            List<Node> constructorDeclarationChildNodes = constructorDeclaration.getChildNodes();
                            Map<String, String> map = new HashMap<String, String>();
                            NodeList<Parameter> parameters = constructorDeclaration.getParameters();
                            for (Parameter parameter : parameters) {
                                String parameterNameAsString = parameter.getNameAsString();
                                map.put(parameterNameAsString, "p_" + RandomStringUtils.random(8, true, true));
                                parameter.setName(map.get(parameterNameAsString));
                            }
                            recursionNode(constructorDeclarationChildNodes, map);
                            recursionMethodNode(constructorDeclarationChildNodes, classVariable);
                        }
                    }
                    recursionClassNode(nodes, classVariableMap, importList, packageName);
                } else {
                }
                stringBuffer.append(node.toString());
            }
            return stringBuffer.toString();
        } else {
            throw new RuntimeException("Parse Error");
        }
    }

    public void recursionNode(List<Node> nodeList, Map<String, String> map) {
        for (Node node : nodeList) {
            // 
            // 
            if (node instanceof VariableDeclarator) {
                VariableDeclarator variableDeclarator = (VariableDeclarator) node;
                map.put(variableDeclarator.getNameAsString(), "lf_" + RandomStringUtils.random(8, true, true));
                variableDeclarator.setName(map.get(variableDeclarator.getNameAsString()));
            }
            // 
            if (node instanceof LambdaExpr) {
                LambdaExpr lambdaExpr = (LambdaExpr) node;
                NodeList<Parameter> lf_KAXMetUI = lambdaExpr.getParameters();
                for (Parameter lf_cl8JTJH5 : lf_KAXMetUI) {
                    map.put(lf_cl8JTJH5.getNameAsString(), "lambda_lf_" + RandomStringUtils.random(8, true, true));
                    lf_cl8JTJH5.setName(map.get(lf_cl8JTJH5.getNameAsString()));
                }
            }
            if (node instanceof NameExpr) {
                NameExpr nameExpr = (NameExpr) node;
                if (map.containsKey(nameExpr.getNameAsString())) {
                    nameExpr.setName(map.get(nameExpr.getNameAsString()));
                }
            } else {
                List<Node> childNodes = node.getChildNodes();
                if (childNodes.size() > 0) {
                    recursionNode(childNodes, map);
                }
            }
        }
    }

    public void recursionMethodNode(List<Node> nodeList, Map<String, String> map) {
        for (Node node : nodeList) {
            if (node instanceof NameExpr) {
                NameExpr nameExpr = (NameExpr) node;
                if (map.containsKey(nameExpr.getNameAsString())) {
                    nameExpr.setName(map.get(nameExpr.getNameAsString()));
                }
            } else if (node instanceof FieldAccessExpr) {
                FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) node;
                if (map.containsKey(fieldAccessExpr.getNameAsString())) {
                    fieldAccessExpr.setName(map.get(fieldAccessExpr.getNameAsString()));
                }
            } else {
                List<Node> childNodes = node.getChildNodes();
                if (childNodes.size() > 0) {
                    recursionMethodNode(childNodes, map);
                }
            }
        }
    }

    public void recursionClassNode(List<Node> nodeList, Map<String, Map<String, String>> classVariableMap, List<String> importList, String packageName) {
        for (Node node : nodeList) {
            if (node instanceof FieldAccessExpr) {
                List<Node> childNodes = node.getChildNodes();
                if (childNodes.size() == 2 && childNodes.get(0) instanceof NameExpr && childNodes.get(1) instanceof SimpleName) {
                    NameExpr nameExpr = (NameExpr) childNodes.get(0);
                    SimpleName simpleName = (SimpleName) childNodes.get(1);
                    String nameAsString = nameExpr.getNameAsString();
                    Optional<String> first = importList.stream().filter(importSingle -> importSingle.substring(importSingle.lastIndexOf(".") + 1).equals(nameAsString)).findFirst();
                    if (first.isPresent()) {
                        String className = first.get();
                        if (classVariableMap.containsKey(className)) {
                            Map<String, String> classSingleMap = classVariableMap.get(className);
                            if (classSingleMap.containsKey(simpleName.getIdentifier())) {
                                simpleName.setIdentifier(classSingleMap.get(simpleName.getIdentifier()));
                            }
                        }
                    } else {
                        String className = packageName + "." + nameAsString;
                        if (classVariableMap.containsKey(className)) {
                            Map<String, String> classSingleMap = classVariableMap.get(className);
                            if (classSingleMap.containsKey(simpleName.getIdentifier())) {
                                simpleName.setIdentifier(classSingleMap.get(simpleName.getIdentifier()));
                            }
                        }
                    }
                }
            } else {
                List<Node> childNodes = node.getChildNodes();
                if (childNodes.size() > 0) {
                    recursionClassNode(childNodes, classVariableMap, importList, packageName);
                }
            }
        }
    }
}
