/*
 *   Galeb - Load Balance as a Service Plataform
 *
 *   Copyright (C) 2014-2015 Globo.com
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

package io.galeb.manager.repository;

public class CommonJpaFilters {

    public static final String SAME_ACCOUNT_FILTER   = "a.name = ?#{ authentication?.name }";
    public static final String HAS_ROLE_ADMIN_FILTER = "1 = ?#{ hasRole('ROLE_ADMIN') ? 1 : 0 }";
    public static final String SECURITY_FILTER       = "(" + HAS_ROLE_ADMIN_FILTER + " OR "
                                                        + SAME_ACCOUNT_FILTER + ")";

}
