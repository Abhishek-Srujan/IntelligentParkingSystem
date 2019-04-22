In this project, the folder "IoT" is implementation of our Webserver and the folder "Leshan-Client-Example" and  "Leshan-Standalone" represent the LWM2M Client and Server respectively. The folder "Python files" contains the bash script "AvahiClient" and necessary files to be run the Leshan client on the Raspberry Pi. The bash script "avahiServer.sh" is used to run the "Standalone" server. Rest of the folders remain unchanged which can be downloaded from URL: https://github.com/eclipse/leshan  

->Leshan-Standalone:
This folder implements the Leshan Server, which communicates with the constrained devices using CoAP. 

This server has maven dependency over sub projects such as Leshan Core. The default source of Leshan downloaded from Git-hub Link: (https://github.com/eclipse/leshan)  is modified as follows:

We have modified the source code in the path "leshan-standalone\src\main\java\org\eclipse\leshan\standalone\servlet\EventServlet.java" to forward new registration of clients and changes notified for observed variable to our designed webserver implemented in the folder "IoT" which is identified through an IP. 

Also the file (leshan-standalone\src\main\resources\webapp\js\app.js) is updated to allow access to the REST API implemented by the server thereby enabling to view the list of registered clients through /api/clients etc...


-> Leshan-Client-Example:
This folder implement the Leshan Client. 

We have modified the code in the path : "IoT Source Code\leshan-client-example\src\main\java\org\eclipse\leshan\client\example" where we have implemented the read, write and execute functions of all the objects as per the requirement and integrated the python implementation of SenseHat to Java. The code is also updated in order to enable Firmware updation over the air.

-> oma-objects-spec.json 
This json object needs to stored in the path "leshan-master\leshan-core\src\main\resources\oma-objects-spec.json" which implements the client objects according to the specification provided through Project Proposal.

-> IoT
This is the Web Server that uses the Leshan Server’s REST API. The main purpose of this server is build frontend of the Parking Lot. The server is built over Apache Tomcat. The servlets used and its uses as follows:

(/IoT/vehicle.jsp) -> Requests the user to enter the Vehicle ID.
(/IoT/addVehicle?vehicleID=IoT)-> Asks for list of available spots for the given vehicle ID
(/IoT/addVehicle?vehicleID=IoT&parkingSlot=Parking-Spot-13) -> Reserves “Parking-Spot-13” to “IoT”
(/IoT/visualizer.jsp) -> Login for the visualizer system. Use (Username: mananger and Password: parkinglot)
(/IoT/visualiser) -> The Visualizer is redirected to this page on successful login.

Java Classes:
(update.java)-> Is Used to update the datastore and manage billing. This will be called from Leshan Server on a client registration or during update on observed variable (Parking Spot State)
