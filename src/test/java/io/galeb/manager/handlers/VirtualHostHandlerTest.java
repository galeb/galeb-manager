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

package io.galeb.manager.handlers;

import com.google.common.collect.Sets;
import io.galeb.manager.entity.AbstractEntity;
import io.galeb.manager.entity.Environment;
import io.galeb.manager.entity.Project;
import io.galeb.manager.entity.VirtualHost;
import io.galeb.manager.exceptions.BadRequestException;
import io.galeb.manager.handler.VirtualHostHandler;
import io.galeb.manager.repository.VirtualHostRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VirtualHostHandlerTest {

    private final List<VirtualHost> virtualhosts = new ArrayList<>();
    private final VirtualHostRepository virtualHostRepository = mock(VirtualHostRepository.class);
    private final VirtualHostHandler virtualHostHandler = new VirtualHostHandler(){
        @Override
        protected VirtualHostRepository getVirtualHostRepository() {
            return virtualHostRepository;
        }
    };

    private VirtualHost newVirtualhost(String name, Set<String> aliasesNew) {
        VirtualHost virtualhostNotPersisted = new VirtualHost(name, new Environment("undef"), new Project("undef"));
        virtualhostNotPersisted.setAliases(aliasesNew);
        return virtualhostNotPersisted;
    }

    private void saveMocked(final VirtualHost virtualHost) {
        virtualhosts.add(virtualHost);
        Set<String> allNames = virtualhosts.stream().map(AbstractEntity::getName).collect(Collectors.toSet());
        allNames.addAll(virtualhosts.stream().flatMap(v -> v.getAliases().stream()).collect(Collectors.toSet()));
        when(virtualHostRepository.findByName(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(virtualhosts));
        when(virtualHostRepository.getAllNames(anyLong())).thenReturn(allNames);
        when(virtualHostRepository.findByFarmId(anyLong(), any(Pageable.class))).thenReturn(new PageImpl<>(virtualhosts));
    }

    @After
    public void cleanup() {
        virtualhosts.clear();
    }

    @Test
    public void aliasesNotChanged() {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c"));
        saveMocked(virtualhost);
        VirtualHost sameVirtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c"));
        Assert.assertTrue(virtualHostHandler.aliasesChanges(sameVirtualhost).isEmpty());
    }

    @Test
    public void aliasAdded() {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c"));
        saveMocked(virtualhost);
        VirtualHost sameVirtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c", "d"));
        Assert.assertFalse(virtualHostHandler.aliasesChanges(sameVirtualhost).isEmpty());
    }

    @Test
    public void aliasRemoved() {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c"));
        saveMocked(virtualhost);
        VirtualHost sameVirtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b"));
        Assert.assertFalse(virtualHostHandler.aliasesChanges(sameVirtualhost).isEmpty());
    }

    @Test
    public void aliasCleaned() {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c"));
        saveMocked(virtualhost);
        VirtualHost sameVirtualhost = newVirtualhost(vname, Collections.emptySet());
        Assert.assertFalse(virtualHostHandler.aliasesChanges(sameVirtualhost).isEmpty());
    }

    @Test
    public void aliasPostDefined() {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(vname, Collections.emptySet());
        saveMocked(virtualhost);
        VirtualHost sameVirtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c"));
        Assert.assertFalse(virtualHostHandler.aliasesChanges(sameVirtualhost).isEmpty());
    }

    @Test(expected = BadRequestException.class)
    public void hasDupVirtualhostNameAlias() throws Exception {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c"));
        saveMocked(virtualhost);
        VirtualHost otherVirtualhost = newVirtualhost(UUID.randomUUID().toString(), Sets.newHashSet("d", "e", vname));
        virtualHostHandler.checkDupOnAliases(otherVirtualhost);
    }

    @Test(expected = BadRequestException.class)
    public void hasDupAliasVirtualhostName() throws Exception {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(UUID.randomUUID().toString(), Sets.newHashSet("a", "b", "c", vname));
        saveMocked(virtualhost);
        VirtualHost otherVirtualhost = newVirtualhost(vname, Sets.newHashSet("d", "e", "f"));
        virtualHostHandler.checkDupOnAliases(otherVirtualhost);
    }

    @Test(expected = BadRequestException.class)
    public void hasDupAliasesName() throws Exception {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(vname, Collections.emptySet());
        saveMocked(virtualhost);
        VirtualHost sameVirtualhost = newVirtualhost(vname, Sets.newHashSet(vname));
        virtualHostHandler.checkDupOnAliases(sameVirtualhost);
    }

    @Test
    public void hasNotDupEqual() throws Exception {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c"));
        saveMocked(virtualhost);
        VirtualHost sameVirtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c"));
        virtualHostHandler.checkDupOnAliases(sameVirtualhost);
    }

    @Test
    public void hasNotDupAdd() throws Exception {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c"));
        saveMocked(virtualhost);
        VirtualHost sameVirtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c", "d"));
        virtualHostHandler.checkDupOnAliases(sameVirtualhost);
    }

    @Test
    public void hasNotDupRemove() throws Exception {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c"));
        saveMocked(virtualhost);
        VirtualHost sameVirtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b"));
        virtualHostHandler.checkDupOnAliases(sameVirtualhost);
    }
}
