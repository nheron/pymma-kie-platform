# Kie Platform Overview


![alt text][image-1]
## Business Central
This is the authoring tool handling the complete life cycle of a drools project.
We only added two functionalities
- We added some rest services to interact with our administration console to the existing ones,
- the login end user management to use the data stored in the Mongo Database.

## Kie-server
We created a kie-server extension that 
- exposes a rest interface as described in the previous part,
- implements callback listeners to trace all details happening during runtime execution,
- Post the execution trace as a json file and send the result to a Apache Kafka topic called « logging »,
- It connects to the Mongo Database to update its configuration : creating, updating or deleting a container.

## Administration Console
It allows to : 
- define a drools project : a git repository containing a maven Kjar and a branch,
- define the main class used as top class for the endpoint, the jbpm process to start if needed and the name of the endpoint,
- define the list of kie-server to deploy the project on,
- deploy a project to the targeted kie-server defined before,
- update the content of guided rule template or decision tables by exporting and importing the content in Microsoft Excel format,
- get the content of the logging of rule execution by transaction id (which is a header variable of the rest request).
The administration console uses the Mongo database to store all informations concerning the configuration, tracing and content of the platform.

## Indexer
It takes the content of the Apache Kafka topic « logging ». 
The indexer will stored the message content in the mongo database. In the administration console, the user can then visualize it. 

## Reverse Proxy
It is the entrance door for all clients to interact with the runtime. 
It listens to an Apache Kafka topic to create or update a new route. When starting, it reads its configuration from the mongo database.
When defining a project in the administration console, the reverse proxy received a message to update its configuration.
## Apache Kafka
We are using this technical component as a message broker. It is used to transmit rule execution tracing from a kie-servcer to the indexer.

## Mongo Database
It is a nosql database and contains all data manipulated by our kie platform.





[image-1]:	diag1.svg "Title"