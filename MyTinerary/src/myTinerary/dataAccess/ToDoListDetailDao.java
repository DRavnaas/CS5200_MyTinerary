package myTinerary.dataAccess;

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.persistence.*;

import myTinerary.entities.*;

// Annotated class providing CRUD resource handling for ToDoListDetail
@Path("/todolist")
public class ToDoListDetailDao {

	private static String findAllQueryName = "findAllToDoLists";
	private static String findByTrip = "findToDoListsByTrip";
	private static String findItemsByTripList = "findItemByTripList";

	private EntityManagerFactory factory = Persistence.createEntityManagerFactory("MyTinerary");

	// GET on http://<server>:<port>/<vdir>/<servlet mapping>/todolist/<toDoListDetailId>
	// for example:  http://localhost:8080/testjpa/api/todolist/1
	// findToDoListDetail - find a toDoListDetail with the given id (specified via url path parameter)
	//  Returns either the JSON representing the toDoListDetail or null if the toDoListDetail wasn't found
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public ToDoListDetail findToDoListDetail(@PathParam("id") int toDoListDetailId) {
		ToDoListDetail toDoListDetail = null;
		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			toDoListDetail = em.find(ToDoListDetail.class, toDoListDetailId);

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
		return toDoListDetail;
	}

	// GET on http://<server>:<port>/<vdir>/<servlet mapping>/todolist/
	// for example:  http://localhost:8080/testjpa/api/todolist/
	// findAllToDoListDetails - return all toDoListDetails
	//  Returns either the JSON representing all currently persisted toDoListDetails,
	//  or JSON representing an empty list if no toDoListDetails are defined
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ToDoListDetail> findAllToDoListDetails() {
		List<ToDoListDetail> toDoListDetails = new ArrayList<ToDoListDetail>();
		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			TypedQuery<ToDoListDetail> query = em.createNamedQuery(findAllQueryName, ToDoListDetail.class);
			toDoListDetails = query.getResultList();

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

		return toDoListDetails;
	}
	
	// Get the to do lists for a shared trip
	public List<ToDoListDetail> findToDoListsForTrip(SharedTrip t)
	{
		return findToDoListsForTrip(t.getTrip());
	}	

	// Get the todo lists for a trip (shared or subtrip)
	public List<ToDoListDetail> findToDoListsForTrip(TripBase t)
	{
		List<ToDoListDetail> toDoListDetails = new ArrayList<ToDoListDetail>();
		
		if (t != null) {
			EntityManager em = null;
			try	{
				em = factory.createEntityManager();

				em.getTransaction().begin();		
				TypedQuery<ToDoListDetail> query = em.createNamedQuery(findByTrip, ToDoListDetail.class);
				query.setParameter("trip", t);
				toDoListDetails = query.getResultList();

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
		
		return toDoListDetails;
	}	
	
	// Get the flights for a trip (shared or subtrip)
	public List<ToDoListItem> findToDoListItemsForTrip(ShareableTrip t)
	{
		List<ToDoListItem> tripItems = new ArrayList<ToDoListItem>();
		List<ToDoListDetail> lists = findToDoListsForTrip(t);
		
		// ToDo: we only have one list per trip, but could have more.		
		
		if ((t != null) && (lists != null) && (lists.size() > 0)){
			EntityManager em = null;
			ToDoListDetail tl = lists.get(0);
			try	{
				em = factory.createEntityManager();

				em.getTransaction().begin();		
				TypedQuery<ToDoListItem> query = em.createNamedQuery(findItemsByTripList, ToDoListItem.class);
				query.setParameter("list", tl);
				tripItems = query.getResultList();

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
		
		return tripItems;
	}	
	
	// PUT on http://<server>:<port>/<vdir>/<servlet mapping>/todolist/<toDoListDetailId>
	// for example:  http://localhost:8080/testjpa/api/todolist/1
	// updateToDoListDetail - update a toDoListDetail
	//  Updates the toDoListDetail at the given id with the properties of the specified toDoListDetail.
	//  Note that the toDoListDetailId is used to find the toDoListDetail - not whatever id is in the
	//  specified toDoListDetail object.
	//  Returns the JSON representing all currently persisted toDoListDetails 
	//  (including the newly updated toDoListDetail).
	//  Note - the specified toDoListDetail id must correspond to a persisted toDoListDetail, or 
	//  this method will result in an error.
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)	
	public List<ToDoListDetail> updateToDoListDetail(@PathParam("id") int toDoListDetailId, ToDoListDetail toDoListDetail) {
		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			toDoListDetail.setId(toDoListDetailId);
			em.merge(toDoListDetail);

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

		return findAllToDoListDetails();
	}

	// DELETE on http://<server>:<port>/<vdir>/<servlet mapping>/todolist/<toDoListDetailId>
	// for example:  http://localhost:8080/testjpa/api/todolist/1
	// removeToDoListDetail - remove a toDoListDetail
	//  Removes the toDoListDetail at the given id.
	//  Returns the JSON representing all currently persisted toDoListDetails 
	//  (with the just deleted toDoListDetail removed).
	//  This method can be called more than once on the same id; if
	//  the toDoListDetail doesn't exist, then there is no change made.
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ToDoListDetail> removeToDoListDetail(@PathParam("id") int toDoListDetailId) {
		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			ToDoListDetail toDoListDetail = null;

			em.getTransaction().begin();

			toDoListDetail = em.find(ToDoListDetail.class, toDoListDetailId);
			if (toDoListDetail != null)
			{
				em.remove(toDoListDetail);
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

		return findAllToDoListDetails();
	}
	
	// Create a todo list item for a specific trip,
	// Return new set of items for that specific trip
	public List<ToDoListItem> createToDoListItemInTrip(ShareableTrip trip, String itemText)
	{
		if ((itemText != null) && (trip != null))
		{
			// Get the to do list for the trip and add the item.
			List<ToDoListDetail> lists = findToDoListsForTrip(trip);
			ToDoListDetail list = null;
			
			if ((lists != null) && (lists.size() > 0))
			{
				// Limiting to just one to do list per trip
				list = lists.get(0);
			}
			if (list == null)
			{
				// Create a list
				list = new ToDoListDetail();
				list.setFriendlyName(trip.getFriendlyName());
				list.setContainingTrip(trip);
				
				createToDoListDetail(list);
			}
			if (list != null)
			{
				List<ToDoListItem> items = list.getListItems();
				if (items == null)
				{
					items = new ArrayList<ToDoListItem>();						
				}
				
				ToDoListItem item = new ToDoListItem();
				item.setItemDescription(itemText);
				item.setCompleted(false);
				item.setToDoList(list);				
				
				items.add(item);
				list.setListItems(items);
				
				updateToDoListDetail(list.getId(), list);
			}
		}	
		
		return findToDoListItemsForTrip(trip);
	}	

	// POST on http://<server>:<port>/<vdir>/<servlet mapping>/todolist/
	// for example:  http://localhost:8080/testjpa/api/todolist/
	// createToDoListDetail - create a toDoListDetail
	//  Creates a toDoListDetail with a new id set with the properties of the specified toDoListDetail.
	//  Note that the given toDoListDetail object's id is ignored and a new one is assigned.
	//  Returns the JSON representing all currently persisted toDoListDetails 
	//  (including the newly created toDoListDetail).
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<ToDoListDetail> createToDoListDetail(ToDoListDetail toDoListDetail) {
		EntityManager em = null;
		try	{
			em = factory.createEntityManager();

			em.getTransaction().begin();

			em.persist(toDoListDetail);

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

		return findAllToDoListDetails();

	}	

	// Main for testing purposes - test the DAO methods.
	// (Fiddler used to test the web service REST calls.)
	public static void main(String[] args) {		

		ToDoListDetailDao toDoListDetailMgr = new ToDoListDetailDao();

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
		ToDoListDetail entity = null;

		List<ToDoListDetail> toDoListDetails = toDoListDetailMgr.findAllToDoListDetails();		

		int expectedNumber = toDoListDetails.size();
		while (expectedNumber < 2) {
			// We need two toDoListDetails for the testing - add two links.
			// We will not modify links that are already created (so we can test with data in the system)
			entity = new ToDoListDetail();
			entity.setFriendlyName("to do list " + Integer.toString(toDoListDetails.size()));
			entity.setContainingTrip(trip);

			toDoListDetails = toDoListDetailMgr.createToDoListDetail(entity);			
			expectedNumber += 1;
		}

		toDoListDetails = toDoListDetailMgr.findAllToDoListDetails(); 
		if (toDoListDetails.size() != expectedNumber) {
			System.out.println("Wrong number of toDoListDetails found?");
			return;
		}		

		// Add a toDoListDetail just like the first one.
		entity = toDoListDetails.get(0);

		entity.setFriendlyName("Added toDoListDetail " + Integer.toString(entity.getId()));
		toDoListDetails = toDoListDetailMgr.createToDoListDetail(entity);
		if (toDoListDetails.size() != expectedNumber + 1) {
			System.out.println("Wrong number of toDoListDetails found - rerun add and clean?");
		}		

		// Find our new item...
		Integer testIndex = expectedNumber + 1;
		do 
		{
			testIndex --;
		}
		while (!toDoListDetails.get(testIndex).getFriendlyName().equalsIgnoreCase(entity.getFriendlyName()));	

		// Get the toDoListDetail we just added.
		entity = toDoListDetailMgr.findToDoListDetail(toDoListDetails.get(testIndex).getId());
		if ((!toDoListDetails.get(testIndex).getFriendlyName().equalsIgnoreCase(entity.getFriendlyName())))
		{
			System.out.println("Updated toDoListDetail properties mismatch");
		}

		// Add an item to the to-do list
		entity.setFriendlyName("Updated toDoListDetail with one item " + Integer.toString(entity.getId()));
		ToDoListItem item = new ToDoListItem();
		item.setItemDescription("I need to do this on the trip");
		item.setToDoList(entity);
		List<ToDoListItem> items = new ArrayList<ToDoListItem>();
		items.add(item);		
		entity.setListItems(items);

		toDoListDetails = toDoListDetailMgr.updateToDoListDetail(entity.getId(), entity);
		// Need to find our item again, the array can be in a different order for different calls
		testIndex = expectedNumber + 1;
		do 
		{
			testIndex --;
		}
		while (!toDoListDetails.get(testIndex).getFriendlyName().equalsIgnoreCase(entity.getFriendlyName()));

		List<ToDoListItem> storedItems = toDoListDetails.get(testIndex).getListItems();
		if ((storedItems.size() != items.size()) ||
				(storedItems.get(0).getCompleted() != item.getCompleted()) ||
				(storedItems.get(0).getToDoList().getId() != entity.getId()) ||
				(!storedItems.get(0).getItemDescription().equalsIgnoreCase(item.getItemDescription())) ||
				(!toDoListDetails.get(testIndex).getFriendlyName().equalsIgnoreCase(entity.getFriendlyName())))
		{
			System.out.println("Updated toDoListDetail properties mismatch");
		}		

		// Remove the list item we just added
		entity.setListItems(new ArrayList<ToDoListItem>());
		toDoListDetailMgr.updateToDoListDetail(entity.getId(), entity);
		entity = toDoListDetailMgr.findToDoListDetail(entity.getId());		
		if (entity.getListItems().size() != 0)
		{
			System.out.println("Updated toDoListDetail item list mismatch");			
		}

		// Remove the toDoListDetail we just added
		toDoListDetails = toDoListDetailMgr.removeToDoListDetail(entity.getId());
		if (toDoListDetails.size() != expectedNumber) {
			System.out.println("Wrong number of toDoListDetails found - rerun add and clean?");
		}	

		// Remove the entity again (should still have 2 entities left, nothing removed)
		toDoListDetails = toDoListDetailMgr.removeToDoListDetail(entity.getId());
		if (toDoListDetails.size() != expectedNumber) {
			System.out.println("Wrong number of toDoListDetails found - rerun add and clean?");
		}	

		System.out.println("Done with CRUD method test");
	}	
}

