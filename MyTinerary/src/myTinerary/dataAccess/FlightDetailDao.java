package myTinerary.dataAccess;

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.persistence.*;

import myTinerary.entities.*;
import myTinerary.services.AirlineCode;
import myTinerary.services.AirportCode;

// Annotated class providing CRUD resource handling for FlightDetail
@Path("/flight")
public class FlightDetailDao {

	private static String findAllQueryName = "findAllFlightDetails";
	private static String findByTrip = "findFlightsByTrip";

	private EntityManagerFactory factory = Persistence.createEntityManagerFactory("MyTinerary");

	// GET on http://<server>:<port>/<vdir>/<servlet mapping>/flight/<flightDetailId>
	// for example:  http://localhost:8080/testjpa/api/flight/1
	// findFlightDetail - find a flightDetail with the given id (specified via url path parameter)
	//  Returns either the JSON representing the flightDetail or null if the flightDetail wasn't found
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public FlightDetail findFlightDetail(@PathParam("id") int flightDetailId) {
		FlightDetail flightDetail = null;
		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			flightDetail = em.find(FlightDetail.class, flightDetailId);

			em.getTransaction().commit();
		}
		catch (Exception ex) {
			if ((em != null) && (em.getTransaction() != null) && (em.getTransaction().isActive()))
			{
				em.getTransaction().rollback();
			}
			throw ex;
		}
		finally {
			if (em != null)
			{
				em.close();
			}
		}
		return flightDetail;
	}

	// GET on http://<server>:<port>/<vdir>/<servlet mapping>/flight/
	// for example:  http://localhost:8080/testjpa/api/flight/
	// findAllFlightDetails - return all flightDetails
	//  Returns either the JSON representing all currently persisted flightDetails,
	//  or JSON representing an empty list if no flightDetails are defined
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<FlightDetail> findAllFlightDetails() {
		List<FlightDetail> flightDetails = new ArrayList<FlightDetail>();
		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			TypedQuery<FlightDetail> query = em.createNamedQuery(findAllQueryName, FlightDetail.class);
			flightDetails = query.getResultList();

			em.getTransaction().commit();
		}
		catch (Exception ex) {
			if ((em != null) && (em.getTransaction() != null) && (em.getTransaction().isActive()))
			{
				em.getTransaction().rollback();
			}
			throw ex;
		}
		finally {
			if (em != null)
			{
				em.close();
			}
		}

		return flightDetails;
	}

	// Get the flights for a shared trip
	public List<FlightDetail> findFlightsForTrip(SharedTrip t)
	{
		return findFlightsForTrip(t.getTrip());
	}

	// Get the flights for a trip
	public List<FlightDetail> findFlightsForTrip(TripBase t)
	{
		List<FlightDetail> flightDetails = new ArrayList<FlightDetail>();	

		if (t != null) {
			EntityManager em = null;
			try	{
				em = factory.createEntityManager();

				em.getTransaction().begin();		
				TypedQuery<FlightDetail> query = em.createNamedQuery(findByTrip, FlightDetail.class);
				query.setParameter("trip", t);
				flightDetails = query.getResultList();

				em.getTransaction().commit();
			}
			catch (Exception ex) {
				if ((em != null) && (em.getTransaction() != null) && (em.getTransaction().isActive()))
				{
					em.getTransaction().rollback();
				}
				throw ex;
			}
			finally {
				if (em != null)
				{
					em.close();
				}
			}
		}

		// Query the service for this trip's flight details.
		// If the times have changed, set a notification.
		// Note we assume an airline and flight number don't change.
		for (FlightDetail flight: flightDetails)
		{
			// Must have an associated flight service record...
			FlightServiceDetail details = flight.getRelatedFlight();
			String notification = null;

			// getLastRefreshed used to indicate the service wrote the details,
			// as opposed to test data.
			if ((details != null) && (details.getLastRefreshed() != null))  
			{
				// Check if the flight times have changed...
				Date origDeparture = new Date(details.getDepartureTime().getTime());
				Date origArrival = new Date(details.getArrivalTime().getTime());

				// Query for the flight specifics, and persist the details.
				details = FlightServiceDetailDao.fillInFlightDetails(details.getAirlineCode(), details.getFlightNumber(), details.getDepartureTime(), false);

				if ((details != null) && 
						((origDeparture.getTime() != details.getDepartureTime().getTime()) ||
						(origArrival.getTime() != details.getArrivalTime().getTime())))
				{
					// TODO: This should cause a notification for all trips that have this flight
					// NOT just the user that happened to query it.
					
					// Update the flight for the new info (all users will get this update)
					FlightServiceDetailDao flightMgr = new FlightServiceDetailDao();
					try
					{
						flightMgr.upsertFlightServiceDetail(details);
					}
					catch (Exception ex)
					{
						System.out.println("Error storing enhanced flight details, ignoring");
						ex.printStackTrace();
						details = null;
					}
					
					// The flight has changed - add a notification for this trip
					notification = String.format("A flight in this trip changed; review flight %s %s details", 
							details.getAirlineCode(), details.getFlightNumber().toString() );

					int tripId = flight.getContainingTrip().getId();
					ShareableTrip trip = new ShareableTripDao().findShareableTrip(tripId);
					if (trip != null)
					{
						new TripNotificationListDao().createTripNotificationInTrip(trip, notification);
					}				

				}
			}
		}

		return flightDetails;
	}


	// PUT on http://<server>:<port>/<vdir>/<servlet mapping>/flight/<flightDetailId>/<confirmationCode>
	// for example:  http://localhost:8080/testjpa/api/flight/1/UUTPC
	// updateFlightDetail - update a flightDetail
	//  Updates the flightDetail at the given id with the properties of the specified flightDetail.
	//  Note that the flightDetailId is used to find the flightDetail - not whatever id is in the
	//  specified flightDetail object.
	//  Returns the JSON representing all currently persisted flightDetails 
	//  (including the newly updated flightDetail).
	//  Note - the specified flightDetail id must correspond to a persisted flightDetail, or 
	//  this method will result in an error.
	@PUT
	@Path("/{id}/{confirmationCode}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)	
	public List<FlightDetail> updateFlightDetail(@PathParam("id") int flightDetailId, FlightDetail flight) {
		EntityManager em = null;

		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			// You can only change the confirmation code and friendly name - other fields are not updateable
			FlightDetail flightDetail = em.find(FlightDetail.class, flightDetailId);
			flightDetail.setConfirmationCode(flight.getConfirmationCode());
			flightDetail.setFriendlyName(flight.getFriendlyName());

			em.merge(flightDetail);

			em.getTransaction().commit();
		}
		catch (Exception ex) {
			if ((em != null) && (em.getTransaction() != null) && (em.getTransaction().isActive()))
			{
				em.getTransaction().rollback();
			}
			throw ex;
		}
		finally {
			if (em != null)
			{
				em.close();
			}
		}

		return findAllFlightDetails();
	}

	// DELETE on http://<server>:<port>/<vdir>/<servlet mapping>/flight/<flightDetailId>
	// for example:  http://localhost:8080/testjpa/api/flight/1
	// removeFlightDetail - remove a flightDetail
	//  Removes the flightDetail at the given id.
	//  Returns the JSON representing all currently persisted flightDetails 
	//  (with the just deleted flightDetail removed).
	//  This method can be called more than once on the same id; if
	//  the flightDetail doesn't exist, then there is no change made.
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<FlightDetail> removeFlightDetail(@PathParam("id") int flightDetailId) {
		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			FlightDetail flightDetail = null;

			em.getTransaction().begin();

			flightDetail = em.find(FlightDetail.class, flightDetailId);
			if (flightDetail != null)
			{
				em.remove(flightDetail);
			}

			em.getTransaction().commit();
		}
		catch (Exception ex) {
			if ((em != null) && (em.getTransaction() != null) && (em.getTransaction().isActive()))
			{
				em.getTransaction().rollback();
			}
			throw ex;
		}
		finally {
			if (em != null)
			{
				em.close();
			}
		}

		return findAllFlightDetails();
	}

	// POST on http://<server>:<port>/<vdir>/<servlet mapping>/flight/
	// for example:  http://localhost:8080/testjpa/api/flight/
	// createFlightDetail - create a flightDetail
	//  Creates a flightDetail with a new id set with the properties of the specified flightDetail.
	//  Note that the given flightDetail object's id is ignored and a new one is assigned.
	//  Returns the JSON representing all currently persisted flightDetails 
	//  (including the newly created flightDetail).
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<FlightDetail> createFlightDetail(FlightDetail flightDetail) throws Exception {
		EntityManager em = null;

		// Look first to see if this flight exists (if not, don't store anything)
		FlightServiceDetail flight = flightDetail.getRelatedFlight();

		// getLastRefreshed used to indicate the service wrote the details,
		// as opposed to test data - ie: don't query service using bogus airline+flight
		if ((flight != null) && (flight.getLastRefreshed() != null))  
		{			
			// Query for the flight specifics, and persist the details.
			flight = FlightServiceDetailDao.fillInFlightDetails(flight.getAirlineCode(), flight.getFlightNumber(), flight.getDepartureTime(), false);		

		}
		if (flight == null)
		{
			// We don't store the flight unless we can match it up to a flight
			throw new Exception("Flight detail must have a flight service detail property, with valid airline, flight # and date");
		}

		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			em.persist(flightDetail);

			em.getTransaction().commit();

		}
		catch (Exception ex) {
			if ((em != null) && (em.getTransaction() != null) && (em.getTransaction().isActive()))
			{
				em.getTransaction().rollback();
			}
			throw ex;
		}
		finally {
			if (em != null)
			{
				em.close();
			}
		}

		return findAllFlightDetails();
	}	

	// Main for testing purposes - test the DAO methods.
	// (Fiddler used to test the web service REST calls.)
	public static void main(String[] args) throws Exception {		

		// Note that a "normal" insert of flight details 
		FlightDetailDao flightDetailMgr = new FlightDetailDao();
		FlightServiceDetailDao flightSvcDetailMgr = new FlightServiceDetailDao();

		// We have to associate flight trip details with flights.
		// Find what flights are already in the system.
		List<FlightServiceDetail> flights = flightSvcDetailMgr.findAllFlightServiceDetails();
		Integer numberFlightServiceRecords = flights.size();

		// Find a trip to add a detail to.
		ShareableTripDao tripMgr = new ShareableTripDao();
		List<ShareableTrip> trips = tripMgr.findAllShareableTrips();
		if (trips.size() == 0)
		{
			System.out.println("Expecting at least one shareable trip defined - rerun add and clean");
			return;
		}
		ShareableTrip trip = trips.get(0);

		// Testing DAO operations - START
		FlightDetail entity = null;

		List<FlightDetail> flightDetails = flightDetailMgr.findAllFlightDetails();

		int expectedNumber = flightDetails.size();
		while (expectedNumber < 2) {
			// We need two flightDetails for the testing - add two links.
			// We will not modify links that are already created (so we can test with data in the system)
			entity = new FlightDetail();
			entity.setFriendlyName("flight details " + Integer.toString(expectedNumber));
			entity.setConfirmationCode("XYZ" + Integer.toString(flightDetails.size()));
			entity.setContainingTrip(trip);

			// Create the flight info record separately - normally, the flight service does this
			if (numberFlightServiceRecords <= expectedNumber)
			{
				FlightServiceDetail details = new FlightServiceDetail(AirlineCode.AAL, 105 + expectedNumber, new Date());
				details.setDepartureCity(AirportCode.SEA);
				details.setArrivalCity(AirportCode.LAX);
				details.setArrivalTime(new Date());
				details.setLastRefreshed(new Date());  // Setting this to "now" helps ensure the DAO query the service.
				flights = flightSvcDetailMgr.upsertFlightServiceDetail(details);
				numberFlightServiceRecords = flights.size();
			}

			entity.setRelatedFlight(flights.get(expectedNumber));

			flightDetails = flightDetailMgr.createFlightDetail(entity);			
			expectedNumber += 1;

		}

		flights = flightSvcDetailMgr.findAllFlightServiceDetails();
		numberFlightServiceRecords = flights.size();
		if (numberFlightServiceRecords < 1)
		{
			System.out.println("Warning, expecting at least two flight records for details");
		}

		// Ensure we have two flights defined
		flightDetails = flightDetailMgr.findAllFlightDetails(); 
		if (flightDetails.size() != expectedNumber) {
			System.out.println("Wrong number of flightDetails found?");
			return;
		}		

		// Add a flightDetail similar to the first one.  The two FlightDetail records refer to the same FlightServiceDetail record
		entity = new FlightDetail();
		entity.setFriendlyName("added flightDetail");
		entity.setConfirmationCode("123456");
		entity.setContainingTrip(trip);
		entity.setRelatedFlight(flightDetails.get(0).getRelatedFlight());

		flightDetails = flightDetailMgr.createFlightDetail(entity);
		if (flightDetails.size() != expectedNumber + 1) {
			System.out.println("Wrong number of flightDetails found - rerun add and clean?");
		}		

		// A new flight service records should not have been created
		// new item should to same record as old item
		flights = flightSvcDetailMgr.findAllFlightServiceDetails();
		if (numberFlightServiceRecords != flights.size())
		{
			System.out.println("number of flight service records changed - did create add a new one?");
		}

		// Find our new item...
		Integer testIndex = expectedNumber + 1;
		do 
		{
			testIndex --;
		}
		while (!flightDetails.get(testIndex).getFriendlyName().equalsIgnoreCase(entity.getFriendlyName()));	

		// Get the flightDetail we just added.
		entity = flightDetailMgr.findFlightDetail(flightDetails.get(testIndex).getId());
		if ((flightDetails.get(testIndex).getConfirmationCode() != entity.getConfirmationCode()) ||
				(!flightDetails.get(testIndex).getFriendlyName().equalsIgnoreCase(entity.getFriendlyName())))
		{
			System.out.println("Updated flightDetail properties mismatch");
		}

		// Update the flightDetail's properties
		entity.setFriendlyName("updated flightDetail" + Integer.toString(entity.getId()));	    
		entity.setConfirmationCode("6789");
		flightDetails = flightDetailMgr.updateFlightDetail(entity.getId(), entity);
		testIndex = expectedNumber + 1;
		do 
		{
			testIndex --;
		}
		while (!flightDetails.get(testIndex).getFriendlyName().equalsIgnoreCase(entity.getFriendlyName()));	

		if ((flightDetails.get(testIndex).getConfirmationCode() != entity.getConfirmationCode()) ||
				(!flightDetails.get(testIndex).getFriendlyName().equalsIgnoreCase(entity.getFriendlyName())))
		{
			System.out.println("Updated flightDetail properties mismatch");
		}		

		// Remove the flightDetail we just added
		Integer removeId = flightDetails.get(testIndex).getId();	
		flightDetails = flightDetailMgr.removeFlightDetail(removeId);
		if (flightDetails.size() != expectedNumber) {
			System.out.println("Wrong number of flightDetails found - rerun add and clean?");
		}	

		// Remove the entity again (should still have 2 entities left, nothing removed)
		flightDetails = flightDetailMgr.removeFlightDetail(removeId);
		if (flightDetails.size() != expectedNumber) {
			System.out.println("Wrong number of flightDetails found - rerun add and clean?");
		}	

		// Removing a flight trip detail should not remove the underlying flight
		// (flight record is shared by other flight trip details)
		flights = flightSvcDetailMgr.findAllFlightServiceDetails();
		if (numberFlightServiceRecords != flights.size())
		{
			System.out.println("number of flight service records changed - did create add a new one?");
		}

		// Finally - test that removing a flight removes the associated detail

		// Add a new flight...
		FlightServiceDetail details = new FlightServiceDetail(AirlineCode.DAL, 105 + expectedNumber, new Date());
		details.setDepartureCity(AirportCode.SEA);
		details.setArrivalCity(AirportCode.LAX);
		details.setArrivalTime(new Date());
		details.setLastRefreshed(new Date());
		flights = flightSvcDetailMgr.upsertFlightServiceDetail(details);
		numberFlightServiceRecords = flights.size();
		Integer flightIndex = numberFlightServiceRecords;
		do {
			flightIndex--;
		}
		while ((flights.get(flightIndex).getTripFlights().size() != 0) &&
				(flights.get(testIndex).getAirlineCode() != details.getAirlineCode()) ||
				(flights.get(testIndex).getArrivalCity() != details.getArrivalCity()) ||
				(flights.get(testIndex).getDepartureCity() != details.getDepartureCity()) ||
				(flights.get(testIndex).getFlightNumber() != details.getFlightNumber()));

		// And add a new trip detail referencing it
		entity = new FlightDetail();
		entity.setFriendlyName("another flight in trip " + Integer.toString(expectedNumber));
		entity.setConfirmationCode("XYZ" + Integer.toString(flightDetails.size()));
		entity.setContainingTrip(trip);
		entity.setRelatedFlight(details);
		flightDetails = flightDetailMgr.createFlightDetail(entity);
		if (flightDetails.size() != expectedNumber + 1) {
			System.out.println("Wrong number of flightDetails found - rerun add and clean?");
		}

		// find the flight detail
		testIndex = expectedNumber + 1;
		do 
		{
			testIndex --;
		}
		while (!flightDetails.get(testIndex).getFriendlyName().equalsIgnoreCase(entity.getFriendlyName()));	
		entity = flightDetails.get(testIndex);

		// remove the trip flight first, then the associated flight details
		// ToDo: move this into the remove method?
		flightDetailMgr.removeFlightDetail(entity.getId());
		flights = flightSvcDetailMgr.removeFlightServiceDetail(entity.getRelatedFlight().getId());
		if ((null != flightDetailMgr.findFlightDetail(entity.getId())) ||
				expectedNumber != flightDetailMgr.findAllFlightDetails().size())
		{
			System.out.println("Removing an underlying flight did not remove the trip detail");
		}		

		System.out.println("Done with CRUD method test");
	}	
}

