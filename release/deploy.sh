#!/bin/bash

####### Constants

VERSION=dev-`date +%Y-%m-%d-%s`
ENV_NAME=dev
NETWORK_STACK=$ENV_NAME-net
STACK_NAME=$ENV_NAME-av-sched
AV_SCHED_SECRET=changeme

####### Functions

function build_java {
    echo
    echo ">>"
    echo ">> Build java"
    echo ">>"
    echo

    echo "FIXME"
    mvn clean package -q
}

function upload_resources {
    echo
    echo ">>"
    echo ">> Upload resources to S3"
    echo ">>"
    echo

    aws s3 cp target/av-sched-*-exec.jar s3://av-repo/apps/av-sched/${VERSION}/artifacts/av-sched.jar
    aws s3 cp release/cloudformation/av-sched.template s3://av-repo/deployments/av-sched/${VERSION}/cloudformation/
}

function build_stack_params {

    echo "Build Stack Params"
    echo "Extract params from $NETWORK_STACK"

    extract=`noho stack extract_params \
            -stackName=$NETWORK_STACK \
            -resourceNames=PublicSubnetA,PublicSubnetB,PublicSubnetC,PublicSecurityGroup,PrivateSubnetA,PrivateSubnetB,PrivateSubnetC,PrivateSecurityGroup`
    stack_params=`echo $extract | noho stack append_params \
                 -parameters=EnvName=$ENV_NAME,KeyPair=dev,AppVersion=$VERSION,AvSchedSecret=$AV_SCHED_SECRET`
    echo "New paremeters are:"
    echo $stack_params | jq '.'
}

function create_or_update_stack {
    local stack_exists=`aws cloudformation list-stacks --stack-status-filter \
                        CREATE_COMPLETE CREATE_IN_PROGRESS UPDATE_IN_PROGRESS UPDATE_COMPLETE UPDATE_ROLLBACK_COMPLETE \
                        | grep "StackName" | grep $STACK_NAME`
    if [ -z "$stack_exists" ]
    then
        echo
        echo ">"
        echo "> Creating stack"
        echo ">"
        echo

        aws cloudformation create-stack \
            --template-url https://s3-eu-west-1.amazonaws.com/av-repo/deployments/av-sched/${VERSION}/cloudformation/av-sched.template \
            --stack-name $STACK_NAME \
            --parameters $stack_params \
            --stack-policy-url https://s3-eu-west-1.amazonaws.com/av-repo/stack-update-policies/deny_all.json \
            --capabilities CAPABILITY_IAM | jq '.'
    else
        echo
        echo ">"
        echo "> Updating stack"
        echo ">"
        echo

        aws cloudformation update-stack \
            --template-url https://s3-eu-west-1.amazonaws.com/av-repo/deployments/av-sched/${VERSION}/cloudformation/av-sched.template \
            --stack-name $STACK_NAME \
            --parameters $stack_params \
            --stack-policy-url https://s3-eu-west-1.amazonaws.com/av-repo/stack-update-policies/allow_all.json \
            --capabilities CAPABILITY_IAM  | jq '.'
    fi
}

####### Main

echo ENV_NAME=$ENV_NAME
echo NETWORK_STACK=$NETWORK_STACK
echo VERSION=$VERSION
echo STACK_NAME=$STACK_NAME


if [ $? -eq 0 ]
    then build_java
fi
if [ $? -eq 0 ]
    then upload_resources
fi
if [ $? -eq 0 ]
    then build_stack_params
fi
if [ $? -eq 0 ]
    then create_or_update_stack
fi
