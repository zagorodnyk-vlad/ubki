package com.ubki.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(indexes = {@Index(name = "fio_birthDay_unique_idx", columnList = "fio, birthday", unique = true)})

public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fio;
    private String birthday;

    public Team() {
    }

    public Team(String fio, String birthday) {
        this.fio = fio;
        this.birthday = birthday;
    }
}
