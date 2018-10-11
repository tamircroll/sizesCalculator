package com.tamir.myPlugin.sizeCalculator;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.List;

public class CalculateManySizesAction extends AnAction
{
    @Override
    public void actionPerformed(AnActionEvent e)
    {
        PsiClass psiClass = getPsiClassFromContext(e);
        GenerateDialog dlg = new GenerateDialog(psiClass);
        dlg.show();
        if (dlg.isOK())
        {
            replaceFieldWithCalculation(psiClass, dlg.getFields());
        }
    }
    
    private void replaceFieldWithCalculation(PsiClass psiClass, List<PsiField> fields)
    {
        Project project = psiClass.getProject();
        new WriteCommandAction.Simple(project, psiClass.getContainingFile())
        {
            @Override
            protected void run()
            {
                fields.forEach(psiField ->
                {
                    PsiField doubleField = Utils.createDoubleField(psiField, getProject(), psiClass);
                    if (doubleField != null)
                    {
                        psiField.replace(doubleField);
                    }
                });
            }
        }.execute();
    }
    
    @Override
    public void update(AnActionEvent e)
    {
        PsiClass psiClass = getPsiClassFromContext(e);
        e.getPresentation().setEnabled(psiClass != null);
    }
    
    private PsiClass getPsiClassFromContext(AnActionEvent e)
    {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        
        if (psiFile == null || editor == null) return null;
        
        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAt = psiFile.findElementAt(offset);
        PsiClass psiClass = PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
        return psiClass;
    }
}
