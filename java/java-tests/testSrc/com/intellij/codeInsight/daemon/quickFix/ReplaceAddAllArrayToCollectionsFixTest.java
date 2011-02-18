/*
 * User: anna
 * Date: 21-Mar-2008
 */
package com.intellij.codeInsight.daemon.quickFix;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.JavaSdkImpl;

public class ReplaceAddAllArrayToCollectionsFixTest extends LightQuickFixTestCase {
   public void test() throws Exception {
     doAllTests();
   }

  @Override
  protected String getBasePath() {
    return "/codeInsight/daemonCodeAnalyzer/quickFix/replaceAddAllArrayToCollections";
  }
}