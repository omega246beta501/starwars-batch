package com.starwars.config;

import com.starwars.domain.Planet;
import com.starwars.listener.SwapiListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableBatchProcessing
public class SwapiConfiguration {

    @Bean
    public ItemWriter<Planet> planetWriter(){
        FlatFileItemWriter<Planet> itemWriter = new FlatFileItemWriter<>();

        itemWriter.setResource(new FileSystemResource("src/main/resources/planets.csv"));

        DelimitedLineAggregator<Planet> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");

        BeanWrapperFieldExtractor<Planet> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[] {"name", "rotationPeriod", "orbitalPeriod", "diameter", "climate", "gravity", "terrain", "surfaceWater", "population"});

        lineAggregator.setFieldExtractor(extractor);
        itemWriter.setLineAggregator(lineAggregator);

        return itemWriter;
    }

    @Bean
    public Step swapiStep(StepBuilderFactory stepBuilderFactory, ItemWriter planetWriter, ItemReader swapiReader,
                          SwapiListener swapiListener) {

        return stepBuilderFactory
                .get("swapiStep")
                .chunk(10)
                .listener(swapiListener)
                .writer(planetWriter)
                .reader(swapiReader)
                .build();
    }

    @Bean
    public Job swapiJob(JobBuilderFactory jobBuilderFactory, Step swapiStep) {
        return jobBuilderFactory
                .get("swapiJob")
                .incrementer(new RunIdIncrementer())
                .start(swapiStep)
                .build();
    }
}
