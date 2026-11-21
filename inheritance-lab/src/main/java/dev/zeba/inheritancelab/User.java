package dev.zeba.inheritancelab;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {
    @Id
    @GeneratedValue//(strategy = GenerationType.IDENTITY)
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;
    //private int id;
    private String name;
    private String email;
}
