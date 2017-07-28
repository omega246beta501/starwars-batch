package com.starwars.reader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starwars.domain.Planet;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

@Component
@ConfigurationProperties(prefix = "swapi.planets")
@StepScope
@Slf4j
public class SwapiReader implements ItemReader<Planet> {

    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();

    private Queue<Planet> planets;

    @Getter
    @Setter
    private String url;

    @Override
    public Planet read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return getPlanets().poll();
    }

    private JsonNode callSwapiRest(String url) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

        ResponseEntity<String> forEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return objectMapper.readTree(forEntity.getBody());
    }

    private void processSwapiResult(JsonNode jsonNode) {
        JsonNode content = jsonNode.get("results");
        content.forEach(node -> {
            try {
                Planet planet = objectMapper.treeToValue(node, Planet.class);
                planets.add(planet);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

    private Queue<Planet> getPlanets() throws IOException{
        if(planets == null) {
            planets = new LinkedList<>();
            JsonNode jsonNode = callSwapiRest(url);
            int count = jsonNode.get("count").asInt();
            String next = jsonNode.get("next").asText();
            processSwapiResult(jsonNode);

            while(!"null".equalsIgnoreCase(next)) {
                log.info("Cargados {}/{}", planets.size(), count);
                jsonNode = callSwapiRest(next);
                processSwapiResult(jsonNode);
                next = jsonNode.get("next").asText();
            }
        }
        return planets;
    }

}
