/*
 * Copyright 2024 Patrik Karlström <patrik@trixon.se>.
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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javax.swing.JFileChooser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.openide.DialogDescriptor;
import org.openide.util.Exceptions;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.FileChooserPaneSwingFx;
import se.trixon.cric.core.StorageManager;
import se.trixon.cric.core.Task;
import se.trixon.cric.core.TaskManager;

/**
 *
 * @author Patrik Karlström
 */
public class TaskEditor extends BorderPane {

    private CheckBox mBindServicesCheckBox;
    private ComboBox mCompressComboBox;
    private TextField mDescTextField;
    private DialogDescriptor mDialogDescriptor;
    private ComboBox mEndianComboBox;
    private CheckBox mIgnoreSigningCheckBox;
    private FileChooserPaneSwingFx mJlinkChooserPane;
    private TextField mLauncherTextField;
    private final TaskManager mManager = TaskManager.getInstance();
    private TextField mNameTextField;
    private CheckBox mNoHeadersCheckBox;
    private CheckBox mNoManPagesCheckBox;
    private FileChooserPaneSwingFx mOutputChooserPane;
    private CheckBox mStripDebugCheckBox;
    private TabPane mTabPane;
    private ChangeListener<Tab> mTabSelectionListener;
    private Task mTask;
    private final ValidationSupport mValidationSupport = new ValidationSupport();

    public TaskEditor() {
        createUI();
        initListeners();

        Platform.runLater(() -> {
            initValidation();
        });
    }

    public void load(Task task, DialogDescriptor dialogDescriptor) {
        mTabPane.getSelectionModel().selectedItemProperty().removeListener(mTabSelectionListener);

        if (task == null) {
            task = new Task();
        }

        mDialogDescriptor = dialogDescriptor;
        mTask = task;

        mNameTextField.setText(task.getName());
        mDescTextField.setText(task.getDescription());
        mLauncherTextField.setText(task.getLauncher());
        mJlinkChooserPane.setPath(task.getJlink());
        mOutputChooserPane.setPath(task.getOutput());
        mBindServicesCheckBox.setSelected(task.isBindServices());
        mNoHeadersCheckBox.setSelected(task.isNoHeaders());
        mNoManPagesCheckBox.setSelected(task.isNoManPages());
        mIgnoreSigningCheckBox.setSelected(task.isIgnoreSigning());
        mStripDebugCheckBox.setSelected(task.isStripDebug());
        mCompressComboBox.getSelectionModel().select(task.getCompress());
        mEndianComboBox.getSelectionModel().select(task.getEndian());

        initTabs(task);

        Platform.runLater(() -> {
            mNameTextField.requestFocus();
            mValidationSupport.revalidate();
        });
    }

    public Task save() {
        mTask.setName(mNameTextField.getText().trim());
        mTask.setDescription(mDescTextField.getText());
        mTask.setLauncher(mLauncherTextField.getText());
        mTask.setJlink(mJlinkChooserPane.getPath());
        mTask.setOutput(mOutputChooserPane.getPath());
        mTask.setBindServices(mBindServicesCheckBox.isSelected());
        mTask.setNoHeaders(mNoHeadersCheckBox.isSelected());
        mTask.setNoManPages(mNoManPagesCheckBox.isSelected());
        mTask.setIgnoreSigning(mIgnoreSigningCheckBox.isSelected());
        mTask.setStripDebug(mStripDebugCheckBox.isSelected());
        mTask.setCompress(mCompressComboBox.getSelectionModel().getSelectedIndex());
        mTask.setEndian(mEndianComboBox.getSelectionModel().getSelectedIndex());

        var modulePaths = mTabPane.getTabs().stream()
                .filter(tab -> (tab instanceof ModulePathTab))
                .map(tab -> (ModulePathTab) tab)
                .map(tab -> tab.getModulePath())
                .collect(Collectors.toCollection(ArrayList::new));

        mTask.setModulePaths(modulePaths);

        mManager.getIdToItem().put(mTask.getId(), mTask);
        StorageManager.save();

        return mTask;
    }

    private void createUI() {
        var gp = new GridPane(FxHelper.getUIScaled(8), 0);
        //gridPane.setGridLinesVisible(true);

        var nameLabel = new Label(Dict.NAME.toString());
        var descLabel = new Label(Dict.DESCRIPTION.toString());

        mNameTextField = new TextField();
        mDescTextField = new TextField();
        mJlinkChooserPane = new FileChooserPaneSwingFx(Dict.SELECT.toString(), "jlink", Almond.getFrame(), JFileChooser.FILES_ONLY);
        mOutputChooserPane = new FileChooserPaneSwingFx(Dict.SELECT.toString(), "output", Almond.getFrame(), JFileChooser.DIRECTORIES_ONLY);

        mLauncherTextField = new TextField();
        mLauncherTextField.setPromptText("<name>=<module>[/<mainclass>]");

        mBindServicesCheckBox = new CheckBox("bind-services");
        mBindServicesCheckBox.setTooltip(new Tooltip("Link in service provider modules and their dependences"));
        mIgnoreSigningCheckBox = new CheckBox("ignore-signing-information");
        mIgnoreSigningCheckBox.setTooltip(new Tooltip("Suppress a fatal error when signed modular JARs are linked in the image. The signature related files of the signed modular JARs are not copied to the runtime image"));
        mNoHeadersCheckBox = new CheckBox("no-header-files");
        mNoHeadersCheckBox.setTooltip(new Tooltip("Exclude include header files"));
        mNoManPagesCheckBox = new CheckBox("no-man-pages");
        mNoManPagesCheckBox.setTooltip(new Tooltip("Exclude man pages"));
        mStripDebugCheckBox = new CheckBox("strip-debug");
        mStripDebugCheckBox.setTooltip(new Tooltip("Strip debug information"));

        mCompressComboBox = new ComboBox();
        mCompressComboBox.getItems().setAll(
                "zip-0 no compression",
                "zip-1",
                "zip-2",
                "zip-3",
                "zip-4",
                "zip-5",
                "zip-6 default compression",
                "zip-7",
                "zip-8",
                "zip-9 best compression"
        );

        mEndianComboBox = new ComboBox();
        mEndianComboBox.getItems().setAll("Native", "Little", "Big");

        int row = 0;
        gp.addRow(row++, nameLabel, descLabel);
        gp.addRow(row++, mNameTextField, mDescTextField);
        gp.addRow(row++, mJlinkChooserPane, mOutputChooserPane);

        var compressLabel = new Label("compress");
        var endianLabel = new Label("endian");
        var launcherLabel = new Label("launcher");

        var box1 = new HBox(FxHelper.getUIScaled(8),
                new VBox(compressLabel, mCompressComboBox),
                new VBox(endianLabel, mEndianComboBox)
        );
        var box2 = new VBox(launcherLabel, mLauncherTextField);

        gp.add(box1, 0, row);
        gp.add(box2, 1, row++);

        var box3 = new HBox(FxHelper.getUIScaled(16),
                mBindServicesCheckBox, mIgnoreSigningCheckBox, mNoHeadersCheckBox, mNoManPagesCheckBox, mStripDebugCheckBox
        );

        gp.add(box3, 0, row++, GridPane.REMAINING, 1);

        var addTab = new Tab("+");
        addTab.setClosable(false);
        mTabPane = new TabPane(addTab);

        FxHelper.setPadding(FxHelper.getUIScaledInsets(8, 0, 0, 0),
                mJlinkChooserPane,
                mOutputChooserPane,
                compressLabel,
                endianLabel,
                launcherLabel
        );

        FxHelper.setPadding(FxHelper.getUIScaledInsets(16, 0, 0, 0),
                box3,
                mTabPane
        );

        FxHelper.autoSizeColumn(gp, 2);

        setTop(gp);
        setCenter(mTabPane);
    }

    private void initListeners() {
        mTabSelectionListener = (p, o, n) -> {
            if (mTabPane.getSelectionModel().getSelectedIndex() == 0) {
                Platform.runLater(() -> {
                    var modulePathTab = new ModulePathTab(mTabPane.getTabs().size(), null);
                    mTabPane.getTabs().add(modulePathTab);
                    mTabPane.getSelectionModel().select(modulePathTab);
                });
            }
        };

        mTabPane.setOnDragOver(dragEvent -> {
            var dragboard = dragEvent.getDragboard();
            if (dragboard.hasFiles()) {
                dragEvent.acceptTransferModes(TransferMode.COPY);
            }
        });

        mTabPane.setOnDragDropped(dragEvent -> {
            var files = dragEvent.getDragboard().getFiles();
            if (!files.isEmpty()) {
                var file = files.get(0);
                if (file.isFile() && file.getName().equals("release")) {
                    try {
                        var content = FileUtils.readLines(file, Charset.forName("utf-8"));
                        for (var line : content) {
                            if (StringUtils.startsWith(line, "MODULES=\"")) {
                                line = StringUtils.removeStart(line, "MODULES=\"");
                                line = StringUtils.removeEnd(line, "\"");
                                final var modules = line;
                                mTabPane.getTabs().stream().filter(tab -> tab instanceof ModulePathTab).forEachOrdered(tab -> {
                                    ((ModulePathTab) tab).select(modules);
                                });
                                break;
                            }

                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
    }

    private void initTabs(Task task) {
        var tabs = mTabPane.getTabs();
        if (tabs.size() > 1) {
            tabs.remove(1, tabs.size());
        }

        if (task.getModulePaths().isEmpty()) {
            tabs.add(new ModulePathTab(0, null));
        } else {
            for (var modulePath : task.getModulePaths()) {
                tabs.add(new ModulePathTab(tabs.size(), modulePath));
            }
        }

        mTabPane.getSelectionModel().select(1);
        mTabPane.getSelectionModel().selectedItemProperty().addListener(mTabSelectionListener);
    }

    private void initValidation() {
        final String text_is_required = "Text is required";
        boolean indicateRequired = false;

        var namePredicate = (Predicate<String>) s -> {
            return mTask != null && mManager.isValid(mTask.getName(), s);
        };

        mValidationSupport.registerValidator(mNameTextField, indicateRequired, Validator.createEmptyValidator(text_is_required));
        mValidationSupport.registerValidator(mNameTextField, indicateRequired, Validator.createPredicateValidator(namePredicate, text_is_required));

        mValidationSupport.validationResultProperty().addListener((p, o, n) -> {
            if (mDialogDescriptor != null) {
                mDialogDescriptor.setValid(!mValidationSupport.isInvalid());
            }
        });

        mValidationSupport.initInitialDecoration();
    }
}
