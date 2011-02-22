/*
 * Copyright 2000-2011 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.codeInspection;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInsight.daemon.impl.analysis.GenericsHighlightUtil;
import com.intellij.codeInsight.intention.AddAnnotationFix;
import com.intellij.openapi.project.Project;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * User: anna
 * Date: 1/28/11
 */
public class SafeVarargsCanBeUsedInspection extends BaseJavaLocalInspectionTool {
  @Nls
  @NotNull
  @Override
  public String getGroupDisplayName() {
    return GroupNames.LANGUAGE_LEVEL_SPECIFIC_GROUP_NAME;
  }

  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return "Method can be annotated as @SafeVarargs";
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @NotNull
  @Override
  public String getShortName() {
    return "SafeVarargsDetector";
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
    return new JavaElementVisitor() {
      @Override
      public void visitMethod(PsiMethod method) {
        super.visitMethod(method);
        if (!PsiUtil.getLanguageLevel(method).isAtLeast(LanguageLevel.JDK_1_7)) return;
        if (AnnotationUtil.isAnnotated(method, "java.lang.SafeVarargs", false)) return;
        if (!method.isVarArgs()) return;
        final PsiParameter psiParameter = method.getParameterList().getParameters()[method.getParameterList().getParametersCount() - 1];
        final PsiType componentType = ((PsiEllipsisType)psiParameter.getType()).getComponentType();
        if (GenericsHighlightUtil.isReifiableType(componentType)) {
          return;
        }
        for (PsiReference reference : ReferencesSearch.search(psiParameter)) {
          final PsiElement element = reference.getElement();
          if (element instanceof PsiExpression && !PsiUtil.isAccessedForReading((PsiExpression)element)) {
            return;
          }
        }
        final PsiIdentifier nameIdentifier = method.getNameIdentifier();
        if (nameIdentifier != null) {
          holder.registerProblem(nameIdentifier, "Possible heap pollution from parameterized vararg type #loc",
                                 //todo check if can be final or static
                                 method.hasModifierProperty(PsiModifier.FINAL) || method.hasModifierProperty(PsiModifier.STATIC) ? new AnnotateAsSafeVarargsQuickFix() : null);
        }
      }

      @Override
      public void visitReferenceExpression(PsiReferenceExpression expression) {
      }
    };
  }

  private static class AnnotateAsSafeVarargsQuickFix implements LocalQuickFix {
    @NotNull
    @Override
    public String getName() {
      return "Annotate as @SafeVarargs";
    }

    @NotNull
    @Override
    public String getFamilyName() {
      return getName();
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      final PsiElement psiElement = descriptor.getPsiElement();
      if (psiElement instanceof PsiIdentifier) {
        final PsiMethod psiMethod = (PsiMethod)psiElement.getParent();
        new AddAnnotationFix("java.lang.SafeVarargs", psiMethod).applyFix(project, descriptor);
      }
    }
  }
}