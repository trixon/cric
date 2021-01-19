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
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.ListSelectionView;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.FileChooserPane;

/**
 *
 * @author Patrik Karlström
 */
public class ModuleSelectionPanel extends Tab {

    private final BorderPane mBorderPane = new BorderPane();
    private final FileChooserPane mFileChooserPane;
    private final ListSelectionView<String> mListSelectionView;

    public ModuleSelectionPanel() {
        mFileChooserPane = new FileChooserPane(Dict.PATH.toString(), Dict.PATH.toString(), FileChooserPane.ObjectMode.DIRECTORY, SelectionMode.SINGLE);
        mListSelectionView = new ListSelectionView<>();
        mBorderPane.setTop(mFileChooserPane);
        mBorderPane.setCenter(mListSelectionView);

        FxHelper.setPadding(FxHelper.getUIScaledInsets(8, 0, 0, 0), mFileChooserPane);

        setContent(mBorderPane);
        setText("jmods");
    }

}
