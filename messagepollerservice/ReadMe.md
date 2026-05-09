# Message Poller Service

- This application is deployed as war is used to pick a transaction from input queue, process it and place the acknowledment in the response queue.

- Here we are using a JTA Transaction Manager which is used to commit both IBM MQ and DB. We will deploy this application in Oracle Weblogic Server which acts as a Transaction Manager whereas IBM MQ and DB acts as Resource Manager.

- This application using Spring Message Listener for picking the message and processing it.

## Changes required to deploy as WAR

- We need to deploy the application as WAR file.

- Application should extend SpringBootServletInitializer, also embedded tomcat should be removed as dependency.

- Even though we are not deploying a web application we need to add spring-web as dependecy because it is a WAR file.

-
