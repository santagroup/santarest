package com.saltar;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;

public class FactoryGenerator implements Generator {
    private final ArrayList<SaltarActionClass> actionClasses;
    private final Filer filer;
    private final Elements elementUtils;

    public FactoryGenerator(ArrayList<SaltarActionClass> actionClasses, Filer filer, Elements elementUtils) {
        this.actionClasses = actionClasses;
        this.filer = filer;
        this.elementUtils = elementUtils;
    }

    @Override
    public void generate() throws IllegalAccessException {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("ActionHelperFactoryImpl")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ParameterizedTypeName.get(Saltar.ActionHelperFactory.class));

        MethodSpec.Builder makeMethodBuilder = MethodSpec.methodBuilder("make")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(Saltar.ActionHelper.class)
                .addParameter(Class.class, "actionClass");

        for (SaltarActionClass actionClass : actionClasses) {
            Object packageName = actionClass.getPackageName();
            makeMethodBuilder.beginControlFlow("if(actionClass == $L.$L.class)", packageName, actionClass.getName());
            makeMethodBuilder.addStatement(" return new $L.$L()", packageName, actionClass.getHelperName());
            makeMethodBuilder.endControlFlow();
        }
        makeMethodBuilder.addStatement("return null");
        classBuilder.addMethod(makeMethodBuilder.build());

        try {
            JavaFile.builder(Saltar.class.getPackage().getName(), classBuilder.build()).build().writeTo(filer);
        } catch (IOException e) {
            throw new IllegalAccessException(e.getMessage());
        }
    }
}
