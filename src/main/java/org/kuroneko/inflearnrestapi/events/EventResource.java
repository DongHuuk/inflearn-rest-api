package org.kuroneko.inflearnrestapi.events;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

@NoArgsConstructor
public class EventResource extends EntityModel<Event> {
    public EventResource(Event event, Link... links) {
        super(event, links);
        add(WebMvcLinkBuilder.linkTo(EventController.class).slash(event.getId()).withSelfRel());
    }
}
