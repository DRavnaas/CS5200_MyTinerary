package myTinerary.dataAccess;

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.persistence.*;

import myTinerary.entities.*;

// Annotated class providing CRUD resource handling for TripNotificationList
@Path("/tripnotificationlist")
public class TripNotificationListDao {

	private static String findAllQueryName = "findAllTripNotificationLists";
	private static String findByTripList = "findNotificationsByTripList";

	private EntityManagerFactory factory = Persistence.createEntityManagerFactory("MyTinerary");

	// GET on http://<server>:<port>/<vdir>/<servlet mapping>/tripnotificationlist/<tripNotificationListId>
	// for example:  http://localhost:8080/MyTinerary/api/tripnotificationlist/1
	// findTripNotificationList - find a tripNotificationList with the given id (specified via url path parameter)
	//  Returns either the JSON representing the tripNotificationList or null if the tripNotificationList wasn't found
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public TripNotificationList findTripNotificationList(@PathParam("id") int tripNotificationListId) {
		TripNotificationList tripNotificationList = null;
		EntityManager em = null;
		try {
			em = factory.createEntityManager();

			em.getTransaction().begin();

			tripNotificationList = em.find(TripNotificationList.class, tripNotificationListId);

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
		return tripNotificationList;
	}

	// GET on http://<server>:<port>/<vdir>/<servlet mapping>/tripnotificationlist/
	// for example:  http://localhost:8080/MyTinerary/api/tripnotificationlist/
	// findAllTripNotificationLists - return all tripNotificationList
	//  Returns either the JSON representing all currently persisted tripNotificationList,
	//  or JSON representing an empty list if no tripNotificationList are defined
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TripNotificationList> findAllTripNotificationLists() {
		List<TripNotificationList> tripNotificationList = new ArrayList<TripNotificationList>();
		EntityManager em = null;
		try {
			em = factory.createEntityManager();

			em.getTransaction().begin();

			TypedQuery<TripNotificationList> query = em.createNamedQuery(findAllQueryName, TripNotificationList.class);
			tripNotificationList = query.getResultList();

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
		return tripNotificationList;
	}
	
	// Get the notifications for a shared trip
	public List<TripNotification> findNotificationsForTrip(SharedTrip t)
	{
		return findNotificationsForTrip(t.getTrip());
	}
	
	// Get the flights for a trip (shared or subtrip)
	public List<TripNotification> findNotificationsForTrip(ShareableTrip t)
	{
		List<TripNotification> tripNotifications = new ArrayList<TripNotification>();
		TripNotificationList tl = t.getNotificationList();
		
		if (t != null) {
			EntityManager em = null;
			try	{
				em = factory.createEntityManager();

				em.getTransaction().begin();		
				TypedQuery<TripNotification> query = em.createNamedQuery(findByTripList, TripNotification.class);
				query.setParameter("list", tl);
				tripNotifications = query.getResultList();

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
		
		return tripNotifications;
	}
		

	// PUT on http://<server>:<port>/<vdir>/<servlet mapping>/tripnotificationlist/<tripNotificationListId>
	// for example:  http://localhost:8080/MyTinerary/api/tripnotificationlist/1
	// updateTripNotificationList - update a tripNotificationList
	//  Updates the tripNotificationList at the given id with the properties of the specified tripNotificationList.
	//  Note that the tripNotificationListId is used to find the tripNotificationList - not whatever id is in the
	//  specified tripNotificationList object.
	//  Returns the JSON representing all currently persisted tripNotificationList 
	//  (including the newly updated tripNotificationList).
	//  Note - the specified tripNotificationList id must correspond to a persisted tripNotificationList, or 
	//  this method will result in an error.
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)	
	public List<TripNotificationList> updateTripNotificationList(@PathParam("id") int tripNotificationListId, TripNotificationList tripNotificationList) {
		EntityManager em = null;
		try {
			em = factory.createEntityManager();

			em.getTransaction().begin();

			tripNotificationList.setId(tripNotificationListId);
			em.merge(tripNotificationList);

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
		return findAllTripNotificationLists();
	}

	// DELETE on http://<server>:<port>/<vdir>/<servlet mapping>/tripnotificationlist/<tripNotificationListId>
	// for example:  http://localhost:8080/MyTinerary/api/tripnotificationlist/1
	// removeTripNotificationList - remove a tripNotificationList
	//  Removes the tripNotificationList at the given id.
	//  Returns the JSON representing all currently persisted tripNotificationList 
	//  (with the just deleted tripNotificationList removed).
	//  This method can be called more than once on the same id; if
	//  the tripNotificationList doesn't exist, then there is no change made.
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TripNotificationList> removeTripNotificationList(@PathParam("id") int tripNotificationListId) {
		EntityManager em = null;

		// First, make sure list items are removed...
		TripNotificationListDao tripNotificationListMgr = new TripNotificationListDao();
		TripNotificationList list = tripNotificationListMgr.findTripNotificationList(tripNotificationListId);
		if (list != null)
		{
			list.setNotifications(new ArrayList<TripNotification>());
			tripNotificationListMgr.updateTripNotificationList(tripNotificationListId, list);

			try {
				em = factory.createEntityManager();

				TripNotificationList tripNotificationList = null;

				em.getTransaction().begin();

				tripNotificationList = em.find(TripNotificationList.class, tripNotificationListId);
				if (tripNotificationList != null)
				{
					em.remove(tripNotificationList);
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
		}

		return findAllTripNotificationLists();
	}

	// Create a notification for a specific trip,
	// Return new set of notifications for that specific trip
	public List<TripNotification> createTripNotificationInTrip(ShareableTrip trip, String notificationText)
	{
		if ((notificationText != null) && (trip != null))
		{
			TripNotificationList list = trip.getNotificationList();
			if (list == null)
			{
				// Create a list
				list = new TripNotificationList();
				list.setContainingTrip(trip);
				trip.setNotificationList(list);
				createTripNotificationList(list);
			}
			if (list != null)
			{
				List<TripNotification> notes = list.getNotifications();
				if (notes == null)
				{
					notes = new ArrayList<TripNotification>();						
				}
				
				TripNotification note = new TripNotification();
				note.setAddedDate(new Date());
				note.setText(notificationText);
				note.setNotificationList(list);
				notes.add(note);
				list.setNotifications(notes);
				
				new TripNotificationListDao().updateTripNotificationList(list.getId(), list);
			}
		}	
		
		return findNotificationsForTrip(trip);
	}
	
	// POST on http://<server>:<port>/<vdir>/<servlet mapping>/tripnotificationlist/
	// for example:  http://localhost:8080/MyTinerary/api/tripnotificationlist/
	// createTripNotificationList - create a tripNotificationList
	//  Creates a tripNotificationList with a new id set with the properties of the specified tripNotificationList.
	//  Note that the given tripNotificationList object's id is ignored and a new one is assigned.
	//  Returns the JSON representing all currently persisted tripNotificationList 
	//  (including the newly created tripNotificationList).
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<TripNotificationList> createTripNotificationList(TripNotificationList tripNotificationList) {
		EntityManager em = null;
		try {
			em = factory.createEntityManager();

			em.getTransaction().begin();

			em.persist(tripNotificationList);

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

		return findAllTripNotificationLists();

	}	

	// Main for testing purposes - test the DAO methods.
	// (Fiddler used to test the web service REST calls.)
	public static void main(String[] args) {		

		TripNotificationListDao tripNotificationListMgr = new TripNotificationListDao();

		// Find all shareable trips - we'll copy properties off one for our "test" trip
		ShareableTripDao tripMgr = new ShareableTripDao();
		List<ShareableTrip> trips = tripMgr.findAllShareableTrips();
		if (trips.size() == 0)
		{
			System.out.println("Expecting at least one shareable trip defined - rerun add and clean");
			return;
		}

		List<TripNotificationList> tripNotificationLists = tripNotificationListMgr.findAllTripNotificationLists();		

		// We expect at most one notification list for each trip - a one to one mapping
		if (trips.size() < tripNotificationLists.size())
		{
			System.out.println("Warning - mismatch in notification lists and shareable trips");
		}

		int expectedNumber = tripNotificationLists.size();

		// Create a shareable trip and add a notification list to it.		
		ShareableTrip trip = new ShareableTrip();
		trip.setStartDate(new Date());
		trip.setFriendlyName("a new trip");
		trip.setDestination(trips.get(0).getDestination());
		trip.setTripOwner(trips.get(0).getTripOwner());
		//AuthenticatedUser tripPlanner = trip.getTripOwner();

		TripNotificationList entity = new TripNotificationList();
		entity.setContainingTrip(trip);
		trip.setNotificationList(entity);
		trips = tripMgr.createShareableTrip(trip);

		// find our new trip, and the attached list...	
		Integer i = trips.size();
		ShareableTrip foundTrip = null;
		do {
			i--;
			foundTrip = trips.get(i);
		}
		while ((!foundTrip.getFriendlyName().equalsIgnoreCase(trip.getFriendlyName())) ||
				(!foundTrip.getDestination().equalsIgnoreCase(trip.getDestination())) ||
				//(foundTrip.getStartDate() != trip.getStartDate()) ||
				(foundTrip.getTripOwner().getId() != trip.getTripOwner().getId()));

		Integer newTripId = foundTrip.getId();
		Integer newListId = foundTrip.getNotificationList().getId();
		TripNotificationList foundTripList = foundTrip.getNotificationList();

		// Add a notification to the list
		TripNotification note = new TripNotification();
		note.setAddedDate(new Date());
		note.setNotificationList(foundTrip.getNotificationList());
		note.setText("This was added today by me");
		List<TripNotification> notes = new ArrayList<TripNotification>();
		notes.add(note);
		foundTripList.setNotifications(notes);
		List<TripNotificationList> foundTripLists = tripNotificationListMgr.updateTripNotificationList(newListId, foundTripList); 

		TripNotificationList list = tripNotificationListMgr.findTripNotificationList(newListId);
		List<TripNotification> newListItems = null;

		if ((foundTripLists == null) ||
				(foundTripLists.size() == 0) ||
				(list.getNotifications() == null) || 
				(list.getNotifications().size() != 1))
		{
			System.out.println("Warning, list does not have new item");		
		}
		else {

			newListItems = list.getNotifications();
			if ((newListItems.get(0).getNotificationList() != null) &&
					(newListItems.get(0).getNotificationList().getId() != newListId))
			{
				System.out.println("Notification list item doesn't point back to parent list id");
			}
		}		

		if ((newListItems != null) && (newListItems.size() > 0))
		{
			// Update an item
			newListItems.get(0).setText("this to do item is updated now");
			foundTripLists = tripNotificationListMgr.updateTripNotificationList(newListId, list);

			trip = tripMgr.findShareableTrip(newTripId);
			if ((trip == null) || (trip.getNotificationList() == null) || 
					(trip.getNotificationList().getNotifications() == null) ||
					(trip.getNotificationList().getNotifications().size() == 0) ||
					(!trip.getNotificationList().getNotifications().get(0).getText().equalsIgnoreCase(newListItems.get(0).getText())))
			{
				System.out.println("Warning - could not drill down from new list to new list item and updated text");
			}	
			
			// Add one more
			tripNotificationListMgr.createTripNotificationInTrip(trip, "this is a new note");
			List<TripNotification> tripNotes = tripNotificationListMgr.findNotificationsForTrip(trip);
			if (tripNotes.size() != newListItems.size() + 1)
			{
				System.out.println("Adding another note to a trip should increate the note count");
			}			
		}		

		//list = tripNotificationListMgr.findTripNotificationList(newListId);
		//list.setNotifications(new ArrayList<TripNotification>());
		//tripNotificationLists = tripNotificationListMgr.updateTripNotificationList(newListId, list);

		tripNotificationLists = tripNotificationListMgr.removeTripNotificationList(newListId);
		trips = tripMgr.removeShareableTrip(newTripId);

		// There's a one to one relationship of trips to notification lists.
		// We just removed the new trip, so the new list should be gone too
		if (tripNotificationLists.size() != expectedNumber)
		{
			System.out.println("Warning - after delete, size of trip list & notification list isn't back to original size");
		}

		System.out.println("Done with CRUD method test");
	}	
}

