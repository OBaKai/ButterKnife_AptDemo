package com.llk.annotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * author: llk
 * group: JJStudio
 */
public class BindUtils {

    public static <T> void bind(T activity){
        try {
            Class<?> clazz = Class.forName(activity.getClass().getCanonicalName() + "_ViewBinding");
            Constructor constructor = clazz.getConstructor();
            Object obj = constructor.newInstance();

            Method method = clazz.getDeclaredMethod("bind", activity.getClass());
            method.invoke(obj, activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
