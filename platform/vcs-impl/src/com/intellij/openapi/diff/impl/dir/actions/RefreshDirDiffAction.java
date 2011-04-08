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
package com.intellij.openapi.diff.impl.dir.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diff.impl.dir.DirDiffTableModel;
import com.intellij.util.Icons;

/**
 * @author Konstantin Bulenkov
 */
public class RefreshDirDiffAction extends DirDiffAction {
  public RefreshDirDiffAction(DirDiffTableModel model) {
    super(model, "Refresh", Icons.SYNCHRONIZE_ICON);
  }

  @Override
  public boolean isSelected(AnActionEvent e) {
    return false;
  }

  @Override
  public void setSelected(AnActionEvent e, boolean state) {
    //TODO getModel().refresh();
  }
}