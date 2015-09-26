#!/bin/bash


PROTOCOL="http"
SERVER="localhost:8000"
HEADER="Content-Type: application/json;charset=UTF-8"
TEAM_NAME='xxxxx'
ADMIN_TEAM_NAME='AdminTeam'
PROVIDER_NAME='galeb'
ENV_NAME='desenv'
RULETYPE_NAME='UrlPath'
FARM_NAME='farm1'
BALANCEPOLICYTYPE_NAME='RoundRobin'
BALANCEPOLICY_NAME='RoundRobin'
DOMAIN="${FARM_NAME}.localhost"
API='http://localhost:9090'

PROJECT_NAME='xxxxxx'
VIRTUALHOST_NAME='test.localhost'
RULE_NAME="rule_of_$VIRTUALHOST_NAME"
POOL_NAME="pool_of_$VIRTUALHOST_NAME"
BACKENDIP='127.0.0.1'
BACKEND_STARTPORT=8081
BACKEND_ENDPORT=8084

TOKEN=""

showPreRequisite() {
cat <<eof

PRE-REQUISITES:

  * jq - lightweight and flexible command-line JSON processor
  * curl - command line tool for transferring data with URL syntax

eof
exit
}

usage() {
cat <<eof

usage: $0 [admin]

eof
exit
}

hasJq() {
  echo {} | jq . > /dev/null 2>&1
  return $?
}

hasCurl() {
  curl --help > /dev/null 2>&1
  return $?
}

if ! (hasJq && hasCurl); then
  showPreRequisite
fi

if [ "x$1" == "x-h" -o "x$1" == "x--help" ]; then
  usage
fi

loginAccount() {
  local MESSAGE=$1
  local LOGIN=$2
  local PASSWORD=$3

  if [ "x$PASSWORD" == "x" ]; then
    echo -n "$MESSAGE password: "
    read -s PASSWORD
    echo
  else
    PASSWORD='password'
  fi

  export TOKEN="$(curl -k -v ${PROTOCOL}://${LOGIN}:${PASSWORD}@${SERVER}/token | jq -r .token)"
  echo
}

logoutAccount() {
  local TOKEN=$1

  curl -k -XPOST -H"x-auth-token: $TOKEN" ${PROTOCOL}://${SERVER}/logout
  echo
}

getId() {
  local TOKEN=$1
  local TYPE=$2
  local NAME=$3

  # TYPE: pool, target, rule, virtualhost, farm, environment, targettype, ruletype,
  #       project, account, team, etc.
  curl -k -s -XGET -H"x-auth-token: $TOKEN" \
    ${PROTOCOL}://${SERVER}/${TYPE}/search/findByName?name=${NAME} | \
  jq ._embedded.${TYPE}[0].id
}

createTeam() {
  local TOKEN=$1
  local NAME=$2

  curl -k -v -XPOST -H"x-auth-token: $TOKEN" -H ${HEADER} \
    -d '{ "name":"'${NAME}'" }' ${PROTOCOL}://${SERVER}/team
  echo
}

createAccount() {
  local TOKEN=$1
  local ROLES=$2
  local TEAM_NAME=$3
  local LOGIN=$4
  local RANDOM_EMAIL="fake.$(date +%s%N)@fake.com"
  local TEAM_ID="$(getId ${TOKEN} team ${TEAM_NAME})"

  curl -k -v -XPOST -H ${HEADER} \
       -d '{
              "name": "'${LOGIN}'",
              "password": "password",
              "email": "'${RANDOM_EMAIL}'" ,
              "roles": [ '${ROLES}' ],
              "teams": [ "'${PROTOCOL}'://'${SERVER}'/team/'${TEAM_ID}'" ]
          }' \
       -H"x-auth-token: $TOKEN" ${PROTOCOL}://${SERVER}/account
  echo
}

createProvider() {
  local TOKEN=$1
  local NAME=$2
  local DRIVER_NAME='GalebV3'

  curl -k -v -XPOST -H ${HEADER} \
       -d '{
              "name": "'${NAME}'",
              "driver": "'${DRIVER_NAME}'"
          }' \
       -H"x-auth-token: $TOKEN" ${PROTOCOL}://${SERVER}/provider
  echo
}

createEnvironment() {
  local TOKEN=$1
  local NAME=$2

  curl -k -v -XPOST -H ${HEADER} \
       -d '{ "name":"'${NAME}'" }' \
       -H"x-auth-token: $TOKEN" ${PROTOCOL}://${SERVER}/environment
  echo
}

createTargetType () {
  local TOKEN=$1
  local NAME=$2

  curl -k -v -XPOST -H ${HEADER} \
       -d '{ "name":"'${NAME}'" }' \
       -H"x-auth-token: $TOKEN" ${PROTOCOL}://${SERVER}/targettype
  echo
}

createRuleType() {
  local TOKEN=$1
  local NAME=$2

  curl -k -v -XPOST -H ${HEADER} \
       -d '{ "name": "'${NAME}'" }' \
       -H"x-auth-token: $TOKEN" ${PROTOCOL}://${SERVER}/ruletype
  echo
}

createFarm () {
  local TOKEN=$1
  local NAME=$2
  local ENV_ID="$(getId ${TOKEN} environment ${ENV_NAME})"
  local PROVIDER_ID="$(getId ${TOKEN} provider ${PROVIDER_NAME})"

  curl -k -v -XPOST -H ${HEADER} \
       -d '{
              "name": "'${NAME}'",
              "domain": "'${DOMAIN}'",
              "api": "'${API}'",
              "autoReload": true,
              "environment": "'${PROTOCOL}'://'${SERVER}'/environment/'${ENV_ID}'",
              "provider": "'${PROTOCOL}'://'${SERVER}'/provider/'${PROVIDER_ID}'"
          }' \
       -H"x-auth-token: $TOKEN" ${PROTOCOL}://${SERVER}/farm
  echo
}

createBalancePolicyType() {
  local TOKEN=$1
  local NAME=$2

  curl -k -v -XPOST -H ${HEADER} \
       -d '{ "name": "'${NAME}'" }' \
       -H"x-auth-token: $TOKEN" ${PROTOCOL}://${SERVER}/balancepolicytype
  echo
}

createBalancePolicy() {
  local TOKEN=$1
  local NAME=$2
  local BALANCEPOLICYTYPE_ID="$(getId ${TOKEN} balancepolicytype ${BALANCEPOLICYTYPE_NAME})"

  curl -k -v -XPOST -H ${HEADER} \
       -d '{
              "name": "'${NAME}'",
              "balancePolicyType": "'${PROTOCOL}'://'${SERVER}'/balancepolicytype/'${BALANCEPOLICYTYPE_ID}'"
          }' \
       -H"x-auth-token: $TOKEN" ${PROTOCOL}://${SERVER}/balancepolicy
  echo
}

createProject() {
  local TOKEN=$1
  local NAME=$2
  local TEAM_ID="$(getId ${TOKEN} team ${TEAM_NAME})"

  curl -k -v -XPOST -H ${HEADER} \
       -d '{
              "name":"'${NAME}'",
              "teams": [ "'${PROTOCOL}://${SERVER}'/team/'${TEAM_ID}'" ]
          }' \
       -H"x-auth-token: $TOKEN" ${PROTOCOL}://${SERVER}/project
  echo
}

createVirtualHost() {
  local TOKEN=$1
  local NAME=$2
  local PROJECT_ID="$(getId ${TOKEN} project ${PROJECT_NAME})"
  local ENV_ID="$(getId ${TOKEN} environment ${ENV_NAME})"

  curl -k -v -XPOST -H ${HEADER} \
       -d '{
              "name": "'${VIRTUALHOST_NAME}'",
              "environment": "'${PROTOCOL}'://'${SERVER}'/environment/'${ENV_ID}'",
              "project": "'${PROTOCOL}'://'${SERVER}'/project/'${PROJECT_ID}'"
          }' \
       -H"x-auth-token: $TOKEN" ${PROTOCOL}://${SERVER}/virtualhost
  echo
}

createBackendPool() {
  local TOKEN=$1
  local NAME=$2
  local ENV_ID="$(getId ${TOKEN} environment ${ENV_NAME})"
  local PROJECT_ID="$(getId ${TOKEN} project ${PROJECT_NAME})"
  local BALANCEPOLICY_ID="$(getId ${TOKEN} balancepolicy ${BALANCEPOLICY_NAME})"

  curl -k -v -XPOST -H ${HEADER} \
       -d '{
              "name": "'${NAME}'",
              "environment": "'${PROTOCOL}'://'${SERVER}'/environment/'${ENV_ID}'",
              "project": "'${PROTOCOL}'://'${SERVER}'/project/'${PROJECT_ID}'",
              "balancePolicy": "'${PROTOCOL}'://'${SERVER}'/balancepolicy/'${BALANCEPOLICY_ID}'",
              "properties": {
                  "hcPath": "/",
                  "hcBody": "OK",
                  "hcStatusCode": 200
              }
          }' \
       -H"x-auth-token: $TOKEN" ${PROTOCOL}://${SERVER}/pool
  echo
}

createBackend() {
  local TOKEN=$1
  local NAME=$2
  local ENV_ID="$(getId ${TOKEN} environment ${ENV_NAME})"
  local PROJECT_ID="$(getId ${TOKEN} project ${PROJECT_NAME})"
  local POOL_ID="$(getId ${TOKEN} pool ${POOL_NAME})"

  curl -k -v -XPOST -H ${HEADER} \
       -d '{
              "name": "'${NAME}'",
              "environment": "'${PROTOCOL}'://'${SERVER}'/environment/'${ENV_ID}'",
              "project": "'${PROTOCOL}'://'${SERVER}'/project/'${PROJECT_ID}'",
              "parent": "'${PROTOCOL}'://'${SERVER}'/pool/'${POOL_ID}'"
          }' \
       -H"x-auth-token: $TOKEN" ${PROTOCOL}://${SERVER}/pool
  echo
}

createRule() {
  local TOKEN=$1
  local NAME=$2
  local RULETYPE_URLPATH_ID="$(getId ${TOKEN} ruletype ${RULETYPE_NAME})"
  local VIRTUALHOST_ID=$(getId ${TOKEN} virtualhost ${VIRTUALHOST_NAME})
  local POOL_ID="$(getId ${TOKEN} pool ${POOL_NAME})"

  curl -k -v -XPOST -H ${HEADER} \
       -d '{
              "name": "'${NAME}'",
              "ruleType": "'${PROTOCOL}'://'${SERVER}'/ruletype/'${RULETYPE_URLPATH_ID}'",
              "pool": "'${PROTOCOL}'://'${SERVER}'/pool/'${POOL_ID}'",
              "default": true,
              "order": 0,
              "properties": {
                  "match": "/"
              }
          }' \
       -H"x-auth-token: $TOKEN" ${PROTOCOL}://${SERVER}/rule
  echo

  local RULE_ID="$(getId ${TOKEN} rule ${NAME})"
  curl -k -v -XPATCH -H 'Content-Type: text/uri-list' \
       -d "$PROTOCOL://$SERVER/virtualhost/$VIRTUALHOST_ID" \
       -H"x-auth-token: $TOKEN" ${PROTOCOL}://${SERVER}/rule/${RULE_ID}/parents
  echo

  # OR:
  # curl -k -v -XPOST -H $HEADER \
  #     -d '{
  #            "name": "'$NAME'",
  #            "ruleType": "'$PROTOCOL'://'$SERVER'/ruletype/'$RULETYPE_URLPATH_ID'",
  #            "pool": "'$PROTOCOL'://'$SERVER'/pool/'$POOL_ID'",
  #            "parents": [ "'$PROTOCOL'://'$SERVER'/virtualhost/'$VIRTUALHOST_ID'" ],
  #            "default": true,
  #            "order": 0,
  #            "properties": {
  #                "match": "/"
  #            }
  #        }' \
  #     -H"x-auth-token: $TOKEN" $PROTOCOL://$SERVER/rule
}

removeRule() {
  local TOKEN=$1
  local NAME=$2
  local ID="$(getId ${TOKEN} rule ${NAME})"

  curl -k -v -XDELETE -H ${HEADER} -H"x-auth-token: $TOKEN" ${PROTOCOL}://${SERVER}/rule/${ID}
  echo
}

removeVirtualHost() {
  local TOKEN=$1
  local NAME=$2
  local ID="$(getId ${TOKEN} virtualhost ${NAME})"

  curl -k -v -XDELETE -H ${HEADER} -H"x-auth-token: $TOKEN" ${PROTOCOL}://${SERVER}/virtualhost/${ID}
  echo
}

getNumBackendsByPool() {
  local TOKEN=$1
  local POOL_NAME=$2
  local POOL_ID="$(getId ${TOKEN} pool ${POOL_NAME})"
  curl -k -s -XGET -H"x-auth-token: $TOKEN" \
    ${PROTOCOL}://${SERVER}/pool/${POOL_ID}/targets?size=9999 | \
  jq '._embedded.target | length'
}

getFirstTargetIdByPool() {
  local TOKEN=$1
  local POOL_NAME=$2
  local POOL_ID="$(getId ${TOKEN} pool ${POOL_NAME})"

  curl -k -s -XGET -H"x-auth-token: $TOKEN" \
    ${PROTOCOL}'://'${SERVER}'/pool/'${POOL_ID}'/targets?size=99999' | \
  jq ._embedded.target[0].id
}

removeBackendsOfPool() {
  local TOKEN=$1
  local POOL_NAME=$2

  NUM_BACKENDS_BY_POOL="$(getNumBackendsByPool ${TOKEN} ${POOL_NAME})"

  if [ -n "$NUM_BACKENDS_BY_POOL" ] && [ ${NUM_BACKENDS_BY_POOL} -gt 0 ]; then
      while [ -n "$(getNumBackendsByPool ${TOKEN} ${POOL_NAME})" ] && [ $(getNumBackendsByPool ${TOKEN} ${POOL_NAME}) -gt 0 ];do
          TARGET_ID="$(getFirstTargetIdByPool ${TOKEN} ${POOL_NAME})"
          curl -k -v -XDELETE -H ${HEADER} -H"x-auth-token: $TOKEN" ${PROTOCOL}://${SERVER}/target/${TARGET_ID}
          echo
      done
  fi
}

removeBackendPool() {
  local TOKEN=$1
  local NAME=$2
  local ID="$(getId ${TOKEN} pool ${NAME})"

  curl -k -v -XDELETE -H ${HEADER} -H"x-auth-token: $TOKEN" ${PROTOCOL}://${SERVER}/pool/${ID}
  echo
}

removeProject() {
  local TOKEN=$1
  local NAME=$2
  local ID="$(getId ${TOKEN} project ${NAME})"

  curl -k -v -XDELETE -H ${HEADER} -H"x-auth-token: $TOKEN" ${PROTOCOL}://${SERVER}/project/${ID}
  echo
}

###
if [ "x$1" == "xadmin" ] ; then

## ADMIN CONTEXT

# LOGIN WITH INTERNAL ADMIN ACCOUNT
loginAccount '(internal admin)' admin password

# CREATE A TEAM
createTeam ${TOKEN} ${ADMIN_TEAM_NAME}

# CREATE A ACCOUNT WITH ADMIN ROLE
echo -n 'Enter a loginAccount with admin role (it will be created, if it does not exist): '
read ADMIN_LOGIN
createAccount ${TOKEN} \
              '"ROLE_USER","ROLE_ADMIN"' ${ADMIN_TEAM_NAME} ${ADMIN_LOGIN}

ADMIN_ACCOUNT_ID="$(getId ${TOKEN} account ${ADMIN_LOGIN})"

# LOGOUT INTERNAL ADMIN
logoutAccount ${TOKEN}


# LOGIN WITH A NEW ADMIN ACCOUNT
loginAccount '(new admin)' ${ADMIN_LOGIN}

# CREATE A TEAM
createTeam ${TOKEN} ${TEAM_NAME}

echo -n 'Enter a loginAccount with user role (it will be created, if it does not exist): '
read USER_LOGIN
if [ "x$USER_LOGIN" != "x$ADMIN_LOGIN" ]; then
  # CREATE A ACCOUNT WITH USER ROLE ONLY
  createAccount ${TOKEN} '"ROLE_USER"' ${TEAM_NAME} ${USER_LOGIN}
  USER_ACCOUNT_ID="$(getId ${TOKEN} account ${USER_LOGIN})"
else
  # MODIFY ADMIN ACCOUNT TEAMS
  ADMIN_TEAM_ID="$(getId ${TOKEN} team ${ADMIN_TEAM_NAME})"
  TEAM_ID="$(getId ${TOKEN} team ${TEAM_NAME})"
  curl -k -v -XPATCH -H ${HEADER} \
       -d '{ "teams": [ "'${PROTOCOL}'://'${SERVER}'/team/'${ADMIN_TEAM_ID}'",
                        "'${PROTOCOL}'://'${SERVER}'/team/'${TEAM_ID}'" ] }' \
       -b ${TOKEN} ${PROTOCOL}://${SERVER}/account/${ADMIN_ACCOUNT_ID}
fi

# CREATE A PROVIDER
createProvider ${TOKEN} ${PROVIDER_NAME}

# CREATE A ENVIRONMENT
createEnvironment ${TOKEN} ${ENV_NAME}

# CREATE TARGET TYPES (unnecessary for now)
#createTargetType $TOKEN $TARGETTYPE_POOL_NAME
#createTargetType $TOKEN $TARGETTYPE_BACKEND_NAME

# CREATE A RULE TYPE
createRuleType ${TOKEN} ${RULETYPE_NAME}

# CREATE A FARM (Environment and Provider are required)
createFarm ${TOKEN} ${FARM_NAME}

# CREATE BALANCE POLICY TYPE
createBalancePolicyType ${TOKEN} ${BALANCEPOLICYTYPE_NAME}

# CREATE BALANCE POLICY
createBalancePolicy ${TOKEN} ${BALANCEPOLICY_NAME}

# LOGOUT NEW ADMIN ACCOUNT
logoutAccount ${TOKEN}

fi # END ADMIN CONTEXT
####

### USER CONTEXT

# LOGIN WITH USER ACCOUNT
if [ "x$USER_LOGIN" == "x" ]; then
  echo -n 'Enter a loginAccount with user role: '
  read USER_LOGIN
fi
loginAccount '(user)' ${USER_LOGIN}

# CREATE A PROJECT
createProject ${TOKEN} ${PROJECT_NAME}

# CREATE A VIRTUALHOST
createVirtualHost ${TOKEN} ${VIRTUALHOST_NAME}

# CREATE A POOL
createBackendPool ${TOKEN} ${POOL_NAME}

# CREATE BACKENDS (Pool is required and defined in parent property)
for PORT in $(seq ${BACKEND_STARTPORT} ${BACKEND_ENDPORT}); do
    createBackend ${TOKEN} 'http://'${BACKENDIP}':'${PORT}
done

# CREATE A RULE (Virtualhost and Pool is required)
createRule ${TOKEN} ${RULE_NAME}

####

# Wait
for x in $(seq 1 5);do echo -n .;sleep 1;done;echo

# REMOVE A RULE
removeRule ${TOKEN} ${RULE_NAME}

# REMOVE A VIRTUALHOST
removeVirtualHost ${TOKEN} ${VIRTUALHOST_NAME}

# REMOVE BACKENDS OF THE POOL
removeBackendsOfPool ${TOKEN} ${POOL_NAME}

# REMOVE A POOL
removeBackendPool ${TOKEN} ${POOL_NAME}

# REMOVE A PROJECT
removeProject ${TOKEN} ${PROJECT_NAME}

# LOGOUT
logoutAccount ${TOKEN}
