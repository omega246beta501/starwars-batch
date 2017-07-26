package com.starwars.config;

import com.starwars.domain.People;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
@EnableBatchProcessing
public class Csv2XmlBatchConfiguration {
    @Bean
    public ItemReader<People> peopleReader() {
        FlatFileItemReader<People> itemReader = new FlatFileItemReader<>();

        itemReader.setResource(new FileSystemResource("src/main/resources/people.csv"));

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames(new String[] {"name","birthYear","gender","height","mass","eyeColor","hairColor","skinColor"});

        BeanWrapperFieldSetMapper<People> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(People.class);

        DefaultLineMapper<People> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        itemReader.setLineMapper(lineMapper);

        return itemReader;
    }

    @Bean
    public ItemWriter<People> getPeopleWriter() {
        StaxEventItemWriter<People> itemWriter = new StaxEventItemWriter<>();

        itemWriter.setResource(new FileSystemResource("src/main/resources/people.xml"));

        itemWriter.setRootTagName("peoples");
        itemWriter.setOverwriteOutput(true);

        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(People.class);
        itemWriter.setMarshaller(marshaller);

        return itemWriter;
    }

    @Bean
    public Step getCsvStep(StepBuilderFactory stepBuilderFactory, ItemWriter peopleWriter, ItemReader peopleReader) {
        return stepBuilderFactory
                .get("csvStep")
                .chunk(10)
                .writer(peopleWriter)
                .reader(peopleReader)
                .build();
    }

    @Bean
    public Job job(JobBuilderFactory jobBuilderFactory, Step csvStep) {
        return jobBuilderFactory
                .get("csv2xmlJob")
                .incrementer(new RunIdIncrementer())
                .start(csvStep)
                .build();
    }
}