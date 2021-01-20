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

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.control.LogPanel;

/**
 *
 * @author Patrik Karlström
 */
public class StatusPanel extends BorderPane {

    private final Tab mErrTab = new Tab(Dict.Dialog.ERROR.toString());
    private final LogPanel mLogErrPanel = new LogPanel();
    private final LogPanel mLogOutPanel = new LogPanel();
    private final Options mOptions = Options.getInstance();
    private final Tab mOutTab = new Tab(Dict.OUTPUT.toString());
    private final ProgressBar mProgressBar = new ProgressBar();
    private final RunStateManager mRunStateManager = RunStateManager.getInstance();
    private final SummaryDetails mSummaryDetails = new SummaryDetails();
    private final SummaryHeader mSummaryHeader = new SummaryHeader();
    private final TabPane mTabPane = new TabPane();

    public StatusPanel() {
        createUI();
        initListeners();
    }

    void clear() {
        mLogOutPanel.clear();
        mLogErrPanel.clear();
    }

    void err(String message) {
        mLogErrPanel.println(message);
    }

    void out(String message) {
        mLogOutPanel.println(message);
    }

    void setProgress(double p) {
        Platform.runLater(() -> {
            mProgressBar.setProgress(p);
        });
    }

    private void createUI() {
        mLogOutPanel.setMonospaced();
        mLogErrPanel.setMonospaced();
        mOutTab.setContent(mLogOutPanel);
        mErrTab.setContent(mLogErrPanel);
        mTabPane.getTabs().addAll(mOutTab);
        mTabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        mTabPane.setSide(Side.BOTTOM);

        var box = new VBox(
                mSummaryHeader,
                mSummaryDetails,
                mProgressBar
        );

        mProgressBar.setMaxWidth(Double.MAX_VALUE);
        mProgressBar.setProgress(0);
        box.setAlignment(Pos.CENTER);
        setTop(box);
        setCenter(mTabPane);

//        mLogOutPanel.setWrapText(mOptions.isWordWrap());
//        mLogErrPanel.setWrapText(mOptions.isWordWrap());
    }

    private void initListeners() {
        mOptions.getPreferences().addPreferenceChangeListener(pce -> {
            switch (pce.getKey()) {
//                case Options.KEY_WORD_WRAP:
//                    mLogOutPanel.setWrapText(mOptions.isWordWrap());
//                    mLogErrPanel.setWrapText(mOptions.isWordWrap());
//                    break;
            }
        });

        mRunStateManager.profileProperty().addListener((observable, oldValue, newValue) -> {
            mSummaryHeader.load(newValue);
            mSummaryDetails.load(newValue);
        });
    }
}
