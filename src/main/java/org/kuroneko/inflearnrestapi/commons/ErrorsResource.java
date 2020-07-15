package org.kuroneko.inflearnrestapi.commons;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.NoArgsConstructor;
import org.kuroneko.inflearnrestapi.events.Event;
import org.kuroneko.inflearnrestapi.events.EventController;
import org.kuroneko.inflearnrestapi.index.IndexController;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.validation.Errors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@NoArgsConstructor
public class ErrorsResource extends RepresentationModel {

    private Errors errors;

    public ErrorsResource(Errors errors) {
        this.errors = errors;
        add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
    }

    public Errors getErrors() {
        return errors;
    }
}
