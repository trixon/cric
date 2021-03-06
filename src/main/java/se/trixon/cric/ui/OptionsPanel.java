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
package se.trixon.cric.ui;

import javafx.geometry.Insets;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;
import org.controlsfx.control.ToggleSwitch;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.FileChooserPane;
import se.trixon.cric.Options;

/**
 *
 * @author Patrik Karlström
 */
public class OptionsPanel extends VBox {

    private ToggleSwitch mDebugToggleSwitch;
    private FileChooserPane mJLinkFileChooserPane;
    private ToggleSwitch mNightModeToggleSwitch;
    private final Options mOptions = Options.getInstance();
    private ToggleSwitch mVerboseSwitch;
    private ToggleSwitch mWordWrapToggleSwitch;

    public OptionsPanel() {
        createUI();
    }

    private void createUI() {
        mJLinkFileChooserPane = new FileChooserPane("jlink", "jlink", FileChooserPane.ObjectMode.FILE, SelectionMode.SINGLE);
        mNightModeToggleSwitch = new ToggleSwitch(Dict.NIGHT_MODE.toString());
        mWordWrapToggleSwitch = new ToggleSwitch(Dict.DYNAMIC_WORD_WRAP.toString());
        mDebugToggleSwitch = new ToggleSwitch("jlink debug");
        mVerboseSwitch = new ToggleSwitch("jlink verbose");

        mNightModeToggleSwitch.prefWidthProperty().bind(mJLinkFileChooserPane.widthProperty());
        mWordWrapToggleSwitch.prefWidthProperty().bind(mJLinkFileChooserPane.widthProperty());
        mDebugToggleSwitch.prefWidthProperty().bind(mJLinkFileChooserPane.widthProperty());
        mVerboseSwitch.prefWidthProperty().bind(mJLinkFileChooserPane.widthProperty());

        getChildren().setAll(
                mJLinkFileChooserPane,
                mDebugToggleSwitch,
                mVerboseSwitch,
                mNightModeToggleSwitch,
                mWordWrapToggleSwitch
        );

        FxHelper.setPadding(new Insets(8, 0, 0, 0),
                mNightModeToggleSwitch,
                mWordWrapToggleSwitch,
                mDebugToggleSwitch,
                mVerboseSwitch
        );

        mVerboseSwitch.selectedProperty().bindBidirectional(mOptions.jlinkVerboseProperty());
        mDebugToggleSwitch.selectedProperty().bindBidirectional(mOptions.jlinkDebugProperty());
        mNightModeToggleSwitch.selectedProperty().bindBidirectional(mOptions.nightModeProperty());
        mWordWrapToggleSwitch.selectedProperty().bindBidirectional(mOptions.wordWrapProperty());
        mJLinkFileChooserPane.getTextField().textProperty().bindBidirectional(mOptions.jlinkPathProperty());

        setPrefSize(480, 360);
    }

}
