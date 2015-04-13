#!/bin/bash

SCRIPT_DIR=`dirname $(realpath $0)`

# Name of the repo where to upload the resources (artifact, template, ...)
REPO_NAME=av-sched-stores

# Default values for EnvName
declare -A DEFAULT_PARAMS
DEFAULT_PARAMS[EnvName]=dev

# Configuration files located on AWS S3 buckets
# The name of the configuration to be used is defined by the argument '--aws-cfg <ConfigName>'
# AWS_S3_BUCKETS[ConfigName]="s3://...""
declare -A AWS_S3_BUCKETS
AWS_S3_BUCKETS[preprod]="s3://av-preprod-secrets/files/config/av-sched-stores.cfg"

# List of the network parameters to extract
NETWORK_PARAMS_NAMES=( PrivateSubnetA PrivateSubnetB PrivateSubnetC PrivateSecurityGroup )

# List of stack parameters (used in the help message)
STACK_PARAMS_NAMES=( Version EnvName DBInstanceClass DBStorageType DBAllocatedStorage DBIops DBUser DBPwd )

# List of secured stack parameters (used to hide the value of the parameters)
SECURED_STACK_PARAMS_NAMES=( DBPwd )


# ----------------------------------------------------------------------------------------
# 
# Initialize the variables used by the script.
# At least the variables ENV_NAME VERSION NETWORK_STACK STACK_NAME need to be set.
# 
# ----------------------------------------------------------------------------------------

function init_variables {
    ENV_NAME=$(get_parameter EnvName)
    NETWORK_STACK=$ENV_NAME-net
    STACK_NAME=$ENV_NAME-av-sched-stores
    
    # Get the parameter 'Version'. If the parameter is not defined then a value will be generated.
    VERSION=$(get_parameter Version $ENV_NAME-`date -u +%Y%m%d%H%M%S`)
}

# ----------------------------------------------------------------------------------------
# 
# Build all required artifacts for your application and upload them on S3.
# This function is only called if the command line parameter '-build' is used.
# 
# ----------------------------------------------------------------------------------------

function build {

    print "Upload resources to S3" 6

    print "Uploading cloudformation" 2
    upload_template $SCRIPT_DIR/cloudformation/av-sched-stores.template
}


## ==================================================================================
##
##                                       MAIN
##
## ==================================================================================

aws s3 cp s3://av-repo/tools/deploy-tools.sh $SCRIPT_DIR
source $SCRIPT_DIR/deploy-tools.sh
