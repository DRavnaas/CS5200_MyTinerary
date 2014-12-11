package myTinerary.services;

// Parts of the code from https://flightaware.com/commercial/flightxml/documentation2.rvt#examples

import java.util.Calendar;
import java.util.Date;

import myTinerary.entities.FlightServiceDetail;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.scribe.model.Request;
import org.scribe.model.Response;
import org.scribe.model.Verb;

// Represents the interface for the FlightAware service
// Filling in flight details for a given airline + flight + date
public class FlightAware {	
	
	//private static String apiKey = "85467c895cf8becb874607c151383a9e0d718ec9";
	//private static String user = "DRavnaas";
	private static String base64EncodedAuthValue = "Basic RFJhdm5hYXM6ODU0NjdjODk1Y2Y4YmVjYjg3NDYwN2MxNTEzODNhOWUwZDcxOGVjOQ==";
	private static String urlBase = "http://flightxml.flightaware.com/json/FlightXML2";

	public static FlightServiceDetail queryScheduledFlightDetails(AirlineCode airline, int flightNumber, Date dayOfTravel)
	{	
		FlightServiceDetail details = null;
		
		Date today = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);	
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		today = cal.getTime();		
		if (dayOfTravel.getTime() < today.getTime())
		{
			// We only query flights that are on today or in the future.			
			return null;
		}
		
		JSONObject jsonResponse = searchForAirlineScheduledFlight(airline, flightNumber, dayOfTravel);

		if (jsonResponse != null)
		{
			try {
				JSONObject flightData = (JSONObject)jsonResponse.get("AirlineFlightSchedulesResult");
				JSONArray flights = null;
				if (flightData != null)
				{
					flights = (JSONArray)flightData.get("data");
				}
				if ((flights == null) || (flights.size() == 0))
				{
					System.out.println("No flight data returned");
				}
				else {
					JSONObject firstFlight = (JSONObject) flights.get(0);
					String origin = firstFlight.get("origin").toString();
					String destination = firstFlight.get("destination").toString();
					
					long startEpochMilliSeconds = 0;
					String temp = firstFlight.get("departuretime").toString();
					if (temp != null)
					{
						startEpochMilliSeconds = Long.parseLong(temp) * 1000L;						
					}
					long endEpochMilliSeconds = 0;
					temp = firstFlight.get("arrivaltime").toString();
					if (temp != null)
					{
						endEpochMilliSeconds = Long.parseLong(temp) * 1000L;
					}					

					if ((origin != null) && !origin.isEmpty() &&
							(destination != null) && !destination.isEmpty() &&
							(startEpochMilliSeconds != 0) &&
							(endEpochMilliSeconds > startEpochMilliSeconds))
					{

						Date departureDateTime = new Date();
						departureDateTime.setTime(startEpochMilliSeconds);
						details = new FlightServiceDetail(airline, flightNumber, departureDateTime);
						
						// convert from 4 letter ICAO codes to the three letter ones users are used to (= enum values)
						// (note the query was willing to work with the three letter one, so requerying later should be ok)
						temp = destination.substring(1);						
						details.setArrivalCity(AirportCode.valueOf(temp));
						temp = origin.substring(1);
						details.setDepartureCity(AirportCode.valueOf(temp));
						details.setArrivalTime(new Date(endEpochMilliSeconds));
						details.setLastRefreshed(new Date());	
					}
				}
			}
			catch (Exception e) {
				System.out.println("could not get expected properties from flightaware response");
			}
		}
		

		return details;
	}
	
	// Note that airline + date + flight number = a unique flight
	protected static JSONObject searchForAirlineScheduledFlight(AirlineCode airline, int flightNumber, Date dayOfTravel)
	{	
		JSONObject jsonResponse = null;
		String JSONResponseBody = null;
		
		// We search the whole day for the flight (this assumes the caller really did give us
		// a date with the hours/minutes/seconds stripped out
		long startUnixEpochTime = dayOfTravel.getTime() / 1000L;
		long endUnixEpochTime = startUnixEpochTime + 24L * 60L * 60L - 1L;
	
		String urlSearchString = String.format("%s/AirlineFlightSchedules?startDate=%d&endDate=%d&airline=%s&flightno=%d&howMany=3&offset=0", urlBase, startUnixEpochTime, endUnixEpochTime, airline.toString(), flightNumber);
		
		Response response = sendGetRequest(urlSearchString);
		
		if (response.getCode() == 200)
		{
			JSONResponseBody = response.getBody();
			System.out.println("Query returned " + response.getMessage());
			
			JSONParser parser = new JSONParser();
			try {
				jsonResponse = (JSONObject) parser.parse(JSONResponseBody);
			} catch (ParseException pe) {
				System.out.println("Error: could not parse JSON response:");
				System.out.println(JSONResponseBody);

			}	  			
		}	
		else 
		{
			System.out.println("Query returned " + response.getMessage());
		}
		
		return jsonResponse;
	}		
	
	protected static Response sendGetRequest(String url)
	{		
		Request request =  new Request(Verb.GET, url);

		request.addHeader("Authorization", base64EncodedAuthValue);    

		System.out.println("Querying " + request.getCompleteUrl() );
		Response response = request.send();
		
		return response;
	}
	
	public static void main(String[] args) {

		FlightServiceDetail flight = queryScheduledFlightDetails(AirlineCode.ASA, 320, new Date());
		
		if (flight != null)
		{
			System.out.println("success for flight " + flight.getId());
		}
		else 
		{
			System.out.println("flight details not filled in");
		}

	}

}
