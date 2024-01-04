package org.example;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
class Pet {
    @Id
    private Long id;
    private String name;
    private Integer age;
    private String description;
}
