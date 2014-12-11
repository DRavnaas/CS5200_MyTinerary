<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"
	import="myTinerary.dataAccess.*,myTinerary.entities.*,java.util.*,java.text.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>MyTinerary Trips</title>
<link href="css/bootstrap.css" rel="stylesheet" />
</head>
<body>


	<script>
		// This is called with the results from from FB.getLoginStatus().

		function statusChangeCallback(response) {
			console.log('statusChangeCallback');
			console.log(response);
			// The response object is returned with a status field that lets the
			// app know the current login status of the person.
			// Full docs on the response object can be found in the documentation
			// for FB.getLoginStatus().
			if (response.status === 'connected') {
				// Logged into your app and Facebook.
				
				console.log('Welcome!  Fetching information for app user id '
						+ response.authResponse.userID);

				FB.api('/me', function(response) {
					console.log('Successful login for: ' + response.name);

                    document.getElementById('status').innerHTML = 'Logged in as '
                        + response.name;
				});

			} else if (response.status === 'not_authorized') {
				window.location.href = "Index.html";

			} else {
				window.location.href = "Index.html";
			}
		}

		// This function is called when someone finishes with the Login
		// Button.  See the onlogin handler attached to it in the sample
		// code below.
		function checkLoginState() {
			FB.getLoginStatus(function(response) {
				statusChangeCallback(response);
			});
		}

		window.fbAsyncInit = function() {
			FB.init({
				appId : '1550728901810387',
				cookie : true, // enable cookies to allow the server to access 
				// the session
				xfbml : true, // parse social plugins on this page
				version : 'v2.1' // use version 2.1
			});

			// Now that we've initialized the JavaScript SDK, we call 
			// FB.getLoginStatus().  This function gets the state of the
			// person visiting this page and can return one of three states to
			// the callback you provide.  They can be:
			//
			// 1. Logged into your app ('connected')
			// 2. Logged into Facebook, but not your app ('not_authorized')
			// 3. Not logged into Facebook and can't tell if they are logged into
			//    your app or not.
			//
			// These three cases are handled in the callback function.

			FB.getLoginStatus(function(response) {
				statusChangeCallback(response);
			});

		};

		// Load the SDK asynchronously
		(function(d, s, id) {
			var js, fjs = d.getElementsByTagName(s)[0];
			if (d.getElementById(id))
				return;
			js = d.createElement(s);
			js.id = id;
			js.src = "//connect.facebook.net/en_US/sdk.js";
			fjs.parentNode.insertBefore(js, fjs);
		}(document, 'script', 'facebook-jssdk'));
	</script>

	<div id="status">Logged in as ...</div>


	<%
			// ToDo: get fb auth working server side
			// For now - pass "fake" id and token to verify
			AuthenticatedUserDao userDao = new AuthenticatedUserDao();
			ShareableTripDao tripDao = null;
			String userStr = request.getParameter("authsvcid");
			String authToken = request.getParameter("authToken");
			if ((userStr == null) || (userStr.isEmpty())) {
				userStr = "dravnaas";
			}
			if ((authToken == null) || (authToken.isEmpty())) {
				authToken = "dravnaasAuth";
			}
			AuthenticatedUser user = userDao.verifyAuthenticatedUser(userStr,
					authToken);

			List<ShareableTrip> trips = null;
			List<SharedTrip> sharedTrips = null;
			boolean showUserUndefined = true;
			int numTrips = 0;
            SimpleDateFormat dateOnly = new SimpleDateFormat("MM/dd/yy");

			if (user != null) {
				showUserUndefined = false;

				tripDao = new ShareableTripDao();

				// Process a request to this page (if any)    
				String action = request.getParameter("action");
				String name = request.getParameter("name");
				String idStr = request.getParameter("id");
				String destination = request.getParameter("dest");
				String startDateText = request.getParameter("startDate");
				String endDateText = request.getParameter("endDate");
				
				String findTripText = request.getParameter("searchTrips");

				ShareableTrip trip = null;
				if ("create".equals(action)) {
					// Create the trip
					trip = new ShareableTrip();
					trip.setFriendlyName(name);
					trip.setTripOwner(user);
					trip.setDestination(destination);

					// Try to start date and end date (will parse)
					trip.setEndDate(endDateText);
					trip.setStartDate(startDateText);

					tripDao.createShareableTrip(trip);
				} else if ("remove".equals(action)) {
					// Remove the trip
					int id = Integer.parseInt(idStr);
					tripDao.removeShareableTrip(id);
				} 

				SharedTripDao sharedTripMgr = new SharedTripDao();

				trips = tripDao.findAllShareableTripsForUser(user);

				sharedTrips = sharedTripMgr
							.findAllTripsSharedWithUser(user);
			    if (trips!= null)
			    {
			    	numTrips += trips.size();
			    }
			    if (sharedTrips != null)
			    {
			    	// Build a list of the trips that the shareable trip points to.
			    	numTrips += sharedTrips.size();
			    	List<ShareableTrip> sharedTripRecords = new ArrayList<ShareableTrip>();
                    for (SharedTrip st : sharedTrips) {
                            ShareableTrip t = tripDao.findShareableTrip(st.getTrip().getId());
                            sharedTripRecords.add(t);
                    }                            
			    }				
		%>

	<div class="nav">
		<a href="UserTrips.jsp">Refresh Trip List</a> <br />

		<% if (numTrips > 0)
    {
    %>
		<br /> <span> Edit details for your trip: </span>
		<form action="TripDetails.jsp">
			<select name="tripId" id="tripSelector">
				<% for (ShareableTrip t : trips) { %>
				<option value="<%=t.getId()%>"><%=t.getFriendlyName()%></option>
				<% } %>
			</select> <input type="submit" value="Go!" class="btn btn-info">

		</form>
		<br /> <span> View details for a trip shared with you: </span>
		<form action="TripDetails.jsp">
		<!--  to do - need a sharedTrip=true here to get the right view, but how to add query params without encoding? -->
			<select name="tripId" id="tripSelector">
				<% for (SharedTrip st : sharedTrips) {
                ShareableTrip t = tripDao.findShareableTrip(st.getTrip().getId());
            	%>
				<option value="<%=t.getId()%>"><%=t.getFriendlyName()%></option>
				<% } %>
			</select> <input type="submit" value="Go!" class="btn btn-info">

		</form>
		<%} %>
	</div>
	<div class="container">


		<h1>Trips planned by you</h1>


		<table class="table">
			<tr>
				<th>Name</th>
				<th>Destination</th>
				<th>Travel Date</th>
				<th>until (optional)</th>
				<th></th>
			</tr>
			<%
					if (trips != null) {
							for (ShareableTrip st : trips) {
								endDateText = "";
								if (st.getEndDate() != null) {
									endDateText = dateOnly.format(st.getEndDate());
								}
				%>
			<tr>
				<td><%=st.getFriendlyName()%></td>
				<td><%=st.getDestination()%></td>
				<td><%=dateOnly.format(st.getStartDate())%></td>
				<td><%=endDateText%></td>
				<td><a href="UserTrips.jsp?action=remove&id=<%=st.getId()%>"
					class="btn btn-danger">Delete</a> <a
					href="TripDetails.jsp?tripId=<%=st.getId()%>" class="btn btn-info">View</a></td>
			</tr>
			<%
   					       }
					}
				%>
		</table>
		<form action="UserTrips.jsp">
			<table class="table">

				<tr>
					<td>Trip name (required):<input name="name"
						class="form-control" value="" /></td>
					<td>Trip destination (required):<input name="dest"
						class="form-control" value="" /></td>
					<td>Start of trip (mm/dd/yy, required):<input name="startDate"
						class="form-control" maxlength=10 size=10 value="" /></td>
					<td>End of trip (mm/dd/yy, optional)<input name="endDate"
						class="form-control" maxlength=10 size=10 value="" /></td>
					<td>
						<button name="action" value="create" class="btn btn-success">Add
							trip</button>
					</td>
				</tr>
			</table>
		</form>

		<h1>Trips shared with you</h1>
		<table class="table">
			<tr>
				<th>Name</th>
				<th>Destination</th>
				<th>Dates</th>
				<th />
			</tr>
			<%
				if (sharedTrips != null) {
						for (SharedTrip st : sharedTrips) {
							ShareableTrip t = tripDao.findShareableTrip(st
									.getTrip().getId());
							endDateText = "";
							if (t.getEndDate() != null) {
								endDateText = " until " + dateOnly.format(t.getEndDate());
							}
			%>
			<tr>
				<td><%=t.getFriendlyName()%></td>
				<td><%=t.getDestination()%></td>
				<td><%=dateOnly.format(t.getStartDate())+endDateText%></td>

				<td><a
					href="TripDetails.jsp?action=select&shared=true&tripId=<%=st.getId()%>"
					class="btn btn-info">View</a></td>
			</tr>
			<%
				}
					}
			%>
		</table>
		<%
			
			} else {
				// User not defined
		%>
		User could not be verified... No trips to display for an unknown user.
		<%
			}
		%>
	</div>
</body>
</html>