/*
 * Copyright 2023 Patrik Karlström <patrik@trixon.se>.
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

import java.awt.Dimension;
import java.util.HashMap;
import java.util.ResourceBundle;
import javafx.scene.Scene;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;
import se.trixon.almond.nbp.dialogs.NbMessage;
import se.trixon.almond.nbp.fx.FxDialogPanel;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.cric.ui.TaskInfoPane;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class ExecutorManager {

    private final ResourceBundle mBundle = NbBundle.getBundle(ExecutorManager.class);
    private final HashMap<String, Executor> mExecutors = new HashMap<>();
    private InputOutput mInputOutput;
    private final Dimension mPreferredSize = SwingHelper.getUIScaledDim(800, 600);

    public static ExecutorManager getInstance() {
        return Holder.INSTANCE;
    }

    private ExecutorManager() {
    }

    public HashMap<String, Executor> getExecutors() {
        return mExecutors;
    }

    public void requestStart(Task task) {
        if (mExecutors.containsKey(task.getId())) {
            NbMessage.error(Dict.Dialog.TITLE_TASK_RUNNING.toString(), Dict.Dialog.MESSAGE_TASK_RUNNING.toString());
        } else {
            var taskInfoPane = new TaskInfoPane(task);
            var dialogPanel = new FxDialogPanel() {
                @Override
                protected void fxConstructor() {
                    setScene(new Scene(taskInfoPane));
                }
            };
            dialogPanel.setPreferredSize(mPreferredSize);

            SwingUtilities.invokeLater(() -> {
                var title = Dict.Dialog.TITLE_TASK_RUN_S.toString().formatted(task.getName());
                var runButton = new JButton(Dict.RUN.toString());
                var d = new DialogDescriptor(
                        dialogPanel,
                        title,
                        true,
                        new Object[]{Dict.CANCEL.toString(), runButton},
                        runButton,
                        0,
                        null,
                        null
                );

                d.setValid(false);
                dialogPanel.setNotifyDescriptor(d);
                dialogPanel.initFx(null);
                SwingHelper.runLaterDelayed(100, () -> runButton.requestFocus());
                var result = DialogDisplayer.getDefault().notify(d);

                if (result == runButton) {
                    start(task);
                }
            });
        }
    }

    public void start(Task task) {
        var executor = new Executor(task);
        mExecutors.put(task.getId(), executor);
        executor.run();
    }

    private static class Holder {

        private static final ExecutorManager INSTANCE = new ExecutorManager();
    }

}
