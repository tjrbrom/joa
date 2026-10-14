#!/bin/sh
# cd /home/opesys/core/
############登录地址&登录用户名和密码########################
ADDIP=$1
USERNAME=$2
PASSWORD=$3
##############设置manager password##########################
Manager_password=$4
###########CORE_JOA和认证json##########################
CORE_JOA=$5
CORE_JOA_authz_config=$6
###########################################################
ACCESS_TOKEN=$(curl --data-urlencode "client_id=admin-cli" --data-urlencode "username=$USERNAME" --data-urlencode "password=$PASSWORD" --data-urlencode "grant_type=password" "https://$ADDIP/core-sso/realms/master/protocol/openid-connect/token" -k)
ACCESS_TOKEN=$(echo $ACCESS_TOKEN | cut -d'"' -f 4)

##########search client: CORE_JOA#################
CORE_JOA_USERS=$(curl   -H "Content-Type:application/json" -H "Authorization: Bearer $ACCESS_TOKEN" "https://$ADDIP/core-sso/admin/realms/master/clients?first=0&max=11&clientId=CORE_JOA&search=true" -k)
CORE_JOA_EXISTED=$(echo $CORE_JOA_USERS | grep '\"clientId\"\:\"CORE_JOA\"')

##########search user: manager#################
MANAGER_USERS=$(curl   -H "Content-Type:application/json" -H "Authorization: Bearer $ACCESS_TOKEN" "https://$ADDIP/core-sso/admin/realms/master/ui-ext/brute-force-user?briefRepresentation=true&first=0&max=11&q=&search=manager" -k)

MANAGER_EXISTED=$(echo $MANAGER_USERS | grep '\"username\"\:\"manager\"')

if [ -z $CORE_JOA_EXISTED ];then
    curl -X POST  -H "Content-Type:application/json"  --data-binary @$CORE_JOA -H "Authorization: Bearer $ACCESS_TOKEN" https://$ADDIP/core-sso/admin/realms/master/clients -k

    ############add system monitor resource json##################
    ############create user monitor resource account##################
    CORE_JOA_USERS=$(curl   -H "Content-Type:application/json" -H "Authorization: Bearer $ACCESS_TOKEN" "https://$ADDIP/core-sso/admin/realms/master/clients?first=0&max=11&clientId=CORE_JOA&search=true" -k)
    CORE_JOA_ID=$(echo $CORE_JOA_USERS | cut -d'"' -f 4)

    # curl -X POST http://example.com/api/resource -H "Content-Type: application/json" -d '{"key1":"value1", "key2":"value2"}'
    curl -X POST  -H "Content-Type:application/json" -d '{ "name": "开户系统","composite": false,"clientRole": true,"containerId": "{}"}'  -H "Authorization: Bearer $ACCESS_TOKEN" https://$ADDIP/core-sso/admin/realms/master/clients/$CORE_JOA_ID/roles -k

    curl -X POST  -H "Content-Type:application/json" -d '{ "name": "故障诊断","composite": false,"clientRole": true,"containerId": "{}"}'  -H "Authorization: Bearer $ACCESS_TOKEN" https://$ADDIP/core-sso/admin/realms/master/clients/$CORE_JOA_ID/roles -k

    curl -X POST  -H "Content-Type:application/json" -d '{ "name": "资源采集","composite": false,"clientRole": true,"containerId": "{}"}'  -H "Authorization: Bearer $ACCESS_TOKEN" https://$ADDIP/core-sso/admin/realms/master/clients/$CORE_JOA_ID/roles -k

    ROLE_USERS=$(curl   -H "Content-Type:application/json" -H "Authorization: Bearer $ACCESS_TOKEN" https://$ADDIP/core-sso/admin/realms/master/clients/$CORE_JOA_ID/roles -k)
    echo $ROLE_USERS
    #query开户系统id
    Account_Opening_System_id=$(curl   -H "Content-Type:application/json" -H "Authorization: Bearer $ACCESS_TOKEN" https://$ADDIP/core-sso/admin/realms/master/clients/$CORE_JOA_ID/roles?search=%E5%BC%80%E6%88%B7%E7%B3%BB%E7%BB%9F -k | cut -d'"' -f 4 )
    #query 故障诊断id
    Fault_Diagnosis_id=$(curl   -H "Content-Type:application/json" -H "Authorization: Bearer $ACCESS_TOKEN" https://$ADDIP/core-sso/admin/realms/master/clients/$CORE_JOA_ID/roles?search=%E6%95%85%E9%9A%9C%E8%AF%8A%E6%96%AD -k | cut -d'"' -f 4 )
    #query 资源采集id
    Resource_Collection_id=$( curl   -H "Content-Type:application/json" -H "Authorization: Bearer $ACCESS_TOKEN" https://$ADDIP/core-sso/admin/realms/master/clients/$CORE_JOA_ID/roles?search=%E8%B5%84%E6%BA%90%E9%87%87%E9%9B%86 -k | cut -d'"' -f 4 )

    curl -X POST  -H "Content-Type:application/json"  --data-binary @$CORE_JOA_authz_config -H "Authorization: Bearer $ACCESS_TOKEN" https://$ADDIP/core-sso/admin/realms/master/clients/$CORE_JOA_ID/authz/resource-server/import -k


    if [ -z $MANAGER_EXISTED ];then
        ##########add user manager#################

        curl -X POST  -H "Content-Type:application/json"  -d '{"username":"manager","email":"","firstName":"","lastName":"","requiredActions":[],"emailVerified":false,"groups":[],"attributes":{},"enabled":true}' -H "Authorization: Bearer $ACCESS_TOKEN" https://$ADDIP/core-sso/admin/realms/master/users -k
    fi
        
    MANAGER_USERS=$(curl   -H "Content-Type:application/json" -H "Authorization: Bearer $ACCESS_TOKEN" "https://$ADDIP/core-sso/admin/realms/master/ui-ext/brute-force-user?briefRepresentation=true&first=0&max=11&q=&search=manager" -k)
    MANAGER_ID=$(echo $MANAGER_USERS | cut -d'"' -f 4)

    ####set manager password####
    curl -X PUT  -H "Content-Type:application/json"  -d "{\"temporary\":false,\"type\":\"password\",\"value\":\"$Manager_password\"}"  -H "Authorization: Bearer $ACCESS_TOKEN" https://$ADDIP/core-sso/admin/realms/master/users/$MANAGER_ID/reset-password -k

    ####role mapping to manager ####
    curl -X POST  -H "Content-Type:application/json" -H "Authorization: Bearer $ACCESS_TOKEN" -d "[{\"id\":\"$Account_Opening_System_id\",\"name\":\"开户系统\"}]" https://$ADDIP/core-sso/admin/realms/master/users/$MANAGER_ID/role-mappings/clients/$CORE_JOA_ID -k

    curl -X POST  -H "Content-Type:application/json" -H "Authorization: Bearer $ACCESS_TOKEN" -d "[{\"id\":\"$Fault_Diagnosis_id\",\"name\":\"故障诊断\"}]" https://$ADDIP/core-sso/admin/realms/master/users/$MANAGER_ID/role-mappings/clients/$CORE_JOA_ID -k

    curl -X POST  -H "Content-Type:application/json" -H "Authorization: Bearer $ACCESS_TOKEN" -d "[{\"id\":\"$Resource_Collection_id\",\"name\":\"资源采集\"}]" https://$ADDIP/core-sso/admin/realms/master/users/$MANAGER_ID/role-mappings/clients/$CORE_JOA_ID -k
else
    CORE_JOA_USERS=$(curl   -H "Content-Type:application/json" -H "Authorization: Bearer $ACCESS_TOKEN" "https://$ADDIP/core-sso/admin/realms/master/clients?first=0&max=11&clientId=CORE_JOA&search=true" -k)
    CORE_JOA_ID=$(echo $CORE_JOA_USERS | cut -d'"' -f 4)

    if [ -z $MANAGER_EXISTED ];then
        ##########add user manager#################
        curl -X POST  -H "Content-Type:application/json"  -d '{"username":"manager","email":"","firstName":"","lastName":"","requiredActions":[],"emailVerified":false,"groups":[],"attributes":{},"enabled":true}' -H "Authorization: Bearer $ACCESS_TOKEN" https://$ADDIP/core-sso/admin/realms/master/users -k
        MANAGER_USERS=$(curl   -H "Content-Type:application/json" -H "Authorization: Bearer $ACCESS_TOKEN" "https://$ADDIP/core-sso/admin/realms/master/ui-ext/brute-force-user?briefRepresentation=true&first=0&max=11&q=&search=manager" -k)
        MANAGER_ID=$(echo $MANAGER_USERS | cut -d'"' -f 4)

        ####set manager password####
        curl -X PUT  -H "Content-Type:application/json"  -d "{\"temporary\":false,\"type\":\"password\",\"value\":\"$Manager_password\"}"  -H "Authorization: Bearer $ACCESS_TOKEN" https://$ADDIP/core-sso/admin/realms/master/users/$MANAGER_ID/reset-password -k

        ####get CORE_JOA roles####
        CLIENT_ROLES=$(curl   -H "Content-Type:application/json" -H "Authorization: Bearer $ACCESS_TOKEN" "https://core-sso:8443/core-sso/admin/realms/master/clients/$CORE_JOA_ID/roles" -k)
        CLIENT_ROLES_FILTERS=$(echo $CLIENT_ROLES | awk -F ',' '{for(i=1; i<=NF; i++) if($i ~ /"name":/) print substr($i, 8)}' | grep -v '^\"uma_protection\"$' | tr -d '"')
        CLIENT_ROLES_ARRAY=($CLIENT_ROLES_FILTERS)
        
        ####role mapping to manager ####
        for item in "${CLIENT_ROLES_ARRAY[@]}"; do            
            urlencoded_item=$(echo -e "$item" | iconv -f UTF-8 -t UTF-8 | tr -d '\n' | od -A n -t x1 | sed 's/ /%/g') 
            id=$( curl -H "Content-Type:application/json" -H "Authorization: Bearer $ACCESS_TOKEN" https://$ADDIP/core-sso/admin/realms/master/clients/$CORE_JOA_ID/roles?search=$urlencoded_item -k | cut -d'"' -f 4 )
            curl -X POST  -H "Content-Type:application/json" -H "Authorization: Bearer $ACCESS_TOKEN" -d "[{\"id\":\"$id\",\"name\":\"$item\"}]" https://$ADDIP/core-sso/admin/realms/master/users/$MANAGER_ID/role-mappings/clients/$CORE_JOA_ID -k
        done
    else
        ####get manager id####
        MANAGER_USERS=$(curl   -H "Content-Type:application/json" -H "Authorization: Bearer $ACCESS_TOKEN" "https://$ADDIP/core-sso/admin/realms/master/ui-ext/brute-force-user?briefRepresentation=true&first=0&max=11&q=&search=manager" -k)
        MANAGER_ID=$(echo $MANAGER_USERS | cut -d'"' -f 4)
        ####set manager password####
        curl -X PUT  -H "Content-Type:application/json"  -d "{\"temporary\":false,\"type\":\"password\",\"value\":\"$Manager_password\"}"  -H "Authorization: Bearer $ACCESS_TOKEN" https://$ADDIP/core-sso/admin/realms/master/users/$MANAGER_ID/reset-password -k
    fi
fi