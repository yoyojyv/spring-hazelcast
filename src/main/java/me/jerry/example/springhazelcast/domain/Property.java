package me.jerry.example.springhazelcast.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Property implements Serializable {

    private Long id;
    private String name;
    private String grade;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
