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
- Install java and setup JAVA_HOME

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
- Node Manager is used to remotely start Admin and Managed Servers, without Node Manager we need manually start them using scripts.
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
- Domain path must be outside **ORACLE_HOME**

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

NOTE: The above Non-SSL configurations are only for Testing Purpose for Production use we need to use SSL.

---

### Advanced Configuration

Select the following options:

- Administrator Server
- Node Manager
- Topology
- Deployments and Services
- File Store

---

### Administrator Server Configuration

- Name: AdminServer
- Listening Address:
  - localhost (local)
  - All Local Addresses (recommended)
- Port:
  - 7001 (Non-SSL for testing)
  - SSL port for production

---

### Node Manager Configuration

- Use custom path (not inside Oracle Home or Domain)
- Credentials:
  - `nodemgr / PassWord@123`

---

### Managed Server Configuration

- Provide server name
- Listening Address:
  - localhost OR All Local Addresses
- Port:
  - 7003 (Non-SSL for testing)
  - SSL port for production

---

### Cluster Configuration

- Ignore if not using clustering
- If using Load Balancer:
  - Configure cluster for routing requests

---

### Server Template

- Create template:
  - Name: `ServerTemplate_1`
  - Configure Non-SSL port

---

### Machine Configuration

- Create Machine
  - Communication: Plain
  - Address: localhost

- Assign:
  - Admin Server
  - Managed Servers

---

### Service Targeting

- Map services to Managed Servers
  - Example: JMS Service → Managed Server

---

### File Store Configuration

- Set Synchronous Write Policy:
  - Direct-Write

---

### Domain Creation Completion

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

### Accessing Admin Console

- Use WebLogic Remote Console (Version 3.0.3)
- Login with Admin credentials
- Connect to Admin Server

---

### Starting Managed Server

- Navigate to:
  - Monitoring Tree
  - Environment → Servers
- Start Managed Server

> Once started, it will be visible in Node Manager console

---

### Summary Diagram (Startup Flow)

```
User → Node Manager → Admin Server → Managed Servers
```

---

### Notes

- Always keep domain path separate from Oracle Home
- Use SSL in production environments
- Node Manager is essential for centralized control

## Oracle Data Source Configuration

1. Login to the **WebLogic Remote Console** and connect to the **Admin Server**.

2. Navigate to: Edit Tree --> Services --> Data Sources

3. Click on **New** and provide the following configuration:

### Basic Configuration

- **Name:** `myapp-oracle-datasource`
- **JNDI Name:** `jdbc/myappDS`
- **Targets:** Managed Server
- **Data Source Type:** Generic Data Source

### Database Configuration

- **Database Type:** Oracle
- **Database Driver:** Oracle Driver Thin XA for Application Continuity
- **Database Name:** `<DB Name>`
- **Hostname:** `<DB IP>`
- **Port:** `<DB Listener Port>`

### Credentials

- **Username:** `<DB App Username>`
- **Password:** `<DB App Password>`

4. Click **Create** to complete the data source setup.

---

### Testing the Data Source

- Test the configuration after creation
- If successful, a **green check mark** will be displayed

---

### Connection Pool Configuration

Configure the following connection pool properties based on environment requirements (UAT / Production):

- Statement Cache Size
- Initial Capacity
- Maximum Capacity
- Minimum Capacity

> Adjust these values according to application load and performance requirements.

- Once all the changes are performed go to shopping cart icon and click on commit changes.

## IBM MQ Resource Adapter Configuration

- IBM MQ provides a Resource Adapter which can be downloaded from its official site.  
  Ensure that the adapter version matches the IBM MQ server version being used.

- After downloading (e.g., `9.4.5.0-IBM-MQ-Java-InstallRA.jar`), extract its contents.

- From the extracted files, locate and extract: `wmq.jakarta.jmsra.rar`

- Navigate to the following directory: wmq.jakarta.jmsra/META-INF

- Place the `weblogic-ra.xml` file (available in this repository) into this folder.

- Before placing the `weblogic-ra.xml` file, update its configuration with: IBM MQ Server details, Queue details, Channel details

- In `weblogic-ra.xml`, configure all queues used by the application.  
  Also, note down the JNDI names for each queue, as they will be used in the Spring application.

- For additional configuration parameters, refer to the `ra.xml` file present in the original JAR.

---

### Repackaging the Resource Adapter

- Navigate to the extracted `wmq.jakarta.jmsra` directory and execute:

```bash
jar -cvf wmq.jakarta.jmsra.rar *
```

This creates a new wmq.jakarta.jmsra.rar file for deployment in WebLogic.

### Deploying Resource Adapter in WebLogic

- Login to the WebLogic Remote Console and connect to the Admin Server.

- Navigate to: Edit Tree → Deployments → App Deployments

- Click New and provide the following details:
  Name: wmq.jakarta.jmsra
  Targets: Managed Server
  Source: Upload the generated wmq.jakarta.jmsra.rar file
- Keep other configurations as default and click Create.

### Activating Changes

- Click on the shopping cart icon and select Commit Changes.

- A confirmation alert will indicate successful deployment.

### Verifying Deployment

- Navigate to: Monitoring Tree → Deployments → Application Management

- Verify the application status: STATE_PREPARED → Successfully deployed

### Starting the Resource Adapter

- Start the application by selecting Servicing all requests.

- After successful startup, the status will change to: STATE_ACTIVE
