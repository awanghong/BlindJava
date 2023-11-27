package com.gitee.freakchicken.blindjava.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FileUtil {

    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);

    public static Map<String, String> obtainClassNameAndPackageName(File file) {
        try {
            log.info(file.getPath());
            JavaParser javaParser = new JavaParser();
            ParseResult<CompilationUnit> parse = javaParser.parse(file);
            if (parse.isSuccessful()) {
                Optional<CompilationUnit> parseResult = parse.getResult();
                CompilationUnit compilationUnit = parseResult.get();
                PackageDeclaration packageDeclaration = compilationUnit.getPackageDeclaration().get();
                String nameAsString = packageDeclaration.getNameAsString();
                NodeList<TypeDeclaration<?>> types = compilationUnit.getTypes();
                String typeZero = types.get(0).getNameAsString();
                Map<String, String> result = new HashMap<>();
                result.put("className", nameAsString + "." + typeZero);
                result.put("packageName", nameAsString);
                return result;
            } else {
                throw new RuntimeException("Parse Error");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] p_77myHTxQ) {
        File lf_IdpHAhmR = new File("D:\\git\\super-api\\dbapi-cluster-apiServer\\src\\main\\java\\com\\gitee\\freakchicken\\dbapi\\apiserver\\conf\\FilterConfig.java");
        Map<String, String> lf_TeCY89p6 = FileUtil.obtainClassNameAndPackageName(lf_IdpHAhmR);
    }
}
