package com.gitee.freakchicken.blindjava.handler;

import com.gitee.freakchicken.blindjava.util.ConfigUtil;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class BlindNote {

    private static final String step3 = ConfigUtil.targetDir + "step3" + File.separator;

    private static final String step4 = ConfigUtil.targetDir + "step4" + File.separator;

    private static boolean m_rKBQU6V2(String p_tJJrJ4Lj) {
        for (String lf_ngUqDPn9 : ConfigUtil.excludeDirList) {
            String lf_vhj2ujGG = step3 + lf_ngUqDPn9;
            if (p_tJJrJ4Lj.startsWith(lf_vhj2ujGG)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] p_9uGutaHo) throws IOException {
        BlindNote blindNote = new BlindNote();
        blindNote.blindNote();
    }

    public void blindNote() throws IOException {
        FileUtils.deleteDirectory(new File(step4));
        Collection<File> files = FileUtils.listFiles(new File(step3), null, true);
        files.stream().filter(fileSingle -> !m_rKBQU6V2(fileSingle.getPath()) && fileSingle.isFile()).forEach(file -> {
            String filePath = file.getPath();
            String step3Path = filePath.substring(step3.length());
            String step4Path = step4 + step3Path;
            File step4File = new File(step4Path);
            try {
                FileUtils.forceMkdirParent(step4File);
                step4File.createNewFile();
                if (file.getName().endsWith(".java")) {
                    String noteSwitchResult = noteSwitch(file);
                    IOUtils.write(noteSwitchResult, new FileOutputStream(step4File), Charsets.UTF_8);
                } else {
                    IOUtils.copy(new FileInputStream(file), new FileOutputStream(step4File));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public String noteSwitch(File file) throws FileNotFoundException {
        JavaParser javaParser = new JavaParser();
        ParseResult<CompilationUnit> parse = javaParser.parse(file);
        if (parse.isSuccessful()) {
            Optional<CompilationUnit> parseResult = parse.getResult();
            CompilationUnit compilationUnit = parseResult.get();
            List<Node> childNodes = compilationUnit.getChildNodes();
            recursionNoteNode(childNodes);
            String compilationString = compilationUnit.toString();
            return compilationString;
        } else {
            throw new RuntimeException("Parse Error");
        }
    }

    private void recursionNoteNode(List<Node> nodes) {
        for (Node node : nodes) {
            Optional<Comment> comment = node.getComment();
            if (comment.isPresent()) {
                node.removeComment();
            } else {
                if (node instanceof LineComment) {
                    LineComment lineComment = (LineComment) node;
                    lineComment.setContent("");
                } else if (node instanceof JavadocComment) {
                    JavadocComment javadocComment = (JavadocComment) node;
                    javadocComment.setContent("");
                }
            }
            if (node.getChildNodes().size() > 0) {
                recursionNoteNode(node.getChildNodes());
            }
        }
    }
}
