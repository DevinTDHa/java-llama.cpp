# Release with `sbt` in a Docker Container

This README provides instructions on how to use the `manylinux.Dockerfile`
to run `sbt` and perform a release.

## 1. **Build the Docker Image**

First, you need to build the Docker image from the `manylinux.Dockerfile`:

```bash
docker build -t jslllama:latest -f manylinux.Dockerfile .
```

The entrypoint is [sbt_publish.sh](./sbt_publish.sh). Which runs the compilation
and release. The first argument are the flags to `sbt`. When no argument is
given, release for cpu is assumed.

## 2. **Perform a Release in Docker**

We need to prepare the four following environment variables 
(see [sbt-ci-release](https://github.com/sbt/sbt-ci-release?tab=readme-ov-file#secrets) for more details):

1. `PGP_SECRET`: The base64-encoded GPG secret key.
    - We can export the secret key with `gpg --export-secret-keys $LONG_ID | base64 -w0`, where `$LONG_ID` is the 
    - long id of the gpg key.
2. `PGP_PASSPHRASE`: The passphrase for the GPG key.
   - Passphrase you use to unlock your GPG key.
3. `SONATYPE_USERNAME`: The username/token for Sonatype.
4. `SONATYPE_PASSWORD`: The password/token for Sonatype.

To perform a release, we need to run the docker container with sbt secrets
and the corresponding pgp key:

```bash
docker run \
   -e PGP_PASSPHRASE="$PGP_PASSPHRASE" \
   -e PGP_SECRET="$PGP_SECRET" \
   -e SONATYPE_USERNAME="$SONATYPE_USERNAME" \
   -e SONATYPE_PASSWORD="$SONATYPE_PASSWORD" \
   jslllama:latest
```

This command runs `sbt release` in the Docker container, which performs a release of
your `sbt` project.

### GPU

For the GPU release, we need to use the `manylinux-gpu.Dockerfile`. TODO

### Other Platforms
For M1:

```bash
docker run $ENV_VARS jslllama:latest -Dis_m1=true
```

For AARCH64:

```bash
docker run $ENV_VARS jslllama:latest -Dis_aarch64=true
```

