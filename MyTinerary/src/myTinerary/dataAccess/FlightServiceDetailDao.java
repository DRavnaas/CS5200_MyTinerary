package myTinerary.dataAccess;

import java.util.*;

import javax.persistence.*;

import myTinerary.entities.FlightDetail;
import myTinerary.entities.FlightServiceDetail;
import myTinerary.services.AirlineCode;
import myTinerary.services.AirportCode;
import myTinerary.services.FlightAware;

// Class that handles querying and persisting details for an actual flight
// Note that this is not exposed as a webservice, this is more a behind-the-scenes operation
public class FlightServiceDetailDao {

	private static String findAllQueryName = "findAllFlightServiceDetails";

	private EntityManagerFactory factory = Persistence.createEntityManagerFactory("MyTinerary");

	// findFlightServiceDetail - find a flightServiceDetail with the given id 
	//  Returns either the flightServiceDetail or null if the flightServiceDetail wasn't found
	public FlightServiceDetail findFlightServiceDetail(String flightServiceDetailId) {
		FlightServiceDetail flightServiceDetail = null;
		EntityManager em = null;
		try {	
			em = factory.createEntityManager();

			em.getTransaction().begin();

			flightServiceDetail = em.find(FlightServiceDetail.class, flightServiceDetailId);

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


		return flightServiceDetail;
	}

	// findAllFlightServiceDetails - return all flightServiceDetails
	//  Returns either all currently persisted flightServiceDetails,
	//  or an empty list if no flightServiceDetails are stored
	public List<FlightServiceDetail> findAllFlightServiceDetails() {
		List<FlightServiceDetail> flightServiceDetails = new ArrayList<FlightServiceDetail>();
		EntityManager em = null;
		try {	
			em = factory.createEntityManager();

			em.getTransaction().begin();

			TypedQuery<FlightServiceDetail> query = em.createNamedQuery(findAllQueryName, FlightServiceDetail.class);
			flightServiceDetails = query.getResultList();

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


		return flightServiceDetails;
	}

	// upsertFlightServiceDetail - insert/update a flightServiceDetail
	//  Updates the flightServiceDetail at the given id with the properties of the specified flightServiceDetail.
	//  Note that the flightServiceDetailId is used to find the flightServiceDetail - not whatever id is in the
	//  specified flightServiceDetail object.
	//  Returns the JSON representing all currently persisted flightServiceDetails 
	//  (including the newly updated flightServiceDetail).
	//  Note - the specified flightServiceDetail id must correspond to a persisted flightServiceDetail, or 
	//  this method will result in an error.
	public List<FlightServiceDetail> upsertFlightServiceDetail(FlightServiceDetail flightServiceDetail) {
		EntityManager em = null;
		try {	
			em = factory.createEntityManager();

			em.getTransaction().begin();

			em.merge(flightServiceDetail);

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

		return findAllFlightServiceDetails();

	}

	// removeFlightServiceDetail - remove a flightServiceDetail
	//  Removes the flightServiceDetail at the given id.
	//  Returns all currently persisted flightServiceDetails 
	//  (with the just deleted flightServiceDetail removed).
	//  This method can be called more than once on the same id; if
	//  the flightServiceDetail doesn't exist, then there is no change made.
	protected List<FlightServiceDetail> removeFlightServiceDetail(String flightServiceDetailId) {

		// ToDo - remove any associated trip flight records?

		EntityManager em = null;
		try {	
			em = factory.createEntityManager();	

			em.getTransaction().begin();

			FlightServiceDetail flight = em.find(FlightServiceDetail.class, flightServiceDetailId);

			if (flight != null)
			{
				em.remove(flight); // remove flight service record
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

		return findAllFlightServiceDetails();

	}

	// Query for and (if found) persist the enhanced details for a given yelp link.
	// Check for enhanced details from yelp, and store them if found for the given link. 
	// Returns true if enhanced details are stored for the input link.
	// If an error occurs (bad link, no results, yelp is down, whatever)
	// then no enhanced details will be stored.
	// The return code indicates if details are stored or not.
	public static FlightServiceDetail fillInFlightDetails(AirlineCode airlineCode, Integer flightNo, Date departureDate, Boolean forceServiceQuery)
	{		
		FlightServiceDetail details = null;

		// Check if inputs look valid
		if (flightNo > 0)
		{
			Calendar cal = Calendar.getInstance();
			
			// Maybe we don't have to query it - is it already in the system?
			details = new FlightServiceDetailDao().findFlightServiceDetail(new FlightServiceDetail(airlineCode, flightNo, departureDate).getId());

			// Did we find it in the system? (and did the user say we can use the persisted value if it's close?)
			if ((details != null) && !forceServiceQuery)
			{
				Date now = new Date();
				cal.setTime(departureDate);	
				cal.set(Calendar.HOUR, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				Date today = cal.getTime();				
				
				// Is the departure in the past - leave alone if so (old flight)
				if (details.getDepartureTime().getTime() < today.getTime())
				{	
					return details;
				}
				else {
					// Finally, have we looked for this info recently (ie: in the last day?)
					// ToDo: maybe check less often when the flight is a ways off, but check more often when it's within 24 hours (ie: delays)
					Date recentTime = new Date(now.getTime() - 24L * 60L * 60L * 1000L);

					if ((details.getLastRefreshed() != null) && (details.getLastRefreshed().getTime() >= recentTime.getTime()))
					{
						// Return the current persisted info for this flight.
						return details;
					}
				}
			}
		
			// Strip off any time - we only care about the date.
			cal.setTime(departureDate);	
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Date justTheDate = cal.getTime();
						
			//	Get the details for a given airline fight on the input date 
			details = FlightAware.queryScheduledFlightDetails(airlineCode, flightNo, justTheDate);		

			// Were we able to fill in our entity from the flight service response?
			if (details != null)
			{
				// Upsert the details into our database.
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

			}
		}

		return details;	
	}


	// Main for testing purposes - test the DAO methods.
	// (Fiddler used to test the web service REST calls.)
	// This test assumes two flightServiceDetails are in the db at the start 
	public static void main(String[] args) {		

		FlightServiceDetailDao flightServiceDetailMgr = new FlightServiceDetailDao();


		// test that a bogus flight doesn't return anything
		FlightServiceDetail entity = null;
		System.out.println("Testing query for bogus flight...");
		entity = fillInFlightDetails(AirlineCode.ASA, 99999, new Date(), true);
		if (entity != null)
		{
			System.out.println("Details returned for bogus flight?");
		}
		
		List<FlightServiceDetail> flightServiceDetails = flightServiceDetailMgr.findAllFlightServiceDetails();
		int expectedNumber = flightServiceDetails.size();
		if (expectedNumber < 1) {
			entity = fillInFlightDetails(AirlineCode.ASA, 320, new Date(), false);

			expectedNumber ++;
		}
		
		flightServiceDetails = flightServiceDetailMgr.findAllFlightServiceDetails(); 
		if (flightServiceDetails.size() != expectedNumber) {
			System.out.println("Wrong number of flightServiceDetails found?");
			return;
		}		

		// Add a new flightServiceDetail just like the first one, but different airline.
		// Note that we aren't querying the service for this dummy flight.
		entity = new FlightServiceDetail(AirlineCode.UAL, flightServiceDetails.get(0).getFlightNumber(), flightServiceDetails.get(0).getDepartureTime());
		entity.setArrivalCity(flightServiceDetails.get(0).getArrivalCity());
		entity.setArrivalTime(flightServiceDetails.get(0).getArrivalTime());
		entity.setDepartureCity(flightServiceDetails.get(0).getDepartureCity());
		entity.setLastRefreshed(new Date());
		
		entity.setTripFlights(new ArrayList<FlightDetail>());
		flightServiceDetails = flightServiceDetailMgr.upsertFlightServiceDetail(entity);
		if (flightServiceDetails.size() != expectedNumber + 1) {
			System.out.println("Wrong number of flightServiceDetails found - rerun add and clean?");
		}		


		// Get the flightServiceDetail we just added (we know the key, we don't have to search for it)
		FlightServiceDetail queriedEntity = flightServiceDetailMgr.findFlightServiceDetail(entity.getId());
		if ((queriedEntity.getAirlineCode() != entity.getAirlineCode()) ||
				(queriedEntity.getArrivalCity() != entity.getArrivalCity()) ||				
				(queriedEntity.getDepartureCity() != entity.getDepartureCity()) ||
				(queriedEntity.getArrivalTime() != entity.getArrivalTime()) ||
				(queriedEntity.getDepartureTime() != entity.getDepartureTime()) ||
				(queriedEntity.getFlightNumber() != entity.getFlightNumber()))
		{
			System.out.println("Updated flightServiceDetail properties mismatch");
		}

		// Update the flightServiceDetail's properties
		entity.setArrivalCity(AirportCode.LAX);			

		flightServiceDetails = flightServiceDetailMgr.upsertFlightServiceDetail(entity);
		Integer testIndex = expectedNumber + 1;
		do 
		{
			testIndex --;
		}
		while (flightServiceDetails.get(testIndex).getId() != entity.getId());

		if ((flightServiceDetails.get(testIndex).getAirlineCode() != entity.getAirlineCode()) ||
				(flightServiceDetails.get(testIndex).getArrivalCity() != entity.getArrivalCity()) ||				
				(flightServiceDetails.get(testIndex).getDepartureCity() != entity.getDepartureCity()) ||
				(flightServiceDetails.get(testIndex).getArrivalTime() != entity.getArrivalTime()) ||
				(flightServiceDetails.get(testIndex).getDepartureTime() != entity.getDepartureTime()) ||
				(flightServiceDetails.get(testIndex).getFlightNumber() != entity.getFlightNumber()))	
		{
			System.out.println("Updated flightServiceDetail properties mismatch");
		}		

		// Remove the flightServiceDetail we just added

		flightServiceDetails = flightServiceDetailMgr.removeFlightServiceDetail(entity.getId());
		if (flightServiceDetails.size() != expectedNumber) {
			System.out.println("Wrong number of flightServiceDetails found - rerun add and clean?");
		}	

		// Remove the entity again (should still have 2 entities left, nothing removed)
		flightServiceDetails = flightServiceDetailMgr.removeFlightServiceDetail(entity.getId());
		if (flightServiceDetails.size() != expectedNumber) {
			System.out.println("Wrong number of flightServiceDetails found - rerun add and clean?");
		}	

		System.out.println("Done with CRUD method test");
	}	
}

