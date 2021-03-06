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
package com.intellij.notification;

import com.intellij.notification.impl.NotificationSettings;
import com.intellij.notification.impl.NotificationsConfiguration;
import com.intellij.openapi.ui.MessageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter
 */
public class NotificationGroup {
  private final String myDisplayId;

  public NotificationGroup(String displayId, NotificationDisplayType defaultDisplayType, boolean logByDefault) {
    this(displayId, defaultDisplayType, logByDefault, null);
  }

  public NotificationGroup(String displayId, NotificationDisplayType defaultDisplayType, boolean logByDefault, @Nullable String toolWindowId) {
    myDisplayId = displayId;

    NotificationsConfiguration configuration = NotificationsConfiguration.getNotificationsConfiguration();
    configuration.registerDefaultSettings(new NotificationSettings(displayId, defaultDisplayType, logByDefault));
    if (toolWindowId != null) {
      configuration.registerToolWindowCapability(displayId, toolWindowId);
    }
  }

  public static NotificationGroup balloonGroup(@NotNull String displayId) {
    return new NotificationGroup(displayId, NotificationDisplayType.BALLOON, true);
  }

  public static NotificationGroup logOnlyGroup(@NotNull String displayId) {
    return new NotificationGroup(displayId, NotificationDisplayType.NONE, true);
  }

  public static NotificationGroup toolWindowGroup(@NotNull String displayId, @NotNull String toolWindowId, final boolean logByDefault) {
    return new NotificationGroup(displayId, NotificationDisplayType.TOOL_WINDOW, logByDefault, toolWindowId);
  }

  public String getDisplayId() {
    return myDisplayId;
  }

  public Notification createNotification(@NotNull final String content, @NotNull final MessageType type) {
    return createNotification(content, type.toNotificationType());
  }

  public Notification createNotification(@NotNull final String content, @NotNull final NotificationType type) {
    return createNotification("", content, type, null);
  }

  public Notification createNotification(@NotNull final String title, @NotNull final String content, @NotNull final NotificationType type, @Nullable NotificationListener listener) {
    return new Notification(myDisplayId, title, content, type, listener);
  }
}
