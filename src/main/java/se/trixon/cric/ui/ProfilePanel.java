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

import java.util.ArrayList;
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.FileChooserPane;
import se.trixon.cric.Profile;
import se.trixon.cric.Profile.ModulePath;
import se.trixon.cric.ProfileManager;

/**
 *
 * @author Patrik Karlström
 */
public class ProfilePanel extends BorderPane {

    private CheckBox mBindServicesCheckBox;
    private ComboBox mCompressComboBox;
    private TextField mDescTextField;
    private ComboBox mEndianComboBox;
    private CheckBox mIgnoreSigningCheckBox;
    private FileChooserPane mJlinkChooserPane;
    private TextField mNameTextField;
    private CheckBox mNoHeadersCheckBox;
    private CheckBox mNoManPagesCheckBox;
    private Button mOkButton;
    private FileChooserPane mOutputChooserPane;
    private final Profile mProfile;
    private final ProfileManager mProfileManager = ProfileManager.getInstance();
    private CheckBox mStripDebugCheckBox;
    private int mTabCounter = 0;
    private TabPane mTabPane;

    public ProfilePanel(Profile p) {
        mProfile = p;
        createUI();

        mNameTextField.setText(p.getName());
        mDescTextField.setText(p.getDescription());
        mJlinkChooserPane.setPath(p.getJlink());
        mOutputChooserPane.setPath(p.getOutput());
        mBindServicesCheckBox.setSelected(p.isBindServices());
        mNoHeadersCheckBox.setSelected(p.isNoHeaders());
        mNoManPagesCheckBox.setSelected(p.isNoManPages());
        mIgnoreSigningCheckBox.setSelected(p.isIgnoreSigning());
        mStripDebugCheckBox.setSelected(p.isStripDebug());
        mCompressComboBox.getSelectionModel().select(p.getCompress());
        mEndianComboBox.getSelectionModel().select(p.getEndian());

        initTab(p);
        initListeners();

        Platform.runLater(() -> {
            initValidation();
            mNameTextField.requestFocus();
        });
    }

    public void save() {
        mProfile.setName(mNameTextField.getText().trim());
        mProfile.setDescription(mDescTextField.getText());
        mProfile.setJlink(mJlinkChooserPane.getPath());
        mProfile.setOutput(mOutputChooserPane.getPath());
        mProfile.setBindServices(mBindServicesCheckBox.isSelected());
        mProfile.setNoHeaders(mNoHeadersCheckBox.isSelected());
        mProfile.setNoManPages(mNoManPagesCheckBox.isSelected());
        mProfile.setIgnoreSigning(mIgnoreSigningCheckBox.isSelected());
        mProfile.setStripDebug(mStripDebugCheckBox.isSelected());
        mProfile.setCompress(mCompressComboBox.getSelectionModel().getSelectedIndex());
        mProfile.setEndian(mEndianComboBox.getSelectionModel().getSelectedIndex());

        ArrayList<ModulePath> modulePaths = new ArrayList<>();

        mTabPane.getTabs().stream().filter(tab -> (tab instanceof ModulePathTab)).forEachOrdered(tab -> {
            modulePaths.add(((ModulePathTab) tab).getModulePath());
        });

        mProfile.setModulePaths(modulePaths);
    }

    void setOkButton(Button button) {
        mOkButton = button;
    }

    private void createUI() {
        var gridPane = new GridPane();
        var headerGridPane = new GridPane();
        //gridPane.setGridLinesVisible(true);
        gridPane.setHgap(8);
        headerGridPane.setHgap(8);

        var nameLabel = new Label(Dict.NAME.toString());
        var descLabel = new Label(Dict.DESCRIPTION.toString());

        mNameTextField = new TextField();
        mDescTextField = new TextField();
        mJlinkChooserPane = new FileChooserPane(Dict.SELECT.toString(), "jlink", FileChooserPane.ObjectMode.FILE, SelectionMode.SINGLE);
        mOutputChooserPane = new FileChooserPane(Dict.SELECT.toString(), "output", FileChooserPane.ObjectMode.DIRECTORY, SelectionMode.SINGLE);

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
        mCompressComboBox.getItems().setAll("No compression", "Constant string sharing", "ZIP");
        mEndianComboBox = new ComboBox();
        mEndianComboBox.getItems().setAll("Native", "Little", "Big");

        int row = 0;

        headerGridPane.add(nameLabel, 0, row, 1, 1);
        headerGridPane.add(descLabel, 1, row, 1, 1);
        headerGridPane.add(mNameTextField, 0, ++row, 1, 1);
        headerGridPane.add(mDescTextField, 1, row, 1, 1);
        headerGridPane.add(mJlinkChooserPane, 0, ++row, 1, 1);
        headerGridPane.add(mOutputChooserPane, 1, row, 1, 1);

        gridPane.add(headerGridPane, 0, row = 0, GridPane.REMAINING, 1);

        var subPane = new GridPane();
        //subPane.setGridLinesVisible(true);
        subPane.add(new Label("compress"), 5, 0);
        subPane.add(new Label("endian"), 6, 0);
        subPane.addRow(1, mBindServicesCheckBox, mIgnoreSigningCheckBox, mNoHeadersCheckBox, mNoManPagesCheckBox, mStripDebugCheckBox, mCompressComboBox, mEndianComboBox);
        subPane.setHgap(8);
        subPane.setMaxWidth(Double.MAX_VALUE);

        gridPane.add(subPane, 0, ++row, 1, 1);
        mTabPane = new TabPane();

        var rowInsets = new Insets(8, 0, 0, 0);
        FxHelper.setPadding(rowInsets, mJlinkChooserPane, mOutputChooserPane, subPane, mTabPane);

        GridPane.setHgrow(mNameTextField, Priority.ALWAYS);
        GridPane.setHgrow(mDescTextField, Priority.ALWAYS);
        GridPane.setHgrow(subPane, Priority.ALWAYS);

        GridPane.setFillWidth(mNameTextField, true);
        GridPane.setFillWidth(mDescTextField, true);
        GridPane.setFillWidth(subPane, true);

        setTop(gridPane);
        setCenter(mTabPane);
    }

    private void initListeners() {
        mTabPane.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Tab> ov, Tab oldTab, Tab newTab) -> {
            if (mTabPane.getSelectionModel().getSelectedIndex() == 0) {
                Platform.runLater(() -> {
                    var modulePathTab = new ModulePathTab(mTabCounter++, null);
                    mTabPane.getTabs().add(modulePathTab);
                    mTabPane.getSelectionModel().select(modulePathTab);
                });
            }
        });
    }

    private void initTab(Profile p) {
        var plusTab = new Tab("+");
        plusTab.setClosable(false);
        mTabPane.getTabs().add(plusTab);

        if (p.getModulePaths().isEmpty()) {
            mTabPane.getTabs().add(new ModulePathTab(mTabCounter++, null));
        } else {
            for (var modulePath : p.getModulePaths()) {
                mTabPane.getTabs().add(new ModulePathTab(mTabCounter++, modulePath));
            }
        }

        mTabPane.getSelectionModel().select(1);
    }

    private void initValidation() {
        final String text_is_required = "Text is required";
        boolean indicateRequired = false;

        Predicate namePredicate = (Predicate) (Object o) -> {
            return mProfileManager.isValid(mProfile.getName(), (String) o);
        };

        var validationSupport = new ValidationSupport();
        validationSupport.registerValidator(mNameTextField, indicateRequired, Validator.createEmptyValidator(text_is_required));
        validationSupport.registerValidator(mNameTextField, indicateRequired, Validator.createPredicateValidator(namePredicate, text_is_required));

        validationSupport.validationResultProperty().addListener((ObservableValue<? extends ValidationResult> observable, ValidationResult oldValue, ValidationResult newValue) -> {
            if (mOkButton != null) {
                mOkButton.setDisable(validationSupport.isInvalid());
            }
        });

        validationSupport.initInitialDecoration();
    }

}
