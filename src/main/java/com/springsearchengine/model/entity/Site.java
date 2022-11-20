package com.springsearchengine.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "site")
@NoArgsConstructor
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum")
    private Status status;
    @Column(name = "status_time")
    private LocalDateTime localDateTime;
    @Column(name = "last_error")
    private String lastError;
    private String url;
    private String name;
}
