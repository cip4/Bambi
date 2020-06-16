# CIP4 Bambi - JDF Device Simulator
[![License (CIP4 Software License)](https://img.shields.io/badge/license-CIP4%20Software%20License-blue)](https://github.com/cip4/xJdfLib/blob/master/LICENSE.md)   ![Bambi Snapshot](https://github.com/cip4/Bambi/workflows/Bambi%20Snapshot/badge.svg)

CIP4 Bambi Device Simulator



## Issue Tracking
Don't write issues, provide Pull-Requests!



## Bambi in Docker
Bambi is also available as Docker image on GitHub Packages: https://github.com/orgs/cip4/packages. Here is the CLI command to launch the image locally on your machine:

```bash
$ docker login https://docker.pkg.github.com -u [GITHUB USER]
$ docker pull docker.pkg.github.com/cip4/bambi/bambi:latest
$ docker run -p 8080:8080 docker.pkg.github.com/cip4/bambi/bambi:latest
```

**NOTE:** The password applies to the GITHUB_TOKEN and can be generated in 'Settings' -> 'Developer Settings' -> 'Personal access tokens' -> 'Generate New Token'. The required token's privilege is 'read:packages'.  



## Development Notes
### Release a new Version
Creation and publishing of a new version to GitHub Release.

```bash
$ git tag -a Bambi-[VERSION] -m "[TITLE]"
$ git push origin Bambi-[VERSION]
```

In case a build has been failed, a tag can be deleted using the following command:
```bash
$ git tag -d Bambi-[VERSION]
$ git push origin :refs/tags/Bambi-[VERSION]
```
