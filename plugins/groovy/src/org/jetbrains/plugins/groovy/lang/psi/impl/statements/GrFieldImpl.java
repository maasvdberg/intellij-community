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
package org.jetbrains.plugins.groovy.lang.psi.impl.statements;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiImplUtil;
import com.intellij.psi.impl.ResolveScopeManager;
import com.intellij.psi.presentation.java.JavaPresentationUtil;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.GroovyIcons;
import org.jetbrains.plugins.groovy.lang.groovydoc.psi.api.GrDocComment;
import org.jetbrains.plugins.groovy.lang.groovydoc.psi.impl.GrDocCommentUtil;
import org.jetbrains.plugins.groovy.lang.parser.GroovyElementTypes;
import org.jetbrains.plugins.groovy.lang.psi.GrVariableEnhancer;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFileBase;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrNamedArgumentSearchVisitor;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinitionBody;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrAccessorMethod;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrTypeElement;
import org.jetbrains.plugins.groovy.lang.psi.impl.synthetic.GrAccessorMethodImpl;
import org.jetbrains.plugins.groovy.lang.psi.stubs.GrFieldStub;
import org.jetbrains.plugins.groovy.lang.psi.util.PsiUtil;

import javax.swing.*;

/**
 * User: Dmitry.Krasilschikov
 * Date: 25.05.2007
 */
public class GrFieldImpl extends GrVariableBaseImpl<GrFieldStub> implements GrField, StubBasedPsiElement<GrFieldStub> {
  private GrAccessorMethod mySetter;
  private GrAccessorMethod[] myGetters;

  private boolean mySetterInitialized = false;

  public GrFieldImpl(@NotNull ASTNode node) {
    super(node);
  }

  public GrFieldImpl(GrFieldStub stub) {
    this(stub, GroovyElementTypes.FIELD);
  }

  public GrFieldImpl(GrFieldStub stub, IStubElementType nodeType) {
    super(stub, nodeType);
  }

  @Override
  public PsiElement getParent() {
    return getParentByStub();
  }

  public void accept(GroovyElementVisitor visitor) {
    visitor.visitField(this);
  }

  @Override
  public GrTypeElement getTypeElementGroovy() {
    final GrFieldStub stub = getStub();
    if (stub != null) {
      final String typeText = stub.getTypeText();
      if (typeText == null) {
        return null;
      }

      return GroovyPsiElementFactory.getInstance(getProject()).createTypeElement(typeText, this);
    }

    return super.getTypeElementGroovy();
  }

  public String toString() {
    return "Field";
  }

  public void setInitializer(@Nullable PsiExpression psiExpression) throws IncorrectOperationException {
  }

  public boolean isDeprecated() {
    final GrFieldStub stub = getStub();
    boolean byDocTag = stub == null ? PsiImplUtil.isDeprecatedByDocTag(this) : stub.isDeprecatedByDocTag();
    if (byDocTag) {
      return true;
    }

    return PsiImplUtil.isDeprecatedByAnnotation(this);
  }

  @Override
  public PsiType getTypeGroovy() {
    if (getDeclaredType() == null && getInitializerGroovy() == null) {
      final PsiType type = GrVariableEnhancer.getEnhancedType(this);
      if (type != null) {
        return type;
      }
    }
    return super.getTypeGroovy();
  }

  public PsiClass getContainingClass() {
    PsiElement parent = getParent().getParent();
    if (parent instanceof GrTypeDefinitionBody) {
      final PsiElement pparent = parent.getParent();
      if (pparent instanceof PsiClass) {
        return (PsiClass)pparent;
      }
    }

    final PsiFile file = getContainingFile();
    if (file instanceof GroovyFileBase) {
      return ((GroovyFileBase)file).getScriptClass();
    }

    return null;
  }

  public boolean isProperty() {
    final GrFieldStub stub = getStub();
    if (stub != null) {
      return stub.isProperty();
    }
    return PsiUtil.isProperty(this);
  }

  public GrAccessorMethod getSetter() {
    if (mySetterInitialized) return mySetter;

    mySetter = GrAccessorMethodImpl.createSetterMethod(this);
    mySetterInitialized = true;

    return mySetter;
  }

  public void clearCaches() {
    mySetterInitialized = false;
    mySetter = null;
    myGetters = null;
  }

  @NotNull
  public GrAccessorMethod[] getGetters() {
    if (myGetters == null) {
      myGetters = GrAccessorMethodImpl.createGetterMethods(this);
    }

    return myGetters;
  }

  @NotNull
  public SearchScope getUseScope() {
    if (isProperty()) {
      return ResolveScopeManager.getElementUseScope(this); //maximal scope
    }
    return PsiImplUtil.getMemberUseScope(this);
  }

  @NotNull
  @Override
  public String getName() {
    final GrFieldStub stub = getStub();
    if (stub != null) {
      return stub.getName();
    }
    return super.getName();
  }

  @Override
  public ItemPresentation getPresentation() {
    return JavaPresentationUtil.getFieldPresentation(this);
  }

  public PsiElement getOriginalElement() {
    final PsiClass containingClass = getContainingClass();
    if (containingClass == null) return this;
    PsiClass originalClass = (PsiClass)containingClass.getOriginalElement();
    PsiField originalField = originalClass.findFieldByName(getName(), false);
    return originalField != null ? originalField : this;
  }

  @Nullable
  public Icon getIcon(int flags) {
    Icon superIcon = GroovyIcons.FIELD;
    if (!isProperty()) return superIcon;
    LayeredIcon rowIcon = new LayeredIcon(2);
    rowIcon.setIcon(superIcon, 0);
    rowIcon.setIcon(GroovyIcons.DEF, 1);
    return rowIcon;
  }

  @NotNull
  public String[] getNamedParametersArray() {
    final GrFieldStub stub = getStub();
    if (stub != null) {
      return stub.getNamedParameters();
    }

    return GrNamedArgumentSearchVisitor.find(this);
  }

  public GrDocComment getDocComment() {
    return GrDocCommentUtil.findDocComment(this);
  }
}
