#!/bin/bash

# Executes tests on a specified module and extracts the results into the 
# standard Android test directories.  This uses the default Firebase Test Lab
# bucket with a custom results directory, in order to be able to copy the
# results back programmatically.
#
# This script only runs a test on a single device configuration at a time, so it
# does not support matrices.  In order to create a matrix, invoke this script
# multiple times with additional arguments.
#
# When files are copied off of gcloud, they are given unique names to ensure
# that multiple invocations of this script will not cause naming collisions.
# 
# This script expects the following parameters in order:

# 1. module tested
# 2. Default Firebase test lab bucket.  You'll need to invoke the gcloud command manually and find the bucket name to pass for this parameter.
# 3. Firebase Test Lab device as a quoted string.  E.g. "model=NexusLowRes,version=25,orientation=portrait"
# 4. Additional Firebase Test Lab arguments, as a quoted string.

# Example usage:
# ./tools/ci/firebase-test-lab-test-module.sh spackleLib test-lab-5fjjivmbih0ck-i7fd9i4fw50ym "model=NexusLowRes,version=25,orientation=portrait"

set -e

module="$1"
default_bucket="$2"
device="$3"
gcloud_args="$4"

uuid=`uuidgen`

# If a test case fails, exit code 10 will be returned. The script should continue if that occurs.
set +e
gcloud firebase test android run --results-dir=${uuid} tools/gcloud.yml:${module} --device ${device} ${gcloud_args}
gcloud_exit_code=$?
set -e

if [[ $gcloud_exit_code != 0 && $gcloud_exit_code != 10 ]]; then exit $gcloud_exit_code; fi


# Copy test results.
echo "Fetching test results"
mkdir -p ${module}/build/outputs/androidTest-results/connected/
gsutil -m cp -r "gs://${default_bucket}/${uuid}/**test_result_*.xml" "${module}/build/outputs/androidTest-results/connected/${uuid}-test-result.xml"

# Copy coverage.
echo "Fetching coverage"
mkdir -p ${module}/build/outputs/code-coverage/connected/
gsutil -m cp -r "gs://${default_bucket}/${uuid}/**/artifacts/coverage.ec" "${module}/build/outputs/code-coverage/connected/${uuid}-coverage.ec"
