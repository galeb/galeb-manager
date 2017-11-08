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
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;
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

    private void saveMocked(final VirtualHost virtualHost, boolean isExcept) {
        if (!isExcept) {
            virtualhosts.add(virtualHost);
        }
        Set<String> allNames = virtualhosts.stream().map(AbstractEntity::getName).collect(Collectors.toSet());
        allNames.addAll(virtualhosts.stream().flatMap(v -> v.getAliases().stream()).collect(Collectors.toSet()));
        when(virtualHostRepository.getAllNamesExcept(any(VirtualHost.class))).thenReturn(allNames);
    }
    private void saveMocked(final VirtualHost virtualHost) {
        saveMocked(virtualHost, false);
    }

    @Before
    public void loadData() {
        VirtualHost vh = newVirtualhost("InitialVH", Sets.newHashSet("initialAlias"));
        virtualhosts.add(vh);
    }

    @After
    public void cleanup() {
        virtualhosts.clear();
    }

    @Test
    public void aliasesNotChanged() throws Exception {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c"));
        saveMocked(virtualhost, true);
        VirtualHost sameVirtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c"));
        virtualHostHandler.checkDupOnAliases(sameVirtualhost, false);
    }

    @Test
    public void aliasAdded() throws Exception {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c"));
        saveMocked(virtualhost, true);
        VirtualHost sameVirtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c", "d"));
        virtualHostHandler.checkDupOnAliases(sameVirtualhost, false);
    }

    @Test
    public void aliasRemoved() throws Exception {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c"));
        saveMocked(virtualhost, true);
        VirtualHost sameVirtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b"));
        virtualHostHandler.checkDupOnAliases(sameVirtualhost, false);
    }

    @Test
    public void aliasCleaned() throws Exception {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c"));
        saveMocked(virtualhost, true);
        VirtualHost sameVirtualhost = newVirtualhost(vname, Collections.emptySet());
        virtualHostHandler.checkDupOnAliases(sameVirtualhost, false);
    }

    @Test
    public void aliasPostDefined() throws Exception {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(vname, Collections.emptySet());
        saveMocked(virtualhost, true);
        VirtualHost sameVirtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c"));
        virtualHostHandler.checkDupOnAliases(sameVirtualhost, false);
    }

    @Test(expected = BadRequestException.class)
    public void hasVirtualhostNameSameAlias() throws Exception {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(vname, Sets.newHashSet("a"));
        saveMocked(virtualhost, true);
        VirtualHost sameVirtualhost = newVirtualhost(vname, Sets.newHashSet(vname));
        virtualHostHandler.checkDupOnAliases(sameVirtualhost, false);
    }

    @Test(expected = BadRequestException.class)
    public void hasDupVirtualhostNameAlias() throws Exception {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c"));
        saveMocked(virtualhost);
        VirtualHost otherVirtualhost = newVirtualhost(UUID.randomUUID().toString(), Sets.newHashSet("d", "e", vname));
        virtualHostHandler.checkDupOnAliases(otherVirtualhost, true);
    }

    @Test(expected = BadRequestException.class)
    public void hasDupAliasVirtualhostName() throws Exception {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(UUID.randomUUID().toString(), Sets.newHashSet("a", "b", "c", vname));
        saveMocked(virtualhost);
        VirtualHost otherVirtualhost = newVirtualhost(vname, Sets.newHashSet("d", "e", "f"));
        virtualHostHandler.checkDupOnAliases(otherVirtualhost, true);
    }

    @Test(expected = BadRequestException.class)
    public void hasDupAliasesName() throws Exception {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(vname, Collections.emptySet());
        saveMocked(virtualhost);
        VirtualHost sameVirtualhost = newVirtualhost(vname, Sets.newHashSet(vname));
        virtualHostHandler.checkDupOnAliases(sameVirtualhost, false);
    }

    @Test
    public void hasNotDupEqual() throws Exception {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c"));
        saveMocked(virtualhost, true);
        VirtualHost sameVirtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c"));
        virtualHostHandler.checkDupOnAliases(sameVirtualhost, false);
    }

    @Test(expected = BadRequestException.class)
    public void hasDupAliasesNameInManyVirtualHosts() throws Exception {
        String vname1 = UUID.randomUUID().toString();
        String vname2 = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(vname1, Sets.newHashSet("a"));
        saveMocked(virtualhost);
        VirtualHost otherVirtualhost = newVirtualhost(vname2, Sets.newHashSet("a"));
        virtualHostHandler.checkDupOnAliases(otherVirtualhost, true);
    }

    @Test
    public void hasNotDupAdd() throws Exception {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c"));
        saveMocked(virtualhost, true);
        VirtualHost sameVirtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c", "d"));
        virtualHostHandler.checkDupOnAliases(sameVirtualhost, false);
    }

    @Test
    public void hasNotDupRemove() throws Exception {
        String vname = UUID.randomUUID().toString();
        VirtualHost virtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b", "c"));
        saveMocked(virtualhost, true);
        VirtualHost sameVirtualhost = newVirtualhost(vname, Sets.newHashSet("a", "b"));
        virtualHostHandler.checkDupOnAliases(sameVirtualhost, false);
    }


}
