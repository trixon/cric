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
package se.trixon.cric.core;

import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.io.FileUtils;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.FoldHandle;
import org.openide.windows.IOFolding;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import se.trixon.almond.nbp.output.OutputHelper;
import se.trixon.almond.nbp.output.OutputLineMode;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class Executor implements Runnable {

    private final ResourceBundle mBundle = NbBundle.getBundle(Executor.class);
    private Thread mExecutorThread;
    private final InputOutput mInputOutput;
    private FoldHandle mMainFoldHandle;
    private final OutputHelper mOutputHelper;
    private ProgressHandle mProgressHandle;
    private final AtomicBoolean mRunning = new AtomicBoolean(false);
    private final StatusDisplayer mStatusDisplayer = StatusDisplayer.getDefault();
    private final Task mTask;

    public Executor(Task task) {
        mTask = task;
        mInputOutput = IOProvider.getDefault().getIO(mTask.getName(), false);
        mInputOutput.select();

        mOutputHelper = new OutputHelper(mTask.getName(), mInputOutput, false);
        mOutputHelper.reset();
    }

    @Override
    public void run() {
        mRunning.set(true);
        var allowToCancel = (Cancellable) () -> {
            mRunning.set(false);
            mExecutorThread.interrupt();
            mProgressHandle.finish();
            ExecutorManager.getInstance().getExecutors().remove(mTask.getId());
            jobEnded(OutputLineMode.WARNING, Dict.CANCELED.toString());

            return true;
        };

        mProgressHandle = ProgressHandle.createHandle(mTask.getName(), allowToCancel);
        mProgressHandle.start();
        mProgressHandle.switchToIndeterminate();

        mExecutorThread = new Thread(() -> {
            mOutputHelper.start();
            mOutputHelper.printSectionHeader(OutputLineMode.INFO, Dict.START.toString(), Dict.TASK.toLower(), mTask.getName());
            mMainFoldHandle = IOFolding.startFold(mInputOutput, true);
            mInputOutput.getOut().println();
            mInputOutput.getOut().println(String.join(" ", mTask.getCommand()));

            if (initTargetDirectory()) {
                var result = runProcess(mTask.getCommand());
                if (result == 0) {
                    jobEnded(OutputLineMode.OK, Dict.DONE.toString());
                } else {
                    jobEnded(OutputLineMode.ERROR, Dict.FAILED.toString());
                }
            } else {
                jobEnded(OutputLineMode.WARNING, Dict.CANCELED.toString());
            }

            mProgressHandle.finish();
            ExecutorManager.getInstance().getExecutors().remove(mTask.getId());
        }, "Executor");

        mExecutorThread.start();
    }

    private boolean initTargetDirectory() {
        boolean result = true;
        var destDir = mTask.getOutput();
        if (destDir.exists()) {
            NotifyDescriptor d = new NotifyDescriptor(
                    "Clear\n%s\nand continue?".formatted(destDir.getAbsolutePath()),
                    "Clear existing directory?",
                    NotifyDescriptor.OK_CANCEL_OPTION, // option type
                    NotifyDescriptor.INFORMATION_MESSAGE, // message type
                    null, // own buttons as Object[]
                    null); // initial value
            var retval = DialogDisplayer.getDefault().notify(d);
            result = retval == NotifyDescriptor.OK_OPTION;

            if (result) {
                FileUtils.deleteQuietly(destDir);
            }
        }

        return result;
    }

    private void jobEnded(OutputLineMode outputLineMode, String action) {
        mMainFoldHandle.silentFinish();
        mStatusDisplayer.setStatusText(action);
        mOutputHelper.printSummary(outputLineMode, action, Dict.TASK.toString());
    }

    private int runProcess(List<String> command) {
        var processBuilder = org.netbeans.api.extexecution.base.ProcessBuilder.getLocal();
        processBuilder.setExecutable(command.getFirst());
        if (command.size() > 1) {
            processBuilder.setArguments(command.subList(1, command.size()));
        }

        var descriptor = new ExecutionDescriptor()
                .frontWindow(true)
                .inputOutput(mInputOutput)
                .noReset(true)
                .errLineBased(true)
                .outLineBased(true)
                .showProgress(false);

        var service = ExecutionService.newService(
                processBuilder,
                descriptor,
                mTask.getName());

        var task = service.run();

        try {
            return task.get();
        } catch (InterruptedException ex) {
            task.cancel(true);
        } catch (ExecutionException ex) {
            task.cancel(true);
            mInputOutput.getErr().println(ex);
            Exceptions.printStackTrace(ex);
        }

        return -1;
    }
}
