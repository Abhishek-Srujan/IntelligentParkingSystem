

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Random;

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
 * Servlet implementation class addVehicle
 */
@WebServlet("/addVehicle")
public class addVehicle extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String clientIP = "192.168.0.191:8080";
	private static String thisIP = "192.168.0.191:8081";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public addVehicle() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/html");
		String VID =  request.getParameter("vehicleID");
		String parkingSlot =  request.getParameter("parkingSlot");
		String[] freeParkingSlot = findParkingSpot(parkingSlot);
		if(parkingSlot==null){
			if(freeParkingSlot==null){
				response.getWriter().append("VID: ").append(VID).append("<br> Plarking slots are already filled <br>");
			}
			else{
				//System.out.println(freeParkingSlot);
				response.getWriter().append("VID: ").append(VID).append("<br> Choose one of the parking lot: <br>");
				response.getWriter().append("<form action=\"addVehicle\" method=\"GET\"> <input type=\"hidden\" name=\"vehicleID\" value=\""+VID+"\">");
				for(int i=0;i<freeParkingSlot.length;i++){
					response.getWriter().append("<input type=\"radio\" name=\"parkingSlot\" value=\""+freeParkingSlot[i]+"\">"+freeParkingSlot[i]+"<br>");
				}
				response.getWriter().append("<input type=\"submit\"> </form>");
			}
		}
		else{
			response.getWriter().append("VID: ").append(VID).append("<br>");
			if(freeParkingSlot[0].equals("success")){
				// Do reservation
				String status = reserveParkingSpot(parkingSlot,VID);
				if (status.equals("success")){
					response.getWriter().append("Parking Lot <b>").append(parkingSlot).append("</b> is reserved for you");
				}
			}
			else{
				response.getWriter().append("The selected parking Lot <b>").append(parkingSlot).append("</b> is unavailable for you");
				response.getWriter().append("<br> Choose one of the parking lot: <br>");
				freeParkingSlot = findParkingSpot(null);
				response.getWriter().append("<form action=\"addVehicle\" method=\"GET\"> <input type=\"hidden\" name=\"vehicleID\" value=\""+VID+"\">");
				for(int i=0;i<freeParkingSlot.length;i++){
					response.getWriter().append("<input type=\"radio\" name=\"parkingSlot\" value=\""+freeParkingSlot[i]+"\">"+freeParkingSlot[i]+"<br>");
				}
				response.getWriter().append("<input type=\"submit\"> </form>");
			}
		}
	}
	
	private String reserveParkingSpot(String clientID, String VID) throws IOException {
		// TODO Auto-generated method stub
		String clients = "http://"+clientIP+"/api/clients";
		
		String payload = "{\"id\":32801,\"value\":\"reserved\"}";
		String status = sendPUT(clients+"/"+clientID+"/32700/0/32801", payload);
		
		payload = "{\"id\":32802,\"value\":\""+VID+"\"}";
		status = sendPUT(clients+"/"+clientID+"/32700/0/32802", payload);
		
		payload = "{\"id\":5527,\"value\":\"orange\"}";
		status = sendPUT(clients+"/"+clientID+"/3341/0/5527", payload);
		
		status = sendPost(clients+"/"+clientID+"/32700/0/32801/observe");
		
		//Update Billing
		status = sendGET("http://"+thisIP+"/IoT/update?id=32801&value=reserved&clientID="+clientID+"&vehicleID="+VID);
		
		return "success";
	}

	private String sendPost(String url) throws IOException {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		String urlParameters = "";
		
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		//print result
		System.out.println(response.toString());

		return "success";
	}

	private static String[] findParkingSpot(String ParkingSpot){
		boolean findall = true;
		if (ParkingSpot != null){
			findall=false;
		}
		String[] Success = new String[1];	
		String[] freeSpot = new String[20];
		int temp=0;
			JSONParser parser = new JSONParser();
			String clients = "http://"+clientIP+"/api/clients";
		try {
			Object obj = parser.parse(sendGET(clients));
			JSONArray array = (JSONArray)obj;
			for(int i=0; i<array.size();i++){
				//System.out.println(array.get(0));
				JSONObject obj2 = (JSONObject)array.get(i);
				String clientID = (String) obj2.get("endpoint");
				System.out.println(clients+"/"+clientID+"/32700/0/32801");
				JSONObject obj3 = (JSONObject) parser.parse(sendGET(clients+"/"+clientID+"/32700/0/32801"));
				obj3 = (JSONObject) obj3.get("content");
				String status = (String) obj3.get("value");
				if(status.equals("free")){
					freeSpot[temp] = clientID;
					temp++;
					if(!findall){
						Success[0]="success";
						return Success;
					}
				}
				//System.out.println(status);
				//System.out.println(obj2.get("endpoint")); 
			}
		} catch (ParseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] copyTo = java.util.Arrays.copyOfRange(freeSpot, 0, temp);
		return copyTo;
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
