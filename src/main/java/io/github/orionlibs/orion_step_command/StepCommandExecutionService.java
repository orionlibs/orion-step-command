package io.github.orionlibs.orion_step_command;

import io.github.orionlibs.orion_reflection.method.access.ReflectionMethodAccessService;
import io.github.orionlibs.orion_tuple.Pair;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StepCommandExecutionService
{
    public static boolean run(String stepToRun)
    {
        boolean stepFound = false;
        for(Map.Entry<Pattern, Pair<Method, Object>> stepPatternStepMethod : StepCommands.stepPatternToStepDefinitionMapper.entrySet())
        {
            Matcher matcher = stepPatternStepMethod.getKey().matcher(stepToRun);
            if(matcher.find())
            {
                stepFound = true;
                List<String> stepInputs = new ArrayList<>();
                if(matcher.groupCount() > 0)
                {
                    for(int i = 1; i <= matcher.groupCount(); i++)
                    {
                        stepInputs.add(matcher.group(i));
                    }
                }
                String[] stepMethodArguments = stepInputs.toArray(new String[0]);
                ReflectionMethodAccessService.callMethod(stepPatternStepMethod.getValue().getFirst(), stepPatternStepMethod.getValue().getSecond(), stepMethodArguments);
                break;
            }
        }
        return stepFound;
    }
}