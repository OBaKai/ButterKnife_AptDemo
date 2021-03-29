package com.llk.compiler;

import com.llk.annotation.BindClick;
import com.llk.annotation.BindLayout;
import com.llk.annotation.BindView;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * 让注解处理器工作起来的方法
 *
 * 方法一：手动生成Processor文件
 * 创建Processor文件
 * src/main/resources/META-INF/services/javax.annotation.processing.Processor
 * 在Processor文件填写内容
 * 注解处理器路径：包名 + 继承extends AbstractProcessor的类名
 *
 * 方法二：依赖auto-service库自动生成
 * implementation 'com.google.auto.service:auto-service:1.0-rc4'
 * 可使用@AutoService(Process.class)自动生成
 *
 * 注意！！！
 * @AutoService(Process.class) 高版本gradle没有自动生成文件，坑了我好久
 */

//@AutoService(Process.class)
public class BindCompiler extends AbstractProcessor {

    private static final String CLASS_TAIL = "_ViewBinding";

    private Messager messager;
    private Elements elementUtils;
    private Filer filer;
    private Types typeUtils;

    /**
     * 初始化方法
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        typeUtils = processingEnvironment.getTypeUtils();
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        System.err.println("===== init");
    }

    /**
     * 返回此注释 Processor 支持的Java版本
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 返回需要处理的注解集合
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> set = new HashSet<>();
        set.add(BindView.class.getCanonicalName());
        set.add(BindLayout.class.getCanonicalName());
        set.add(BindClick.class.getCanonicalName());
        return set;
    }

    /**
     * 注解处理器的核心方法，处理具体的注解
     *
     * 扫描所有类，获取所有存在指定注解的字段
     * @param set 扫描所有类文件，返回我们关注的注解类型（都是getSupportedAnnotationTypes方法里边配置的，只有用到的才返回）
     * @param roundEnvironment 扫描所有类文件，获取所有存在指定注解的字段
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.err.println("===== process");

        Map<Element, List<BindElementInfo>> map = new HashMap<>();

        for (Element element : roundEnvironment.getElementsAnnotatedWith(BindView.class)){
            String fieldName = element.getSimpleName().toString();
            int id = element.getAnnotation(BindView.class).id();
            Element supperElement = element.getEnclosingElement();
            BindElementInfo info = new BindElementInfo(BindView.class, fieldName, id);
            if (map.containsKey(supperElement)){
                map.get(supperElement).add(info);
            }else {
                List<BindElementInfo> list = new ArrayList<>();
                list.add(info);
                map.put(supperElement, list);
            }
        }

        for (Element element : roundEnvironment.getElementsAnnotatedWith(BindLayout.class)){
            int id = element.getAnnotation(BindLayout.class).id();
            BindElementInfo info = new BindElementInfo(BindLayout.class,
                    element.getSimpleName().toString(),
                    id);
            if (map.containsKey(element)){
                map.get(element).add(0, info);
            }else {
                List<BindElementInfo> list = new ArrayList<>();
                list.add(info);
                map.put(element, list);
            }
        }

        for (Element element : roundEnvironment.getElementsAnnotatedWith(BindClick.class)){
            int[] ids = element.getAnnotation(BindClick.class).ids();

            BindElementInfo info = new BindElementInfo(BindClick.class,
                    element.getSimpleName().toString(),
                    ids);

            Element supperElement= element.getEnclosingElement();
            if (map.containsKey(supperElement)){
                map.get(supperElement).add(info);
            }else {
                List<BindElementInfo> list = new ArrayList<>();
                list.add(info);
                map.put(supperElement, list);
            }
        }

        /**
         * aaaaa com.llk.kt.AActivity
         * bbbbb BindElementInfo{ ids=[2131165190] name=aBtn clazz=interface com.llk.annotation.BindView}
         * bbbbb BindElementInfo{ ids=[2131361820] name=AActivity clazz=interface com.llk.annotation.BindLayout}
         * aaaaa com.llk.kt.MainActivity
         * bbbbb BindElementInfo{ ids=[2131165251] name=btn clazz=interface com.llk.annotation.BindView}
         * bbbbb BindElementInfo{ ids=[2131165252] name=btn2 clazz=interface com.llk.annotation.BindView}
         * bbbbb BindElementInfo{ ids=[2131361821] name=MainActivity clazz=interface com.llk.annotation.BindLayout}
         * bbbbb BindElementInfo{ ids=[2131165251, 2131165252] name=click clazz=interface com.llk.annotation.BindClick}
         */
//        for (Element element : map.keySet()){
//            System.err.println("aaaaa " + element);
//            for (BindElementInfo i : map.get(element)){
//                System.err.println("bbbbb " + i.toString());
//            }
//        }

        for (Element element : map.keySet()){
            //包名
            String pkgName = elementUtils.getPackageOf(element).getQualifiedName().toString();

            //方法
            MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("bind") //方法名
                .addModifiers(Modifier.PUBLIC) //修饰符
                .returns(void.class) //返回值
                .addParameter(TypeName.get(element.asType()), "activity"); //传参

            //方法内添加代码
            List<BindElementInfo> bindElementInfos = map.get(element);
            BindElementInfo firstInfo = bindElementInfos.get(0);
            //setContentView(R.layout.activity_main);
            if (BindLayout.class.equals(firstInfo.clazz)){
                methodSpecBuilder.addStatement("activity.setContentView($L)", firstInfo.ids[0]);
            }

            for (BindElementInfo info : map.get(element)) {
                //Button btn = findViewById(R.id.btn);
                if (BindView.class.equals(info.clazz)) {
                    methodSpecBuilder.addStatement("activity.$L=activity.findViewById($L)",
                            info.name,
                            info.ids[0]);
                }else if (BindClick.class.equals(info.clazz)){
                    //findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
                    //            @Override
                    //            public void onClick(View v) { click(v); }
                    //        });
                    for (int id : info.ids){
                        methodSpecBuilder.addStatement(
                                "activity.findViewById($L).setOnClickListener(new android.view.View.OnClickListener() {\n" +
                                        "            @Override\n" +
                                        "            public void onClick(android.view.View v) { activity.$L(v); }\n" +
                                        "        });", id, info.name);
                    }
                }
            }
            MethodSpec methodSpec = methodSpecBuilder.build();

            TypeSpec typeSpec = TypeSpec.classBuilder(element.getSimpleName() + CLASS_TAIL) //类名
                    .addModifiers(Modifier.PUBLIC) //修饰符
                    .addMethod(methodSpec) //方法
                    .build();

            //通过包名和类生成一个java文件
            JavaFile build = JavaFile.builder(pkgName, typeSpec).build();
            try {
                build.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    static class BindElementInfo{
        int[] ids;
        String name;
        Class<? extends Annotation> clazz;

        public BindElementInfo(Class<? extends Annotation> clazz, String name, int... ids) {
            this.ids = ids;
            this.name = name;
            this.clazz = clazz;
        }

        @Override
        public String toString() {
            return "BindElementInfo{" +
                    " ids=" + Arrays.toString(ids) +
                    " name=" + name +
                    " clazz=" + clazz.toString() +
                    "}";
        }
    }
}