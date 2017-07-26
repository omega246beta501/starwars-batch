package com.starwars.domain;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "people")
public class People {
    private String name;
    private String birthYear;
    private String gender;
    private String height;
    private String mass;
    private String eyeColor;
    private String hairColor;
    private String skinColor;
}
