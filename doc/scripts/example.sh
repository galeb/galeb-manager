#!/bin/bash


PROTOCOL="http"
SERVER="localhost:8000"
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

login() {
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

  TOKEN="$(curl -k -v ${PROTOCOL}://${LOGIN}:${PASSWORD}@${SERVER}/token | jq -r .token)"
  echo
}

logout() {
  local TOKEN=$1

  curl -k -XPOST -H"x-auth-token: $TOKEN" $PROTOCOL://$SERVER/logout
  echo
}

getId() {
  local TOKEN=$1
  local TYPE=$2
  local NAME=$3

  # TYPE: target, rule, virtualhost, farm, environment, targettype, ruletype,
  #       project, account, team, etc.
  curl -k -s -XGET -H"x-auth-token: $TOKEN" \
    $PROTOCOL://$SERVER/$TYPE/search/findByName?name=$NAME | \
  jq ._embedded.$TYPE[0].id
}

createTeam() {
  local TOKEN=$1
  local NAME=$2

  curl -k -v -XPOST -H"x-auth-token: $TOKEN" -H $HEADER \
    -d '{ "name":"'$NAME'" }' $PROTOCOL://$SERVER/team
  echo
}

createAccount() {
  local TOKEN=$1
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
       -H"x-auth-token: $TOKEN" $PROTOCOL://$SERVER/account
  echo
}

createProvider() {
  local TOKEN=$1
  local NAME=$2
  local DRIVER_NAME='GalebV3'

  curl -k -v -XPOST -H $HEADER \
       -d '{
              "name": "'$NAME'",
              "driver": "'$DRIVER_NAME'"
          }' \
       -H"x-auth-token: $TOKEN" $PROTOCOL://$SERVER/provider
  echo
}

createEnvironment() {
  local TOKEN=$1
  local NAME=$2

  curl -k -v -XPOST -H $HEADER \
       -d '{ "name":"'$NAME'" }' \
       -H"x-auth-token: $TOKEN" $PROTOCOL://$SERVER/environment
  echo
}

createTargetType () {
  local TOKEN=$1
  local NAME=$2

  curl -k -v -XPOST -H $HEADER \
       -d '{ "name":"'$NAME'" }' \
       -H"x-auth-token: $TOKEN" $PROTOCOL://$SERVER/targettype
  echo
}

createRuleType() {
  local TOKEN=$1
  local NAME=$2

  curl -k -v -XPOST -H $HEADER \
       -d '{ "name": "'$NAME'" }' \
       -H"x-auth-token: $TOKEN" $PROTOCOL://$SERVER/ruletype
  echo
}

createFarm () {
  local TOKEN=$1
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
       -H"x-auth-token: $TOKEN" $PROTOCOL://$SERVER/farm
  echo
}

createBalancePolicyType() {
  local TOKEN=$1
  local NAME=$2

  curl -k -v -XPOST -H $HEADER \
       -d '{ "name": "'$NAME'" }' \
       -H"x-auth-token: $TOKEN" $PROTOCOL://$SERVER/balancepolicytype
  echo
}

createBalancePolicy() {
  local TOKEN=$1
  local NAME=$2
  local BALANCEPOLICYTYPE_ID="$(getId $COOKIE balancepolicytype $BALANCEPOLICYTYPE_NAME)"

  curl -k -v -XPOST -H $HEADER \
       -d '{
              "name": "'$NAME'",
              "balancePolicyType": "'$PROTOCOL'://'$SERVER'/balancepolicytype/'$BALANCEPOLICYTYPE_ID'"
          }' \
       -H"x-auth-token: $TOKEN" $PROTOCOL://$SERVER/balancepolicy
  echo
}

createProject() {
  local TOKEN=$1
  local NAME=$2
  local TEAM_ID="$(getId $COOKIE team $TEAM_NAME)"

  curl -k -v -XPOST -H $HEADER \
       -d '{
              "name":"'$NAME'",
              "teams": [ "'$PROTOCOL://$SERVER'/team/'$TEAM_ID'" ]
          }' \
       -H"x-auth-token: $TOKEN" $PROTOCOL://$SERVER/project
  echo
}

createVirtualHost() {
  local TOKEN=$1
  local NAME=$2
  local PROJECT_ID="$(getId $COOKIE project $PROJECT_NAME)"
  local ENV_ID="$(getId $COOKIE environment $ENV_NAME)"

  curl -k -v -XPOST -H $HEADER \
       -d '{
              "name": "'$VIRTUALHOST_NAME'",
              "environment": "'$PROTOCOL'://'$SERVER'/environment/'$ENV_ID'",
              "project": "'$PROTOCOL'://'$SERVER'/project/'$PROJECT_ID'"
          }' \
       -H"x-auth-token: $TOKEN" $PROTOCOL://$SERVER/virtualhost
  echo
}

createBackendPool() {
  local TOKEN=$1
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
       -H"x-auth-token: $TOKEN" $PROTOCOL://$SERVER/target
  echo
}

createBackend() {
  local TOKEN=$1
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
              "parents": [ "'$PROTOCOL'://'$SERVER'/target/'$POOL_ID'" ]
          }' \
       -H"x-auth-token: $TOKEN" $PROTOCOL://$SERVER/target
  echo
}

createRule() {
  local TOKEN=$1
  local NAME=$2
  local RULETYPE_URLPATH_ID="$(getId $COOKIE ruletype $RULETYPE_NAME)"
  local VIRTUALHOST_ID=$(getId $COOKIE virtualhost $VIRTUALHOST_NAME)
  local POOL_ID="$(getId $COOKIE target $POOL_NAME)"

  curl -k -v -XPOST -H $HEADER \
       -d '{
              "name": "'$NAME'",
              "ruleType": "'$PROTOCOL'://'$SERVER'/ruletype/'$RULETYPE_URLPATH_ID'",
              "virtualhosts": [ "'$PROTOCOL'://'$SERVER'/virtualhost/'$VIRTUALHOST_ID'" ],
              "target": "'$PROTOCOL'://'$SERVER'/target/'$POOL_ID'",
              "default": true,
              "order": 0,
              "properties": {
                  "match": "/"
              }
          }' \
       -H"x-auth-token: $TOKEN" $PROTOCOL://$SERVER/rule
  echo
}

removeRule() {
  local TOKEN=$1
  local NAME=$2
  local ID="$(getId $COOKIE rule $NAME)"

  curl -k -v -XDELETE -H $HEADER -H"x-auth-token: $TOKEN" $PROTOCOL://$SERVER/rule/$ID
  echo
}

removeVirtualHost() {
  local TOKEN=$1
  local NAME=$2
  local ID="$(getId $COOKIE virtualhost $NAME)"

  curl -k -v -XDELETE -H $HEADER -H"x-auth-token: $TOKEN" $PROTOCOL://$SERVER/virtualhost/$ID
  echo
}

getNumBackendsByPool() {
  local TOKEN=$1
  local POOL_NAME=$2
  local POOL_ID="$(getId $COOKIE target $POOL_NAME)"
  curl -k -s -XGET -H"x-auth-token: $TOKEN" \
    $PROTOCOL'://'$SERVER'/target/'$POOL_ID'/children?size=99999' | \
  jq .page.totalElements
}

getFirstTargetIdByPool() {
  local TOKEN=$1
  local POOL_NAME=$2
  local POOL_ID="$(getId $COOKIE target $POOL_NAME)"

  curl -k -s -XGET -H"x-auth-token: $TOKEN" \
    $PROTOCOL'://'$SERVER'/target/'$POOL_ID'/children?size=99999' | \
  jq ._embedded.target[0].id
}

removeBackendsOfPool() {
  local TOKEN=$1
  local POOL_NAME=$2

  NUM_BACKENDS_BY_POOL="$(getNumBackendsByPool $COOKIE $POOL_NAME)"

  if [ -n "$NUM_BACKENDS_BY_POOL" -a "x$NUM_BACKENDS_BY_POOL" != "x0" ]; then
      while [ "$(getNumBackendsByPool $COOKIE $POOL_NAME)" -gt 0 ];do
          TARGET_ID="$(getFirstTargetIdByPool $COOKIE $POOL_NAME)"
          curl -k -v -XDELETE -H $HEADER -H"x-auth-token: $TOKEN" $PROTOCOL://$SERVER/target/$TARGET_ID
          echo
      done
  fi
}

removeBackendPool() {
  local TOKEN=$1
  local NAME=$2
  local ID="$(getId $COOKIE target $NAME)"

  curl -k -v -XDELETE -H $HEADER -H"x-auth-token: $TOKEN" $PROTOCOL://$SERVER/target/$ID
  echo
}

removeProject() {
  local TOKEN=$1
  local NAME=$2
  local ID="$(getId $COOKIE project $NAME)"

  curl -k -v -XDELETE -H $HEADER -H"x-auth-token: $TOKEN" $PROTOCOL://$SERVER/project/$ID
  echo
}

###
if [ "x$1" == "xadmin" ] ; then

## ADMIN CONTEXT

# LOGIN WITH INTERNAL ADMIN ACCOUNT
login '(internal admin)' admin password

# CREATE A TEAM
createTeam $TOKEN $ADMIN_TEAM_NAME

# CREATE A ACCOUNT WITH ADMIN ROLE
echo -n 'Enter a login with admin role (it will be created, if it does not exist): '
read ADMIN_LOGIN
createAccount $TOKEN \
              '"ROLE_USER","ROLE_ADMIN"' $ADMIN_TEAM_NAME $ADMIN_LOGIN

ADMIN_ACCOUNT_ID="$(getId $TOKEN account $ADMIN_LOGIN)"

# LOGOUT INTERNAL ADMIN
logout $TOKEN


# LOGIN WITH A NEW ADMIN ACCOUNT
login '(new admin)' $ADMIN_LOGIN

# CREATE A TEAM
createTeam $TOKEN $TEAM_NAME

if [ "x$USER_LOGIN" != "x$ADMIN_LOGIN" ]; then
  # CREATE A ACCOUNT WITH USER ROLE ONLY
  echo -n 'Enter a login with user role (it will be created, if it does not exist): '
  read USER_LOGIN
  createAccount $TOKEN '"ROLE_USER"' $TEAM_NAME $USER_LOGIN
  USER_ACCOUNT_ID="$(getId $TOKEN account $USER_LOGIN)"
else
  # MODIFY ADMIN ACCOUNT TEAMS
  ADMIN_TEAM_ID="$(getId $TOKEN team $ADMIN_TEAM_NAME)"
  TEAM_ID="$(getId $TOKEN team $TEAM_NAME)"
  curl -k -v -XPATCH -H $HEADER \
       -d '{ "teams": [ "'$PROTOCOL'://'$SERVER'/team/'$ADMIN_TEAM_ID'",
                        "'$PROTOCOL'://'$SERVER'/team/'$TEAM_ID'" ] }' \
       -b $TOKEN $PROTOCOL://$SERVER/account/$ADMIN_ACCOUNT_ID
fi

# CREATE A PROVIDER
createProvider $TOKEN $PROVIDER_NAME

# CREATE A ENVIRONMENT
createEnvironment $TOKEN $ENV_NAME

# CREATE TARGET TYPES
createTargetType $TOKEN $TARGETTYPE_POOL_NAME
createTargetType $TOKEN $TARGETTYPE_BACKEND_NAME

# CREATE A RULE TYPE
createRuleType $TOKEN $RULETYPE_NAME

# CREATE A FARM (Environment and Provider are required)
createFarm $TOKEN $FARM_NAME

# CREATE BALANCE POLICY TYPE
createBalancePolicyType $TOKEN $BALANCEPOLICYTYPE_NAME

# CREATE BALANCE POLICY
createBalancePolicy $TOKEN $BALANCEPOLICY_NAME

# LOGOUT NEW ADMIN ACCOUNT
logout $TOKEN

fi # END ADMIN CONTEXT
####

### USER CONTEXT

# LOGIN WITH USER ACCOUNT
if [ "x$USER_LOGIN" == "x" ]; then
  echo -n 'Enter a login with user role: '
  read USER_LOGIN
fi
login '(user)' $USER_LOGIN

# CREATE A PROJECT
createProject $TOKEN $PROJECT_NAME

# CREATE A VIRTUALHOST
createVirtualHost $TOKEN $VIRTUALHOST_NAME

# CREATE A POOL
createBackendPool $TOKEN $POOL_NAME

# CREATE BACKENDS (Pool is required and defined in parent property)
for PORT in $(seq $BACKEND_STARTPORT $BACKEND_ENDPORT); do
    createBackend $TOKEN 'http://'$BACKENDIP':'$PORT
done

# CREATE A RULE (Virtualhost and Pool is required)
createRule $TOKEN $RULE_NAME

####

# Wait
for x in $(seq 1 5);do echo -n .;sleep 1;done;echo

# REMOVE A RULE
removeRule $TOKEN $RULE_NAME

# REMOVE A VIRTUALHOST
removeVirtualHost $TOKEN $VIRTUALHOST_NAME

# REMOVE BACKENDS OF THE POOL
removeBackendsOfPool $TOKEN $POOL_NAME

# REMOVE A POOL
removeBackendPool $TOKEN $POOL_NAME

# REMOVE A PROJECT
removeProject $TOKEN $PROJECT_NAME

# LOGOUT
logout $TOKEN
