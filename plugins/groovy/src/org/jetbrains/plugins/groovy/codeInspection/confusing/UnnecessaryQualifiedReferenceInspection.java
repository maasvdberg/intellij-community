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
package org.jetbrains.plugins.groovy.codeInspection.confusing;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.codeInspection.BaseInspection;
import org.jetbrains.plugins.groovy.codeInspection.BaseInspectionVisitor;
import org.jetbrains.plugins.groovy.codeInspection.GroovyFix;
import org.jetbrains.plugins.groovy.codeInspection.GroovyInspectionBundle;
import org.jetbrains.plugins.groovy.intentions.style.ReplaceQualifiedReferenceWithImportIntention;
import org.jetbrains.plugins.groovy.lang.GrReferenceAdjuster;
import org.jetbrains.plugins.groovy.lang.psi.GrQualifiedReference;
import org.jetbrains.plugins.groovy.lang.psi.GrReferenceElement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrCodeReferenceElement;

/**
 * @author Max Medvedev
 */
public class UnnecessaryQualifiedReferenceInspection extends BaseInspection {
  private static final Logger LOG = Logger.getInstance(UnnecessaryQualifiedReferenceInspection.class);

  @Override
  protected BaseInspectionVisitor buildVisitor() {
    return new BaseInspectionVisitor() {
      @Override
      public void visitCodeReferenceElement(GrCodeReferenceElement refElement) {
        super.visitCodeReferenceElement(refElement);

        if (ReplaceQualifiedReferenceWithImportIntention.canBeReplacedWithImport(refElement)) {
          registerError(refElement);
        }
      }

      @Override
      public void visitReferenceExpression(GrReferenceExpression referenceExpression) {
        super.visitReferenceExpression(referenceExpression);

        if (ReplaceQualifiedReferenceWithImportIntention.canBeReplacedWithImport(referenceExpression)) {
          registerError(referenceExpression);
        }
      }
    };
  }

  @Nls
  @NotNull
  @Override
  public String getGroupDisplayName() {
    return CONFUSING_CODE_CONSTRUCTS;
  }

  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return GroovyInspectionBundle.message("unnecessary.qualified.reference");
  }

  @Override
  protected String buildErrorString(Object... args) {
    return GroovyInspectionBundle.message("unnecessary.qualified.reference");
  }

  @Override
  protected GroovyFix buildFix(PsiElement location) {
    return new GroovyFix() {
      @Override
      protected void doFix(Project project, ProblemDescriptor descriptor) throws IncorrectOperationException {
        final PsiElement startElement = descriptor.getStartElement();
        LOG.assertTrue(startElement instanceof GrReferenceElement);
        GrReferenceAdjuster.shortenReference((GrQualifiedReference)startElement);
      }

      @NotNull
      @Override
      public String getName() {
        return GroovyInspectionBundle.message("unnecessary.qualified.reference");
      }
    };
  }
}
