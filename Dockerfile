FROM eclipse-temurin:17-jdk-jammy

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

ARG ANDROID_SDK_ROOT=/opt/android-sdk
ARG ANDROID_API_LEVEL=36
ARG ANDROID_BUILD_TOOLS=36.0.0
ARG ANDROID_CMDLINE_TOOLS_VERSION=11076708

ENV DEBIAN_FRONTEND=noninteractive \
    ANDROID_HOME=${ANDROID_SDK_ROOT} \
    ANDROID_SDK_ROOT=${ANDROID_SDK_ROOT} \
    GRADLE_USER_HOME=/opt/gradle-home \
    PATH=${PATH}:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        bash \
        ca-certificates \
        curl \
        git \
        openssh-client \
        unzip \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools ${GRADLE_USER_HOME} /root/.android \
    && touch /root/.android/repositories.cfg \
    && curl -fsSL -o /tmp/android-commandlinetools.zip https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_CMDLINE_TOOLS_VERSION}_latest.zip \
    && unzip -q /tmp/android-commandlinetools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools \
    && mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest \
    && rm -f /tmp/android-commandlinetools.zip

RUN set +o pipefail \
    && yes | sdkmanager --sdk_root=${ANDROID_SDK_ROOT} --licenses > /dev/null \
    && set -o pipefail \
    && sdkmanager --sdk_root=${ANDROID_SDK_ROOT} \
        "build-tools;${ANDROID_BUILD_TOOLS}" \
        "platform-tools" \
        "platforms;android-${ANDROID_API_LEVEL}"

WORKDIR /workspace
