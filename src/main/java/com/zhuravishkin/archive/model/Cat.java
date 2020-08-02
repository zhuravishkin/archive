package com.zhuravishkin.archive.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "springboottable")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Cat {
    @Id
    @SequenceGenerator(name = "jpaSequenceGenerator", sequenceName = "jpa_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "jpaSequenceGenerator")
    @JsonProperty
    @Column
    private Long id;

    @JsonProperty
    @Column
    private String name;

    @JsonProperty
    @Column
    private Integer age;

    @JsonProperty
    @Column
    private Integer weight;

    @JsonProperty
    @Column
    private Double height;

    @JsonProperty("date_time")
    @Column
    private LocalDateTime dateTime;
}
