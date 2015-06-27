package com.saltar;

import com.google.auto.service.AutoService;
import com.saltar.annotations.SaltarAction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class SaltarProcessor extends AbstractProcessor {
    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    ArrayList<SaltarActionClass> actionClasses = new ArrayList<SaltarActionClass>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new HashSet<String>();
        annotataions.add(SaltarAction.class.getCanonicalName());
        return annotataions;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element saltarElement : roundEnv.getElementsAnnotatedWith(SaltarAction.class)) {
            try {
                new ClassValidator(saltarElement).validate();
                TypeElement typeElement = (TypeElement) saltarElement;
                SaltarActionClass actionClass = new SaltarActionClass(elementUtils, typeElement);
                new AnnotationsValidator(messager, actionClass).validate();
                actionClasses.add(actionClass);

                new HelperGenerator(actionClass, filer, elementUtils).generate();
            } catch (IllegalAccessException e) {
                error(saltarElement, e.getMessage());
                return true;
            }
        }

        if (!actionClasses.isEmpty()) {
            try {
                new FactoryGenerator(actionClasses, filer, elementUtils).generate();
            } catch (IllegalAccessException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                return true;
            }
//            error(actionClasses.get(0).getTypeElement(), "ddd");
            actionClasses.clear();
        }
        return false;
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }
}