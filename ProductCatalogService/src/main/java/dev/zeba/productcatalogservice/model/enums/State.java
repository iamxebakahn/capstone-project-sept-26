package dev.zeba.productcatalogservice.model.enums;

public enum State {
    ACTIVE, //0
    INACTIVE //1
    //By default JPA stores enums as ordinals/numbers; if a record is deleted - the ordinal is 0 in db
    //Problem: if it is stored in numbers: not very readable, if enum's order is changed in db, no. can mean something else later
    //benefit: less storage
    //in prod store them as strings
}
