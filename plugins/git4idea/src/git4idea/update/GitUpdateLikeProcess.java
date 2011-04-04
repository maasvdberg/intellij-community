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
package git4idea.update;

import com.intellij.ide.GeneralSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.util.continuation.Continuation;
import com.intellij.util.continuation.ContinuationContext;
import com.intellij.util.continuation.TaskDescriptor;
import com.intellij.util.continuation.Where;
import com.intellij.util.ui.UIUtil;

/**
 * @author irengrig
 *         Date: 3/31/11
 *         Time: 2:59 PM
 */
public abstract class GitUpdateLikeProcess {
  private final Project myProject;
  private GeneralSettings myGeneralSettings;
  private ProjectManagerEx myProjectManager;

  public GitUpdateLikeProcess(final Project project) {
    myProject = project;
    myGeneralSettings = GeneralSettings.getInstance();
    myProjectManager = ProjectManagerEx.getInstanceEx();
  }

  public void execute() {
    final boolean saveOnFrameDeactivation = myGeneralSettings.isSaveOnFrameDeactivation();
    final boolean syncOnFrameDeactivation = myGeneralSettings.isSyncOnFrameActivation();
    myProjectManager.blockReloadingProjectOnExternalChanges();
    UIUtil.invokeAndWaitIfNeeded(new Runnable() {
      @Override
      public void run() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          @Override
          public void run() {
            FileDocumentManager.getInstance().saveAllDocuments();
            myGeneralSettings.setSaveOnFrameDeactivation(false);
            myGeneralSettings.setSyncOnFrameActivation(false);
          }
        });
      }
    });

    final Continuation continuation = new Continuation(myProject, true);
    final ContinuationContext.GatheringContinuationContext initContext = new ContinuationContext.GatheringContinuationContext();
    initContext.next(new TaskDescriptor("Git: updating", Where.POOLED) {
        @Override
        public void run(final ContinuationContext context) {
          runImpl(context);
        }
      }, new TaskDescriptor("", Where.AWT) {
      @Override
      public void run(ContinuationContext context) {
        myProjectManager.unblockReloadingProjectOnExternalChanges();
        myGeneralSettings.setSaveOnFrameDeactivation(saveOnFrameDeactivation);
        myGeneralSettings.setSyncOnFrameActivation(syncOnFrameDeactivation);
      }
    });
    continuation.runAndWait(initContext.getList());
  }

  protected abstract void runImpl(ContinuationContext context);
}