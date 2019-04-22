<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" href="index.css"/>
<title>Vehicle</title>
</head>
<body>
<center><h1>
	Welcome to the Vehicle broker
</h1></center><br><br><br>
	<form action="addVehicle" method="GET">
		<center>
		 	<input type="text" name="vehicleID" required class="search-box" placeholder="Enter vehicle ID" /><br><br><br>
			<button class="button center" type="Submit">
  				Search
				<span class="pulse"></span>
			</button>
		</center>
	</form>

</body>
</html>