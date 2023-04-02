package com.catchthemoment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users" , indexes = @Index(name = "usr_mail_index",columnList = "email,name"))
@NamedEntityGraph(name = "usr-entity-graph", attributeNodes = {
        @NamedAttributeNode("id"),
        @NamedAttributeNode("name"),
        @NamedAttributeNode("password") //entity graph for avoiding n+1 problem multiple select queries
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @NotNull
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "confirmation_reset_token", length = 20)
    private String confirmationResetToken;

    @Column(name = "reset_password_token", length = 20)
    private String resetPasswordToken;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user",orphanRemoval = true)
    private List<Album> albums;

}
