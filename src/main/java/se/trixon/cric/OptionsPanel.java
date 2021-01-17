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

import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;
import se.trixon.almond.util.fx.control.FileChooserPane;
import static se.trixon.cric.Options.*;

/**
 *
 * @author Patrik Karlström
 */
public class OptionsPanel extends VBox {

    private final Options mOptions = Options.getInstance();

    private FileChooserPane mJLinkFileChooserPane;

    public OptionsPanel() {
        createUI();
    }

    void load() {
        mJLinkFileChooserPane.setPath(mOptions.get(KEY_JLINK, "/path/to/jlink"));
    }

    void save() {
        mOptions.put(KEY_JLINK, mJLinkFileChooserPane.getPathAsString());
    }

    private void createUI() {
        mJLinkFileChooserPane = new FileChooserPane("jlink", "jlink", FileChooserPane.ObjectMode.FILE, SelectionMode.SINGLE);
        getChildren().setAll(
                mJLinkFileChooserPane
        );

        setPrefSize(480, 360);
    }

}
