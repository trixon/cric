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
package se.trixon.cric.core;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.OptionsBase;
import se.trixon.cric.Cric;

/**
 *
 * @author Patrik Karlström
 */
public class Options extends OptionsBase {

    private static final String KEY_JLINK_DEBUG = "jlink.debug";
    private static final String KEY_JLINK_VERBOSE = "jlink.verbose";
    private final BooleanProperty mJlinkDebugProperty = new SimpleBooleanProperty();
    private final BooleanProperty mJlinkVerboseProperty = new SimpleBooleanProperty();

    public static Options getInstance() {
        return Holder.INSTANCE;
    }

    private Options() {
        setPreferences(NbPreferences.forModule(Cric.class));

        mJlinkDebugProperty.set(is(KEY_JLINK_DEBUG, false));
        mJlinkVerboseProperty.set(is(KEY_JLINK_VERBOSE, true));

        initListeners();
    }

    public boolean isJlinkDebug() {
        return mJlinkDebugProperty.get();
    }

    public boolean isJlinkVerbose() {
        return mJlinkVerboseProperty.get();
    }

    public BooleanProperty jlinkDebugProperty() {
        return mJlinkDebugProperty;
    }

    public BooleanProperty jlinkVerboseProperty() {
        return mJlinkVerboseProperty;
    }

    public void setJlinkDebug(boolean debug) {
        mJlinkDebugProperty.set(debug);
    }

    public void setJlinkVerbose(boolean debug) {
        mJlinkVerboseProperty.set(debug);
    }

    private void initListeners() {
        ChangeListener<Object> changeListener = (observable, oldValue, newValue) -> {
            save();
        };

        mJlinkDebugProperty.addListener(changeListener);
        mJlinkVerboseProperty.addListener(changeListener);
    }

    private void save() {
        put(KEY_JLINK_DEBUG, isJlinkDebug());
        put(KEY_JLINK_VERBOSE, isJlinkVerbose());
    }

    private static class Holder {

        private static final Options INSTANCE = new Options();
    }
}
