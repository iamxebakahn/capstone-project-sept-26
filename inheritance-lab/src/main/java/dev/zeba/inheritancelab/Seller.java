package dev.zeba.inheritancelab;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Seller extends User{
    private String gstNo;
}
