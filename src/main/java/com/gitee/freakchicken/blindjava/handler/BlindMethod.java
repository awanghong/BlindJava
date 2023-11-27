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
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BlindMethod {

    private static final Logger log = LoggerFactory.getLogger(BlindMethod.class);

    private static final String step1 = ConfigUtil.targetDir + "step1" + File.separator;

    private static final String step2 = ConfigUtil.targetDir + "step2" + File.separator;

    // 
    // 
    static Map<String, Map<String, String>> classMethodSwitch = new HashMap<>();

    public static boolean checkPath(String path) {
        for (String excludeDir : ConfigUtil.excludeDirList) {
            String step1Path = step1 + excludeDir;
            if (path.startsWith(step1Path)) {
                return true;
            }
        }
        return false;
    }

    public static void blindMethod() throws IOException {
        FileUtils.deleteDirectory(new File(step2));
        Collection<File> fileCollection = FileUtils.listFiles(new File(step1), null, true);
        fileCollection.stream().filter(singleFile -> !checkPath(singleFile.getPath()) && singleFile.isFile()).forEach(file -> {
            String filePath = file.getPath();
            String step1Path = filePath.substring(step1.length());
            String step2Path = step2 + step1Path;
            File step2File = new File(step2Path);
            try {
                FileUtils.forceMkdirParent(step2File);
                step2File.createNewFile();
                if (file.getName().endsWith(".java")) {
                    Map<String, String> classNameAndPackageName = FileUtil.obtainClassNameAndPackageName(file);
                    log.info(classNameAndPackageName.get("packageName") + ":" + classNameAndPackageName.get("className"));
                    if ((!ConfigUtil.checkPackage(classNameAndPackageName.get("packageName"))) && (!ConfigUtil.checkClass(classNameAndPackageName.get("className")))) {
                        methodSwitch(file);
                    } else {
                        log.info("excluded: " + step2Path);
                        IOUtils.copy(new FileInputStream(file), new FileOutputStream(step2File));
                    }
                } else {
                    IOUtils.copy(new FileInputStream(file), new FileOutputStream(step2File));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        fileCollection.stream().filter(singleFile -> !checkPath(singleFile.getPath()) && singleFile.isFile()).forEach(file -> {
            String filePath = file.getPath();
            String step1Path = filePath.substring(step1.length());
            String step2Path = step2 + step1Path;
            File step2File = new File(step2Path);
            try {
                FileUtils.forceMkdirParent(step2File);
                step2File.createNewFile();
                if (file.getName().endsWith(".java")) {
                    Map<String, String> classNameAndPackageName = FileUtil.obtainClassNameAndPackageName(file);
                    if ((!ConfigUtil.checkPackage(classNameAndPackageName.get("packageName"))) && (!ConfigUtil.checkClass(classNameAndPackageName.get("className")))) {
                        String methodResult = methodParse(file);
                        IOUtils.write(methodResult, new FileOutputStream(step2File), Charsets.UTF_8);
                    } else {
                        IOUtils.copy(new FileInputStream(file), new FileOutputStream(step2File));
                    }
                } else {
                    IOUtils.copy(new FileInputStream(file), new FileOutputStream(step2File));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        log.info("方法名混淆成功");
    }

    private static String methodParse(File file) throws IOException {
        JavaParser javaParser = new JavaParser();
        ParseResult<CompilationUnit> parse = javaParser.parse(file);
        StringBuffer sb = new StringBuffer();
        if (parse.isSuccessful()) {
            Optional<CompilationUnit> parseResult = parse.getResult();
            CompilationUnit compilationUnit = parseResult.get();
            PackageDeclaration packageDeclaration = compilationUnit.getPackageDeclaration().get();
            String packageName = packageDeclaration.getNameAsString();
            NodeList<ImportDeclaration> imports = compilationUnit.getImports();
            List<String> importList = imports.stream().map(singleImport -> singleImport.getNameAsString()).collect(Collectors.toList());
            sb.append(packageDeclaration.toString());
            imports.stream().forEach(singleImport -> {
                sb.append(singleImport.toString());
            });
            NodeList<TypeDeclaration<?>> types = compilationUnit.getTypes();
            types.stream().forEach(typeDeclaration -> {
                String typeName = typeDeclaration.getNameAsString();
                String classAll = packageName + "." + typeName;
                List<FieldDeclaration> fields = typeDeclaration.getFields();
                Map<String, String> map = new HashMap<>();
                for (FieldDeclaration fieldDeclaration : fields) {
                    NodeList<VariableDeclarator> variables = fieldDeclaration.getVariables();
                    for (VariableDeclarator variableDeclarator : variables) {
                        String typeAsString = variableDeclarator.getTypeAsString();
                        String nameAsString = variableDeclarator.getNameAsString();
                        Optional<String> optionalS = importList.stream().filter(importSingle -> importSingle.substring(importSingle.lastIndexOf(".") + 1).equals(typeAsString)).findFirst();
                        if (optionalS.isPresent()) {
                            String s = optionalS.get();
                            map.put(nameAsString, s);
                        } else {
                            map.put(nameAsString, null);
                        }
                    }
                }
                List<Node> childNodes = typeDeclaration.getChildNodes();
                recursionMethodNode(childNodes, map, classAll, importList);
                // 
                for (MethodDeclaration methodDeclaration : typeDeclaration.getMethods()) {
                    Optional<BlockStmt> body = methodDeclaration.getBody();
                    if (body.isPresent()) {
                        Map<String, String> mapMethod = new HashMap<String, String>();
                        recursionMethodChildrenNode(body.get().getChildNodes(), mapMethod, classAll, importList);
                    }
                }
                String lf_V5ASaHJW = packageName + "." + typeName;
                if (classMethodSwitch.containsKey(lf_V5ASaHJW)) {
                    Map<String, String> lf_WCvDsfzA = classMethodSwitch.get(lf_V5ASaHJW);
                    List<MethodDeclaration> lf_RIOvDaLd = typeDeclaration.getMethods();
                    lf_RIOvDaLd.stream().forEach(lambda_lf_9fDe6DJK -> {
                        if (lf_WCvDsfzA.containsKey(lambda_lf_9fDe6DJK.getNameAsString())) {
                            lambda_lf_9fDe6DJK.setName(lf_WCvDsfzA.get(lambda_lf_9fDe6DJK.getNameAsString()));
                        }
                    });
                }
                sb.append(typeDeclaration.toString());
            });
            return sb.toString();
            // 
            // 
            // 
        } else {
            throw new RuntimeException("parse failed");
        }
    }

    public static void recursionMethodChildrenNode(List<Node> nodeList, Map<String, String> map, String classAll, List<String> importList) {
        for (Node node : nodeList) {
            if (node instanceof VariableDeclarator) {
                VariableDeclarator variableDeclarator = (VariableDeclarator) node;
                String typeAsString = variableDeclarator.getTypeAsString();
                String nameAsString = variableDeclarator.getNameAsString();
                Optional<String> first = importList.stream().filter(importSingle -> importSingle.substring(importSingle.lastIndexOf(".") + 1).equals(typeAsString)).findFirst();
                if (first.isPresent()) {
                    String importFirst = first.get();
                    if (classMethodSwitch.containsKey(importFirst)) {
                        map.put(nameAsString, importFirst);
                    }
                } else {
                    String classMany = classAll.substring(0, classAll.lastIndexOf("."));
                    String classMethodName = classMany + "." + typeAsString;
                    if (classMethodSwitch.containsKey(classMethodName)) {
                        map.put(nameAsString, classMethodName);
                    }
                }
            }
            if (node instanceof MethodCallExpr) {
                MethodCallExpr methodCallExpr = (MethodCallExpr) node;
                List<Node> childNodes = methodCallExpr.getChildNodes();
                if (childNodes.size() >= 2) {
                    if (childNodes.get(0) instanceof NameExpr && childNodes.get(1) instanceof SimpleName) {
                        String methodZero = ((NameExpr) childNodes.get(0)).getNameAsString();
                        String methodCallExprNameAsString = methodCallExpr.getNameAsString();
                        if (map.containsKey(methodZero)) {
                            String s = map.get(methodZero);
                            if (s != null) {
                                if (classMethodSwitch.containsKey(s)) {
                                    Map<String, String> methodSwitch = classMethodSwitch.get(s);
                                    if (methodSwitch.containsKey(methodCallExprNameAsString)) {
                                        String value = methodSwitch.get(methodCallExprNameAsString);
                                        methodCallExpr.setName(value);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            List<Node> childNodes = node.getChildNodes();
            if (childNodes.size() > 0) {
                recursionMethodChildrenNode(childNodes, map, classAll, importList);
            }
        }
    }

    public static void recursionMethodNode(List<Node> childNodes, Map<String, String> map, String classAll, List<String> importList) {
        for (Node node : childNodes) {
            if (node instanceof MethodCallExpr) {
                MethodCallExpr methodCallExpr = (MethodCallExpr) node;
                List<Node> nodeList = methodCallExpr.getChildNodes();
                if (nodeList.get(0) instanceof NameExpr) {
                    if (nodeList.get(0) instanceof NameExpr && nodeList.get(1) instanceof SimpleName) {
                        String nameAsString = ((NameExpr) nodeList.get(0)).getNameAsString();
                        String methodCallExprNameAsString = methodCallExpr.getNameAsString();
                        if (map.containsKey(nameAsString)) {
                            String s = map.get(nameAsString);
                            if (s != null) {
                                if (classMethodSwitch.containsKey(s)) {
                                    Map<String, String> singleClassMethod = classMethodSwitch.get(s);
                                    if (singleClassMethod.containsKey(methodCallExprNameAsString)) {
                                        String methodName = singleClassMethod.get(methodCallExprNameAsString);
                                        methodCallExpr.setName(methodName);
                                    }
                                }
                            }
                        } else {
                            String lf_W4PHesgJ = nameAsString;
                            Optional<String> lf_x0Oon2Vg = importList.stream().filter(lambda_lf_dPIDHefs -> lambda_lf_dPIDHefs.substring(lambda_lf_dPIDHefs.lastIndexOf(".") + 1).equals(lf_W4PHesgJ)).findFirst();
                            if (lf_x0Oon2Vg.isPresent()) {
                                String lf_RQSlzrM7 = lf_x0Oon2Vg.get();
                                if (classMethodSwitch.containsKey(lf_RQSlzrM7)) {
                                    Map<String, String> lf_BfSAwG4g = classMethodSwitch.get(lf_RQSlzrM7);
                                    if (lf_BfSAwG4g.containsKey(methodCallExprNameAsString)) {
                                        String lf_ZwgA0FOe = lf_BfSAwG4g.get(methodCallExprNameAsString);
                                        methodCallExpr.setName(lf_ZwgA0FOe);
                                    }
                                }
                            } else {
                                String lf_T9f3YClq = classAll.substring(0, classAll.lastIndexOf("."));
                                String lf_oJ0hkNH5 = lf_T9f3YClq + "." + lf_W4PHesgJ;
                                if (classMethodSwitch.containsKey(lf_oJ0hkNH5)) {
                                    Map<String, String> lf_iR8pvvcm = classMethodSwitch.get(lf_oJ0hkNH5);
                                    if (lf_iR8pvvcm.containsKey(methodCallExprNameAsString)) {
                                        String lf_02QQqL3Z = lf_iR8pvvcm.get(methodCallExprNameAsString);
                                        methodCallExpr.setName(lf_02QQqL3Z);
                                    }
                                }
                            }
                        }
                    }
                } else if (nodeList.get(0) instanceof SimpleName) {
                    String lf_hfIZwb2d = methodCallExpr.getNameAsString();
                    if (classMethodSwitch.containsKey(classAll)) {
                        Map<String, String> lf_QRh8kV4p = classMethodSwitch.get(classAll);
                        if (lf_QRh8kV4p.containsKey(lf_hfIZwb2d)) {
                            String lf_hTGCMpMA = lf_QRh8kV4p.get(lf_hfIZwb2d);
                            methodCallExpr.setName(lf_hTGCMpMA);
                        }
                    }
                } else if (nodeList.get(0) instanceof ThisExpr) {
                }
            }
            List<Node> lf_ByJD7CNS = node.getChildNodes();
            if (lf_ByJD7CNS.size() > 0) {
                recursionMethodNode(lf_ByJD7CNS, map, classAll, importList);
            }
        }
    }

    private static void methodSwitch(File file) throws IOException {
        JavaParser javaParser = new JavaParser();
        ParseResult<CompilationUnit> parse = javaParser.parse(file);
        if (parse.isSuccessful()) {
            Optional<CompilationUnit> parseResult = parse.getResult();
            CompilationUnit compilationUnit = parseResult.get();
            String packageName = compilationUnit.getPackageDeclaration().get().getNameAsString();
            NodeList<TypeDeclaration<?>> types = compilationUnit.getTypes();
            types.stream().forEach(typeDeclaration -> {
                String typeName = typeDeclaration.getNameAsString();
                if (!ConfigUtil.checkKmnPackage(packageName, packageName + "." + typeName)) {
                    List<MethodDeclaration> methods = typeDeclaration.getMethods();
                    Map<String, String> methodSwitch = new HashMap<>();
                    Set<String> methodNameSet = methods.stream().filter(methodDeclaration -> {
                        List<String> methodAnnotations = methodDeclaration.getAnnotations().stream().map(annotationExpr -> annotationExpr.getNameAsString()).collect(Collectors.toList());
                        return !ConfigUtil.checkKmnMethodHasAnnotation(methodAnnotations);
                    }).map(item -> {
                        String nameAsString = item.getNameAsString();
                        return nameAsString;
                    }).collect(Collectors.toSet());
                    methodNameSet.remove("main");
                    if (methodNameSet.size() > 0) {
                        methodNameSet.stream().forEach(methodNameSingle -> {
                            methodSwitch.put(methodNameSingle, "m_" + RandomStringUtils.random(8, true, true));
                        });
                        classMethodSwitch.put(packageName + "." + typeName, methodSwitch);
                    }
                }
            });
        }
    }

    public static void main(String[] p_XhJlfy5l) throws IOException {
        blindMethod();
        // 
        // 
    }
}
