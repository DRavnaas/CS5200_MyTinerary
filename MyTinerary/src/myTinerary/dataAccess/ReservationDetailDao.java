package myTinerary.dataAccess;

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.persistence.*;

import myTinerary.entities.*;

// Annotated class providing CRUD resource handling for ReservationDetail
@Path("/reservation")
public class ReservationDetailDao {

	private static String findAllQueryName = "findAllReservations";
	private static String findByTrip = "findReservationsByTrip";

	private EntityManagerFactory factory = Persistence.createEntityManagerFactory("MyTinerary");

	// GET on http://<server>:<port>/<vdir>/<servlet mapping>/reservation/<reservationDetailId>
	// for example:  http://localhost:8080/testjpa/api/reservation/1
	// findReservationDetail - find a reservationDetail with the given id (specified via url path parameter)
	//  Returns either the JSON representing the reservationDetail or null if the reservationDetail wasn't found
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public ReservationDetail findReservationDetail(@PathParam("id") int reservationDetailId) {
		ReservationDetail reservationDetail = null;
		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			reservationDetail = em.find(ReservationDetail.class, reservationDetailId);

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

		return reservationDetail;
	}

	// Get the reservations for a shared trip
	public List<ReservationDetail> findReservationDetailsForTrip(SharedTrip t)
	{
		List<ReservationDetail> reservations = findReservationsForTrip(t.getTrip());
		return reservations;
	}
	
	// Get the reservations for a trip (shared or subtrip)
	public List<ReservationDetail> findReservationsForTrip(TripBase t)
	{
		List<ReservationDetail> reservations = new ArrayList<ReservationDetail>();
		
		if (t != null) {
			EntityManager em = null;
			try	{
				em = factory.createEntityManager();

				em.getTransaction().begin();		
				TypedQuery<ReservationDetail> query = em.createNamedQuery(findByTrip, ReservationDetail.class);
				query.setParameter("trip", t);
				reservations = query.getResultList();

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
		
		return reservations;
	}

	
	// GET on http://<server>:<port>/<vdir>/<servlet mapping>/reservation/
	// for example:  http://localhost:8080/testjpa/api/reservation/
	// findAllReservationDetails - return all reservationDetails
	//  Returns either the JSON representing all currently persisted reservationDetails,
	//  or JSON representing an empty list if no reservationDetails are defined
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ReservationDetail> findAllReservationDetails() {
		List<ReservationDetail> reservationDetails = new ArrayList<ReservationDetail>();
		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			TypedQuery<ReservationDetail> query = em.createNamedQuery(findAllQueryName, ReservationDetail.class);
			reservationDetails = query.getResultList();

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

		return reservationDetails;
	}

	// PUT on http://<server>:<port>/<vdir>/<servlet mapping>/reservation/<reservationDetailId>
	// for example:  http://localhost:8080/testjpa/api/reservation/1
	// updateReservationDetail - update a reservationDetail
	//  Updates the reservationDetail at the given id with the properties of the specified reservationDetail.
	//  Note that the reservationDetailId is used to find the reservationDetail - not whatever id is in the
	//  specified reservationDetail object.
	//  Returns the JSON representing all currently persisted reservationDetails 
	//  (including the newly updated reservationDetail).
	//  Note - the specified reservationDetail id must correspond to a persisted reservationDetail, or 
	//  this method will result in an error.
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)	
	public List<ReservationDetail> updateReservationDetail(@PathParam("id") int reservationDetailId, ReservationDetail reservationDetail) {
		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			reservationDetail.setId(reservationDetailId);
			em.merge(reservationDetail);


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

		return findAllReservationDetails();
	}

	// DELETE on http://<server>:<port>/<vdir>/<servlet mapping>/reservation/<reservationDetailId>
	// for example:  http://localhost:8080/testjpa/api/reservation/1
	// removeReservationDetail - remove a reservationDetail
	//  Removes the reservationDetail at the given id.
	//  Returns the JSON representing all currently persisted reservationDetails 
	//  (with the just deleted reservationDetail removed).
	//  This method can be called more than once on the same id; if
	//  the reservationDetail doesn't exist, then there is no change made.
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ReservationDetail> removeReservationDetail(@PathParam("id") int reservationDetailId) {
		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			ReservationDetail reservationDetail = null;

			em.getTransaction().begin();

			reservationDetail = em.find(ReservationDetail.class, reservationDetailId);
			if (reservationDetail != null)
			{
				em.remove(reservationDetail);
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

		return findAllReservationDetails();
	}

	// POST on http://<server>:<port>/<vdir>/<servlet mapping>/reservation/
	// for example:  http://localhost:8080/testjpa/api/reservation/
	// createReservationDetail - create a reservationDetail
	//  Creates a reservationDetail with a new id set with the properties of the specified reservationDetail.
	//  Note that the given reservationDetail object's id is ignored and a new one is assigned.
	//  Returns the JSON representing all currently persisted reservationDetails 
	//  (including the newly created reservationDetail).
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<ReservationDetail> createReservationDetail(ReservationDetail reservationDetail) {
		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			em.persist(reservationDetail);

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

		return findAllReservationDetails();
	}	

	// Main for testing purposes - test the DAO methods.
	// (Fiddler used to test the web service REST calls.)
	// This test assumes two reservationDetails are in the db at the start 
	public static void main(String[] args) {		

		ReservationDetailDao reservationDetailMgr = new ReservationDetailDao();

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
		ReservationDetail entity = null;

		List<ReservationDetail> reservationDetails = reservationDetailMgr.findAllReservationDetails();

		int expectedNumber = reservationDetails.size();
		while (expectedNumber < 2) {
			// We need two reservationDetails for the testing - add two links.
			// We will not modify links that are already created (so we can test with data in the system)
			entity = new ReservationDetail();
			entity.setFriendlyName("reservation " + Integer.toString(reservationDetails.size()));
			entity.setNotes("notes for reservation" + Integer.toString(reservationDetails.size()));
			entity.setContainingTrip(trip);

			reservationDetails = reservationDetailMgr.createReservationDetail(entity);			
			expectedNumber += 1;
		}

		reservationDetails = reservationDetailMgr.findAllReservationDetails(); 
		if (reservationDetails.size() != expectedNumber) {
			System.out.println("Wrong number of reservationDetails found?");
			return;
		}		

		// Add a reservationDetail just like the first one.
		entity = reservationDetails.get(0);

		entity.setFriendlyName("added reservationDetail");
		entity.setStart(new Date().toString());
		reservationDetails = reservationDetailMgr.createReservationDetail(entity);
		if (reservationDetails.size() != expectedNumber + 1) {
			System.out.println("Wrong number of reservationDetails found - rerun add and clean?");
		}		

		// Find our new item...
		Integer testIndex = expectedNumber + 1;
		do 
		{
			testIndex --;
		}
		while (!reservationDetails.get(testIndex).getFriendlyName().equalsIgnoreCase(entity.getFriendlyName()));	

		// Get the reservationDetail we just added.
		entity = reservationDetailMgr.findReservationDetail(reservationDetails.get(testIndex).getId());
		if ((!reservationDetails.get(testIndex).getStart().equalsIgnoreCase(entity.getStart())) ||
				(!reservationDetails.get(testIndex).getNotes().equalsIgnoreCase(entity.getNotes())) ||
				(!reservationDetails.get(testIndex).getFriendlyName().equalsIgnoreCase(entity.getFriendlyName())))
		{
			System.out.println("Updated reservationDetail properties mismatch");
		}

		// Update the reservationDetail's properties
		entity.setFriendlyName("updated reservationDetail" + Integer.toString(entity.getId()));	    
		entity.setDuration(new Date().toString());
		reservationDetails = reservationDetailMgr.updateReservationDetail(entity.getId(), entity);
		testIndex = expectedNumber + 1;
		do 
		{
			testIndex --;
		}
		while (!reservationDetails.get(testIndex).getFriendlyName().equalsIgnoreCase(entity.getFriendlyName()));	

		if ((!reservationDetails.get(testIndex).getStart().equalsIgnoreCase(entity.getStart())) ||
				(!reservationDetails.get(testIndex).getDuration().equalsIgnoreCase(entity.getDuration())) ||		
				(!reservationDetails.get(testIndex).getNotes().equalsIgnoreCase(entity.getNotes())) ||
				(!reservationDetails.get(testIndex).getFriendlyName().equalsIgnoreCase(entity.getFriendlyName())))
		{
			System.out.println("Updated reservationDetail properties mismatch");
		}		

		// Remove the reservationDetail we just added
		Integer removeId = reservationDetails.get(testIndex).getId();	
		reservationDetails = reservationDetailMgr.removeReservationDetail(removeId);
		if (reservationDetails.size() != expectedNumber) {
			System.out.println("Wrong number of reservationDetails found - rerun add and clean?");
		}	

		// Remove the entity again (should still have 2 entities left, nothing removed)
		reservationDetails = reservationDetailMgr.removeReservationDetail(removeId);
		if (reservationDetails.size() != expectedNumber) {
			System.out.println("Wrong number of reservationDetails found - rerun add and clean?");
		}	

		System.out.println("Done with CRUD method test");
	}	
}
