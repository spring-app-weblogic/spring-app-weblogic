# Oracle WebLogic Installation and Configuration Guide

## Overview

This document provides step-by-step instructions for installing and configuring Oracle WebLogic Server.

---

## Software Requirements

- Download the Oracle WebLogic Software:
  - File: `fmw_15.1.1.0.0_wls_generic.jar`
- Supported Java Versions:
  - Java 17
  - Java 21

> This setup uses **Java 21**

---

## WebLogic Architecture

WebLogic Server consists of the following components:

- Admin Server
- Managed Server(s)
- Node Manager

### Production Architecture

- 1 Admin Server
- Multiple Managed Servers
- 1 Node Manager per physical machine

### Architecture Diagram

```
                +----------------------+
                |     Admin Server     |
                |   (Admin Console)    |
                +----------+-----------+
                           |
        -----------------------------------------
        |                    |                  |
+---------------+   +---------------+   +---------------+
| ManagedServer1|   | ManagedServer2|   | ManagedServerN|
+---------------+   +---------------+   +---------------+
        |                    |                  |
        -----------------------------------------
                           |
                  +------------------+
                  |   Node Manager   |
                  +------------------+
```

---

## Key Concepts

- Each machine running WebLogic must have **one Node Manager**
- Node Manager is used to start Admin and Managed Servers
- Without Node Manager, servers must be started manually on each machine
- Admin Console (GUI) is provided by the Admin Server for:
  - Deployments
  - Database configurations
  - JMS configurations

---

## Installation Steps

1. Open Command Prompt as Administrator
2. Run:
   ```
   java -jar fmw_15.1.1.0.0_wls_generic.jar
   ```
3. This launches the Middleware Installer
4. Provide the `ORACLE_HOME` path
5. Click Install

---

## Launch Configuration Wizard

After installation:

Navigate to:

```
<INSTALL_PATH>\weblogic\oracle_common\common\bin
```

Run:

```
config.cmd
```

---

## Domain Creation and Configurations

### Domain Setup

- Create domain using Configuration Wizard
- Domain path must be **outside ORACLE_HOME**

### Extensions

- Select required extensions
- Example:
  - JMS Extension (for JMS applications)

### Admin User

- Create admin credentials
  - Example: `weblogic / PassWord@123`

### Mode and Security

- Select **Production Mode**
- Disable Secure Mode
- Use Non-SSL port

---

## Advanced Configuration

Select the following options:

- Administrator Server
- Node Manager
- Topology
- Deployments and Services
- File Store

---

## Administrator Server Configuration

- Name: AdminServer
- Listening Address:
  - localhost (local)
  - All Local Addresses (recommended)
- Port:
  - 7001 (Non-SSL for testing)
  - SSL port for production

---

## Node Manager Configuration

- Use custom path (not inside Oracle Home or Domain)
- Credentials:
  - `nodemgr / PassWord@123`

---

## Managed Server Configuration

- Provide server name
- Listening Address:
  - localhost OR All Local Addresses
- Port:
  - 7003 (Non-SSL for testing)
  - SSL port for production

---

## Cluster Configuration

- Ignore if not using clustering
- If using Load Balancer:
  - Configure cluster for routing requests

---

## Server Template

- Create template:
  - Name: `ServerTemplate_1`
  - Configure Non-SSL port

---

## Machine Configuration

- Create Machine
  - Communication: Plain
  - Address: localhost

- Assign:
  - Admin Server
  - Managed Servers

---

## Service Targeting

- Map services to Managed Servers
  - Example: JMS Service → Managed Server

---

## File Store Configuration

- Set Synchronous Write Policy:
  - Direct-Write

---

## Domain Creation Completion

- Click Create
- Admin Console URL:
  ```
  http://localhost:7001/console
  ```

---

## Starting Services

### Start Node Manager

Navigate to:

```
<DOMAIN_PATH>\bin
```

Run:

```
startNodeManager.cmd
```

---

### Start Admin Server

Navigate to:

```
<DOMAIN_PATH>\bin
```

Run:

```
startWebLogic.cmd
```

- Enter Admin credentials when prompted

---

## Accessing Admin Console

- Use WebLogic Remote Console (Version 3.0.3)
- Login with Admin credentials
- Connect to Admin Server

---

## Starting Managed Server

- Navigate to:
  - Monitoring Tree
  - Environment → Servers
- Start Managed Server

> Once started, it will be visible in Node Manager console

---

## Summary Diagram (Startup Flow)

```
User → Node Manager → Admin Server → Managed Servers
```

---

## Notes

- Always keep domain path separate from Oracle Home
- Use SSL in production environments
- Node Manager is essential for centralized control
