# Kie Platform extending standard drools Tooling
---- 
## Kie standard tools
The drools community is providing a certain numbers of tools :
1. An api provided as a Apache Maven dependency. This is the classical approach used since drools exists
2. An eclipse plugin to assist us in writing drools native language called « drl ». This plugin allows also to authors bpmn2 files for the jbpm library 
3. a web application called « Business Central » that allows to handle the complete lifecycle of a drools/jbpm project
4. A runtime server, called « lie-server » that has a number of extension for standard components of the kielbasa’s community : drools, jbpm,  optaplanner, etc.
To use the kie-server there are two possibilities :
1. use it as a standalone server
2. link it to a business central and deploy components from the user interface
In case 2, the connection only handles a set of kie-servers, this can be use in development mode and In case 1, you have to call the rest interfaces to start using components.
The drools community is the the kie=knowledge is everything and has its own we site : [http://kie.org/][1]
And by visiting it, you can see that there fours parts :
- Kogito : it is called the next generation platform and targets the micro-profile approach by allowing to embed in your applications a cloud-native approach components that uses the 
	- Quarkus : [https://quarkus.io/][2],
	- GrallVM virtual machine [https://www.graalvm.org/][3],
	- DMN notation [https://www.omg.org/dmn/][4]to model the business rules (it an OMG standard for modeling and running rules that is supported by most rule providers on the market),
	- and the DMN runtime with drools which is the reference implementation, it as a technology compatibility Kit [https://github.com/dmn-tck/tck][5]. You can see here the test results [https://dmn-tck.github.io/tck/index.html][6]and all supported and supporting tools.
- Optaplanner : it is a constraint solver that offers an Api to use it. It uses drools as one possibility to implement business constraints.
- Jbpm : bpmn2 implementation that allows to run Business processes.
- Drools : rule engine that has a forward-chaining and backward-chaining inference algorithm to implement in an efficient way the business rules we want it to implement.
Our Kie Platform is focusing exclusively on drools for the moment.
## Apache Maven
Starting with version 6.x of drools, Apache Maven is the only possiblility to compile and build a runtime jar for drools project (the same applies to jbpm, Optaplanner  and Kogito).
The drools compiler is provided as an Apache Maven plugin.
A drools project is now an Apache Maven module and is adding a new packaging  type: « Kjar ». This means a drools project can now be added to any other project as an Apache Maven dependency.
The usage of standard Continuous Integration (CI) tools allows to build and deploy java applications using drools Apache Maven module/artefact : 
- git serveur to store the source code (Gitlab, Github, etc..)
- jenkins to build the drools component
- Nexus to store the constructed Kjar to make it available to other projects
## Business Central
Business Central is a web application that handles the complete lifecycle of a drools project (a maven module).
- Authoring different rule artefact types : native drools rule, guided rules, guided rules template, Decision tables, etc..
- multi-user management,
- handles the history of all modifications using git behind,
- allows to define git hooks to push every modification to an outside git server, in the near future, we shall provide a git Gerrit server ([https://en.wikipedia.org/wiki/Gerrit\_(software)][7] ) that would contain all rule modules as a copy of all modification done on Business central [https://github.com/pymma/pymma-kie-platform/issues/149][8]
- exposes each module the git repository using git/http network protocol,
- compiles and builds rule artefacts using Apache Maven behind,
- stores all rule Apache Maven artefacts and all dependencies in its own Apache Maven repository,  
- exposes the Apache Maven repository behind http as a normal Apache Maven repository.

Business Central is a complete CI tools by itself and we shall use it for that. 
We have added some rest interfaces services to our Kie Plalform Business Central version to facilitate its integration with our platform/
## Execution Server
 As already explained, Apache Maven is used to build a Kjar file that contains all artefacts of a drools project. 
We can integrate the Kjar in an application that will use it using the standard API of drools.
The other way is to propose a runtime that can dynamically build and expose a Kjar. This is done out of the box by the KieServer proposed by the community as a standard Execution Server.
The rest KieServer extension proposed by the community has the same interface as the API. You have to insert object by object, ask for fireallrules and all is then transmitted to the KieServer in JSON format. On the other side, all is unmarshall and transformed in java instances. 
If you have an instance A related to B and you transmit A and the B. 
The sequence is 
1. Insert A, 
2. Insert B that is an attribute of A , 
3. execute fireAllrules.
The JSON message will contain something like this : 

```json	
	{ "commands":[
	       {
	          "insert":{
	             "out-identifier":"CLASSA",
	             "return-object":"true",
	             "object": {
	              "CLASSB":{
					"name" : "heron"
	
	              } 
	           },
	       "insert":{
	             "out-identifier":"CLASSB",
	             "return-object":"true",
	             "object": {
					"name" : "heron"
	
	              }
	           },
	
	       {
	          "fire-all-rules":""
	       } 
	     ]
	  }
```

On the server side, 
1. instance A will be created with its attribute B  and inserted to the working memory. This new A instance will be created with a B instance that is not inserted. 
2. Instance B will be created and inserted in the working memory.  
3. fireallrules command will be executed
You can see here that when un-marshaling on the other side,  there are going to be two instances of CLASSB. 
If some rules are using the relation between A and B, here the Object CLASSA has a relation with CLASSB but not for drools as another instance of CLASSB will be inserted which is not the instances linked to the object CLASSA.
## Service Approach for execution server
The standard rest interface obliges the client to insert one by one the java instances to insert into the drools runtime session.
To avoid this, another approach is : 
1. use a service approach with java class as the top transmitted object
2. insert all connected instance by reflection following all get/set object and Lists
3. start a jbpm rule flow if needed
4. we shall define an end point like follows : 
	1. per project and branch
	2. a process to start (if needed)
	3. the top class to expose.
In our Kie Platform our execution server exposes endpoint like this.
## Missing features
### logging rule execution
The API proposed by drools allows the add callbacks to see what is happening in the  engine during its execution.
For many business areas, traceability is key feature that is mandatory.  Our platform is offering that functionality : traceability and storing of all execution request. Over the user interface, it is possible to get details of all rule executed,  fact inserted/updated/retracted and rule flow executions. 
### Logging deployment history
In a production system, each time a deployment is done, informations about should be stored : date, time and git commit ID. It is then possible to create a branch with git from that commit and deploy that precise version. Within the logging feature, the Kie platform shall memorize the commit id of each Apache Maven artefact used to produce the logging.
[errer]()






[1]:	http://kie.org/
[2]:	https://quarkus.io/
[3]:	https://www.graalvm.org/
[4]:	https://www.omg.org/dmn/
[5]:	https://github.com/dmn-tck/tck
[6]:	https://dmn-tck.github.io/tck/index.html
[7]:	https://en.wikipedia.org/wiki/Gerrit_(software)
[8]:	https://github.com/pymma/pymma-kie-platform/issues/149
