package com.chinmay.caching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryAccountDto implements Serializable {
    private Long id;

    private Long balance;
}
