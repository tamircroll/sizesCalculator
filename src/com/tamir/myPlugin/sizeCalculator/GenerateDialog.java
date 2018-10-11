package com.tamir.myPlugin.sizeCalculator;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateDialog extends DialogWrapper
{
    private final LabeledComponent<JPanel> m_myComponent;
    private CollectionListModel<PsiField> m_myFields;
    
    protected GenerateDialog(PsiClass psiClass)
    {
        super(psiClass.getProject());
        setTitle("calculate?");
        
        List<PsiField> allDoubleFields = getAllValidDoubleFields(psiClass);
        m_myFields = new CollectionListModel<>(allDoubleFields);
        JList fieldList = new JList(m_myFields);
        fieldList.setCellRenderer(new DefaultListCellRenderer());
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(fieldList);
        decorator.disableAddAction();
        decorator.disableUpDownActions();
        JPanel panel = decorator.createPanel();
        m_myComponent = LabeledComponent.create(panel, "Fields that may be calculated");
        
        init();
    }
    
    @NotNull
    private List<PsiField> getAllValidDoubleFields(PsiClass psiClass)
    {
        List<PsiField> psiFields = Arrays.asList(psiClass.getAllFields());
        return psiFields.stream().filter(psiField -> PsiType.DOUBLE.equals(psiField.getType()) && Utils.isValidField(psiField)).collect(Collectors.toList());
    }
    
    @Nullable
    @Override
    protected JComponent createCenterPanel()
    {
        return m_myComponent;
    }
    
    public List<PsiField> getFields()
    {
        return m_myFields.getItems();
    }
    
    public List<PsiField> getPrimitiveNumericFields()
    {
        return m_myFields.getItems().stream().filter(psiField -> PsiType.DOUBLE.equals(psiField.getType())).collect(Collectors.toList());
    }
}
