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

    private static final String KEY_JLINK = "path.jlink";
    private static final String KEY_NIGHTMODE = "nightmode";
    private final StringProperty mJlinkProperty = new SimpleStringProperty();
    private final BooleanProperty mNightModeProperty = new SimpleBooleanProperty();

    public static Options getInstance() {
        return OptionsHolder.INSTANCE;
    }

    private Options() {
        setPreferences(Preferences.userNodeForPackage(App.class));
        mNightModeProperty.set(is(KEY_NIGHTMODE, true));
        jlinkProperty().set(get(KEY_JLINK, "/path/to/jlink"));

        initListeners();
    }

    public String getJlink() {
        return mJlinkProperty.get();
    }

    public boolean isNightMode() {
        return mNightModeProperty.get();
    }

    public StringProperty jlinkProperty() {
        return mJlinkProperty;
    }

    public BooleanProperty nightModeProperty() {
        return mNightModeProperty;
    }

    public void setJlinkProperty(String jlink) {
        mJlinkProperty.set(jlink);
    }

    public void setNightMode(boolean nightMode) {
        mNightModeProperty.set(nightMode);
    }

    private void initListeners() {
        ChangeListener<Object> changeListener = (observable, oldValue, newValue) -> {
            save();
        };

        mJlinkProperty.addListener(changeListener);
        mNightModeProperty.addListener(changeListener);
    }

    private void save() {
        put(KEY_NIGHTMODE, isNightMode());
        put(KEY_JLINK, getJlink());
    }

    private static class OptionsHolder {

        private static final Options INSTANCE = new Options();
    }
}
