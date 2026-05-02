# IBM MQ Remote Connection Setup (Client Mode)

This guide walks through the steps required to configure an IBM MQ Queue
Manager for remote access using client mode.

## Prerequisites

- IBM MQ installed
- Access to mqm user
- Linux/Unix environment

## 1. Create OS User and Group

```bash
sudo groupadd appgrp
sudo useradd -m -s /bin/bash appusr
sudo passwd appusr
sudo usermod -aG appgrp appusr
groups appusr
```

## 2. Create and Start Queue Manager

```bash
crtmqm APPQM
strmqm APPQM
```

## 3. Grant Permissions

```bash
setmqaut -m APPQM -t qmgr -g appgrp +connect +inq
setmqaut -m APPQM -n APP.** -t queue -g appgrp +put +get +browse +inq
```

## 4. Define Queues and Security Configuration

Run:

```bash
runmqsc APPQM
```

### Queue Definitions

    DEFINE QLOCAL('APP.IN.QUEUE.1') REPLACE DEFPSIST(YES) MAXDEPTH(50000)
    DEFINE QLOCAL('APP.OUT.QUEUE.1') REPLACE DEFPSIST(YES) MAXDEPTH(50000)
    DEFINE QLOCAL('APP.RES.QUEUE.1') REPLACE DEFPSIST(YES) MAXDEPTH(50000)

### Authentication Configuration

    DEFINE AUTHINFO('APP.AUTHINFO') AUTHTYPE(IDPWOS) CHCKCLNT(REQDADM) CHCKLOCL(OPTIONAL) ADOPTCTX(YES) REPLACE
    ALTER QMGR CONNAUTH('APP.AUTHINFO')
    REFRESH SECURITY(*) TYPE(CONNAUTH)

### Server Connection Channel

    DEFINE CHANNEL('APP.SVRCONN') CHLTYPE(SVRCONN) MCAUSER('appusr') REPLACE

### Channel Authentication Rules

    SET CHLAUTH('*') TYPE(ADDRESSMAP) ADDRESS('*') USERSRC(NOACCESS) ACTION(REPLACE)
    SET CHLAUTH('APP.SVRCONN') TYPE(ADDRESSMAP) ADDRESS('*') USERSRC(CHANNEL) CHCKCLNT(REQUIRED) ACTION(REPLACE)

### Authorization Records

    SET AUTHREC PRINCIPAL('appusr') OBJTYPE(QMGR) AUTHADD(CONNECT,INQ)
    SET AUTHREC PROFILE('APP.**') PRINCIPAL('appusr') OBJTYPE(QUEUE) AUTHADD(BROWSE,GET,INQ,PUT)

### SSL Configuration

    ALTER QMGR SSLKEYR('')
    ALTER QMGR CERTLABL('')
    ALTER QMGR SSLFIPS(NO)
    REFRESH SECURITY(*) TYPE(SSL)

## Notes

- Ensure PRINCIPAL match OS user.
- Ensure listener is running and port is open.

## Application Setup

- Queue Manager: APPQM
- Channel: APP.SVRCONN
- Configure host and port in your client app.
- Also make sure the application queue names will always start with APP.
