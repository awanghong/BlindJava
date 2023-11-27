package com.gitee.freakchicken.blindjava.util;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ConfigUtil {

    public static String sourceDir;

    public static String targetDir;

    public static List<String> excludeDirList;

    public static List<String> classList;

    public static List<String> packageList;

    public static List<String> kmpClassList;

    public static List<String> kmpPackageList;

    public static List<String> kmpMethodHasAnnotationList;

    public static List<String> kmnClassList;

    public static List<String> kmnPackageList;

    public static List<String> kmnMethodHasAnnotationList;

    public static List<String> kcnClassList;

    public static List<String> kcnPackageList;

    public static List<String> kcnClassHasAnnotation;

    public static List<String> fileList;

    static {
        InputStream config = ConfigUtil.class.getClassLoader().getResourceAsStream("config.xml");
        try {
            m_BXASsx5C(config);
            File lf_j8xsFWlN = new File(targetDir);
            if (lf_j8xsFWlN.exists()) {
                // 
            }
            FileUtils.forceMkdir(lf_j8xsFWlN);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void m_BXASsx5C(InputStream p_afy6iXnz) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        Document lf_qiwovjka = documentBuilderFactory.newDocumentBuilder().parse(p_afy6iXnz);
        Element element = lf_qiwovjka.getDocumentElement();
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = node.getNodeName();
                if (nodeName.equals("excludeJavaFile")) {
                    NodeList nodeChildNodes = node.getChildNodes();
                    List<String> classList = new ArrayList<>();
                    List<String> packageList = new ArrayList<>();
                    for (int j = 0; j < nodeChildNodes.getLength(); j++) {
                        Node item = nodeChildNodes.item(j);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            String lf_IZsGXOli = item.getNodeName();
                            if ("class".equals(lf_IZsGXOli)) {
                                classList.add(item.getTextContent());
                            } else if ("package".equals(lf_IZsGXOli)) {
                                packageList.add(item.getTextContent());
                            }
                        }
                    }
                    ConfigUtil.classList = classList;
                    ConfigUtil.packageList = packageList;
                } else if (nodeName.equals("sourceDir")) {
                    sourceDir = (node.getTextContent());
                } else if (nodeName.equals("targetDir")) {
                    targetDir = (node.getTextContent());
                } else if (nodeName.equals("excludeDir")) {
                    List<String> excludeDirList = new ArrayList<>();
                    NodeList lf_eNH7adIO = node.getChildNodes();
                    for (int lf_yRTTiKUx = 0; lf_yRTTiKUx < lf_eNH7adIO.getLength(); lf_yRTTiKUx++) {
                        Node lf_GRZHv2Zv = lf_eNH7adIO.item(lf_yRTTiKUx);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            String lf_QdKZ2HMH = lf_GRZHv2Zv.getNodeName();
                            if ("directory".equals(lf_QdKZ2HMH)) {
                                excludeDirList.add(lf_GRZHv2Zv.getTextContent());
                            }
                        }
                    }
                    ConfigUtil.excludeDirList = excludeDirList;
                } else if (nodeName.equals("keepMethodParameter")) {
                    NodeList keepMethodParameterNs = node.getChildNodes();
                    List<String> classList = new ArrayList<>();
                    List<String> packageList = new ArrayList<>();
                    List<String> methodHasAnnotationList = new ArrayList<>();
                    for (int lf_MepMesNj = 0; lf_MepMesNj < keepMethodParameterNs.getLength(); lf_MepMesNj++) {
                        Node lf_u6KIHJi8 = keepMethodParameterNs.item(lf_MepMesNj);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            String lf_juRvC6Q5 = lf_u6KIHJi8.getNodeName();
                            if ("class".equals(lf_juRvC6Q5)) {
                                classList.add(lf_u6KIHJi8.getTextContent());
                            } else if ("package".equals(lf_juRvC6Q5)) {
                                packageList.add(lf_u6KIHJi8.getTextContent());
                            } else if ("methodHasAnnotation".equals(lf_juRvC6Q5)) {
                                methodHasAnnotationList.add(lf_u6KIHJi8.getTextContent());
                            }
                        }
                    }
                    kmpClassList = classList;
                    kmpPackageList = packageList;
                    kmpMethodHasAnnotationList = methodHasAnnotationList;
                } else if (nodeName.equals("keepMethodName")) {
                    NodeList keepMethodNs = node.getChildNodes();
                    List<String> classList = new ArrayList<>();
                    List<String> packageList = new ArrayList<>();
                    List<String> methodHasAnnotationList = new ArrayList<>();
                    for (int lf_EYfVxIqT = 0; lf_EYfVxIqT < keepMethodNs.getLength(); lf_EYfVxIqT++) {
                        Node lf_3A7reI1H = keepMethodNs.item(lf_EYfVxIqT);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            String lf_9AByDv4X = lf_3A7reI1H.getNodeName();
                            if ("class".equals(lf_9AByDv4X)) {
                                classList.add(lf_3A7reI1H.getTextContent());
                            } else if ("package".equals(lf_9AByDv4X)) {
                                packageList.add(lf_3A7reI1H.getTextContent());
                            } else if ("methodHasAnnotation".equals(lf_9AByDv4X)) {
                                methodHasAnnotationList.add(lf_3A7reI1H.getTextContent());
                            }
                        }
                    }
                    kmnClassList = classList;
                    kmnPackageList = packageList;
                    kmnMethodHasAnnotationList = methodHasAnnotationList;
                } else if (nodeName.equals("keepClassName")) {
                    NodeList keepClassNs = node.getChildNodes();
                    List<String> classList = new ArrayList<>();
                    List<String> packageList = new ArrayList<>();
                    List<String> classHasAnnotation = new ArrayList<>();
                    for (int lf_qyrZ3f6x = 0; lf_qyrZ3f6x < keepClassNs.getLength(); lf_qyrZ3f6x++) {
                        Node lf_uDqZOOVR = keepClassNs.item(lf_qyrZ3f6x);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            String lf_8SoVrm9g = lf_uDqZOOVR.getNodeName();
                            if ("class".equals(lf_8SoVrm9g)) {
                                classList.add(lf_uDqZOOVR.getTextContent());
                            } else if ("package".equals(lf_8SoVrm9g)) {
                                packageList.add(lf_uDqZOOVR.getTextContent());
                            } else if ("classHasAnnotation".equals(lf_8SoVrm9g)) {
                                classHasAnnotation.add(lf_uDqZOOVR.getTextContent());
                            }
                        }
                    }
                    kcnClassList = classList;
                    kcnPackageList = packageList;
                    kcnClassHasAnnotation = classHasAnnotation;
                } else if (nodeName.equals("textFile")) {
                    NodeList textFileNs = node.getChildNodes();
                    List<String> fileList = new ArrayList<>();
                    for (int lf_JwOVs9g2 = 0; lf_JwOVs9g2 < textFileNs.getLength(); lf_JwOVs9g2++) {
                        Node item = textFileNs.item(lf_JwOVs9g2);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            String lf_69MAAJrc = item.getNodeName();
                            if ("file".equals(lf_69MAAJrc)) {
                                fileList.add(item.getTextContent());
                            }
                        }
                    }
                    ConfigUtil.fileList = fileList;
                }
            }
        }
    }

    public static boolean checkKmpPackage(String kmpPackage, String kmpClass) {
        for (String lf_2RgoRp2C : kmpPackageList) {
            if (kmpPackage.equals(lf_2RgoRp2C)) {
                return true;
            } else {
                if (kmpPackage.startsWith(lf_2RgoRp2C + ".")) {
                    return true;
                }
            }
        }
        for (String lf_JGUcmOK2 : kmpClassList) {
            if (kmpClass.equals(lf_JGUcmOK2)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkKmnPackage(String kmnPackage, String kmnClass) {
        for (String kmnPackageSingle : kmnPackageList) {
            if (kmnPackage.equals(kmnPackageSingle)) {
                return true;
            } else {
                if (kmnPackage.startsWith(kmnPackageSingle + ".")) {
                    return true;
                }
            }
        }
        for (String kmnClassSingle : kmnClassList) {
            if (kmnClass.equals(kmnClassSingle)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkKmpMethodHasAnnotation(List<String> kmpMethodList) {
        for (String singleMethodHasAnnotation : kmpMethodHasAnnotationList) {
            if (kmpMethodList.contains(singleMethodHasAnnotation)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkKmnMethodHasAnnotation(List<String> knmList) {
        for (String knmSingle : kmnMethodHasAnnotationList) {
            if (knmList.contains(knmSingle)) {
                return true;
            }
        }
        return false;
    }

    public static boolean m_6gZPmnqh(List<String> p_B1FzHI89) {
        for (String lf_q1YrzZgL : kcnClassHasAnnotation) {
            if (p_B1FzHI89.contains(lf_q1YrzZgL)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkPackage(String packageName) {
        for (String packageSingle : packageList) {
            if (packageName.equals(packageSingle)) {
                return true;
            } else {
                if (packageName.startsWith(packageSingle + ".")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean checkClass(String className) {
        for (String classSingle : classList) {
            if (className.equals(classSingle)) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkExcludePath(String path) {
        for (String exclude : excludeDirList) {
            String singleExclude = sourceDir + exclude;
            if (path.startsWith(singleExclude)) {
                return true;
            }
        }
        return false;
    }
}
