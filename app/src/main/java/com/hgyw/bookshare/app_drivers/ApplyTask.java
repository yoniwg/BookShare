package com.hgyw.bookshare.app_drivers;

import android.os.AsyncTask;
import android.view.View;

import com.annimon.stream.function.BiConsumer;
import com.annimon.stream.function.Consumer;
import com.annimon.stream.function.Function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The purpose of this class. is to invoke async tast, that do function from source to target in
 * background, and accept the target on post-execute. then the target became a source for more
 * asynctask with their target.
 */
public class ApplyTask<Source,Target> {
    private final Function<Source,Target> targetFunction;
    private final Consumer<Target> targetConsumer;
    private List<ApplyTask<Target, ?>> afterTasks = new ArrayList<>();

    private ApplyTask(Function<Source,Target> targetFunction, Consumer<Target> targetConsumer) {
        this.targetFunction = targetFunction;
        this.targetConsumer = targetConsumer;
    }

    ApplyTask<Source,Target> andThen(ApplyTask<Target,?> task) {
        afterTasks.add(task);
        return this;
    }

    public List<ApplyTask<Target, ?>> getAfterTasks() {
        return Collections.unmodifiableList(afterTasks);
    }

    public void executeAsync(Source source) {
        toAsyncTask(source).execute();
    }

    public AsyncTask<Void, Void, Target> toAsyncTask(Source source) {
        return new AsyncTask<Void, Void, Target>() {
            @Override
            protected Target doInBackground(Void... params) {
                return targetFunction.apply(source);
            }
            @Override
            protected void onPostExecute(Target target) {
                targetConsumer.accept(target);
                for (ApplyTask<Target,?> task : afterTasks) {
                    task.toAsyncTask(target).execute();
                }
            }
        };
    }

    public static <Source,Target,View> ApplyTask<Source,Target>
    toBiConsumer(Function<Source,Target> function, BiConsumer<View, Target> viewApplier, View view) {
        return new ApplyTask<>(function, target -> viewApplier.accept(view, target));
    }

    public static <Source,Target> ApplyTask<Source,Target>
    toConsumer(Function<Source,Target> function, Consumer<Target> consumer) {
        return new ApplyTask<>(function, consumer);
    }
}
