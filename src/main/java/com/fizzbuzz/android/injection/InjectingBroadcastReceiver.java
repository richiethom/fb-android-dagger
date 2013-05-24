/*
 * Copyright (c) 2013 Fizz Buzz LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fizzbuzz.android.injection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.fizzbuzz.android.injection.Injector;
import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;

import javax.inject.Qualifier;
import javax.inject.Singleton;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class InjectingBroadcastReceiver
        extends BroadcastReceiver
        implements Injector {

    private Context mContext;
    private ObjectGraph mObjectGraph;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;

        // extend the application-scope object graph with the modules for this broadcast receiver
        mObjectGraph = ((Injector)context.getApplicationContext()).getObjectGraph().plus(getModules().toArray());

        // then inject ourselves
        mObjectGraph.inject(this);
    }

    @Override
    public ObjectGraph getObjectGraph() {
        return mObjectGraph;
    }

    public void inject(Object target) {
        checkState(mObjectGraph != null, "object graph must be initialized prior to calling inject");
        mObjectGraph.inject(target);
    }

    protected List<Object> getModules() {
        List<Object> result = new ArrayList<Object>();
        result.add(new InjectingBroadcastReceiverModule(mContext));
        return result;
    }

    @Module(library=true)
    public static class InjectingBroadcastReceiverModule {
        Context mBrContext;

        public InjectingBroadcastReceiverModule(Context brContext) {
            mBrContext = brContext;
        }

        @Provides
        @Singleton
        @BroadcastReceiver
        public Context provideBroadcastReceiverContext() {
            return mBrContext;
        }

        @Qualifier
        @Target({FIELD, PARAMETER, METHOD})
        @Documented
        @Retention(RUNTIME)
        public @interface BroadcastReceiver {
        }
    }
}