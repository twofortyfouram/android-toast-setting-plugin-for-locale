#!/bin/bash

# Fetches test results from the google cloud, by parsing the log output of the 
# Firebase Test lab console messages.  This is brittle and may break in the
# future.  This could be improved by using a custom bucket, however custom
# buckets are not available on free Firebase plans.
#
# This script expects the following parameters in order:

# 1. module tested
# 2. logs directory.  Logs are expected to follow the naming pattern ${logs_dir}/${module}-log.txt
# 3. list of device types, e.g. Nexus4-19-en-portrait or NexusLowRes-25-en-portrait

set +e

module="$1"
logs_dir="$2"
shift 2
device_types=("$@")


# Slurp in and extract the gcloud bucket from the log file.
gcloud_path=`cat ${logs_dir}/${module}-log.txt | awk '/Raw results will be stored in your GCS bucket at/ { gsub(/([\[\]]|\.$)/, "", $11); print $11 }' | sed 's/https:\/\/console\.developers\.google\.com\/storage\/browser\/\(.*\)/\1/'`

# Copy test results.
mkdir -p ${module}/build/outputs/androidTest-results/connected/
gsutil -m cp -r "gs://${gcloud_path}**/test_result_*.xml" "${module}/build/outputs/androidTest-results/connected/"

# Copy coverage.
mkdir -p ${module}/build/outputs/code-coverage/connected/

for device in ${device_types[@]}; do
    echo "Fetching coverage for ${device}"
    gsutil -m cp -r "gs://${gcloud_path}${device}/artifacts/coverage.ec" "${module}/build/outputs/code-coverage/connected/${module}-${device}-coverage.ec"
done
