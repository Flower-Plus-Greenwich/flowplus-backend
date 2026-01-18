package com.greenwich.flowerplus.entity;

import jakarta.persistence.*;
import lombok.*;


/**
 * Role entity for authorization.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
@Entity
@Table(name = "roles")
public class Role extends BaseSoftDeleteEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Override
    public Long getId() {
        return this.id;
    }

    @Column(name = "name", length = 30, nullable = false, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "text", nullable = false)
    private String description;


}



