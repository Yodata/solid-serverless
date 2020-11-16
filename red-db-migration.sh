#!/bin/bash -e

mkdir -p /bhhs/src
mkdir -p /bhhs/data

apt-get update

apt-get install -y awscli

## Get backup from FTP
## Store in S3

apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg-agent \
    software-properties-common

curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -

add-apt-repository \
   "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
   $(lsb_release -cs) \
   stable"

apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io

aws s3 cp s3://bhhs-rolloff-data/BHHS_RED_Export_backup_2019_1024_AM.bak /bhhs/src/BHHS_RED_Export_backup_2019_1024_AM.bak

docker run -d --restart=always \
  -p 1433:1433 \
  --name sqlserver \
  -e ACCEPT_EULA=Y \
  -e SA_PASSWORD=bs*ifts00T \
  -v /bhhs/data:/var/opt/mssql/data \
  -v /bhhs/src:/bhhs \
  mcr.microsoft.com/mssql/server

```sql
USE [master]
RESTORE DATABASE [contacts] FROM  DISK = N'/bhhs/BHHS_RED_Export_backup_2019_1024_AM.bak' WITH  FILE = 1,  MOVE N'BHHS_RED_Export_Data' TO N'/var/opt/mssql/data/contacts_Data.mdf',  MOVE N'BHHS_RED_Export_log' TO N'/var/opt/mssql/data/contacts_Log.ldf',  NOUNLOAD,  STATS = 5
```

aws s3 cp /bhhs/src/BHHS_RED_Export_backup_2019_1024_AM.bak s3://hsf-bhhs-reflex-red-db/BHHS_RED_Export_backup_2019_1024_AM.bak
