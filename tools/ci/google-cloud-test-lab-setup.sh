#!/bin/bash

# Sets up gcloud.  Marks a flag to remember whether the SDK has been installed previously
# and will bypass downloading if the flag exists. Note that if you change the version,
# you should delete the flag and the installation so that the script will download
# the new version.  The script expects several parameters in this order:
# 1. gcloud installation directory — the directory must exist and should be empty (if the flag doesn't exist).  e.g. ~/gcloud
# 2. gcloud version, e.g. 143.0.1
# 3. flag path, e.g. ~/flags/gcloud-setup
# 4. gcloud service key — base64 encoded gcloud service key
# 5. gcloud project name — name of the gcloud project.

# usage:
# google-cloud-test-lab-setup.sh ~/gcloud 143.0.1 ~/flags/gcloud-setup <service_key_base64> <project>

set +e

gcloud_installation_dir="$1"
gcloud_version="$2"
gcloud_flag_file="$3"
gcloud_service_key_base64="$4"
gcloud_project="$5"

if ! test -f ${gcloud_flag_file}; then
  echo "Install gcloud SDK ${gcloud_version} to ${gcloud_installation_dir}"

  (cd ~ && curl "https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-${gcloud_version}-linux-x86_64.tar.gz" | tar zx && mv google-cloud-sdk ${gcloud_installation_dir})

  ${gcloud_installation_dir}/bin/gcloud --quiet components install beta

  touch ${gcloud_flag_file}
fi

gcloud_service_account_key_path=~/gcloud_service_account_key_temp.json
echo $gcloud_service_key_base64 | base64 --decode > ${gcloud_service_account_key_path}
${gcloud_installation_dir}/bin/gcloud --quiet auth activate-service-account --key-file ${gcloud_service_account_key_path}
rm ${gcloud_service_account_key_path}

${gcloud_installation_dir}/bin/gcloud --quiet config set project $gcloud_project
