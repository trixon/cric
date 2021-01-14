/*
 * Copyright 2021 Patrik Karlström.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.trixon.cric;

import java.util.prefs.Preferences;
import se.trixon.almond.util.OptionsBase;

/**
 *
 * @author Patrik Karlström
 */
public class Options extends OptionsBase {

    public static final String KEY_JLINK = "path.jlink";
    public static final String KEY_LINUX_JDK = "path.linux.jdk";
    public static final String KEY_LINUX_JFX = "path.linux.jfx";
    public static final String KEY_MAC_JDK = "path.mac.jdk";
    public static final String KEY_MAC_JFX = "path.mac.jfx";
    public static final String KEY_PROFILES = "profiles";
    public static final String KEY_PROFILE_BIND_SERVICES = "bind_services";
    public static final String KEY_PROFILE_COMPRESS = "compress";
    public static final String KEY_PROFILE_CUSTOM_MODULES = "custom_modules";
    public static final String KEY_PROFILE_CUSTOM_MODULE_PATH = "custom_module_path";
    public static final String KEY_PROFILE_ENDIAN = "endian";
    public static final String KEY_PROFILE_IGNORE_SIGNING = "ignore_signing";
    public static final String KEY_PROFILE_MODULES = "modules";
    public static final String KEY_PROFILE_NO_HEADER = "no_header";
    public static final String KEY_PROFILE_NO_MAN = "no_man";
    public static final String KEY_PROFILE_OUTPUT = "output";
    public static final String KEY_PROFILE_STRIP_DEBUG = "strip_debug";
    public static final String KEY_WINDOWS_JDK = "path.windows.jdk";
    public static final String KEY_WINDOWS_JFX = "path.windows.jfx";

    public static Options getInstance() {
        return OptionsHolder.INSTANCE;
    }

    private Options() {
        setPreferences(Preferences.userNodeForPackage(App.class));
    }

    private static class OptionsHolder {

        private static final Options INSTANCE = new Options();
    }
}
