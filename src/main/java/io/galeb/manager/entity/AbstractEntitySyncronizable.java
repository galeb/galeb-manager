/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.manager.entity;

import com.google.common.base.Enums;
import io.galeb.core.json.JsonObject;
import io.galeb.core.model.Entity;
import io.galeb.manager.cache.DistMap;
import io.galeb.manager.routermap.RouterMap;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractEntitySyncronizable {

    private static final Log LOGGER = LogFactory.getLog(AbstractEntitySyncronizable.class);

    private static DistMap distMap;
    private static RouterMap routerMap;

    private DistMap getDistMap() {
        if (distMap == null) {
            distMap = DistMap.getInstance();
        }
        return distMap;
    }

    private RouterMap getRouterMap() {
        if (routerMap == null) {
            routerMap = RouterMap.getInstance();
        }
        return routerMap;
    }

    protected abstract AbstractEntity entity();

    protected String getEnvName() { return "NULL"; }

    protected AbstractEntity.EntityStatus getDynamicStatus() {
        try {
            final AbstractEntity.EntityStatus distMapStatus;
            final RouterMap.State routerMapState = getRouterMap().state(getEnvName());
            final String valueFromDistMap = getValueDistMap();
            return (valueFromDistMap == null && farmEnabled()) ||
                    (distMapStatus = getEntityStatusFromValueMap(valueFromDistMap)) != AbstractEntity.EntityStatus.OK ||
                    routerMapState == RouterMap.State.NOSYNC ? AbstractEntity.EntityStatus.PENDING : distMapStatus;
        } catch (Exception e) {
            LOGGER.error(ExceptionUtils.getFullStackTrace(e));
        }
        return AbstractEntity.EntityStatus.PENDING;
    }

    private String getValueDistMap() {
        if (farmEnabled()) {
            return getDistMap().get(entity());
        }
        return null;
    }

    private AbstractEntity.EntityStatus getEntityStatusFromValueMap(String value) {
        if (value == null) return AbstractEntity.EntityStatus.OK;
        AbstractEntity.EntityStatus statusDistMap;
        boolean valueIsStatus = Enums.getIfPresent(AbstractEntity.EntityStatus.class, value).isPresent();
        if (valueIsStatus) {
            statusDistMap = AbstractEntity.EntityStatus.valueOf(value);
        } else {
            Entity entity = (Entity) JsonObject.fromJson(value, Entity.class);
            statusDistMap = (entity.getVersion() == entity().getHash()) ? AbstractEntity.EntityStatus.OK : AbstractEntity.EntityStatus.PENDING;
        }
        return statusDistMap;
    }

    @SuppressWarnings("unchecked")
    private boolean farmEnabled() {
        return this instanceof WithFarmID && ((WithFarmID)this).getFarm().isAutoReload();
    }

    public void releaseSync() {
        getRouterMap().releaseSync(getEnvName());
    }

    protected Farm getFakeFarm() {
        return new Farm().setName("fake").setAutoReload(false);
    }
}
