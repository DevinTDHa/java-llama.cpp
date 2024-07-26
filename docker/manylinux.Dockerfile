FROM quay.io/pypa/manylinux_2_28_x86_64:2024.07.23-1

# Install necessary dependencies
RUN yum install -y \
  git \
  java-1.8.0-openjdk-devel \
  maven \
  cmake \
  gcc \
  gcc-c++ \
  && yum clean all

# Setup sbt
RUN curl -L https://github.com/sbt/sbt/releases/download/v1.9.9/sbt-1.9.9.tgz -o /sbt.tgz && \
  tar -xvf /sbt.tgz -C / && \
  ln -s /sbt/bin/sbt /usr/bin/sbt

# Set environment variables for Java and SBT
ENV JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk/
ENV PATH=$JAVA_HOME/bin:$PATH

# Copy the project files into the Docker container
COPY . /workspace
WORKDIR /workspace

# Set the entrypoint to a shell
ENTRYPOINT ["bash","docker/sbt_publish.sh"]

