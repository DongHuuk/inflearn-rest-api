package org.kuroneko.inflearnrestapi.events;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.net.URI;

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
            return ResponseEntity.badRequest().body(errors);
        }

        eventValidator.validate(eventDTO, errors);

        if(errors.hasErrors()){
            return ResponseEntity.badRequest().body(errors);
        }

        Event event = modelMapper.map(eventDTO, Event.class);
        event.freeUpdate();
        event.offlineUpdate();
        Event newEvent = this.eventRepository.save(event);
        URI uri = linkTo(EventController.class).slash(newEvent.getId()).toUri();

        return ResponseEntity.created(uri).body(newEvent);
    }


}
