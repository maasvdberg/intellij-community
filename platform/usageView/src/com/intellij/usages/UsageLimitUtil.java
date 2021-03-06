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
package com.intellij.usages;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.ui.GuiUtils;
import com.intellij.usageView.UsageViewBundle;
import org.jetbrains.annotations.NotNull;

/**
 * User: cdr
 */
public class UsageLimitUtil {
  public static final int USAGES_LIMIT = 1000;

  public static void showAndCancelIfAborted(final Project project, final String message) {
    int retCode = showTooManyUsagesWarning(project, message);

    if (retCode != DialogWrapper.OK_EXIT_CODE) {
      throw new ProcessCanceledException();
    }
  }

  public static int showTooManyUsagesWarning(@NotNull Project project, @NotNull String message) {
    String[] buttons = {UsageViewBundle.message("button.text.continue"), UsageViewBundle.message("button.text.abort")};
    return showMessage(project, message, UsageViewBundle.message("find.excessive.usages.title"), buttons);
  }

  private static int runOrInvokeAndWait(final Computable<Integer> f) {
    final int[] answer = new int[1];
    try {
      GuiUtils.runOrInvokeAndWait(new Runnable() {
        public void run() {
          answer[0] = f.compute();
        }
      });
    }
    catch (Exception e) {
      answer[0] = 0;
    }

    return answer[0];
  }

  private static int showMessage(final Project project, final String message, final String title, final String[] buttons) {
    return runOrInvokeAndWait(new Computable<Integer>() {
      public Integer compute() {
        return Messages.showOkCancelDialog(project, message, title, buttons[0], buttons[1], Messages.getWarningIcon());
      }
    });
  }
}
