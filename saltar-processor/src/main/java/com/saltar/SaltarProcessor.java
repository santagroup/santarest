package com.saltar;

import com.google.auto.service.AutoService;
import com.saltar.annotations.SaltarAction;
import com.saltar.validation.ClassValidator;
import com.saltar.validation.SaltarActionValidators;
import com.saltar.validation.ValidationError;

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
public class SaltarProcessor extends AbstractProcessor {
    private Elements elementUtils;
    private Messager messager;
    private ClassValidator classValidator;
    private SaltarActionValidators saltarActionValidators;
    private FactoryGenerator factoryGenerator;
    private HelpersGenerator helpersGenerator;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
        classValidator = new ClassValidator();
        saltarActionValidators = new SaltarActionValidators();
        Filer filer = processingEnv.getFiler();
        factoryGenerator = new FactoryGenerator(filer);
        helpersGenerator = new HelpersGenerator(filer);
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
        ArrayList<SaltarActionClass> actionClasses = new ArrayList<SaltarActionClass>();
        for (Element saltarElement : roundEnv.getElementsAnnotatedWith(SaltarAction.class)) {
            Set<ValidationError> errors = new HashSet<ValidationError>();
            errors.addAll(classValidator.validate(saltarElement));
            if (!errors.isEmpty()) {
                printErrors(errors);
                return true;
            }
            TypeElement typeElement = (TypeElement) saltarElement;
            SaltarActionClass actionClass = new SaltarActionClass(elementUtils, typeElement);
            errors.addAll(saltarActionValidators.validate(actionClass));
            if (!errors.isEmpty()) {
                printErrors(errors);
                return true;
            }
            actionClasses.add(actionClass);
        }

        if (!actionClasses.isEmpty()) {
            factoryGenerator.generate(actionClasses);
            helpersGenerator.generate(actionClasses);
        }
        return false;
    }

    private void printErrors(Collection<ValidationError> errors) {
        for (ValidationError error : errors) {
            messager.printMessage(Diagnostic.Kind.ERROR, error.getMessage(), error.getElement());
        }
    }

}