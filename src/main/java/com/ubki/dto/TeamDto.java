package com.ubki.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeamDto {
    private String fio;
    private String birthDay;
    private String fileName;
}
