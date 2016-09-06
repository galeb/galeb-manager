/*
 *   Galeb - Load Balance as a Service Plataform
 *
 *   Copyright (C) 2014-2016 Globo.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.galeb.manager.engine.service;

import io.galeb.core.cluster.ClusterLocker;
import io.galeb.core.cluster.ignite.IgniteClusterLocker;
import io.galeb.manager.engine.util.CounterDownLatch;

import java.io.Serializable;
import java.util.Arrays;

public class LockerManager implements Serializable {

    private ClusterLocker locker = IgniteClusterLocker.getInstance().start();

    public void release(String lockId) {
        locker.release(lockId);
    }

    public void release(String lockId, final String[] apis) {
        release(lockId);
        Arrays.stream(apis).forEach(CounterDownLatch::remove);
    }

    public boolean lock(String lockId) {
        return locker.lock(lockId);
    }

    public Boolean contains(String lockId) { return locker.contains(lockId); }

    public String name() {
        return locker.name();
    }
}
