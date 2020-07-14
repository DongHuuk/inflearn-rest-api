package org.kuroneko.inflearnrestapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
//@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@AutoConfigureRestDocs
public class EventControllerTest {

    @Autowired
    MockMvc mockMvc; //dispatcherServlet를 만들어서 처리함 Webserver는 띄우지 않아서 비교적 빠르다.
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    EventRepository eventRepository;

    @Test
    @DisplayName("Event 생성 성공")
    public void createEvent_Success() throws Exception {
        EventDTO event = EventDTO.builder()
                    .name("한글테스트")
                    .description("REST API Development with Spring")
                    .beginEnrollmentDateTime(LocalDateTime.of(2020, 7, 9, 16, 4))
                    .closeEnrollmentDateTime(LocalDateTime.of(2020, 7, 10, 16, 4))
                    .beginEventDateTime(LocalDateTime.of(2020, 7, 11, 16, 4))
                    .endEventDateTime(LocalDateTime.of(2020, 7, 12, 16, 4))
                    .basePrice(100)
                    .maxPrice(200)
                    .limitOfEnrollment(100)
                    .location("강남역 D2 스타텁 팩토리")
                    .build();

        mockMvc.perform(post("/api/events/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/hal+json;charset=UTF-8"))
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists())
                .andDo(document("create-event",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-events").description("link to query-events"),
                                linkWithRel("update-event").description("link to update-event")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept request header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("contentType request header")
                        ),
                        requestFields(
                                fieldWithPath("name").description("name of new event request"),
                                fieldWithPath("description").description("description of new event request"),
                                fieldWithPath("beginEnrollmentDateTime").description("beginEnrollmentDateTime of new event request"),
                                fieldWithPath("closeEnrollmentDateTime").description("closeEnrollmentDateTime of new event request"),
                                fieldWithPath("beginEventDateTime").description("beginEventDateTime of new event request"),
                                fieldWithPath("endEventDateTime").description("endEventDateTime of new event request"),
                                fieldWithPath("location").description("location of new event request"),
                                fieldWithPath("basePrice").description("basePrice of new event request"),
                                fieldWithPath("maxPrice").description("maxPrice of new event request"),
                                fieldWithPath("limitOfEnrollment").description("limitOfEnrollment of new event request")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("location response header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("contentType response header")
                        ),
                        responseFields(
                                fieldWithPath("id").description("identifier of new event response"),
                                fieldWithPath("name").description("name of new event response"),
                                fieldWithPath("description").description("description of new event response"),
                                fieldWithPath("beginEnrollmentDateTime").description("beginEnrollmentDateTime of new event response"),
                                fieldWithPath("closeEnrollmentDateTime").description("closeEnrollmentDateTime of new event response"),
                                fieldWithPath("beginEventDateTime").description("beginEventDateTime of new event response"),
                                fieldWithPath("endEventDateTime").description("endEventDateTime of new event response"),
                                fieldWithPath("location").description("location of new event response"),
                                fieldWithPath("basePrice").description("basePrice of new event response"),
                                fieldWithPath("maxPrice").description("maxPrice of new event response"),
                                fieldWithPath("limitOfEnrollment").description("limitOfEnrollment of new event response"),
                                fieldWithPath("offline").description("offline of new event response"),
                                fieldWithPath("free").description("free of new event response"),
                                fieldWithPath("eventStatus").description("eventStatus of new event response"),
                                fieldWithPath("_links.self.href").description("self of new event response"),
                                fieldWithPath("_links.query-events.href").description("query-events of new event response"),
                                fieldWithPath("_links.update-event.href").description("update-event of new event response")
                        )
                    )
                );
    }

    @Test
    public void updateFree(){
        //Given
        Event event = Event.builder()
                .basePrice(0)
                .maxPrice(0)
                .build();
        //When
        event.freeUpdate();
        //That
        assertTrue(event.isFree());

        //Given
        event = Event.builder()
                .basePrice(100)
                .maxPrice(0)
                .build();
        //When
        event.freeUpdate();
        //That
        assertFalse(event.isFree());

        //Given
        event = Event.builder()
                .basePrice(0)
                .maxPrice(100)
                .build();
        //When
        event.freeUpdate();
        //That
        assertFalse(event.isFree());
    }

    @Test
    public void updateLocation(){
        //Given
        Event event = Event.builder()
                .location("평택역 11시")
                .build();
        //When
        event.offlineUpdate();
        //That
        assertTrue(event.isOffline());

        //Given
        event = Event.builder()
                .build();
        //When
        event.offlineUpdate();
        //That
        assertFalse(event.isOffline());
    }

    @Test
    @DisplayName("Event 생성 실패")
    public void createEvent_Bad_Request() throws Exception {
        Event event = Event.builder()
                .id(100)
                .name("한글테스트")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 7, 9, 16, 4))
                .closeEnrollmentDateTime(LocalDateTime.of(2020, 7, 10, 16, 4))
                .beginEventDateTime(LocalDateTime.of(2020, 7, 11, 16, 4))
                .endEventDateTime(LocalDateTime.of(2020, 7, 12, 16, 4))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .free(true)
                .offline(false)
                .eventStatus(EventStatus.STARTED)
                .build();

        mockMvc.perform(post("/api/events/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Event 생성 빈값 입력시 badRequest")
    public void createEvent_Bad_Request_Empty_Input() throws Exception {
        EventDTO eventDTO = EventDTO.builder().build();

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Event 생성 값 논리 오류시 badRequest")
    public void createEvent_Bad_Request_Wrong_Input() throws Exception {
        EventDTO eventDTO = EventDTO.builder()
                .name("한글테스트")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 7, 9, 16, 4))
                .closeEnrollmentDateTime(LocalDateTime.of(2020, 8, 10, 16, 4))
                .beginEventDateTime(LocalDateTime.of(2020, 7, 11, 16, 4))
                .endEventDateTime(LocalDateTime.of(2020, 6, 12, 16, 4))
                .basePrice(300)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .build();

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].code").exists());
    }

}
