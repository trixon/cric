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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import se.trixon.almond.util.OptionsBase;

/**
 *
 * @author Patrik Karlström
 */
public class Options extends OptionsBase {

    private static final String KEY_JLINK_DEBUG = "jlink.debug";
    private static final String KEY_JLINK_PATH = "jlink.path";
    private static final String KEY_JLINK_VERBOSE = "jlink.verbose";
    private static final String KEY_UI_NIGHTMODE = "ui.nightmode";
    private static final String KEY_UI_WORDWRAP = "ui.wordwrap";
    private final BooleanProperty mJlinkDebugProperty = new SimpleBooleanProperty();
    private final StringProperty mJlinkPathProperty = new SimpleStringProperty();
    private final BooleanProperty mJlinkVerboseProperty = new SimpleBooleanProperty();
    private final BooleanProperty mNightModeProperty = new SimpleBooleanProperty();
    private final BooleanProperty mWordWrapProperty = new SimpleBooleanProperty();

    public static Options getInstance() {
        return OptionsHolder.INSTANCE;
    }

    private Options() {
        setPreferences(Preferences.userNodeForPackage(App.class));

        mJlinkPathProperty.set(get(KEY_JLINK_PATH, "/path/to/jlink"));
        mJlinkDebugProperty.set(is(KEY_JLINK_DEBUG, false));
        mJlinkVerboseProperty.set(is(KEY_JLINK_VERBOSE, true));

        mNightModeProperty.set(is(KEY_UI_NIGHTMODE, true));
        mWordWrapProperty.set(is(KEY_UI_WORDWRAP, true));

        initListeners();
    }

    public String getJlinkPath() {
        return mJlinkPathProperty.get();
    }

    public boolean isJlinkDebug() {
        return mJlinkDebugProperty.get();
    }

    public boolean isJlinkVerbose() {
        return mJlinkVerboseProperty.get();
    }

    public boolean isNightMode() {
        return mNightModeProperty.get();
    }

    public boolean isWordWrap() {
        return mWordWrapProperty.get();
    }

    public BooleanProperty jlinkDebugProperty() {
        return mJlinkDebugProperty;
    }

    public StringProperty jlinkPathProperty() {
        return mJlinkPathProperty;
    }

    public BooleanProperty jlinkVerboseProperty() {
        return mJlinkVerboseProperty;
    }

    public BooleanProperty nightModeProperty() {
        return mNightModeProperty;
    }

    public void setJlinkDebug(boolean debug) {
        mJlinkDebugProperty.set(debug);
    }

    public void setJlinkPath(String jlink) {
        mJlinkPathProperty.set(jlink);
    }

    public void setJlinkVerbose(boolean debug) {
        mJlinkVerboseProperty.set(debug);
    }

    public void setNightMode(boolean nightMode) {
        mNightModeProperty.set(nightMode);
    }

    public BooleanProperty wordWrapProperty() {
        return mWordWrapProperty;
    }

    private void initListeners() {
        ChangeListener<Object> changeListener = (observable, oldValue, newValue) -> {
            save();
        };

        mJlinkPathProperty.addListener(changeListener);
        mJlinkDebugProperty.addListener(changeListener);
        mJlinkVerboseProperty.addListener(changeListener);
        mNightModeProperty.addListener(changeListener);
        mWordWrapProperty.addListener(changeListener);
    }

    private void save() {
        put(KEY_JLINK_PATH, getJlinkPath());
        put(KEY_JLINK_DEBUG, isJlinkDebug());
        put(KEY_JLINK_VERBOSE, isJlinkVerbose());
        put(KEY_UI_NIGHTMODE, isNightMode());
        put(KEY_UI_WORDWRAP, isWordWrap());
    }

    private static class OptionsHolder {

        private static final Options INSTANCE = new Options();
    }
}
