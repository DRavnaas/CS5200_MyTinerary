package myTinerary.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

import org.eclipse.persistence.annotations.CascadeOnDelete;

// Represents a to-do list in a trip.  A to-do list is a container for to-do list items.
@Entity
@Table(name="ToDoListDetail")
@DiscriminatorValue("ToDoListDetail")
@NamedQueries(value = { @NamedQuery( 
 		name = "findAllToDoLists", 
 		query = "SELECT td FROM ToDoListDetail td"),
 		@NamedQuery(
 				name="findToDoListsByTrip",
 				query = "SELECT td FROM ToDoListDetail td WHERE td.containingTrip = :trip")})
public class ToDoListDetail extends TripDetail implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// References list items belonging to this to-do list
	@OneToMany(mappedBy="toDoList", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER) 
	@CascadeOnDelete
	private List<ToDoListItem> listItems;
	
	public ToDoListDetail() {
		super();
	}

	public List<ToDoListItem> getListItems() {
		return listItems;
	}
	public void setListItems(List<ToDoListItem> listItems) {
		this.listItems = listItems;
	}
}
