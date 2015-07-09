package com.santarest;

import com.google.auto.service.AutoService;

import com.santarest.annotations.RestAction;
import com.santarest.validation.ClassValidator;
import com.santarest.validation.RestActionValidators;
import com.santarest.validation.ValidationError;

import java.util.ArrayList;
import java.util.Collection;
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
import javax.tools.Diagnostic;

@AutoService(Processor.class)
//@SupportedAnnotationTypes({"com.santarest.RestAction"})
public class SantaProcessor extends AbstractProcessor {
    private Elements elementUtils;
    private Messager messager;
    private ClassValidator classValidator;
    private RestActionValidators restActionValidators;
    private FactoryGenerator factoryGenerator;
    private HelpersGenerator helpersGenerator;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
        classValidator = new ClassValidator();
        restActionValidators = new RestActionValidators();
        Filer filer = processingEnv.getFiler();
        factoryGenerator = new FactoryGenerator(filer);
        helpersGenerator = new HelpersGenerator(filer);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new HashSet<String>();
        annotataions.add(RestAction.class.getCanonicalName());
        return annotataions;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if(annotations.isEmpty()) return true;
        ArrayList<RestActionClass> actionClasses = new ArrayList<RestActionClass>();
        for (Element saltarElement : roundEnv.getElementsAnnotatedWith(RestAction.class)) {
            Set<ValidationError> errors = new HashSet<ValidationError>();
            errors.addAll(classValidator.validate(saltarElement));
            if (!errors.isEmpty()) {
                printErrors(errors);
                continue;
            }
            TypeElement typeElement = (TypeElement) saltarElement;
            RestActionClass actionClass = new RestActionClass(elementUtils, typeElement);
            errors.addAll(restActionValidators.validate(actionClass));
            if (!errors.isEmpty()) {
                printErrors(errors);
                continue;
            }
            actionClasses.add(actionClass);
        }
        if (!actionClasses.isEmpty()) {
            helpersGenerator.generate(actionClasses);
        }
        factoryGenerator.generate(actionClasses);
        return true;
    }

    private void printErrors(Collection<ValidationError> errors) {
        for (ValidationError error : errors) {
            messager.printMessage(Diagnostic.Kind.ERROR, error.getMessage(), error.getElement());
        }
    }

}