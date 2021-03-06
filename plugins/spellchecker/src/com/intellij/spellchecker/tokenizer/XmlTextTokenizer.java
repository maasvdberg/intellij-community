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
package com.intellij.spellchecker.tokenizer;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.xml.XmlText;
import com.intellij.spellchecker.inspections.SplitterFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class XmlTextTokenizer extends Tokenizer<XmlText> {


  @Nullable
  @Override
  public Token[] tokenize(@NotNull XmlText element) {
    if(element.getContainingFile().getContext() != null) return null; // outer element should care of spell checking
    List<Pair<PsiElement,TextRange>> list = InjectedLanguageUtil.getInjectedPsiFiles(element);
    if (list != null && list.size() > 0) return null;
    return new Token[]{new Token<XmlText>(element, element.getText(),false, SplitterFactory.getInstance().getPlainTextSplitter())};
  }
}
