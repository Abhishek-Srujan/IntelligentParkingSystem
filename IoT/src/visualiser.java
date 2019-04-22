

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Servlet implementation class visualiser
 */
@WebServlet("/visualiser")
public class visualiser extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String clientIP = "192.168.0.191:8080";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public visualiser() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/html");
		JSONArray statusParking = new JSONArray();
		JSONObject temp = new JSONObject();
		JSONParser parser = new JSONParser();
		statusParking=statusParkingSpot();
		//System.out.println(statusParking);
		response.getWriter().append("<html><body><center><br><br><br><br><br>");
		response.getWriter().append("<b><u>Welcome to Parking Spot Visuvalizer</b></u> <br><br>");
		for(int i=0;i<statusParking.size();i++){
			try {
				temp = (JSONObject)parser.parse(statusParking.get(i).toString());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			response.getWriter().append(temp.get("ID").toString()+"        ").append(temp.get("value").toString());
			if(!((temp.get("value").toString()).equals("free"))){
				response.getWriter().append(" by Vehicle ID:<b>"+temp.get("vehicle").toString()+"</b><br>");
				response.getWriter().append("Current Cost:"+Float.parseFloat(getBill(temp.get("ID").toString(),"1"))/100+" EUR");
			}
			else{
				
				response.getWriter().append("<br>");
			}
			
			response.getWriter().append("<br><b><u>History of "+temp.get("ID").toString()+"</u></b><br><table  border=\"1\"><tr><th>S.No</th><th>Vehicle ID</th><th> Reservation Time</th><th>Occupied Time</th><th>Occupied Till</th><th>Cost</th></tr> ");
			response.getWriter().append(decodeListVehicles(temp.get("ID").toString()));
			response.getWriter().append("</table><br><br>");
		}
		response.getWriter().append("</center></body></html>");
		
	}

	private String getBill(String id, String fee) {
		JSONParser parser = new JSONParser();
		Date d = new Date();
		String bill = "";
		
		try {
			Object obj = parser.parse(new FileReader("C:/IoT/parkingSpot.json"));
			JSONObject parkingSpotJSON = (JSONObject) obj;
			JSONObject tempStatus = (JSONObject) parkingSpotJSON.get(id);
			String vehicleStatus = (String) tempStatus.get("vehicle");
			if(vehicleStatus == "null"){
				bill = "0";
			}
			else{
				try{
					Object obj2 = parser.parse(new FileReader("C:/IoT/vehicle.json"));
					JSONObject vehicleJSON = (JSONObject) obj2;
					JSONObject tempBill = (JSONObject) vehicleJSON.get(vehicleStatus);
					if((long) tempBill.get("occupiedTimeMS") != 0){
						long billingTime = d.getTime() - (long) tempBill.get("occupiedTimeMS");
						billingTime = (int) billingTime / 1000;
						bill = String.valueOf((billingTime * Integer.parseInt(fee)));
					}
					else{
						bill = "0";
					}
					
				}catch (Exception k) {
					// TODO Auto-generated catch block
					k.printStackTrace();
				}
			}
			
			
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bill;
	}

	private String decodeListVehicles(String id) {
		// TODO Auto-generated method stub
		JSONParser parser = new JSONParser();
		String returnString = "";
		
		try {
			Object obj = parser.parse(new FileReader("C:/IoT/parkingSpot.json"));
			JSONObject parkingSpotJSON = (JSONObject) obj;
			JSONObject parkingSpot = (JSONObject) parkingSpotJSON.get(id);
			JSONArray vehicleList = (JSONArray) parkingSpot.get("vehicleList");
			System.out.print(vehicleList);
			for(int i = 0; i < vehicleList.size(); i++){
				returnString += "<tr><td>" + String.valueOf(i+1) + "</td>";
				JSONObject tempVehicle =  (JSONObject) vehicleList.get(i);
				returnString += "<td>";
				returnString += tempVehicle.get("vid");
				returnString += "</td>";
				returnString += "<td>";
				returnString += tempVehicle.get("reservedTime");
				returnString += "</td>";
				returnString += "<td>";
				returnString += tempVehicle.get("occupiedTime");
				returnString += "</td>";
				returnString += "<td>";
				returnString += tempVehicle.get("freeTime");
				returnString += "</td>";
				returnString += "<td>";
				returnString += (Float.parseFloat(tempVehicle.get("bill").toString())/100);
				returnString += " EUR </td>";
				returnString += "</tr>";
				
			}
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.print("\n");
		System.out.print(returnString);
		return returnString;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		if(username.equals("manager") & password.equals("parkinglot")){
			doGet(request, response);
		}
		else{
			response.getWriter().append("Username and Password doesnot match");
		}
	}
	private static JSONArray statusParkingSpot(){
		JSONArray statusParking = new JSONArray();
		JSONObject statusJSON = new JSONObject();
		
		JSONParser parser = new JSONParser();
		String clients = "http://"+clientIP+"/api/clients";
		try {
			Object obj = parser.parse(sendGET(clients));
			JSONArray array = (JSONArray)obj;
			for(int i=0; i<array.size();i++){
				statusJSON =  new JSONObject();
				//System.out.println(array.get(0));
				JSONObject obj2 = (JSONObject)array.get(i);
				String clientID = (String) obj2.get("endpoint");
				System.out.println(clients+"/"+clientID+"/32700/0/32801");
				String s = sendGET(clients+"/"+clientID+"/32700/0/32801");
				if(!s.equals("error")){
					JSONObject obj3 = (JSONObject) parser.parse(s);
					obj3 = (JSONObject) obj3.get("content");
					String status = (String) obj3.get("value");
					statusJSON.put("ID",clientID);
					statusJSON.put("value",status);
					String vehicle = null;
					if(!status.equals("free")){
						obj3 = (JSONObject) parser.parse(sendGET(clients+"/"+clientID+"/32700/0/32802"));
						obj3 = (JSONObject) obj3.get("content");
						vehicle = (String) obj3.get("value");
					}
					statusJSON.put("vehicle",vehicle);
					statusParking.add(statusJSON);
				}
			}
		} catch (ParseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return statusParking;
	}
	
	private static final String USER_AGENT = "Mozilla/5.0";
	 private static String sendGET(String URL) throws IOException {
	        URL obj = new URL(URL);
	        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	        con.setRequestMethod("GET");
	        con.setRequestProperty("User-Agent", USER_AGENT);
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
	           // System.out.println(response.toString());
	            return response.toString();
	        } else {
	            System.out.println("GET request not worked");
	            return "error";
	        }
	    }
}
