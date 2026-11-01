package dev.zeba.productcatalogservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GroqPitchResponseDto {
    private List<Choice> choices;
}
