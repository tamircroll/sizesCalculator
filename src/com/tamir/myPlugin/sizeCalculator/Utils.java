package com.tamir.myPlugin.sizeCalculator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils
{
    private static final Pattern splitFieldPattern = Pattern.compile("(.*)=(.*);(.*)");
    private static final int FIELD_DEF_INDEX = 1;
    private static final int FIELD_VALUE_INDEX = 2;
    private static final int FIELD_COMMENT_INDEX = 3;
    private static List<String> operatorList = Arrays.asList("+", "-", "/", "*");
    
    public static Double calculateExpression(String expressionToCalc)
    {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        
        try
        {
            return (Double) engine.eval(expressionToCalc);
        }
        catch (Exception e)
        {
            System.out.println("ERROR: Tamir: failed to evaluate expression: " + expressionToCalc);
            e.printStackTrace();
            return null;
        }
    }
    
    public static PsiField createDoubleField(PsiField psiField, Project project, PsiClass psiClass)
    {
        try
        {
            if (!isValidField(psiField))
            {
                return null;
            }
            
            Matcher matcher = splitFieldPattern.matcher(psiField.getText());
            matcher.find();
            String expression = matcher.group(FIELD_VALUE_INDEX).replaceAll("\\s", "");
            
            String comment = matcher.group(FIELD_COMMENT_INDEX);
            if (comment.isEmpty() || comment.matches("\\s+"))
            {
                comment = expression.startsWith("(") && expression.endsWith(")") ?
                        " // " + expression :
                        " // (" + expression + ")";
            }
            
            Double calculationResult = calculateExpression(expression);
            if (calculationResult != null)
            {
                return JavaPsiFacade.getElementFactory(project).createFieldFromText(String.format("%s = %s;%s", matcher.group(FIELD_DEF_INDEX), calculationResult, comment), psiClass);
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    public static boolean isValidField(PsiField psiField)
    {
        try
        {
            Matcher matcher = splitFieldPattern.matcher(psiField.getText());
            matcher.find();
            
            if (matcher.groupCount() != 3)
            {
                return false;
            }
            
            String expression = matcher.group(FIELD_VALUE_INDEX).replaceAll("\\s", "");
            if (operatorList.stream().noneMatch(expression::contains))
            {
                return false;
            }
            
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
