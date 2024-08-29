package io.github.orionlibs.orion_step_command;

import io.github.orionlibs.orion_assert.InaccessibleException;
import io.github.orionlibs.orion_reflection.classes.ReflectionClassesService;
import io.github.orionlibs.orion_reflection.method.retrieval.ReflectionMethodRetrievalService;
import io.github.orionlibs.orion_tuple.Pair;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

public class StepCommandInitialisationService
{
    public static void initialise(String stepDefinitionPackages)
    {
        ConfigurationBuilder config = new ConfigurationBuilder().forPackages(stepDefinitionPackages)
                        .addScanners(Scanners.TypesAnnotated);
        Reflections reflections = new Reflections(config);
        Set<Class<?>> steps = reflections.getTypesAnnotatedWith(Steps.class);
        for(Class<?> stepDefinitionClass : steps)
        {
            storeStepDefinitionMethods(stepDefinitionClass);
        }
        if(!StepCommands.stepMethodsFoundAndTheirClass.isEmpty())
        {
            for(Map.Entry<Method, Class<?>> stepMethodAndClass : StepCommands.stepMethodsFoundAndTheirClass.entrySet())
            {
                Step stepAnnotation = stepMethodAndClass.getKey().getAnnotation(Step.class);
                if(stepAnnotation != null)
                {
                    try
                    {
                        Object stepsDefinitionObject = ReflectionClassesService.instantiateClass(stepMethodAndClass.getValue(), null, null);
                        StepCommands.stepPatternToStepDefinitionMapper.put(Pattern.compile(stepAnnotation.value()), Pair.<Method, Object>of(stepMethodAndClass.getKey(), stepsDefinitionObject));
                    }
                    catch(InvocationTargetException e)
                    {
                        throw new RuntimeException(e);
                    }
                    catch(InaccessibleException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }


    private static void storeStepDefinitionMethods(Class<?> clazz)
    {
        Method[] methods = clazz.getDeclaredMethods();
        for(Method method : methods)
        {
            if(ReflectionMethodRetrievalService.isPublicMethod(method))
            {
                Step stepAnnotation = method.getAnnotation(Step.class);
                if(stepAnnotation != null)
                {
                    StepCommands.stepMethodsFoundAndTheirClass.put(method, clazz);
                }
            }
        }
    }
}