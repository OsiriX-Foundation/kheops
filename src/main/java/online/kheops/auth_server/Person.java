package online.kheops.auth_server;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

//import org.hibernate.annotations.Entity;
//import org.hibernate.annotations.Table;
//import org.hibernate.annotations.CollectionId;
//import org.hibernate.annotations.Column;



@Entity
@Table(name="person")
public class Person {
    @Id
    @Column(name="id")
    private int id;
    @Column(name="name")
    private String name;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
