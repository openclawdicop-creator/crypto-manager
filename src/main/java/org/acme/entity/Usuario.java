package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.mindrot.jbcrypt.BCrypt;

@Entity
@Table(name = "usuario")
public class Usuario extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(unique = true, nullable = false, length = 50)
    public String username;

    @Column(nullable = false)
    public String password;

    @Column(nullable = false, length = 100)
    public String email;

    @Column(nullable = false)
    public boolean ativo;

    public static Usuario findByUsername(String username) {
        return find("username", username).firstResult();
    }

    public boolean checkPassword(String plainPassword) {
        return BCrypt.checkpw(plainPassword, this.password);
    }

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }
}
