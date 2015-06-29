package com.saltar;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

public class FactoryGenerator extends Generator {

    public FactoryGenerator(Filer filer) {
        super(filer);
    }

    @Override
    public void generate(ArrayList<SaltarActionClass> actionClasses) {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(Saltar.HELPERS_FACTORY_CLASS_SIMPLE_NAME)
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
        saveClass(Saltar.class.getPackage().getName(), classBuilder.build());
    }
}
