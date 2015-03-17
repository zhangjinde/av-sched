#!/bin/bash

CURRENT_DIR=`dirname $(realpath $0)`

# Path and URL of av-repo
AV_REPO_PATH=s3://av-repo
AV_REPO_URL=https://s3-eu-west-1.amazonaws.com/av-repo

# Name of the repo where to upload the resources (artifact, template, ...)
REPO_NAME=av-sched

# Name of the cloudformation template
TEMPLATE_NAME=av-sched.template

# Default values for EnvName, BaseAMI
declare -A DEFAULT_PARAMS
DEFAULT_PARAMS[EnvName]=dev
DEFAULT_PARAMS[BaseAMI]=ami-cbe87bbc

# Configuration files located on AWS S3 buckets
# The name of the configuration to be used is defined by the argument '--aws-cfg <ConfigName>'
# AWS_S3_BUCKETS[ConfigName]="s3://...""
declare -A AWS_S3_BUCKETS
AWS_S3_BUCKETS[preprod]="s3://av-preprod-secrets/files/config/av-sched.cfg"

# List of the network parameters to extract
NETWORK_PARAMS_NAMES=( PrivateSubnetA PrivateSubnetB PrivateSubnetC PrivateSecurityGroup KeyPair EnvType )

# List of stack parameters (used in the help message)
STACK_PARAMS_NAMES=( Version EnvName BaseAMI InstanceType DBUser DBPwd AvSchedSecret HealthCheckGracePeriod )

# List of secured stack parameters (used to hide the value of the parameters)
SECURED_STACK_PARAMS_NAMES=( DBPwd AvSchedSecret )


# ----------------------------------------------------------------------------------------
# 
# Initialize the variables used by the script.
# At least the variables VERSION NETWORK_STACK STACK_NAME need to be set.
# 
# ----------------------------------------------------------------------------------------

function init_variables {
    ENV_NAME=$(get_parameter EnvName)
    NETWORK_STACK=$ENV_NAME-net
    STACK_NAME=$ENV_NAME-av-sched

    # Get the parameter 'Version'. If the parameter is not defined then a value will be generated.
    VERSION=$(get_parameter Version $ENV_NAME-`date -u +%Y%m%d%H%M%S`)
    # Set the parameter 'Version' to be sure the generated value is taken into account.
    set_parameter Version $VERSION
}

# ----------------------------------------------------------------------------------------
# 
# Build all required artifacts for your application and upload them on S3.
# 
# This function is only called if the command line parameter '-build' is used.
# 
# ----------------------------------------------------------------------------------------

function build {

    print "Build Artifact" 6

    initial_dir=`pwd`
    
    cd $CURRENT_DIR/..
    mvn clean package -q

    # Back to the initial directory
    cd $initial_dir

    # --------

    print "Upload resources to S3" 6

    aws s3 cp $CURRENT_DIR/cloudformation/$TEMPLATE_NAME $AV_REPO_PATH/deployments/$REPO_NAME/$VERSION/cloudformation/
    aws s3 cp $CURRENT_DIR/../target/av-sched-*-exec.jar $AV_REPO_PATH/apps/$REPO_NAME/$VERSION/artifacts/av-sched.jar
}

# ----------------------------------------------------------------------------------------
# 
# Function called just before to deploy a version of your application.
# This function is used to perform some checks ckecks (artifacts exists on S3, ...).
# 
# This function is only called if the command line parameter '-deploy' is used.
# 
# ----------------------------------------------------------------------------------------

function check_before_deploy {

    print "Check resources" 6

    # Check if the artifact exists for the specified version
    local artifact_exists=`aws s3 ls $AV_REPO_PATH/apps/$REPO_NAME/$VERSION/artifacts/av-sched.jar`
    if [ -z "$artifact_exists" ]
    then
        echo "No artifact found for the version: $VERSION"
        exit 1
    fi
}


## ==================================================================================
##
##                                       MAIN
##
## ==================================================================================

aws s3 cp $AV_REPO_PATH/tools/deploy-tools.sh $CURRENT_DIR
source $CURRENT_DIR/deploy-tools.sh
