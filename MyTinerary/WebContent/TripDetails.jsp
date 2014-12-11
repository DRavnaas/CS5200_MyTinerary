<%@page
	import="com.sun.tools.xjc.reader.xmlschema.bindinfo.EnumMemberMode"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"
	import="myTinerary.dataAccess.*,myTinerary.entities.*,myTinerary.services.*,java.util.*,java.text.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>MyTinerary Trip Details</title>
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
                        + response.authResponse.accessToken);

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
	            AuthenticatedUserDao userDao = new AuthenticatedUserDao();
	                         
	            String userStr = request.getParameter("authsvcid");
	            String authToken = request.getParameter("authToken");
	            if ((userStr == null) || (userStr.isEmpty())) {
	                userStr = "dravnaas";
	            }
	            if ((authToken == null) || (authToken.isEmpty())) {
	                authToken = "dravnaasAuth";
	            }
	            AuthenticatedUser user = userDao.verifyAuthenticatedUser(userStr, authToken);

	            boolean showUserUndefined = true;
	            
	            if (user != null) {
	                showUserUndefined = false;
	                
	                SimpleDateFormat dateOnly = new SimpleDateFormat("MM/dd/yy");                
	                SimpleDateFormat dateTime = new SimpleDateFormat("MM/dd/yy hh:mm");                


	                // Process a request to this page (if any)    
	                String action = request.getParameter("action");
	                String tripIdStr = request.getParameter("tripId");
	                String detailIdStr = request.getParameter("detailId");                
	                String sharedText = request.getParameter("shared");
	                String detailType = request.getParameter("detailType");
	                String shareUserId = request.getParameter("shareId");
	                String name = request.getParameter("name");
                                        
	                
	                int tripId = -1;
	                int detailId = -1;
	                ShareableTrip trip = null;
	                boolean sharedTrip = false;
	                List<TripNotification> notificationList = null;
	                List<AuthenticatedUser> sharedWith =  null;
	                if ((sharedText != null) && (sharedText.equals("true")))
	                {
	                    // No editting on a shared trip.
	                    sharedTrip = true;
	                    action = "";
	                }
	                if ((detailIdStr != null) && (!detailIdStr.isEmpty()))
	                {
	                    detailId = Integer.parseInt(detailIdStr);
	                }
	                if ((tripIdStr != null) && (!tripIdStr.isEmpty()))
	                {
	                    tripId = Integer.parseInt(tripIdStr);
	                    if (sharedTrip)
	                    {
	                       SharedTripDao sharedTripMgr = new SharedTripDao();
	                       SharedTrip strip = sharedTripMgr.findSharedTrip(tripId);
	                       trip = strip.getTrip();                           
	                    }
	                    else {
	                        ShareableTripDao tripMgr = new ShareableTripDao();
	                        trip = tripMgr.findShareableTrip(tripId);
	                        
	                        SharedTripDao sharedTripMgr = new SharedTripDao();
	                        sharedWith = sharedTripMgr.findUsersTripSharedWith(trip);
	                   }                             
	                }                             

	                AuthenticatedUser tripOwner = trip.getTripOwner();        
	                
	                		
	                // Figure out any actions in the request for this trip
	                ReservationDetailDao rsvMgr = new ReservationDetailDao();
	                FlightDetailDao fltMgr = new FlightDetailDao();
	                ActivityLinkDetailDao linkMgr = new ActivityLinkDetailDao();
	                ToDoListDetailDao toDoListOfListsMgr = new ToDoListDetailDao();

	                if (!sharedTrip && ("shareTrip".equals(action))) {
	                	AuthenticatedUser viewer = userDao.findAuthenticatedUser(shareUserId);
	                	SharedTrip newSharedTrip = new SharedTrip();
	                	newSharedTrip.setSharedUser(viewer);
	                	newSharedTrip.setTrip(trip);
	                	SharedTripDao sharedTripMgr = new SharedTripDao();
	                	sharedTripMgr.createSharedTrip(newSharedTrip);
	                	
	                	// Refresh our sharedWith list
	                	sharedWith = sharedTripMgr.findUsersTripSharedWith(trip);
	                	
	                } else if (!sharedTrip && "remove".equals(action) && shareUserId != null)
	                {
	                	// ToDo: Need to find shared trip by user + base trip
	                	//sharedTripMgr.removeSharedTrip();	                	
	                }
	                else if ("reserve".equals(detailType)) {
	                	
	                	String startTimeText = request.getParameter("startDateTime");
	                    String durationText = request.getParameter("duration");
	                    String notes = request.getParameter("notes");            
	                    
	                    if ("create".equals(action)) {

	                        ReservationDetail rsv = new ReservationDetail();
	                        rsv.setFriendlyName(name);
	                        rsv.setStart(startTimeText);
	                        rsv.setDuration(durationText);
	                        rsv.setContainingTrip(trip);
	                        rsv.setNotes(notes);
	                        rsvMgr.createReservationDetail(rsv);

	                    } else if ("remove".equals(action)) {

	                    	   rsvMgr.removeReservationDetail(detailId);

	                    } else if ("edit".equals(action)) {
	                    	
	                    }
	                }
	                else if ("flight".equals(detailType)) {
	                	String confcode = request.getParameter("confcode");
                        String flightDateText = request.getParameter("flightDate");
                        String flightNum = request.getParameter("flightNum"); 
                        String airline = request.getParameter("airline"); 

	                	FlightServiceDetailDao fltSvcDao = new FlightServiceDetailDao();
                        
	                    if ("create".equals(action)) {

	                    	FlightDetail flt = new FlightDetail();
	                        flt.setConfirmationCode(confcode);
	                        flt.setContainingTrip(trip);
	                        Date departureDate = dateOnly.parse(flightDateText);	                        
	                        
	                        AirlineCode airlineEnum = AirlineCode.valueOf(airline.toUpperCase());	                        
	                        
	                        FlightServiceDetail details = FlightServiceDetailDao.fillInFlightDetails(airlineEnum, Integer.parseInt(flightNum), departureDate, false);	                        

	                        // If they gave us a bad flight number or date, don't store anything
	                        if (details != null)
	                        {
	                        	   flt.setRelatedFlight(details);                        
                        
	                        	   flt.setFriendlyName(String.format("%s %s from %s to %s", 
	                        		 airline.toString(), flightNum.toString(), details.getDepartureCity().toString(), details.getArrivalCity().toString() ));
                            
	                        	   fltMgr.createFlightDetail(flt);
	                        }

	                    } else if ("remove".equals(action)) {

	                         fltMgr.removeFlightDetail(detailId);

	                    } else if ("edit".equals(action)) {
	                    }
                    } else if ("todolist".equals(detailType)) {

                        ToDoListDetailDao toDoListDao = new ToDoListDetailDao();
                        
                        if ("create".equals(action)) {                        	                       	                    	
                            
                        	toDoListDao.createToDoListItemInTrip(trip, name);

                        } else if ("remove".equals(action)) {

                        	//toDoListDao.removeToDoListItem(detailId);

                        } else if ("complete".equals(action)) {
                        	String itemIdText = request.getParameter("itemId");
                        	
                        	// Find the list, find the item in the list, mark it complete
                        	ToDoListDetail list = toDoListDao.findToDoListDetail(detailId);
                        	if ((list != null) && (itemIdText != null))
                        	{
                        		   List<ToDoListItem> items = list.getListItems();
                        		   if ((items != null) && (items.size() > 0))
                        		   {
                        			   for (ToDoListItem i : items)
                        			   {
                        				   if (i.getId().toString().equalsIgnoreCase(itemIdText))
                        				   {
                        					   i.setCompleted(true);
                        				   }
                        			   }
                        			   
                        			   // Update list with completed item
                        			   toDoListDao.updateToDoListDetail(list.getId(), list);
                        		   }
                        	}
                        	
                        } else if ("edit".equals(action))
                        {
                        	
                        }                        
                    } else if ("activity".equals(detailType)) {
                       String url = request.getParameter("url");                         

                        ActivityLinkDetailDao linkDao = new ActivityLinkDetailDao();
                    
                        if ("create".equals(action)) {

                            ActivityLinkDetail al = new ActivityLinkDetail();
                            al.setUrl(url);
                            al.setFriendlyName(name);
                            al.setContainingTrip(trip);                         
                        
                            linkDao.createActivityLinkDetail(al);

                        } else if ("remove".equals(action)) {

                            linkDao.removeActivityLinkDetail(detailId);

                        } else if ("edit".equals(action)) {
                        }
                    
                    }
	                
	                // Now that we've performed whatever action, get latest details for display
	                
                    // Get any notifications for this trip
                    TripNotificationListDao notificationMgr = new TripNotificationListDao();
                    notificationList = notificationMgr.findNotificationsForTrip(trip);                      

	                
			// Build out our lists of different types of details for the list
			List<ReservationDetail> rsvList = rsvMgr
					.findReservationsForTrip(trip);

			List<FlightDetail> fltList = fltMgr.findFlightsForTrip(trip);

 			List<ActivityLinkDetail> linkList = linkMgr
					.findActivitiesForTrip(trip);

			List<ToDoListDetail> toDoListOfLists = toDoListOfListsMgr
					.findToDoListsForTrip(trip);
	%>
	<div class="nav">
		<!--  to do - back to trips and refresh details -->
		<a href="UserTrips.jsp">Back to Trip List</a> <br />
		<%
		String pageQuery = "TripDetails.jsp?&tripId=" + tripIdStr;
		if (sharedTrip)
		{
			pageQuery = pageQuery + "&shared=true";
		}
		%>
		<a href="<%=pageQuery%>">Refresh Trip Details</a>

	</div>

	<div class="container">

		<h1>Trip details</h1>
		<% 
        if (trip == null)
        {
        %>
		Trip not found...
		<%
        }
        else {
        %>

		<%
        if ((notificationList != null) && (notificationList.size() > 0))
        { %>

		<h4>Notifications</h4>
		<table class="table">

			<%                  
                    for (TripNotification note : notificationList) {    
                        String noteDate = dateOnly.format(note.getAddedDate());
                       
                        %>
			<tr>
				<td><%=noteDate%></td>
				<td><%=note.getText()%></td>
			</tr>
			<%
                    } // for sharedWith
                    %>
			<%          
                    } // if sharedWith not null
                %>

		</table>

		<%
        if (!sharedTrip)
        {
            // Allow them to add a user to share with
            // and show who it is already shared with
            List<AuthenticatedUser> otherUsers = userDao.findAllAuthenticatedUsers();
            %>
		<h3>Shared with...</h3>

		<table class="table">

			<%
                if ((sharedWith != null) && (sharedWith.size() > 0)) {
                	%>
			<tr>
				<td><b>Already shared with...</b></td>
			</tr>
			<%                	
                    for (AuthenticatedUser u : sharedWith) {    
                    	if (otherUsers.contains(u))
                    	{
                    		// ToDo - this isn't working, maybe equal/gethashcode needed
                    		// Weed out users that already can see the trip
                    		otherUsers.remove(u);
                    	}
                    	String firstName = "";
                    	String lastName = "";
                    	if (u.getFirstName() != null)
                    	{
                    		firstName = u.getFirstName();
                    	}
                        if (u.getLastName() != null)
                        {
                            lastName = u.getLastName();
                        }
                        %>
			<tr>
				<td><%=u.getId()%></td>
				<td><%=firstName%></td>
				<td><%=lastName%></td>
				<td><a
					href="TripDetails.jsp?action=remove&shareId=<%=u.getId()%>&tripId=<%=tripIdStr%>"
					class="btn btn-danger">Unshare</a></td>
			</tr>
			<%
                    } // for sharedWith
                } // if sharedWith not null
                %>
			<tr>
				<td>
					<form action="TripDetails.jsp">
						<div>
							Add a user to share this trip with: <span> <select
								name="shareId" id="userSelector">
									<% for (AuthenticatedUser u : otherUsers) {
                if (u.getId() != tripOwner.getId())
                {%>
									<option value="<%=u.getId()%>"><%=u.getId()%></option>
									<% } 
                 }%>
							</select>
							</span> <input name="tripId" class="form-control" value="<%=tripIdStr%>"
								type="hidden" /> <input name="action" class="form-control"
								value="shareTrip" type="hidden" /> <input type="submit"
								value="Share" class="btn btn-success">
						</div>

					</form>
				</td>
			</tr>
		</table>
		<%
            } // if !sharedTrip
            %>

		<br />
		<h3>Flights</h3>

		<table class="table">
			<%
                if ((fltList != null) && (fltList.size() > 0)) {
                	%>
			<tr>
				<th>Airline</th>
				<th>Flight number</th>
				<th>Departing from</th>
				<th></th>
				<th>Arriving at</th>
				<th></th>
				<th>Confirmation code</th>
				<th></th>
			</tr>
			<%                	
                    for (FlightDetail flt : fltList) {  
                    	String confCode = flt.getConfirmationCode();
                    	if (confCode == null)
                    	{
                    		confCode = "";
                    	}
                    	
                    	FlightServiceDetail details = flt.getRelatedFlight();
                    	String departureTime = details.getDepartureTime().toString();
                        
                        %>

			<tr>
				<td><%=details.getAirlineCode()%></td>
				<td><%=details.getFlightNumber().toString()%></td>
				<td><%=details.getDepartureCity()%></td>
				<td>at <%=dateTime.format(details.getDepartureTime()) %></td>
				<td><%=details.getArrivalCity() %></td>
				<td>at <%=dateTime.format(details.getArrivalTime()) %></td>
				<td><%=confCode%></td>
				<td>
					<%
                           if (!sharedTrip)
                            {
                             %> <a
					href="TripDetails.jsp?action=remove&detailType=flight&detailId=<%=flt.getId()%>&tripId=<%=tripIdStr%>"
					class="btn btn-danger">Delete</a> <%
                            }
                        %>
				
			</tr>
			<%
                    } // for flights
                } // if flights not null
                %>
		</table>
		<%
            if (!sharedTrip)        {
                %>

		<form action="TripDetails.jsp">

			<table class="table">

				<tr>
					<td>Airline (3 letter code): <input name="airline"
						class="form-control" value="" /></td>
					<td>Flight # (numbers only):<input name="flightNum"
						class="form-control" value="" /></td>
					<td></td>
					<td>Departure date (mm/dd/yy):<input name="flightDate"
						class="form-control" value="" /></td>
					<td>Confirmation code (optional):<input name="confCode"
						class="form-control" value="" /></td>
					<td><input type=hidden name="tripId" class="form-control"
						value="<%=tripIdStr%>" /> <input type=hidden name="detailType"
						class="form-control" value="flight" />
						<button name="action" value="create" class="btn btn-success">Add</button>
					</td>
					<td></td>
				</tr>
			</table>
		</form>
		<%
            } %>

		<br />
		<h3>Activities</h3>


		<table class="table">

			<%
                if ((linkList != null) && (linkList.size() > 0)) { 
                	%>
			<tr>
				<th></th>
				<th>Name</th>
				<th></th>
				<th></th>
				<th></th>
			</tr>
			<%
                	EnhancedLinkDetailDao elDao = new EnhancedLinkDetailDao();
                    for (ActivityLinkDetail al : linkList) { 
                    	String imgUrl = "";
                    	String description = "";
                    	String altText = al.getFriendlyName();
                    	int height = 15;
                    	
                    	EnhancedLinkDetail el = elDao.findEnhancedLinkDetail(al.getUrl());
                    	if (el != null)
                    	{
                    		height = 60;
                    		imgUrl = el.getThumbnailUrl();
                    		altText = el.getName();
                    		description = el.getName() + " at " + el.getLocation();
                    	}
                    	else {
                    		imgUrl = "./favicon.ico";
                    	}
                        
                        %>

			<tr>

				<td><img src="<%=imgUrl%>" alt="<%=altText%>"
					height=<%=height %> /></td>
				<td><a href="<%=al.getUrl()%>" target="_blank"><%=al.getFriendlyName()%></a></td>
				<td><%=description%></td>
				<td></td>
				<td></td>
				<td>
					<%
                           if (!sharedTrip)
                            {
                             %> <a
					href="TripDetails.jsp?action=remove&detailType=activity&detailId=<%=al.getId()%>&tripId=<%=tripIdStr%>"
					class="btn btn-danger">Delete</a> <%
                            }
                        %>
				
			</tr>
			<%
                    } // for activities
                } // if list not null
                %>
		</table>
		<%
            if (!sharedTrip)        {
                %>
		<form action="TripDetails.jsp">


			<table class="table">

				<tr>
					<td></td>
					<td>Name (required):<input name="name" class="form-control"
						value="" /></td>
					<td>Url (required):<input name="url" class="form-control"
						value="" /></td>
					<td><input type=hidden name="tripId" class="form-control"
						value="<%=tripIdStr%>" /> <input type=hidden name="detailType"
						class="form-control" value="activity" />
						<button name="action" value="create" class="btn btn-success">Add</button>
					</td>
				</tr>
			</table>
		</form>
		<%
            } %>

		<br />
		<h3>To Do List</h3>

		<table class="table">
			<%
			    // Changed to just one to do list per trip - not multiple
                if ((toDoListOfLists != null) && (toDoListOfLists.size() > 0)) {
                	%>
			<tr>
				<th>Completed?</th>
				<th>Description</th>
				<th></th>
			</tr>
			<%
                    ToDoListDetail list = toDoListOfLists.get(0);                    
                    List<ToDoListItem> items = list.getListItems();
                    if (items == null)
                    {
                    	items = new ArrayList<ToDoListItem>();
                    }
                    for (ToDoListItem item : items)
                    {
                        
                        %>

			<tr>
				<td><%=item.getCompleted()%></td>
				<td><%=item.getItemDescription()%></td>
				<td>
					<%
                           if ((!sharedTrip) && !item.getCompleted())
                            {
                             %> <a
					href="TripDetails.jsp?action=complete&detailType=todolist&detailId=<%=list.getId()%>&itemId=<%=item.getId()%>&tripId=<%=tripIdStr%>"
					class="btn btn-danger">Mark Complete</a> <%
                            }
                        %>
				
			</tr>
			<%
                    } // for items in to do list
                } // if rsv not null
                %>
		</table>
		<%
            if (!sharedTrip)        {
                %>

		<form action="TripDetails.jsp">


			<table class="table">

				<tr>
					<td>Thing to do:<input name="name" class="form-control"
						value="" /></td>
					<td><input type=hidden name="tripId" class="form-control"
						value="<%=tripIdStr%>" /> <input type=hidden name="detailType"
						class="form-control" value="todolist" />
						<button name="action" value="create" class="btn btn-success">Add</button>
					</td>
				</tr>
			</table>
		</form>
		<%
            } %>

		<br />
		<h3>Reservations</h3>

		<table class="table">
			<%
                if ((rsvList != null) && (rsvList.size() > 0)) {
                	%>
			<tr>
				<th>Name</th>
				<th>Date/time (optional)</th>
				<th>Duration (optional)</th>
				<th>Notes</th>
			</tr>
			<%
                    for (ReservationDetail r : rsvList) {                        
                    	
                        %>
			<tr>
				<td><%=r.getFriendlyName()%></td>
				<td><%=r.getStart()%></td>
				<td><%=r.getDuration()%></td>
				<td><%=r.getNotes()%></td>
				<td>
					<%
						   if (!sharedTrip)
							{
							 %> <a
					href="TripDetails.jsp?action=remove&detailId=<%=r.getId()%>&tripId=<%=tripIdStr%>"
					class="btn btn-danger">Delete</a> <%
    					    }
					    %>
				
			</tr>
			<%
                    } // for reservation
                } // if rsv not null
                %>
		</table>
		<%
            if (!sharedTrip)        {
                %>

		<form action="TripDetails.jsp">


			<table class="table">

				<tr>
					<td>Business Name (required):<input name="name"
						class="form-control" value="" /></td>
					<td>At (optional)<input name="startDateTime"
						class="form-control" value="" /></td>
					<td>For (optional)<input name="duration" class="form-control"
						value="" /></td>
					<td>Notes (optional)<input name="notes" class="form-control"
						value="" /></td>
					<td><input type=hidden name="tripId" class="form-control"
						value="<%=tripIdStr%>" /> <input type=hidden name="detailType"
						class="form-control" value="reserve" />
						<button name="action" value="create" class="btn btn-success">Add</button>
					</td>
				</tr>
			</table>
		</form>
		<%
            } %>
		<%
		} // if trip not null
            }
        else 
        {
        %>
		User could not be verified... No trip details to display for unknown
		user.
		<%
        }
		%>
	</div>
</body>
</html>