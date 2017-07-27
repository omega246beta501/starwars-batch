package com.starwars.config;

import com.starwars.domain.People;
import com.starwars.listener.BatchListener;
import com.starwars.listener.StepListener;
import com.starwars.processor.PeopleProcessor;
import com.starwars.repository.PeopleRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableBatchProcessing
@EnableScheduling
public class Csv2XmlBatchConfiguration {

    @Autowired
    private PeopleRepository peopleRepository;

    @Autowired
    private BatchListener batchListener;

    @Autowired
    private StepListener stepListener;

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
        itemReader.setLinesToSkip(1);

        return itemReader;
    }

    @Bean
    public ItemProcessor<People, People> getPeopleProcessor() {
        return new PeopleProcessor();
    }

//    @Bean
//    public ItemWriter<People> getPeopleWriter() {
//        StaxEventItemWriter<People> itemWriter = new StaxEventItemWriter<>();
//
//        itemWriter.setResource(new FileSystemResource("src/main/resources/people.xml"));
//
//        itemWriter.setRootTagName("peoples");
//        itemWriter.setOverwriteOutput(true);
//
//        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
//        marshaller.setClassesToBeBound(People.class);
//        itemWriter.setMarshaller(marshaller);
//
//        return itemWriter;
//    }

    @Bean
    public ItemWriter<People> getPeopleWriter() {
        RepositoryItemWriter<People> itemWriter = new RepositoryItemWriter<>();

        itemWriter.setRepository(peopleRepository);
        itemWriter.setMethodName("save");

        return itemWriter;
    }

    @Bean
    public Step getCsvStep(StepBuilderFactory stepBuilderFactory, ItemWriter peopleWriter,
                           ItemProcessor peopleProcessor,
                           ItemReader peopleReader) {

        return stepBuilderFactory
                .get("csvStep")
                .chunk(10)
                .writer(peopleWriter)
                .processor(peopleProcessor)
                .reader(peopleReader)
                .listener(stepListener)
                .build();
    }

    @Bean
    public Job job(JobBuilderFactory jobBuilderFactory, Step csvStep) {
        return jobBuilderFactory
                .get("csv2xmlJob")
                .incrementer(new RunIdIncrementer())
                .listener(batchListener)
                .start(csvStep)
                .build();
    }
}
