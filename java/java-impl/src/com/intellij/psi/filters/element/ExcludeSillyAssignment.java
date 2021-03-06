/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
package com.intellij.psi.filters.element;

import com.intellij.psi.*;
import com.intellij.psi.filters.ElementFilter;

/**
 * Created by IntelliJ IDEA.
 * User: ik
 * Date: 06.01.2004
 * Time: 17:59:58
 * To change this template use Options | File Templates.
 */
public class ExcludeSillyAssignment implements ElementFilter {
  public boolean isAcceptable(Object element, PsiElement context) {
    if(!(element instanceof PsiElement)) return true;

    PsiElement each = context;
    while (each != null && !(each instanceof PsiFile)) {
      if (each instanceof PsiExpressionList || each instanceof PsiPrefixExpression || each instanceof PsiPolyadicExpression) {
        return true;
      }

      if (each instanceof PsiAssignmentExpression) {
        final PsiExpression left = ((PsiAssignmentExpression)each).getLExpression();
        if (left instanceof PsiReferenceExpression) {
          final PsiReferenceExpression referenceExpression = (PsiReferenceExpression)left;
          final PsiElement qualifier = referenceExpression.getQualifier();
          if (qualifier != null) {
            if (!(qualifier instanceof PsiThisExpression) || ((PsiThisExpression)qualifier).getQualifier() != null) {
              return true;
            }
          }

          return !referenceExpression.isReferenceTo((PsiElement)element);
        }
        return true;
      }

      each = each.getContext();
    }

    return true;
  }

  public boolean isClassAcceptable(Class hintClass) {
    return true;
  }
}
