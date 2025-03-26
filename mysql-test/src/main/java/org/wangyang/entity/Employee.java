package org.wangyang.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Employee {
    @Id
    private Integer id;

    private Integer no;
}
