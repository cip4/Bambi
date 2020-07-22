# CIP4 Bambi - JDF Device Simulator
[![License (CIP4 Software License)](https://img.shields.io/badge/license-CIP4%20Software%20License-blue)](https://github.com/cip4/xJdfLib/blob/master/LICENSE.md)   ![Bambi Snapshot](https://github.com/cip4/Bambi/workflows/Bambi%20Snapshot/badge.svg)

Bambi is a CIP4 Tool for the simulation of JDF Devices and JDF Controllers. Origianlly, it was designed in order to provide a test framework for the development of Management Information Systems. Using configuration files, a set of individual JDF Devices can be defined and simulated such as presses, post press devices etc.

Another use case of Bambi is the simulation of production processes within a printing house. When configuring the Bambi Devices with the original characteristics of production devices, the affects of scenarios within a printing house can be simulated. This is useful especially for highly standardized production lines.

Here is an online version of CIP4 Bambi: **https://bambi.cip4.org**

<br />

## Usage
### Job Submission
There are multiple ways to submit jobs to Bambi. Besides the standardized JMF approach, Bambi also provides multiple simplifed ways to submit jobs. In the following is a list of the job submission methods. Sample JDF Jobs can be found in the project's [sample-jdfs](./sample-jdfs) folder:

#### Using the Command Line:
```bash
$ curl -X POST -H "Content-Type: application/vnd.cip4-jdf+xml" -d @sim003-sample.jdf http://localhost:8080/SimWorker/jmf/sim003
```

#### Alces
CIP4 Alces can be used to submit jobs to Bambi using JMF SubmitQueueEntry messages. Here is the link to Alces: https://github.com/cip4/Alces

<br />

## Issue Tracking
Don't write issues, provide Pull-Requests!

<br />

## Bambi Docker
Bambi is also available as Docker image on GitHub Packages: https://github.com/orgs/cip4/packages. Here is the CLI commands to launch the image locally on your machine:

```bash
$ docker pull docker.pkg.github.com/cip4/bambi/bambi:latest
$ docker run -p 8080:8080 docker.pkg.github.com/cip4/bambi/bambi:latest
```

<br />

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
