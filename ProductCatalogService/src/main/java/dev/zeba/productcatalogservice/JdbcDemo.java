package dev.zeba.productcatalogservice;

import java.sql.SQLException;

public class JdbcDemo {
    public static void main(String[] args) throws SQLException {
        //using Hibernate as ORM framework (famous ORM frameworks in market: hibernate, MyBatis, JOOQ)
        //ORM (Object Relational Mapping)framework and not JPA is library which consists of functions which can be used as wrappers over existing JDBC operations
        //it has simple java functions which programmer can use, which will work behind the scenes and do the same thing, i.e. write JDBC get connection
        //statement, create the statement, get the result, map the result to java object
        //findbyid(3) -> in hibernate
        //ORM framework contains functions/methods that we can use to do db ops. Benefit: we dont need to write JDBC code, we can call and use it per
        //convenience
        //if we switch ORM frameworks tomorrow: huge pain, function signature, i/p, o/p will change
        //abstraction: abstract classes or interface to solve issue, i.e. not depending on these concrete classes
        //common interface or a specification implemented by all ORMs. It doesnot contain any implementation. It only contains annotation/function name
        // : JPA. Actual implementation is given by ORM
        // Repo Layer talks to -> JPA behind scenese use-> ORM framework (hibernate/JOOQ/MyBatis) -> ORM frameworks talk to JDBC -> JDBC does db ops

        //Types of SQL commands: DDL commands - create, alter, drop : DML - insert, delete, update - DTL also part of it
        //ddl-auto: tells how hibernate should manage schema of data
    }
}
