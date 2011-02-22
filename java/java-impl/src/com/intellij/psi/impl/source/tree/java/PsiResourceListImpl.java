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
package com.intellij.psi.impl.source.tree.java;

import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiResource;
import com.intellij.psi.PsiResourceList;
import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.intellij.psi.impl.source.tree.JavaElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PsiResourceListImpl extends CompositePsiElement implements PsiResourceList {
  public PsiResourceListImpl() {
    super(JavaElementType.RESOURCE_LIST);
  }

  @NotNull
  @Override
  public List<PsiResource> getResources() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PsiResource.class);
  }

  @Override
  public void accept(@NotNull final PsiElementVisitor visitor) {
    if (visitor instanceof JavaElementVisitor) {
      ((JavaElementVisitor)visitor).visitResourceList(this);
    }
    else {
      visitor.visitElement(this);
    }
  }

  @Override
  public String toString() {
    return "PsiResourceList:" + getText();
  }
}