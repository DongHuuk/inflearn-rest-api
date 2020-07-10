package org.kuroneko.inflearnrestapi.events;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @Test
    public void bulider(){
        Event event = Event.builder()
                .name("Inflearn Spring REST APII")
                .description("REST API 학습")
                .build();
        assertThat(event).isNotNull();
    }

    @Test
    public void javaBean(){
        Event event = new Event();
        String eventName = "Event";
        String eventDescription = "Spring";

        event.setName(eventName);
        event.setDescription(eventDescription);

        assertThat(event.getName()).isEqualTo(eventName);
        assertThat(event.getDescription()).isEqualTo(eventDescription);

        assertEquals(event.getName(), eventName);
        assertEquals(event.getDescription(), eventDescription);
    }

}