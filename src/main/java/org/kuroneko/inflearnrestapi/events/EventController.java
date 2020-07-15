package org.kuroneko.inflearnrestapi.events;

import org.kuroneko.inflearnrestapi.commons.ErrorsResource;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping(value = "/api/events", produces = "application/hal+json;charset=UTF-8")
public class EventController {

    @Autowired private EventRepository eventRepository;
    @Autowired private ModelMapper modelMapper;
    @Autowired private EventValidator eventValidator;

    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDTO eventDTO, Errors errors){
        if(errors.hasErrors()){
            return badRequest(errors);
        }

        eventValidator.validate(eventDTO, errors);

        if(errors.hasErrors()){
            return badRequest(errors);
        }

        Event event = modelMapper.map(eventDTO, Event.class);
        event.freeUpdate();
        event.offlineUpdate();
        Event newEvent = this.eventRepository.save(event);
        WebMvcLinkBuilder selfLink = linkTo(EventController.class).slash(newEvent.getId());
        URI uri = selfLink.toUri();

        EventResource eventResource = new EventResource(event);
        eventResource.add(linkTo(EventController.class).withRel("query-events"));
        eventResource.add(selfLink.withRel("update-event"));
        eventResource.add(new Link("/docs/index.html#resources-events-create").withRel("profile"));

        return ResponseEntity.created(uri).body(eventResource);
    }

    private ResponseEntity<ErrorsResource> badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }

    @GetMapping
    public ResponseEntity queryEvents(Pageable pageable, PagedResourcesAssembler<Event> assembler) {
        Page<Event> page = this.eventRepository.findAll(pageable);
        var pagedModel = assembler.toModel(page, e -> new EventResource(e));

        return ResponseEntity.ok().body(pagedModel);
    }


}
