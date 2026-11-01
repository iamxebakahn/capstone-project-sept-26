package dev.zeba.productcatalogservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Category extends BaseEntity{

    @Column(unique = true,nullable = false) //for non duplicate rows
    private String name;
    private String description;
    @OneToMany(mappedBy = "category") //Added mapped by to avoid creating mappingdoe List<Product> products;
    //cascade: when you apply operation on parent, same applies to child automatically, eg google account for all its app: CRUD on parent applied auto to child
    //.save applies to child to when used for parent
    private List<Product> products;
}
