# We start from latest Ubuntu LTS (18.04)
FROM ubuntu:bionic

# We install all the various tools needed to build the various artifacts
RUN apt-get update && apt-get upgrade -y && apt-get install -y \
    # System/dev utilities
    sudo nano vim curl wget apt-transport-https ca-certificates gnupg-agent software-properties-common \
    # Python for the various AWS CLI tools
    python-dev python-wheel python-pip \
    # JDK for the Java-based components
    default-jdk-headless \
    # Dependencies for the preceeding tools
    git groff \

    # Docker client for building images
    && curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add - \
    && add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" \
    && apt-get install -y docker-ce-cli

# We create the build user environment
RUN useradd -rm -d /home/builder -s /bin/bash -g root -G sudo -u 1000 builder
RUN echo "builder ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers
USER builder

# We install AWS SAM CLI to build and deploy the various lambdas
RUN pip install --user awscli aws-sam-cli

# Install npm, node
RUN cd /home/builder/.local/lib && wget https://nodejs.org/dist/v10.15.1/node-v10.15.1-linux-x64.tar.xz -O node.tar.xz \
    && tar xvf node.tar.xz && rm node.tar.xz

# Make necessary symbolic links
RUN ln -s /home/builder/.local/lib/node-v10.15.1-linux-x64/bin/npm /home/builder/.local/bin/npm \
    && ln -s /home/builder/.local/lib/node-v10.15.1-linux-x64/bin/node /home/builder/.local//bin/node \
    && ln -s /home/builder/.local/lib/node-v10.15.1-linux-x64/bin/npx /home/builder/.local/bin/npx

# We set the PATH to include local binary folder for SAM
ENV PATH="/home/builder/src/scripts:/home/builder/.local/bin:${PATH}"

RUN make --version && docker --version && python --version && aws --version && sam --version && npm --version && node --version

# We will mount the mono repo here
VOLUME /home/builder/src

ADD scripts/docker/entrypoint /home/builder/entrypoint

# We start in the source directory
WORKDIR /home/builder/src

# We run the entry point script
ENTRYPOINT /home/builder/entrypoint
