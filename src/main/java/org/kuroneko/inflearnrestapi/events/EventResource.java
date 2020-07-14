package org.kuroneko.inflearnrestapi.events;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import javax.validation.valueextraction.UnwrapByDefault;

@NoArgsConstructor
public class EventResource extends RepresentationModel {

    @JsonUnwrapped
    private Event event;

    public EventResource(Event event) {
        this.event = event;
        add(WebMvcLinkBuilder.linkTo(EventController.class).slash(event.getId()).withSelfRel());
    }

    public Event getEvent() {
        return event;
    }
}
