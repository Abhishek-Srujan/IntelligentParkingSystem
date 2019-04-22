

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Servlet implementation class update
 */
@WebServlet("/update")
public class update extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String clientIP = "192.168.0.191:8080";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public update() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String clients = "http://"+clientIP+"/api/clients";
		String id = "null";
		id = request.getParameter("id");
		String value = "null";
		value = request.getParameter("value");
		String clientID="null";
		clientID = request.getParameter("clientID");
		String vehicleID = "null";
		vehicleID =request.getParameter("vehicleID");
		if(id.equals("null")&value.equals("null")){
			String status = parkingSpotJSON("add",clientID,"null");
		}else if(id.equals("bill")&value.equals("null")){
			response.getWriter().append("1000");
		}
		else{
		//System.out.println(id + "-" + value);
			String payload=null;
			String status = null;
			if(id.equals("32801")){
				if(value.equals("occupied")){
					//System.out.println(clients+"/"+clientID+"/3345/0/5703");
					payload = "{\"id\":5703,\"value\":100}";
					status = sendPUT(clients+"/"+clientID+"/3345/0/5703", payload);
					status = parkingSpotJSON("occupied", clientID, "");
				}
				if(value.equals("free")){
					payload = "{\"id\":5703,\"value\":-100}";
					status = sendPUT(clients+"/"+clientID+"/3345/0/5703", payload);
					status = parkingSpotJSON("free", clientID, "1");
				}
				if(value.equals("reserved")){
					System.out.println("reservationnn....");
					status = parkingSpotJSON("reserved", clientID, vehicleID);
				}
			}
		}
		response.getWriter().append("<br>Served at: ").append(request.getContextPath());
	}
	
	/*
	parkingSpotJSON("add", id, " ") //when a new parking spot registers
	parkingSpotJSON("reserved", PSid, vid) //when a parking spot becomes reserved, id & vid are strings
	parkingSpotJSON("occupied", id, "") //when a parking spot becomes occupied, id is string
	bill = parkingSpotJSON("free", id, fee) //when a parking spot becomes free, id, fee & bill are strings

	For Visualization this JSON file can be used
	*/

	@SuppressWarnings("unchecked")
	private String parkingSpotJSON(String command, String id, String message) {
		JSONParser parser = new JSONParser();
		Date d = new Date();
		String bill = "";

		try {
			Object obj = parser.parse(new FileReader("C:/IoT/parkingSpot.json"));
			JSONObject parkingSpotJSON = (JSONObject) obj;
			switch (command) {
			case "add":
				if(parkingSpotJSON.get(id) == null){	
					JSONObject tempAdd = new JSONObject();
					tempAdd.put("status", "free");
					tempAdd.put("vehicle", "null");
					JSONArray vehicleList = new JSONArray();
					tempAdd.put("vehicleList", vehicleList);
					parkingSpotJSON.put(id, tempAdd);
					bill = "SUCCESS";
					break;
				}
				else{
					bill = "Fail";
				}
			case "reserved":
				JSONObject tempReserved = (JSONObject) parkingSpotJSON.get(id);
				tempReserved.put("status", "reserved");
				tempReserved.put("vehicle", message);
				vehicleJSON("add", message, d, id);
				break;
			case "occupied":
				JSONObject tempOccupied = (JSONObject) parkingSpotJSON.get(id);
				tempOccupied.put("status", "occupied");				
				String vid = (String) tempOccupied.get("vehicle");
				vehicleJSON("modify", vid, d, "");
				break;
			case "status":
				JSONObject tempStatus = (JSONObject) parkingSpotJSON.get(id);
				String vehicleStatus = (String) tempStatus.get("vehicle");
				if(vehicleStatus == "null"){
					bill = "0";
				}
				else{
					bill = vehicleJSON("status", vehicleStatus, d, message);
				}
				
				break;
			case "free":
				JSONObject tempFree = (JSONObject) parkingSpotJSON.get(id);
				String vidFree = (String) tempFree.get("vehicle");
				bill = vehicleJSON("remove", vidFree, d, message);
				tempFree.put("status", "free");
				tempFree.put("vehicle", "null");
				JSONArray tempArray = (JSONArray) tempFree.get("vehicleList");
				JSONObject addVehicle = new JSONObject();
				try{
					Object obj2 = parser.parse(new FileReader("C:/IoT/vehicle.json"));
					JSONObject vehicleJSON = (JSONObject) obj2;
					JSONObject tempVehicle = (JSONObject) vehicleJSON.get(vidFree);
					addVehicle.put("vid", vidFree);
					addVehicle.put("reservedTime", tempVehicle.get("reservedTime"));
					addVehicle.put("occupiedTime", tempVehicle.get("occupiedTime"));
					addVehicle.put("freeTime", tempVehicle.get("freeTime"));
					addVehicle.put("bill", tempVehicle.get("bill"));
					tempArray.add(addVehicle);
					tempFree.put("vehicleList", tempArray);
				} catch (Exception e){
					e.printStackTrace();
				}
				break;
			}
			System.out.print(parkingSpotJSON);
			System.out.print("\n");
			try (FileWriter file = new FileWriter("C:/IoT/parkingSpot.json")) {
				file.write(parkingSpotJSON.toJSONString());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO Auto-generated method stub
		return bill;
	}

	private String vehicleJSON(String command, String VID, Date time, String message) {
		JSONParser parser = new JSONParser();
		long billingTime = 0;
		int bill = 0;

		try {
			Object obj = parser.parse(new FileReader("C:/IoT/vehicle.json"));
			JSONObject vehicleJSON = (JSONObject) obj;
			switch (command) {
			case "add":
				JSONObject tempAdd = new JSONObject();
				
				tempAdd.put("reservedTime", time.toString());
				tempAdd.put("reservedTimeMS", time.getTime());
				tempAdd.put("occupiedTime", null);
				tempAdd.put("occupiedTimeMS", 0);
				tempAdd.put("freeTime", null);
				tempAdd.put("freeTimeMS", 0);
				tempAdd.put("bill", 0);
				tempAdd.put("psID", message);
				vehicleJSON.put(VID, tempAdd);
				//parkingSpotJSON("status", message, "reserved");
				//parkingSpotJSON("vid", message, VID);
				break;
			case "modify":
				JSONObject tempModify = (JSONObject) vehicleJSON.get(VID);
				System.out.println(MicroTimestamp.INSTANCE.get());
				tempModify.put("occupiedTime", time.toString());
				tempModify.put("occupiedTimeMS", time.getTime());
				break;
			case "remove":
				JSONObject tempRemove = (JSONObject) vehicleJSON.get(VID);
				System.out.println(MicroTimestamp.INSTANCE.get());
				tempRemove.put("freeTime", time.toString());
				tempRemove.put("freeTimeMS", time.getTime());
				billingTime = time.getTime() - (long) tempRemove.get("occupiedTimeMS");
				billingTime = (int) billingTime / 1000;
				bill =  (int) ((billingTime * Integer.parseInt(message)));
				tempRemove.put("bill", bill);
				break;
			case "status":
				JSONObject tempStatus = (JSONObject) vehicleJSON.get(VID);
				if((long) tempStatus.get("occupiedTimeMS") == 0){
					bill = 0;
				}
				else{
					billingTime = time.getTime() - (long) tempStatus.get("occupiedTimeMS");
					billingTime = (int) billingTime / 1000;
					bill = (int) billingTime * Integer.parseInt(message);
					tempStatus.put("bill", bill);					
				}
				break;
			}
			System.out.print(vehicleJSON);
			System.out.print("\n");
			try (FileWriter file = new FileWriter("C:/IoT/vehicle.json")) {
				file.write(vehicleJSON.toJSONString());
			}
			if (command == "remove") {
				JSONObject temp = (JSONObject) vehicleJSON.get(VID);
			}
			return String.valueOf(bill);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO Auto-generated method stub
		return null;
	}
	private static final String USER_AGENT = "Mozilla/5.0";
	private static String sendPUT(String URL, String payload) throws IOException {
	        URL obj = new URL(URL);
	        
	        //String payload = "	{\"on\":true, \"sat\":254, \"bri\":0,\"hue\":10000}";
	        Random random = new Random();
	        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	        con.setRequestMethod("PUT");
	        con.setDoOutput(true);
	        con.setRequestProperty("Content-Type", "application/json");
	        con.setRequestProperty("Accept", "application/json");
	        con.setRequestProperty("User-Agent", USER_AGENT);
	        
	        OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
	        osw.write(String.format(payload, random.nextInt(30), random.nextInt(20)));
	        osw.flush();
	        osw.close();
	        System.err.println(con.getResponseCode());
	    
	 
	        int responseCode = con.getResponseCode();
	        System.out.println("GET Response Code :: " + responseCode);
	        if (responseCode == HttpURLConnection.HTTP_OK) { // success
	            BufferedReader in = new BufferedReader(new InputStreamReader(
	                    con.getInputStream()));
	            String inputLine;
	            StringBuffer response = new StringBuffer();
	 
	            while ((inputLine = in.readLine()) != null) {
	                response.append(inputLine);
	            }
	            in.close();
	 
	            // print result
	            System.out.println(response.toString());
	            return response.toString();
	        } else {
	            System.out.println("PUT request not worked");
	            return null;
	        }
	        
	 
	    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
