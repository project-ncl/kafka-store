#!/bin/bash
#
# SPDX-FileCopyrightText: Copyright © 2019 Red Hat, Inc., and individual contributors as indicated by the @author tags.
# SPDX-License-Identifier: Apache-2.0
#


# TODO: once we move to external-secrets operator, we can in theory remove
#       that script

# NCL-7125: avoid using openshift secrets with app secrets
export SECRETS_PATH="/mnt/secrets"
export QUARKUS_DATASOURCE_JDBC_URL="$(cat ${SECRETS_PATH}/${KAFKA_STORE_SECRET_NAME}-${APP_ENV}/datasource.connection)"
export QUARKUS_DATASOURCE_USERNAME="$(cat ${SECRETS_PATH}/${KAFKA_STORE_SECRET_NAME}-${APP_ENV}/datasource.user)"
export QUARKUS_DATASOURCE_PASSWORD="$(cat ${SECRETS_PATH}/${KAFKA_STORE_SECRET_NAME}-${APP_ENV}/datasource.password)"

echo "Datasource url: ${QUARKUS_DATASOURCE_JDBC_URL}"

export KAFKA_JAAS_CONFIG="$(cat ${SECRETS_PATH}/${KAFKA_CLIENT_SECRET_NAME}-${APP_ENV}/kafka_jaas_conf)"

java \
    -Djava.util.logging.manager=org.jboss.logmanager.LogManager \
    -jar quarkus-run.jar
