package com.starwars.processor;


import com.starwars.domain.People;
import org.springframework.batch.item.ItemProcessor;

public class PeopleProcessor implements ItemProcessor<People, People> {
    @Override
    public People process(People people) throws Exception {
        if("n/a".equalsIgnoreCase(people.getGender())) {
            people.setGender("droid");
        }

        return people;
    }
}
