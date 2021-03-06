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
package com.intellij.openapi.roots;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Provides information about files contained in a project.
 *
 * @see ProjectRootManager#getFileIndex()
 */
public interface ProjectFileIndex extends FileIndex {
  class SERVICE {
    private SERVICE() {
    }

    public static ProjectFileIndex getInstance(Project project) {
      return ServiceManager.getService(project, ProjectFileIndex.class);
    }
  }

  /**
   * Returns module to which the specified file belongs.
   *
   * @param file the file for which the module is requested.
   * @return the module instance or null if the file does not belong to content of any module.
   */
  @Nullable
  Module getModuleForFile(@NotNull VirtualFile file);

  /**
   * Returns the order entries which contain the specified file (either in CLASSES or SOURCES).
   *
   * @param file the file for which the order entries are requested.
   * @return the array of order entries containing the file.
   */
  @NotNull
  List<OrderEntry> getOrderEntriesForFile(@NotNull VirtualFile file);

  /**
   * Returns a classpath entry to which the specified file or directory belongs.
   *
   * @param file the file or directory for which the information is requested.
   * @return the file for the classpath entry, or null if the file is not a compiled
   *         class file or directory belonging to a library.
   */
  @Nullable
  VirtualFile getClassRootForFile(@NotNull VirtualFile file);

  /**
   * Returns the module source root or library source root to which the specified file
   * or directory belongs.
   *
   * @param file the file or directory for which the information is requested.
   * @return the file for the source root, or null if the file is not located under any
   *         of the source roots for the module.
   */
  @Nullable
  VirtualFile getSourceRootForFile(@NotNull VirtualFile file);

  /**
   * Returns the module content root to which the specified file or directory belongs.
   *
   * @param file the file or directory for which the information is requested.
   * @return the file for the content root, or null if the file does not belong to this project.
   */
  @Nullable
  VirtualFile getContentRootForFile(@NotNull VirtualFile file);

  /**
   * Returns the name of the package corresponding to the specified directory.
   *
   * @param dir the directory for which the package name is requested.
   * @return the package name, or null if the directory does not correspond to any package.
   */
  @Nullable
  String getPackageNameByDirectory(@NotNull VirtualFile dir); //Q: move to FileIndex?

  /**
   * Returns true if <code>file</code> is a compiled class file which belongs to some library.
   *
   * @param file the file to check.
   * @return true if the file belongs to library classes, false otherwise.
   */
  boolean isLibraryClassFile(@NotNull VirtualFile file);

  /**
   * Returns true if <code>fileOrDir</code> is a file or directory from the content source or library sources.
   *
   * @param fileOrDir the file or directory to check.
   * @return true if the file or directory belongs to project or library sources, false otherwise.
   */
  boolean isInSource(@NotNull VirtualFile fileOrDir);

  /**
   * Returns true if <code>fileOrDir</code> is a file or directory from library classes.
   *
   * @param fileOrDir the file or directory to check.
   * @return true if the file belongs to library classes, false otherwise.
   */
  boolean isInLibraryClasses(@NotNull VirtualFile fileOrDir);

  /**
   * Returns true if <code>fileOrDir</code> is a file or directory from library source.
   *
   * @param fileOrDir the file or directory to check.
   * @return true if the file belongs to library sources, false otherwise.
   */
  boolean isInLibrarySource(@NotNull VirtualFile fileOrDir);

  /**
   * Checks if the specified file or directory is ignored (either excluded by exclude roots
   * or ignored by {@link com.intellij.openapi.fileTypes.FileTypeManager#isFileIgnored(String)}).
   *
   * @param file the file to check.
   * @return true if <code>file</code> is ignored, false otherwise.
   */
  boolean isIgnored(@NotNull VirtualFile file);
}
