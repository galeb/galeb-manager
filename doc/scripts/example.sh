#!/bin/bash


PROTOCOL="http"
SERVER="localhost"
HEADER="Content-Type: application/json;charset=UTF-8"
TEAM_NAME='xxxxx'
ADMIN_TEAM_NAME='AdminTeam'
PROVIDER_NAME='galeb'
ENV_NAME='desenv'
TARGETTYPE_POOL_NAME='BackendPool'
TARGETTYPE_BACKEND_NAME='Backend'
RULETYPE_NAME='UrlPath'
FARM_NAME='farm1'
BALANCEPOLICYTYPE_NAME='RoundRobin'
BALANCEPOLICY_NAME='RoundRobin'
DOMAIN="${FARM_NAME}.localhost"
API='localhost:9090'

PROJECT_NAME='xxxxxx'
VIRTUALHOST_NAME='test.localhost'
RULE_NAME="rule_of_$VIRTUALHOST_NAME"
POOL_NAME="pool_of_$VIRTUALHOST_NAME"
BACKENDIP='127.0.0.1'
BACKEND_STARTPORT=8081
BACKEND_ENDPORT=8084

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

if [ "x$1" == "x-h" || "x$1" == "x--help" ]; then
  usage
fi

login() {
  local COOKIE=$1
  local MESSAGE=$2
  local LOGIN=$3
  local PASSWORD=$4

  if [ "x$PASSWORD" == "x" ]; then
    echo -n "$MESSAGE password: "
    read -s PASSWORD
    echo
  else
    PASSWORD='password'
  fi

  curl -k -v -c $COOKIE -F "username=$LOGIN" -F "password=$PASSWORD" $PROTOCOL://$SERVER/login
  # or: curl -k -v -c $COOKIE ${PROTOCOL}://${LOGIN}:${PASSWORD}@${SERVER}/login
  echo
}

logout() {
  local COOKIE=$1

  curl -k -XPOST -b $COOKIE $PROTOCOL://$SERVER/logout && rm -f $COOKIE
  echo
}

getId() {
  local COOKIE=$1
  local TYPE=$2
  local NAME=$3

  # TYPE: target, rule, virtualhost, farm, environment, targettype, ruletype,
  #       project, account, team, etc.
  curl -k -s -XGET -b $COOKIE \
    $PROTOCOL://$SERVER/$TYPE/search/findByName?name=$NAME | \
  jq ._embedded.$TYPE[0].id
}

createTeam() {
  local COOKIE=$1
  local NAME=$2

  curl -k -v -XPOST -b $COOKIE -H $HEADER \
    -d '{ "name":"'$ADMIN_TEAM_NAME'" }' $PROTOCOL://$SERVER/team
  echo
}

createAccount() {
  local COOKIE=$1
  local ROLES=$2
  local TEAM_NAME=$3
  local LOGIN=$4
  local RANDOM_EMAIL="fake.$(date +%s%N)@fake.com"
  local TEAM_ID="$(getId $COOKIE team $TEAM_NAME)"

  curl -k -v -XPOST -H $HEADER \
       -d '{
              "name": "'$LOGIN'",
              "password": "password",
              "email": "'$RANDOM_EMAIL'" ,
              "roles": [ '$ROLES' ],
              "teams": [ "'$PROTOCOL'://'$SERVER'/team/'$TEAM_ID'" ]
          }' \
       -b $COOKIE $PROTOCOL://$SERVER/account
  echo
}

createProvider() {
  local COOKIE=$1
  local NAME=$2
  local DRIVER_NAME='GalebV3'

  curl -k -v -XPOST -H $HEADER \
       -d '{
              "name": "'$NAME'",
              "driver": "'$DRIVER_NAME'"
          }' \
       -b $COOKIE $PROTOCOL://$SERVER/provider
  echo
}

createEnvironment() {
  local COOKIE=$1
  local NAME=$2

  curl -k -v -XPOST -H $HEADER \
       -d '{ "name":"'$NAME'" }' \
       -b $COOKIE $PROTOCOL://$SERVER/environment
  echo
}

createTargetType () {
  local COOKIE=$1
  local NAME=$2

  curl -k -v -XPOST -H $HEADER \
       -d '{ "name":"'$NAME'" }' \
       -b $COOKIE $PROTOCOL://$SERVER/targettype
  echo
}

createRuleType() {
  local COOKIE=$1
  local NAME=$2

  curl -k -v -XPOST -H $HEADER \
       -d '{ "name": "'$NAME'" }' \
       -b $COOKIE $PROTOCOL://$SERVER/ruletype
  echo
}

createFarm () {
  local COOKIE=$1
  local NAME=$2
  local ENV_ID="$(getId $COOKIE environment $ENV_NAME)"
  local PROVIDER_ID="$(getId $COOKIE provider $PROVIDER_NAME)"

  curl -k -v -XPOST -H $HEADER \
       -d '{
              "name": "'$NAME'",
              "domain": "'$DOMAIN'",
              "api": "'$API'",
              "environment": "'$PROTOCOL'://'$SERVER'/environment/'$ENV_ID'",
              "provider": "'$PROTOCOL'://'$SERVER'/provider/'$PROVIDER_ID'"
          }' \
       -b $COOKIE $PROTOCOL://$SERVER/farm
  echo
}

createBalancePolicyType() {
  local COOKIE=$1
  local NAME=$2

  curl -k -v -XPOST -H $HEADER \
       -d '{ "name": "'$NAME'" }' \
       -b $COOKIE $PROTOCOL://$SERVER/balancepolicytype
  echo
}

createBalancePolicy() {
  local COOKIE=$1
  local NAME=$2
  local BALANCEPOLICYTYPE_ID="$(getId $COOKIE balancepolicytype $BALANCEPOLICYTYPE_NAME)"

  curl -k -v -XPOST -H $HEADER \
       -d '{
              "name": "'$NAME'",
              "balancePolicyType": "'$PROTOCOL'://'$SERVER'/balancepolicytype/'$BALANCEPOLICYTYPE_ID'"
          }' \
       -b $COOKIE $PROTOCOL://$SERVER/balancepolicy
  echo
}

createProject() {
  local COOKIE=$1
  local NAME=$2
  local TEAM_ID="$(getId $COOKIE team $TEAM_NAME)"

  curl -k -v -XPOST -H $HEADER \
       -d '{
              "name":"'$NAME'",
              "teams": [ "'$PROTOCOL://$SERVER'/team/'$TEAM_ID'" ]
          }' \
       -b $COOKIE $PROTOCOL://$SERVER/project
  echo
}

createVirtualHost() {
  local COOKIE=$1
  local NAME=$2
  local PROJECT_ID="$(getId $COOKIE project $PROJECT_NAME)"
  local ENV_ID="$(getId $COOKIE environment $ENV_NAME)"

  curl -k -v -XPOST -H $HEADER \
       -d '{
              "name": "'$VIRTUALHOST_NAME'",
              "environment": "'$PROTOCOL'://'$SERVER'/environment/'$ENV_ID'",
              "project": "'$PROTOCOL'://'$SERVER'/project/'$PROJECT_ID'"
          }' \
       -b $COOKIE $PROTOCOL://$SERVER/virtualhost
  echo
}

createBackendPool() {
  local COOKIE=$1
  local NAME=$2
  local TARGETTYPE_POOL_ID="$(getId $COOKIE targettype BackendPool)"
  local ENV_ID="$(getId $COOKIE environment $ENV_NAME)"
  local PROJECT_ID="$(getId $COOKIE project $PROJECT_NAME)"
  local BALANCEPOLICY_ID="$(getId $COOKIE balancepolicy $BALANCEPOLICY_NAME)"

  curl -k -v -XPOST -H $HEADER \
       -d '{
              "name": "'$NAME'",
              "targetType": "'$PROTOCOL'://'$SERVER'/targettype/'$TARGETTYPE_POOL_ID'",
              "environment": "'$PROTOCOL'://'$SERVER'/environment/'$ENV_ID'",
              "project": "'$PROTOCOL'://'$SERVER'/project/'$PROJECT_ID'",
              "balancePolicy": "'$PROTOCOL'://'$SERVER'/balancepolicy/'$BALANCEPOLICY_ID'",
              "properties": {
                  "hcPath": "/",
                  "hcBody": "OK",
                  "hcStatusCode": 200
              }
          }' \
       -b $COOKIE $PROTOCOL://$SERVER/target
  echo
}

createBackend() {
  local COOKIE=$1
  local NAME=$2
  local TARGETTYPE_BACKEND_ID="$(getId $COOKIE targettype Backend)"
  local ENV_ID="$(getId $COOKIE environment $ENV_NAME)"
  local PROJECT_ID="$(getId $COOKIE project $PROJECT_NAME)"
  local POOL_ID="$(getId $COOKIE target $POOL_NAME)"

  curl -k -v -XPOST -H $HEADER \
       -d '{
              "name": "'$NAME'",
              "targetType": "'$PROTOCOL'://'$SERVER'/targettype/'$TARGETTYPE_BACKEND_ID'",
              "environment": "'$PROTOCOL'://'$SERVER'/environment/'$ENV_ID'",
              "project": "'$PROTOCOL'://'$SERVER'/project/'$PROJECT_ID'",
              "parent": "'$PROTOCOL'://'$SERVER'/target/'$POOL_ID'"
          }' \
       -b $COOKIE $PROTOCOL://$SERVER/target
  echo
}

createRule() {
  local COOKIE=$1
  local NAME=$2
  local RULETYPE_URLPATH_ID="$(getId $COOKIE ruletype $RULETYPE_NAME)"
  local VIRTUALHOST_ID=$(getId $COOKIE virtualhost $VIRTUALHOST_NAME)
  local POOL_ID="$(getId $COOKIE target $POOL_NAME)"

  curl -k -v -XPOST -H $HEADER \
       -d '{
              "name": "'$NAME'",
              "ruleType": "'$PROTOCOL'://'$SERVER'/ruletype/'$RULETYPE_URLPATH_ID'",
              "parent": "'$PROTOCOL'://'$SERVER'/virtualhost/'$VIRTUALHOST_ID'",
              "target": "'$PROTOCOL'://'$SERVER'/target/'$POOL_ID'",
              "default": true,
              "order": 0,
              "properties": {
                  "match": "/"
              }
          }' \
       -b $COOKIE $PROTOCOL://$SERVER/rule
  echo
}

removeRule() {
  local COOKIE=$1
  local NAME=$2
  local ID="$(getId $COOKIE rule $NAME)"

  curl -k -v -XDELETE -H $HEADER -b $COOKIE $PROTOCOL://$SERVER/rule/$ID
  echo
}

removeVirtualHost() {
  local COOKIE=$1
  local NAME=$2
  local ID="$(getId $COOKIE virtualhost $NAME)"

  curl -k -v -XDELETE -H $HEADER -b $COOKIE $PROTOCOL://$SERVER/virtualhost/$ID
  echo
}

getNumBackendsByPool() {
  local COOKIE=$1
  local POOL_NAME=$2

  curl -k -s -XGET -b $COOKIE \
    $PROTOCOL'://'$SERVER'/target/search/findByParentName?name='$POOL_NAME'&size=99999' | \
  jq .page.totalElements
}

getFirstTargetIdByPool() {
  local COOKIE=$1
  local POOL_NAME=$2

  curl -k -s -XGET -b $COOKIE \
    $PROTOCOL'://'$SERVER'/target/search/findByParentName?name='$POOL_NAME'&size=99999' | \
  jq ._embedded.target[0].id
}

removeBackendsOfPool() {
  local COOKIE=$1
  local POOL_NAME=$2

  NUM_BACKENDS_BY_POOL="$(getNumBackendsByPool $COOKIE $POOL_NAME)"

  if [ -n "$NUM_BACKENDS_BY_POOL" -a "x$NUM_BACKENDS_BY_POOL" != "x0" ]; then
      while [ "$(getNumBackendsByPool $COOKIE $POOL_NAME)" -gt 0 ];do
          TARGET_ID="$(getFirstTargetIdByPool $COOKIE $POOL_NAME)"
          curl -k -v -XDELETE -H $HEADER -b $COOKIE $PROTOCOL://$SERVER/target/$TARGET_ID
          echo
      done
  fi
}

removeBackendPool() {
  local COOKIE=$1
  local NAME=$2
  local ID="$(getId $COOKIE target $NAME)"

  curl -k -v -XDELETE -H $HEADER -b $COOKIE $PROTOCOL://$SERVER/target/$ID
  echo
}

removeProject() {
  local COOKIE=$1
  local NAME=$2
  local ID="$(getId $COOKIE project $NAME)"

  curl -k -v -XDELETE -H $HEADER -b $COOKIE $PROTOCOL://$SERVER/project/$ID
  echo
}

###
if [ "x$1" == "xadmin" ] ; then

## ADMIN CONTEXT

# LOGIN WITH INTERNAL ADMIN ACCOUNT
login /tmp/cookiestorage1 '(internal admin)' admin password

# CREATE A TEAM
createTeam /tmp/cookiestorage1 $ADMIN_TEAM_NAME

# CREATE A ACCOUNT WITH ADMIN ROLE
echo -n 'Enter a login with admin role (it will be created, if it does not exist): '
read ADMIN_LOGIN
createAccount /tmp/cookiestorage1 \
              '"ROLE_USER","ROLE_ADMIN"' $ADMIN_TEAM_NAME $ADMIN_LOGIN

ADMIN_ACCOUNT_ID="$(getId /tmp/cookiestorage1 account $ADMIN_LOGIN)"

# LOGOUT INTERNAL ADMIN
logout /tmp/cookiestorage1


# LOGIN WITH A NEW ADMIN ACCOUNT
login /tmp/cookiestorage2 '(new admin)' $ADMIN_LOGIN

# CREATE A TEAM
createTeam /tmp/cookiestorage2 $TEAM_NAME

if [ "x$USER_LOGIN" != "x$ADMIN_LOGIN "]; then
  # CREATE A ACCOUNT WITH USER ROLE ONLY
  echo -n 'Enter a login with user role (it will be created, if it does not exist): '
  read USER_LOGIN
  createAccount /tmp/cookiestorage1 '"ROLE_USER"' $TEAM_NAME $USER_LOGIN
  USER_ACCOUNT_ID="$(getId /tmp/cookiestorage2 account $USER_LOGIN)"
else
  # MODIFY ADMIN ACCOUNT TEAMS
  curl -k -v -XPATCH -H $HEADER \
       -d '{ "teams": [ "'$PROTOCOL'://'$SERVER'/team/'$ADMIN_TEAM_ID'",
                        "'$PROTOCOL'://'$SERVER'/team/'$TEAM_ID'" ] }' \
       -b /tmp/cookiestorage2 $PROTOCOL://$SERVER/account/$ADMIN_ACCOUNT_ID
fi

# CREATE A PROVIDER
createProvider /tmp/cookiestorage2 $PROVIDER_NAME

# CREATE A ENVIRONMENT
createEnvironment /tmp/cookiestorage2 $ENV_NAME

# CREATE TARGET TYPES
createTargetType /tmp/cookiestorage2 $TARGETTYPE_POOL_NAME
createTargetType /tmp/cookiestorage2 $TARGETTYPE_BACKEND_NAME

# CREATE A RULE TYPE
createRuleType /tmp/cookiestorage2 $RULETYPE_NAME

# CREATE A FARM (Environment and Provider are required)
createFarm /tmp/cookiestorage2 $FARM_NAME

# CREATE BALANCE POLICY TYPE
createBalancePolicyType /tmp/cookiestorage2 $BALANCEPOLICYTYPE_NAME

# CREATE BALANCE POLICY
createBalancePolicy /tmp/cookiestorage2 $BALANCEPOLICY_NAME

# LOGOUT NEW ADMIN ACCOUNT
logout /tmp/cookiestorage2

fi # END ADMIN CONTEXT
####

### USER CONTEXT

# LOGIN WITH USER ACCOUNT
if [ "x$USER_LOGIN" == "x" ]; then
  echo -n 'Enter a login with user role: '
  read USER_LOGIN
fi
login /tmp/cookiestorage3 '(user)' $USER_LOGIN

# CREATE A PROJECT
createProject /tmp/cookiestorage3 $PROJECT_NAME

# CREATE A VIRTUALHOST
createVirtualHost /tmp/cookiestorage3 $VIRTUALHOST_NAME

# CREATE A POOL
createBackendPool /tmp/cookiestorage3 $POOL_NAME

# CREATE BACKENDS (Pool is required and defined in parent property)
for PORT in $(seq $BACKEND_STARTPORT $BACKEND_ENDPORT); do
    createBackend /tmp/cookiestorage3 'http://'$BACKENDIP':'$PORT
done

# CREATE A RULE (Virtualhost and Pool is required)
createRule /tmp/cookiestorage3 $RULE_NAME

####

# REMOVE A RULE
removeRule /tmp/cookiestorage3 $RULE_NAME

# REMOVE A VIRTUALHOST
removeVirtualHost /tmp/cookiestorage3 $VIRTUALHOST_NAME

# REMOVE BACKENDS OF THE POOL
removeBackendsOfPool /tmp/cookiestorage3 $POOL_NAME

# REMOVE A POOL
removeBackendPool /tmp/cookiestorage3 $POOL_NAME

# REMOVE A PROJECT
removeProject /tmp/cookiestorage3 $PROJECT_NAME

# LOGOUT
logout /tmp/cookiestorage3
