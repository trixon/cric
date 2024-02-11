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

import java.util.logging.Logger;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import se.trixon.almond.util.Log;
import se.trixon.almond.util.SystemHelper;
import se.trixon.cric.Options;
import se.trixon.cric.RunState;
import se.trixon.cric.RunStateManager;

/**
 *
 * @author Patrik Karlström
 */
public class AppForm extends BorderPane {

    private static final Logger LOGGER = Logger.getLogger(AppForm.class.getName());
    private Font mDefaultFont;
    private final ListEditor mListEditor = new ListEditor();
    private final Log mLog = new Log();
    private final Options mOptions = Options.getInstance();
    private BorderPane mRightBorderPane = new BorderPane();
    private final RunStateManager mRunStateManager = RunStateManager.getInstance();
    private final StatusPanel mStatusPanel = new StatusPanel();

    public AppForm() {
        mLog.setUseTimestamps(false);
        createUI();
        initListeners();
        mRunStateManager.setRunState(RunState.STARTABLE);
    }

    public void setToolBar(ToolBar toolBar) {
        mRightBorderPane.setTop(toolBar);
    }

    public void updateNightMode() {
        mListEditor.getEditableList().refreshIcons();
    }

    private void createUI() {
        mDefaultFont = Font.getDefault();

        mListEditor.getEditableList().disableProperty().bind(mRunStateManager.runningProperty());
        setLeft(mListEditor.getEditableList());
        mRightBorderPane.setCenter(mStatusPanel);
        setCenter(mRightBorderPane);
        mLog.setOut(s -> {
            System.out.println(s);
            mStatusPanel.out(s);
        });

        mLog.setErr(s -> {
            System.err.println(s);
            mStatusPanel.err(s);
        });

        mLog.out(SystemHelper.getSystemInfo());
        //        mAddAction = new Action(Dict.ADD.toString(), actionEvent -> {
        ////            profileEdit(null);
        //        });
        //        FxHelper.setTooltip(mAddAction, new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN));
        //        mAddAction.disabledProperty().bind(mRunStateManager.runningProperty());
        //
        //        BooleanBinding profileBooleanBinding = mRunStateManager.profileProperty().isNull().or(mRunStateManager.runningProperty());
        //
        //        mRunAction = new Action(Dict.RUN.toString(), actionEvent -> {
        //            profileRun(getSelectedProfile());
        //            mEditableList.getListView().requestFocus();
        //        });
        //        FxHelper.setTooltip(mRunAction, new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN));
        //        mRunAction.disabledProperty().bind(profileBooleanBinding);
        //
        //        mEditAction = new Action(Dict.EDIT.toString(), actionEvent -> {
        ////            profileEdit(getSelectedProfile());
        //            mEditableList.getListView().requestFocus();
        //        });
        //        FxHelper.setTooltip(mEditAction, new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN));
        //        mEditAction.disabledProperty().bind(profileBooleanBinding);
        //
        //        mCloneAction = new Action(Dict.CLONE.toString(), actionEvent -> {
        //            profileClone(getSelectedProfile());
        //            mEditableList.getListView().requestFocus();
        //        });
        //        FxHelper.setTooltip(mCloneAction, new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN));
        //        mCloneAction.disabledProperty().bind(profileBooleanBinding);
        //
        //        mRemoveAction = new Action(Dict.REMOVE.toString(), actionEvent -> {
        //            profileRemove(getSelectedProfile());
        //            mEditableList.getListView().requestFocus();
        //        });
        //        FxHelper.setTooltip(mRemoveAction, new KeyCodeCombination(KeyCode.DELETE));
        //        mRemoveAction.disabledProperty().bind(profileBooleanBinding);
        //
        //        updateNightMode();
        //
    }

    private void initListeners() {
        mListEditor.getListView().getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            mRunStateManager.setProfile(newValue);
        });

        mOptions.nightModeProperty().addListener((observable, oldValue, newValue) -> {
            updateNightMode();
        });
    }

}
