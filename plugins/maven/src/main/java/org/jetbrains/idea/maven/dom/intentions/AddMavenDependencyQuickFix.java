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
package org.jetbrains.idea.maven.dom.intentions;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.LowPriorityAction;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.dom.MavenDomBundle;
import org.jetbrains.idea.maven.dom.MavenDomUtil;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;
import org.jetbrains.idea.maven.indices.MavenArtifactSearchDialog;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.project.MavenProject;

import java.util.List;

public class AddMavenDependencyQuickFix implements IntentionAction, LowPriorityAction {
  private final PsiJavaCodeReferenceElement myRef;

  public AddMavenDependencyQuickFix(PsiJavaCodeReferenceElement ref) {
    myRef = ref;
  }

  @NotNull
  public String getText() {
    return "Add Maven Dependency...";
  }

  @NotNull
  public String getFamilyName() {
    return MavenDomBundle.message("inspection.group");
  }

  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    return MavenDomUtil.findContainingProject(file) != null;
  }

  public void invoke(@NotNull final Project project, Editor editor, final PsiFile file) throws IncorrectOperationException {
    MavenProject mavenProject = MavenDomUtil.findContainingProject(file);
    if (mavenProject == null) return;

    final List<MavenId> ids = MavenArtifactSearchDialog.searchForClass(project, getReferenceText());
    if (ids.isEmpty()) return;

    final MavenDomProjectModel model = MavenDomUtil.getMavenDomProjectModel(project, mavenProject.getFile());
    if (model == null) return;

    new WriteCommandAction(project, "Add Maven Dependency", DomUtil.getFile(model)) {
      @Override
      protected void run(Result result) throws Throwable {
        for (MavenId each : ids) {
          MavenDomUtil.createDomDependency(model, null, each);
        }
      }
    }.execute();
  }

  private String getReferenceText() {
    PsiElement result = myRef;
    while (result.getParent() instanceof PsiJavaCodeReferenceElement) {
      result = result.getParent();
    }
    return result.getText();
  }

  public boolean startInWriteAction() {
    return false;
  }
}
