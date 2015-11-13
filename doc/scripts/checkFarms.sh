#!/usr/bin/env bash

TOKEN=""
TIMEOUT=3 #seconds

checkRequisites() {
    if ! type jq > /dev/null 2>&1 || ! type curl > /dev/null 2>&1 ; then
        echo "ERROR: jq and curl are required."
        echo
        echo "- MacOS X       : brew install jq curl"
        echo "- Ubuntu        : apt-get install jq curl"
        echo "- Fedora/CentOS : yum install jq curl"
        exit 1
    fi
}

usage() {
    echo "Usages:"
    echo "checkFams.sh <galebManager_baseUrl> showEnv"
    echo "checkFams.sh <galebManager_baseUrl> checkAll"
    echo "checkFams.sh <galebManager_baseUrl> <environmentName> [UniversalHealthCheckPath] [customFarmEntry]"
    echo
    echo "Example: ./checkFarms.sh http://localhost checkAll"
    exit 0
}

checkRequisites
if [ $# -lt 2 ]; then
    usage
fi

if [ -n $4 ];then
    export FARM_ENTRY=$4
fi

export PROTOCOL="$(echo $1 | cut -d':' -f1)"
export SERVER="$(echo $1 | cut -d'/' -f3)"


loginAccount() {
    if [ -z $LOGIN ]; then
        echo -n "login: "; read LOGIN
    fi
    if [ -z $PASSWORD ]; then
        echo -n "password: "; read -s PASSWORD
    fi
    export TOKEN="$(curl -k -v ${PROTOCOL}://${LOGIN}:${PASSWORD}@${SERVER}/token 2> /dev/null | jq -r .token)"
    export IS_ADMIN="$(curl -k -v ${PROTOCOL}://${LOGIN}:${PASSWORD}@${SERVER}/token 2> /dev/null | jq -r .admin)"
    echo

    if [ "x${IS_ADMIN}" == "xtrue" ]; then
        echo "OK. I'm Admin. Continue..."
        echo
    else
        echo "ERROR: ADMIN access is necessary"
        exit 0
    fi
}

getId() {
  local TYPE=$1
  local NAME=$2

  curl -k -s -H"x-auth-token: $TOKEN" \
    ${PROTOCOL}'://'${SERVER}'/'${TYPE}'/search/findByName?name='${NAME}'&page=0&size=999999' | \
  jq ._embedded.${TYPE}[0].id
}

getFarmId() {
    local ENV_NAME=$1
    local ENV_ID="$(getId environment ${ENV_NAME})"

    curl -k -s -H"x-auth-token: $TOKEN" \
        ${PROTOCOL}'://'${SERVER}'/farm/search/findByEnvironment?environment='${ENV_ID}'&page=0&size=999999' | \
        jq ._embedded.farm[0].id
}

getHosts() {
    local FARM_ID=$1
     curl -k -s -H"x-auth-token: $TOKEN" \
        ${PROTOCOL}'://'${SERVER}'/virtualhost/search/findByFarmId?id='${FARM_ID}'&page=0&size=999999' | \
        jq -r ._embedded.virtualhost[].name
}

getRouterDomainEntry() {
    if [ -n ${FARM_ENTRY} ] && [ "x$FARM_ENTRY" != "x" ] ; then
        echo ${FARM_ENTRY}
    else
        local FARM_ID=$1
         curl -k -s -H"x-auth-token: $TOKEN" \
                ${PROTOCOL}'://'${SERVER}'/farm/'${FARM_ID}'?page=0&size=999999' | \
                jq -r .domain
    fi
}

showEnv() {
    curl -k -s -H"x-auth-token: $TOKEN" \
        ${PROTOCOL}'://'${SERVER}'/environment/?page=0&size=999999' | \
        jq -r ._embedded.environment[].name
    exit 0
}

showStatus() {
    local ENV_NAME=$1
    local FARM_ID="$(getFarmId ${ENV_NAME})"
    local FARM_ENTRY="$(getRouterDomainEntry ${FARM_ID})"
    local HC_PATH="/"
    if [ $# -eq 2 ]; then
        HC_PATH=$2
    fi
    echo

    for host in $(getHosts ${FARM_ID});do
        ACTION="curl --max-time ${TIMEOUT} -k -s -XGET -I -H\"Host: ${host}\" http://${FARM_ENTRY}${HC_PATH} 2>&1 | head -1 | cut -d' ' -f2-"
        ACTION_RUN="$(eval ${ACTION})"
        RESULT="${ACTION_RUN}"
        NO_COLOUR="\033[0m"
        COR="\033[0;31m"

        if echo ${RESULT} | grep '20\|30' > /dev/null 2>&1; then
            COR="\033[0;32m"
        fi
        if [ "x${RESULT}" == "x" ]; then
            COR="\033[0;31m"
            RESULT="-- Timeout --"
        fi
        echo -e ${host}${HC_PATH} " : " ${COR}${RESULT}${NO_COLOUR}
    done
}

loginAccount

if [ $# -eq 2 ] && [ "x$2" == "xshowEnv" ]; then
    showEnv
elif [ "x$2" == "xcheckAll" ]; then
        IFS=$'\n'
        for envirName in $(showEnv); do
            echo "--------------"
            echo ">> ${envirName}"
            echo "--------------"
            showStatus "${envirName}" $3 | column -t -s:
            echo
        done
else
    showStatus $2 $3 | column -t -s:
fi




