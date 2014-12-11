package myTinerary.entities;

import java.io.Serializable;

import javax.persistence.*;

import org.codehaus.jackson.annotate.JsonIgnore;

@Entity
@Table(name="ToDoListItem")
@NamedQueries(value = { @NamedQuery( 
 		name = "findAllToDoListItem", 
 		query = "SELECT li FROM ToDoListItem li"),
 		@NamedQuery(
 				name="findItemByTripList",
 				query = "SELECT item FROM ToDoListItem item WHERE item.toDoList = :list")})
public class ToDoListItem implements Serializable {

	// Primary key field
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;		
	
	@Column(name = "completed", nullable=false)
	private Boolean completed = false;
	
	@Column(name = "itemDescription", nullable=false)
	private String itemDescription;
	
	@ManyToOne(optional=false)
	@JoinColumn(name="listId", nullable=false)
	@JsonIgnore
	private ToDoListDetail toDoList;
	
	private static final long serialVersionUID = 1L;
	
	public ToDoListItem() {
		super();
	}

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}

	public Boolean getCompleted() {
		return completed;
	}
	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}

	public String getItemDescription() {
		return itemDescription;
	}
	public void setItemDescription(String itemDescription) {
		this.itemDescription = itemDescription;
	}

	public ToDoListDetail getToDoList() {
		return toDoList;
	}
	public void setToDoList(ToDoListDetail toDoList) {
		this.toDoList = toDoList;
	}
}
