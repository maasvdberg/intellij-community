/*
 * Copyright 2000-2010 JetBrains s.r.o.
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

/*
 * @author max
 */
package com.intellij.util.ui;

import com.intellij.ui.SeparatorComponent;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class FormBuilder {
  private static final boolean ALIGN_LABELS_TO_RIGHT = UIUtil.isUnderAquaLookAndFeel();

  private int line = 0;
  private int indent;
  private final JPanel panel;
  private boolean vertical;

  /**
   * @param vertical labels will be placed on their own rows
   */
  public FormBuilder(final boolean vertical, final int indent) {
    this.vertical = vertical;
    panel = new JPanel(new GridBagLayout());
    this.indent = indent;
  }

  public FormBuilder(final boolean vertical) {
    this(vertical, 5);
  }

  public FormBuilder() {
    this(false, 5);
  }

  public FormBuilder addLabeledComponent(String labelText, JComponent component, final int verticalSpace) {
    JLabel label = null;
    if (labelText != null) {
      label = new JLabel(UIUtil.removeMnemonic(labelText));
      label.setDisplayedMnemonicIndex(UIUtil.getDisplayMnemonicIndex(labelText));
      label.setLabelFor(component);
    }

    return addTwoComponents(label, component, verticalSpace, false);
  }

  public FormBuilder addLabeledComponent(String labelText, JComponent component) {
    return addLabeledComponent(labelText, component, 10);
  }

  public FormBuilder addSeparator(final int verticalSpace) {
    return addTwoComponents(new SeparatorComponent(3, 0), new SeparatorComponent(3, 0), verticalSpace, true);
  }

  public FormBuilder addSeparator() {
    return addSeparator(10);
  }

  public FormBuilder addTwoComponents(@Nullable JComponent component1, JComponent component2, final int verticalSpace, boolean fillFirstComponent) {
    GridBagConstraints c = new GridBagConstraints();
    int verticalInset = line > 0 ? verticalSpace : 0;

    if (vertical) {
      c.gridwidth = 1;

      c.gridx = 0;
      c.gridy = line;
      c.weightx = 1.0;
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.WEST;
      c.insets = new Insets(verticalInset, 0, 0, this.indent);

      if (component1 != null) panel.add(component1, c);

      c.gridx = 0;
      c.gridy = line + 1;
      c.weightx = 1.0;
      c.fill = component2 instanceof JComboBox ? GridBagConstraints.NONE : GridBagConstraints.HORIZONTAL;
      c.anchor = GridBagConstraints.WEST;
      c.insets = new Insets(0, 0, 0, this.indent);

      panel.add(component2, c);

      line += 2;
    }
    else {
      c.gridx = 0;
      c.gridy = line;
      c.weightx = 0;
      c.anchor = ALIGN_LABELS_TO_RIGHT ? GridBagConstraints.EAST : GridBagConstraints.WEST;
      c.insets = new Insets(verticalInset, 0, 0, fillFirstComponent ? 0 : this.indent);

      if (fillFirstComponent) c.fill = GridBagConstraints.HORIZONTAL;

      if (component1 != null) panel.add(component1, c);

      c.gridx = 1;
      c.gridy = line;
      c.fill = component2 instanceof JComboBox ? GridBagConstraints.NONE : GridBagConstraints.HORIZONTAL;
      c.anchor = GridBagConstraints.WEST;
      c.weightx = 1;
      c.insets = new Insets(verticalInset, 0, 0, 0);
      panel.add(component2, c);

      line++;
    }

    return this;
  }

  public JPanel getPanel() {
    return panel;
  }

  public int getLine() {
    return line;
  }
}
