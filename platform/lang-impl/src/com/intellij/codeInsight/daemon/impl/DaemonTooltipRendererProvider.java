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

/*
 * @author max
 */
package com.intellij.codeInsight.daemon.impl;

import com.intellij.codeInsight.daemon.DaemonBundle;
import com.intellij.codeInsight.daemon.impl.actions.ShowErrorDescriptionAction;
import com.intellij.codeInsight.hint.LineTooltipRenderer;
import com.intellij.codeInsight.hint.TooltipLinkHandlerEP;
import com.intellij.codeInsight.hint.TooltipRenderer;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.ErrorStripTooltipRendererProvider;
import com.intellij.openapi.editor.impl.TrafficTooltipRenderer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.SmartList;
import com.intellij.util.ui.UIUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DaemonTooltipRendererProvider implements ErrorStripTooltipRendererProvider {
  private final Project myProject;

  public DaemonTooltipRendererProvider(final Project project) {
    myProject = project;
  }

  public TooltipRenderer calcTooltipRenderer(@NotNull final Collection<RangeHighlighter> highlighters) {
    LineTooltipRenderer bigRenderer = null;
    List<HighlightInfo> infos = new SmartList<HighlightInfo>();
    Collection<String> tooltips = new THashSet<String>(); //do not show same tooltip twice
    for (RangeHighlighter marker : highlighters) {
      final Object tooltipObject = marker.getErrorStripeTooltip();
      if (tooltipObject == null) continue;
      if (tooltipObject instanceof HighlightInfo) {
        HighlightInfo info = (HighlightInfo)tooltipObject;
        if (info.toolTip != null && tooltips.add(info.toolTip)) {
          infos.add(info);
        }
      }
      else {
        final String text = tooltipObject.toString();
        if (tooltips.add(text)) {
          if (bigRenderer == null) {
            bigRenderer = new MyRenderer(text, new Object[] {highlighters});
          }
          else {
            bigRenderer.addBelow(text);
          }
        }
      }
    }
    if (!infos.isEmpty()) {
      // show errors first
      Collections.sort(infos, new Comparator<HighlightInfo>() {
        public int compare(final HighlightInfo o1, final HighlightInfo o2) {
          int i = SeverityRegistrar.getInstance(myProject).compare(o2.getSeverity(), o1.getSeverity());
          if (i != 0) return i;
          return o1.toolTip.compareTo(o2.toolTip);
        }
      });
      final HighlightInfoComposite composite = new HighlightInfoComposite(infos);
      if (bigRenderer == null) {
        bigRenderer = new MyRenderer(UIUtil.convertSpace2Nbsp(composite.toolTip), new Object[] {highlighters});
      }
      else {
        final LineTooltipRenderer renderer = new MyRenderer(UIUtil.convertSpace2Nbsp(composite.toolTip), new Object[] {highlighters});
        renderer.addBelow(bigRenderer.getText());
        bigRenderer = renderer;
      }
    }
    return bigRenderer;
  }

  public TooltipRenderer calcTooltipRenderer(@NotNull final String text) {
    return new MyRenderer(text, new Object[] {text});
  }

  public TooltipRenderer calcTooltipRenderer(@NotNull final String text, final int width) {
    return new MyRenderer(text, width, new Object[] {text});
  }

  @Override
  public TrafficTooltipRenderer createTrafficTooltipRenderer(Runnable onHide, Editor editor) {
    return new TrafficTooltipRendererImpl(onHide, editor);
  }

  private static class MyRenderer extends LineTooltipRenderer {
    public MyRenderer(final String text, Object[] comparable) {
      super(text, comparable);
    }

    public MyRenderer(final String text, final int width, Object[] comparable) {
      super(text, width, comparable);
    }

    protected void onHide(final JComponent contentComponent) {
      ShowErrorDescriptionAction.rememberCurrentWidth(contentComponent.getWidth());
    }

    protected boolean dressDescription(@NotNull final Editor editor) {
      final String[] problems = UIUtil.getHtmlBody(myText).split(BORDER_LINE);
      String text = "";
      for (String problem : problems) {
        final String ref = getLinkRef(problem);
        if (ref != null) {
          String description = TooltipLinkHandlerEP.getDescription(ref, editor);
          if (description != null) {
            final Pattern pattern = Pattern.compile(".*Use.*(the (panel|checkbox|checkboxes|field|button|controls).*below).*", Pattern.DOTALL);
            final Matcher matcher = pattern.matcher(description);
            int startFindIdx = 0;
            while (matcher.find(startFindIdx)) {
              final int end = matcher.end(1);
              startFindIdx = end;
              description = description.substring(0, matcher.start(1)) + " inspection settings " + description.substring(end);
            }
            text += UIUtil.getHtmlBody(problem).replace(DaemonBundle.message("inspection.extended.description"),
                                                        DaemonBundle.message("inspection.collapse.description")) +
                    BORDER_LINE + UIUtil.getHtmlBody(description) + BORDER_LINE;
            break;
          }
        }
      }
      if (text.length() > 0) { //otherwise do not change anything
        myText = "<html><body>" +  StringUtil.trimEnd(text, BORDER_LINE) + "</body></html>";
        return true;
      }
      return false;
    }

    @Nullable
    private static String getLinkRef(@NonNls String text) {
      final String linkWithRef = "<a href=\"";
      final int linkStartIdx = text.indexOf(linkWithRef);
      if (linkStartIdx >= 0) {
        final String ref = text.substring(linkStartIdx + linkWithRef.length());
        final int quoteIdx = ref.indexOf('"');
        if (quoteIdx > 0) {
          return ref.substring(0, quoteIdx);
        }
      }
      return null;
    }

    protected void stripDescription() {
      final String[] problems = UIUtil.getHtmlBody(myText).split(BORDER_LINE);
      myText = "<html><body>";
      for (int i = 0; i < problems.length; i++) {
        final String problem = problems[i];
        if (i % 2 == 0) {
          myText += UIUtil.getHtmlBody(problem).replace(DaemonBundle.message("inspection.collapse.description"),
                                                 DaemonBundle.message("inspection.extended.description")) + BORDER_LINE;
        }
      }
      myText = StringUtil.trimEnd(myText, BORDER_LINE) + "</body></html>";
    }

    protected LineTooltipRenderer createRenderer(final String text, final int width) {
      return new MyRenderer(text, width, getEqualityObjects());
    }
  }
}