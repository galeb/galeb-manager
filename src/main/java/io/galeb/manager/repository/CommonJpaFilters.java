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

    public static final String IS_GLOBAL_FILTER = "e.global = TRUE";

    public static final String NATIVE_QUERY_PROJECT_TO_ACCOUNT =
            "inner join project p on e.project_id=p.id " +
            "inner join project_teams teams on p.id=teams.project_id " +
            "inner join team t on teams.team_id=t.id " +
            "left outer join account_teams accounts on t.id=accounts.team_id " +
            "left outer join account a on accounts.account_id=a.id ";

    public static final String QUERY_PROJECT_TO_ACCOUNT = "INNER JOIN e.project.teams t " +
            "LEFT JOIN t.accounts a ";
}
