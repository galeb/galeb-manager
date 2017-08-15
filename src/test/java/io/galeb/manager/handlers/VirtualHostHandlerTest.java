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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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

    private VirtualHost newVirtualhost(Set<String> aliasesNew) {
        return newVirtualhost("undef", aliasesNew);
    }

    private VirtualHost newVirtualhost(String name, Set<String> aliasesNew) {
        VirtualHost virtualhostNotPersisted = new VirtualHost(name, new Environment("undef"), new Project("undef"));
        virtualhostNotPersisted.setAliases(aliasesNew);
        return virtualhostNotPersisted;
    }

    private void addPersistedVirtualhost() {
        addPersistedVirtualhost("undef");
    }

    private void addPersistedVirtualhost(String name) {
        Set<String> aliases = Sets.newHashSet("a", "b", "c");
        VirtualHost virtualhostPersisted = newVirtualhost(name, aliases);
        virtualhosts.add(virtualhostPersisted);
    }

    private void extractAllNames() {
        Set<String> allNames = virtualhosts.stream().map(AbstractEntity::getName).collect(Collectors.toSet());
        allNames.addAll(virtualhosts.stream().flatMap(v -> v.getAliases().stream()).collect(Collectors.toSet()));
        when(virtualHostRepository.getAllNames(anyLong())).thenReturn(allNames);
        when(virtualHostRepository.findAll()).thenReturn(virtualhosts);
    }

    @Before
    public void setup() {
        addPersistedVirtualhost();
    }

    @After
    public void cleanup() {
        virtualhosts.clear();
    }

    @Test
    public void aliasesNotChanged() {
        when(virtualHostRepository.findByName(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(virtualhosts));
        Set<String> aliasesNew = Sets.newHashSet("a", "b", "c");
        VirtualHost virtualhostNotPersisted = newVirtualhost(aliasesNew);
        Assert.assertTrue(virtualHostHandler.aliasesChanges(virtualhostNotPersisted).isEmpty());
    }

    @Test
    public void aliasAdded() {
        when(virtualHostRepository.findByName(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(virtualhosts));
        Set<String> aliasesNew = Sets.newHashSet("a", "b", "c", "d");
        VirtualHost virtualhostNotPersisted = newVirtualhost(aliasesNew);
        Assert.assertFalse(virtualHostHandler.aliasesChanges(virtualhostNotPersisted).isEmpty());
    }

    @Test
    public void aliasRemoved() {
        when(virtualHostRepository.findByName(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(virtualhosts));
        Set<String> aliasesNew = Sets.newHashSet("a", "b");
        VirtualHost virtualhostNotPersisted = newVirtualhost(aliasesNew);
        Assert.assertFalse(virtualHostHandler.aliasesChanges(virtualhostNotPersisted).isEmpty());
    }

    @Test
    public void aliasCleaned() {
        when(virtualHostRepository.findByName(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(virtualhosts));
        VirtualHost virtualhostNotPersisted = newVirtualhost(Collections.emptySet());
        Assert.assertFalse(virtualHostHandler.aliasesChanges(virtualhostNotPersisted).isEmpty());
    }

    @Test(expected = BadRequestException.class)
    public void hasDupVirtualhostNameAlias() throws Exception {
        Set<String> aliases = Sets.newHashSet("a", "b", "other");
        VirtualHost virtualHost1 = newVirtualhost(aliases);
        virtualhosts.add(virtualHost1);
        when(virtualHostRepository.findByName(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(virtualhosts));
        VirtualHost virtualHost2 = newVirtualhost("other", aliases);
        extractAllNames();
        virtualHostHandler.checkDupOnAliases(virtualHost2);
    }

    @Test(expected = BadRequestException.class)
    public void hasDupVirtualhostName() throws Exception {
        when(virtualHostRepository.findByName(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(virtualhosts));
        Set<String> aliasesNew = Sets.newHashSet("a", "b", "c", "undef");
        VirtualHost virtualhostNotPersisted = newVirtualhost(aliasesNew);
        extractAllNames();
        virtualHostHandler.checkDupOnAliases(virtualhostNotPersisted);
    }

    @Test(expected = BadRequestException.class)
    public void hasDupAliasesName() throws Exception {
        VirtualHost otherVirtualhostPersisted = newVirtualhost("other", Sets.newHashSet("d"));
        virtualhosts.add(otherVirtualhostPersisted);
        when(virtualHostRepository.findByName(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(virtualhosts));
        Set<String> aliasesNew = Sets.newHashSet("a", "b", "c", "d");
        VirtualHost virtualhostNotPersisted = newVirtualhost(aliasesNew);
        extractAllNames();
        virtualHostHandler.checkDupOnAliases(virtualhostNotPersisted);
    }

    @Test
    public void hasNotDupEqual() throws Exception {
        when(virtualHostRepository.findByName(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(virtualhosts));
        Set<String> aliasesNew = Sets.newHashSet("a", "b", "c");
        VirtualHost virtualhostNotPersisted = newVirtualhost(aliasesNew);
        extractAllNames();
        virtualHostHandler.checkDupOnAliases(virtualhostNotPersisted);
    }

    @Test
    public void hasNotDupAdd() throws Exception {
        when(virtualHostRepository.findByName(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(virtualhosts));
        Set<String> aliasesNew = Sets.newHashSet("a", "b", "c", "d");
        VirtualHost virtualhostNotPersisted = newVirtualhost(aliasesNew);
        extractAllNames();
        virtualHostHandler.checkDupOnAliases(virtualhostNotPersisted);
    }

    @Test
    public void hasNotDupRemove() throws Exception {
        when(virtualHostRepository.findByName(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(virtualhosts));
        Set<String> aliasesNew = Sets.newHashSet("a", "b");
        VirtualHost virtualhostNotPersisted = newVirtualhost(aliasesNew);
        extractAllNames();
        virtualHostHandler.checkDupOnAliases(virtualhostNotPersisted);
    }
}
