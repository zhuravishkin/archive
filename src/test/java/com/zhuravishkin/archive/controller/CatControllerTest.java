package com.zhuravishkin.archive.controller;

import com.zhuravishkin.archive.configuration.ActuatorConfig;
import com.zhuravishkin.archive.model.Cat;
import com.zhuravishkin.archive.service.CatService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest
class CatControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private CatController controller;

    @MockBean
    private CatService service;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private ActuatorConfig actuatorConfig;

    @Test
    void getCats() throws Exception {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .pathSegment("message/get")
                .build()
                .encode();
        mockMvc.perform(get(uriComponents.toUri()))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"))
                .andReturn();
    }

    @Test
    void getCatsByTime() {
        List<Cat> cats = new ArrayList<>();
        Cat cat = new Cat(7L, "Tom", 31, 4, 30.5, LocalDateTime.parse("2019-06-18T19:15:01"));
        cats.add(cat);
        when(service.findCats("Tom", 31, LocalDateTime.parse("2019-06-18T19:15:01")))
                .thenReturn(cats);
        doNothing().when(controller).putToSftpServer(any());
        when(actuatorConfig.getSendMessage()).thenReturn(Counter.builder("").register(new SimpleMeterRegistry()));
        when(actuatorConfig.getReceivedMessage()).thenReturn(Counter.builder("").register(new SimpleMeterRegistry()));
        List<Cat> list = controller.postCats("spring-boot-exchange", "spring-boot-routing-key",
                new Cat(7L, "Tom", 31, 4, 30.5, LocalDateTime.parse("2019-06-18T19:15:01"))).getBody();
        assertNotNull(list);
        verify(service, times(1)).findCats(anyString(), anyInt(), any(LocalDateTime.class));
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void postCats() throws Exception {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .pathSegment("message/post")
                .queryParam("exchange", "spring-boot-exchange")
                .queryParam("key", "spring-boot-routing-key")
                .build()
                .encode();
        String requestBody = Files.readString(Paths.get("src/test/resources/requestBody.json"));
        doNothing().when(controller).putToSftpServer(any());
        when(actuatorConfig.getSendMessage()).thenReturn(Counter.builder("").register(new SimpleMeterRegistry()));
        when(actuatorConfig.getReceivedMessage()).thenReturn(Counter.builder("").register(new SimpleMeterRegistry()));
        mockMvc.perform(post(uriComponents.toUri()).contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"))
                .andReturn();
    }
}
