package com.springsearchengine.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "field")
@NoArgsConstructor
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;
    private String selector;
    private double weight;

    public Field(String name, String selector, double weight) {
        this.name = name;
        this.selector = selector;
        this.weight = weight;
    }

}
