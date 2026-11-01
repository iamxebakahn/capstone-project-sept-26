package dev.zeba.productcatalogservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(indexes = @Index(name = "idx_product_name",columnList = "name")) // in pprod indexes are written via sql queries not via hibernate
//even if am writing queries in sql i need to annotate here, coz it's going to serve as documentation to fellow developers
public class Product extends BaseEntity{
    //Double is not used in production for precision - only BigDecimal is used - for 0 error
    private String name;
    @Column(precision = 19,scale = 3)
    private BigDecimal price;
    private String description;
    // for images upload images in blob storage (drive for all files formats e.g. Google drive - which is not scaleable,
    // AWS S3 (solid state) is scalebale, URL of image of S3 bucket is stored in db
    private String imageUrl;
    @ManyToOne(cascade = CascadeType.ALL) //ALL = all CRUD operations
    //cascade: when you apply operation on parent, same applies to child automatically, eg google account for all its app: CRUD on parent applied auto to child
    //.save applies to child to when used for parent, can be written on any side
    @JoinColumn(name="category_id")
    private Category category; // CATEGORY is not an enum coz, its highly updateable and will req maybe millions of rows to be effected in db
    // Category is now a separate table, so that one is row effected in case of change and coz of its id, in other tables no changes are needed
    //Amazon gets their product tables data from vendors and vendors will have seller accounts to add details of products
    //API call to save data


    //added new column random, but when deleted it, it stayed in product table, coz hibernate was able to add new column but was unable to delete
    //changed random to random2: column name didnot change random to random2, instead added new column random2, JPA by default can add new things, but cannot
    //remove existing schema to avoid data loss (can't delete/modify anything)
    //Types of SQL commands: DDL commands - create, alter, drop : DML - insert, delete, update - DTL also part of it
    //ddl-auto: tells how hibernate should manage schema of data
    //ddl-auto can take below values:
    //create: creates fresh table on every restart of app: data loss: like in memory/non persistent, persists table till the time you do not rerun it (never used in prod: purely for testing, study etc)
    //create-drop: data remains/persists till app is running in intellij, worse than create,app stops: data is lost (never used in prod: purely for testing, study etc)
    //validate:hibernate will not manage schema, it will not fire DDL commands, developers need to create all this (can be used in prod) (hibernate validates schema) keeps code and schema data in sync
    // none : hibernate will not manage schema, it will not fire DDL commands, developers need to create all this (can be used in prod) (hibernate has nothing to do here)
    // never allow hibernate to manage prod db/schema
    //changing schema is rare ops (1x,2x,3x maybe in a year)
    //Flyway/Liquidbase manual sql is tracked here: sql script folder in backend has manual sql scripts, latest version of script is picked when code is deployed
    //our learning: using update
    //to debug in prod: we dont use sout,  storeable print statements are created: logs
    //5 levels of logs: info(default),  trace, want, debug, error
    //write logs: 1. inbuild class logger factory to get logger object 2. library slf4j to write logs
}
