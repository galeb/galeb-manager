/*
 *   Galeb - Load Balance as a Service Plataform
 *
 *   Copyright (C) 2014-2017 Globo.com
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

package io.galeb.manager.routermap;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;
import java.util.Set;

public class JsonSchema {

    @SuppressWarnings({"unused", "WeakerAccess"})
    public static class Env {
        private final String envName;
        private final Set<GroupID> groupIDs;

        public Env(String envName, Set<GroupID> groupIDs) {
            this.envName = envName;
            this.groupIDs = groupIDs;
        }

        @SerializedName("name")
        public String getEnvName() {
            return envName;
        }

        public Set<GroupID> getGroupIDs() {
            return groupIDs;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Env env = (Env) o;
            return Objects.equals(envName, env.envName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(envName);
        }
    }

    public static class GroupID {
        private final String groupID;
        private final Set<Router> routers;

        public GroupID(String groupID, Set<Router> routers) {
            this.routers = routers;
            this.groupID = groupID;
        }

        public Set<Router> getRouters() { return routers; }
        public String getGroupID() { return groupID; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GroupID groupObj = (GroupID) o;
            return Objects.equals(getGroupID(), groupObj.getGroupID());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getGroupID());
        }
    }

    @SuppressWarnings("unused")
    public static class Router {
        private final String localIp;
        private final String etag;
        private final long expire;

        public Router(String localIp, String etag, long expire) {
            this.localIp = localIp;
            this.etag = etag;
            this.expire = expire;
        }

        public String getLocalIp() {
            return localIp;
        }

        public String getEtag() {
            return etag;
        }

        public long getExpire() {
            return expire;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Router router = (Router) o;
            return Objects.equals(localIp, router.localIp);
        }

        @Override
        public int hashCode() {
            return Objects.hash(localIp);
        }
    }
}
