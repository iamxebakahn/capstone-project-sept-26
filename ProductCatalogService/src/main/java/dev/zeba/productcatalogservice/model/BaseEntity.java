package dev.zeba.productcatalogservice.model;

import dev.zeba.productcatalogservice.model.enums.State;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
//Tell JPA not to create separate table but add them as columns in child class
@MappedSuperclass
public abstract class BaseEntity { //Abstract so its objects can't be instantiated standalone, and made purely for code reuse purpose
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY ) // to make primary key autoincrement (1,2,3,4..) we use GenerationType.Identity
    private Long id;
    private Date createdAt;
    private Date lastUpdatedAt;
    @Enumerated(value = EnumType.STRING)
    private State state; //ACTIVE, INACTIVE : for soft deletes and we dont do hard deletes,from db just mark a row as inactive
    //instead of permanently removing the row from db
}
