FROM quay.io/pypa/manylinux_2_28_x86_64:2024.07.23-1

# Adapted from sameli/manylinux_2_28_x86_64_cuda_12.3 to use CUDA 11.8
# ------------
# Install cuda and other dependencies
# ------------

ARG VER="11-8"
ARG ARCH="x86_64"

RUN dnf config-manager --add-repo https://developer.download.nvidia.com/compute/cuda/repos/rhel8/x86_64/cuda-rhel8.repo
RUN dnf -y remove gcc-toolset-12*
RUN dnf -y install cuda-compiler-${VER}.${ARCH} \
  cuda-libraries-${VER}.${ARCH} \
  cuda-libraries-devel-${VER}.${ARCH} \
  git \
  java-1.8.0-openjdk-devel \
  maven \
  cmake \
  gcc-toolset-11 \
  && dnf clean all \
  && rm -rf /var/cache/dnf/*
RUN echo "/usr/local/cuda/lib64" >> /etc/ld.so.conf.d/999_nvidia_cuda.conf

# -------------------------
# Set environment variables
# -------------------------
ENV PATH="/usr/local/cuda/bin:${PATH}"
ENV LD_LIBRARY_PATH="/usr/local/cuda/lib64:${LD_LIBRARY_PATH}"
ENV CUDA_HOME=/usr/local/cuda
ENV CUDA_ROOT=/usr/local/cuda
ENV CUDA_PATH=/usr/local/cuda
ENV CUDADIR=/usr/local/cuda

## sbt compile dependencies

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
ENTRYPOINT ["bash","docker/sbt_publish.sh", "-Dis_gpu=true"]

