# Release with `sbt` in a Docker Container

This README provides instructions on how to use the `manylinux.Dockerfile`
to run `sbt` (Simple Build Tool) and perform a release.

1. **Build the Docker Image**

   First, you need to build the Docker image from the `manylinux.Dockerfile`:

   ```bash
   docker build -t jslllama:latest -f manylinux.Dockerfile .
   ```

  The entrypoint is [sbt_publish.sh](./sbt_publish.sh). Which runs the compilation
  and release. The first argument are the flags to `sbt`. When no argument is
  given, release for cpu is assumed.

2. **Perform a Release**

   To perform a release, we need to run the docker container with sbt secrets
   and the corresponding gpg key:

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

   Or for M1:

   ```bash
   docker run jslllama:latest -Dis_m1=true
   ```

## Exporting the PGP secret and key

TODO: 

```bash
SECRET=$(gpg --armor --export-secret-keys $LONGID | base64 -w0)
```

## GPU

TODO

