<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" href="visuvalizer.css" />
<title>Visualizer</title>
</head>
<body>
	<br>
	<br>
	<br>
	<div id="warp">
		<h2>Parking Lot Login Console</h2>
		<form action="visualiser" id="POST">
			<div class="admin">
				<div class="rota">
					<h1>ADMIN</h1>
					<input id="username" type="text" name="username" value=""
						placeholder="Username" /><br /> <input id="password"
						type="password" name="password" value="" placeholder="Password" />
				</div>
			</div>
			<div class="cms">
				<div class="roti">
					<h1>&nbsp;Login</h1>
					<button id="valid" type="submit">Login</button>
					<br />
					<p>
						<a href="#">Register</a> <a href="#">Forgot Password</a> <a
							href="#">Help</a>
					</p>
				</div>
			</div>
		</form>
	</div>

</body>
</html>