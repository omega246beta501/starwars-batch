package com.starwars.config;

import com.starwars.tasklet.HelloWorldTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
//@EnableBatchProcessing
public class BatchConfiguration {
    @Bean
    public Step helloWorldStep(StepBuilderFactory stepBuilderFactory, HelloWorldTasklet helloWorldTasklet) {
        return stepBuilderFactory
                .get("helloWorldStep")
                .tasklet(helloWorldTasklet)
                .build();
    }

    @Bean
    public Job job(JobBuilderFactory jobBuilderFactory, Step helloWorldStep) {
        return jobBuilderFactory
                .get("job")
                .incrementer(new RunIdIncrementer())
                .start(helloWorldStep)
                .build();
    }

}
