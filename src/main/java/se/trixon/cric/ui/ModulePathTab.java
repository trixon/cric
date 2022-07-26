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

import java.io.File;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.ListSelectionView;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.FileChooserPane;
import se.trixon.cric.Profile.ModulePath;

/**
 *
 * @author Patrik Karlström
 */
public class ModulePathTab extends Tab {

    private final BorderPane mBorderPane = new BorderPane();
    private final FileChooserPane mFileChooserPane;
    private final ListSelectionView<String> mListSelectionView;

    public ModulePathTab(int tabCounter, ModulePath modulePath) {
        mFileChooserPane = new FileChooserPane(Dict.PATH.toString(), Dict.PATH.toString(), FileChooserPane.ObjectMode.DIRECTORY, SelectionMode.SINGLE);
        mListSelectionView = new ListSelectionView<>();
        ((Label) mListSelectionView.getSourceHeader()).setText(Dict.AVAILABLE.toString());
        ((Label) mListSelectionView.getTargetHeader()).setText(Dict.SELECTED.toString());
        mBorderPane.setTop(mFileChooserPane);
        mBorderPane.setCenter(mListSelectionView);

        FxHelper.setPadding(FxHelper.getUIScaledInsets(8, 0, 0, 0), mFileChooserPane);

        setClosable(tabCounter > 0);
        setContent(mBorderPane);
        setText("module-path #" + tabCounter);

        if (modulePath != null) {
            load(modulePath);
        }

        initListeners();
    }

    ModulePath getModulePath() {
        var modulePath = new ModulePath();
        modulePath.setDirectory(mFileChooserPane.getPath());
        modulePath.setSelectedModules(new TreeSet(mListSelectionView.getTargetItems()));

        return modulePath;
    }

    void select(String modules) {
        for (var module : StringUtils.split(modules)) {
            if (mListSelectionView.getSourceItems().contains(module)) {
                mListSelectionView.getSourceItems().remove(module);
                mListSelectionView.getTargetItems().add(module);
            }
        }
    }

    private void initListeners() {
        mFileChooserPane.getTextField().textProperty().addListener((observable, oldValue, newValue) -> {
            var file = new File(newValue);
            if (file.isDirectory()) {
                rescanModuleDirectory(file);
            } else {
                mListSelectionView.getSourceItems().clear();
            }
        });

        mFileChooserPane.setFileChooserListener(new FileChooserPane.FileChooserListener() {
            @Override
            public void onFileChooserCancel(FileChooserPane chooserPane) {
            }

            @Override
            public void onFileChooserCheckBoxChange(FileChooserPane chooserPane, boolean selected) {
            }

            @Override
            public void onFileChooserDrop(FileChooserPane chooserPane, File file) {
                rescanModuleDirectory(file);
            }

            @Override
            public void onFileChooserDrop(FileChooserPane chooserPane, List<File> files) {
            }

            @Override
            public void onFileChooserOk(FileChooserPane chooserPane, File file) {
                rescanModuleDirectory(file);
            }

            @Override
            public void onFileChooserOk(FileChooserPane chooserPane, List<File> files) {
            }

            @Override
            public void onFileChooserPreSelect(FileChooserPane chooserPane) {
            }
        });
    }

    private void load(ModulePath modulePath) {
        mFileChooserPane.setPath(modulePath.getDirectory());

        rescanModuleDirectory(mFileChooserPane.getPath());

        mListSelectionView.getSourceItems().removeAll(modulePath.getSelectedModules());
        mListSelectionView.getTargetItems().setAll(modulePath.getSelectedModules());
    }

    private void rescanModuleDirectory(File dir) {
        mListSelectionView.getSourceItems().clear();

        try {
            mListSelectionView.getSourceItems().setAll(FileUtils.listFiles(dir, new String[]{"jmod"}, false).stream()
                    .map(f -> FilenameUtils.getBaseName(f.getName()))
                    .filter(s -> !mListSelectionView.getTargetItems().contains(s))
                    .collect(Collectors.toCollection(TreeSet::new)));
        } catch (Exception e) {
        }
    }
}
