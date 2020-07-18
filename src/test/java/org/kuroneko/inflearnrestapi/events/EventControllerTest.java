package org.kuroneko.inflearnrestapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.CsvSource;
import org.kuroneko.inflearnrestapi.RestDocsConfiguration;
import org.kuroneko.inflearnrestapi.account.Account;
import org.kuroneko.inflearnrestapi.account.AccountRepository;
import org.kuroneko.inflearnrestapi.account.AccountRole;
import org.kuroneko.inflearnrestapi.account.AccountService;
import org.kuroneko.inflearnrestapi.commons.AppProperties;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.common.util.Jackson2JsonParser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
@ActiveProfiles("test")
public class EventControllerTest {

    @Autowired
    MockMvc mockMvc; //dispatcherServlet를 만들어서 처리함 Webserver는 띄우지 않아서 비교적 빠르다.
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    AccountService accountService;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    AppProperties appProperties;

    @BeforeEach
    public void setUp(){
        this.eventRepository.deleteAll();
        this.accountRepository.deleteAll();
    }

    @Test
    @DisplayName("/api Root link check")
    public void rootLink() throws Exception{
        this.mockMvc.perform(get("/api"))
                .andDo(print())
                .andExpect(jsonPath("_links").exists());
    }

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
                .header(HttpHeaders.AUTHORIZATION, getBearer())
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
                .andDo(document("create-event",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-events").description("link to query-events"),
                                linkWithRel("update-event").description("link to update-event"),
                                linkWithRel("profile").description("link to profile")
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
                                fieldWithPath("manager").description("manager of new event response"),
                                fieldWithPath("eventStatus").description("eventStatus of new event response"),
                                fieldWithPath("_links.self.href").description("self of new event response"),
                                fieldWithPath("_links.query-events.href").description("query-events of new event response"),
                                fieldWithPath("_links.update-event.href").description("update-event of new event response"),
                                fieldWithPath("_links.profile.href").description("profile of new event response")
                        )
                    )
                );
    }

    private String getBearer() throws Exception {
        return "Bearer " + getAccessToken();
    }

    private String getAccessToken() throws Exception {
        Account account = Account.builder()
                .email(appProperties.getUserUsername())
                .password(appProperties.getUserPassword())
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();
        accountService.savePassword(account);

        ResultActions perform = this.mockMvc.perform(post("/oauth/token")
                .with(httpBasic(appProperties.getClientId(), appProperties.getClientSecret()))
                .param("grant_type", "password")
                .param("username", appProperties.getUserUsername())
                .param("password", appProperties.getUserPassword()));

        String contentAsString = perform.andReturn().getResponse().getContentAsString();
        Jackson2JsonParser jsonParser = new Jackson2JsonParser();
        return jsonParser.parseMap(contentAsString).get("access_token").toString();

    }

    @ParameterizedTest
    @CsvSource({
            "0,0,true",
            "100,0,false",
            "0,100,false"
    })
    public void updateFree(ArgumentsAccessor accessor){
        //Given
        Event event = Event.builder()
                .basePrice(accessor.getInteger(0))
                .maxPrice(accessor.getInteger(1))
                .build();
        //When
        event.freeUpdate();
        //That
        assertEquals(event.isFree(), accessor.getBoolean(2));
    }

    @ParameterizedTest
    @CsvSource({
            "평택역 11시, true",
            " ,false"
    })
    public void updateLocation(ArgumentsAccessor accessor){
        //Given
        Event event = Event.builder()
                .location(accessor.getString(0))
                .build();
        //When
        event.offlineUpdate();
        //That
        assertEquals(event.isOffline(), accessor.getBoolean(1));
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
                .header(HttpHeaders.AUTHORIZATION, getBearer())
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
                .header(HttpHeaders.AUTHORIZATION, getBearer())
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
                .header(HttpHeaders.AUTHORIZATION, getBearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].objectName").exists())
                .andExpect(jsonPath("errors[0].defaultMessage").exists())
                .andExpect(jsonPath("errors[0].code").exists())
                .andExpect(jsonPath("_links.index").exists());
    }

    @Test
    @DisplayName("30개의 이벤트를 10개씩 두번째 페이지 조회하기")
    public void queryEvents() throws Exception{
        //Given
        IntStream.range(0, 30).forEach(this::generateEvent);

        //When
        this.mockMvc.perform(get("/api/events")
                .param("page", "1")
                .param("size", "10")
                .param("sort", "name,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("query-events",
                        requestParameters(
                                parameterWithName("page").description("요청 할 페이지 0 부터 시작"),
                                parameterWithName("size").description("한 페이지에서 요청할 최대 값"),
                                parameterWithName("sort").description("페이지 정렬 방식")
                        ),
                        responseFields(
                                beneathPath("_embedded.eventList"),
                                fieldWithPath("id").description("identifier of query event response"),
                                fieldWithPath("name").description("name of query event response"),
                                fieldWithPath("description").description("description of query event response"),
                                fieldWithPath("beginEnrollmentDateTime").description("beginEnrollmentDateTime of query event response"),
                                fieldWithPath("closeEnrollmentDateTime").description("closeEnrollmentDateTime of query event response"),
                                fieldWithPath("beginEventDateTime").description("beginEventDateTime of query event response"),
                                fieldWithPath("endEventDateTime").description("endEventDateTime of query event response"),
                                fieldWithPath("location").description("location of query event response"),
                                fieldWithPath("basePrice").description("basePrice of query event response"),
                                fieldWithPath("maxPrice").description("maxPrice of query event response"),
                                fieldWithPath("limitOfEnrollment").description("limitOfEnrollment of query event response"),
                                fieldWithPath("offline").description("offline of query event response"),
                                fieldWithPath("free").description("free of query event response"),
                                fieldWithPath("manager").description("manager of query event response"),
                                fieldWithPath("eventStatus").description("eventStatus of query event response"),
                                fieldWithPath("_links.self.href").description("self of query event response")
                        ),
                        responseFields(
                                beneathPath("page"),
                                fieldWithPath("size").description("한 페이지내에서 표시 가능한 속성의 최대 갯수"),
                                fieldWithPath("totalElements").description("속성의 최대 갯수"),
                                fieldWithPath("totalPages").description("총 페이지"),
                                fieldWithPath("number").description("현재 페이지")
                        ),
                        links(
                                linkWithRel("first").description("첫 페이지"),
                                linkWithRel("next").description("다음 페이지"),
                                linkWithRel("self").description("현재 페이지"),
                                linkWithRel("prev").description("이전 페이지"),
                                linkWithRel("last").description("마지막 페이지"),
                                linkWithRel("profile").description("profile to link")

                        )
                    ));

    }

    @Test
    @DisplayName("이벤트 한개 조회")
    public void queryEvent() throws Exception{
        //Given
        Event event = this.generateEvent(100);

        //When
        this.mockMvc.perform(get("/api/events/{id}", event.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("get-an-event"));
    }

    @Test
    @DisplayName("없는 이벤트를 조회했을 경우 404응답")
    public void queryEvent_404error() throws Exception {
        //When
        this.mockMvc.perform(get("/api/events/1937820"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("이벤트 수정")
    public void updateEvent() throws Exception{
        //Given
        Event event = this.generateEvent(100);
        EventDTO eventDTO = this.modelMapper.map(event, EventDTO.class);
        String updatedEvent = "Updated Event";
        eventDTO.setName(updatedEvent);

        //When
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").exists())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("update-event",
                        links(
                                linkWithRel("self").description("자신의 링크"),
                                linkWithRel("profile").description("해당 api에 대한 정보를 얻을 수 있는 링크")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("request에 대한 contentType header")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("response에 대한 contentType header")
                        ),
                        requestFields(
                                fieldWithPath("name").description("name of update event request"),
                                fieldWithPath("description").description("description of update event request"),
                                fieldWithPath("beginEnrollmentDateTime").description("beginEnrollmentDateTime of update event request"),
                                fieldWithPath("closeEnrollmentDateTime").description("closeEnrollmentDateTime of update event request"),
                                fieldWithPath("beginEventDateTime").description("beginEventDateTime of update event request"),
                                fieldWithPath("endEventDateTime").description("endEventDateTime of update event request"),
                                fieldWithPath("location").description("location of update event request"),
                                fieldWithPath("basePrice").description("basePrice of update event request"),
                                fieldWithPath("maxPrice").description("maxPrice of update event request"),
                                fieldWithPath("limitOfEnrollment").description("limitOfEnrollment of update event request")
                        ),
                        responseFields(
                                fieldWithPath("id").description("identifier of update event response"),
                                fieldWithPath("name").description("name of update event response"),
                                fieldWithPath("description").description("description of update event response"),
                                fieldWithPath("beginEnrollmentDateTime").description("beginEnrollmentDateTime of update event response"),
                                fieldWithPath("closeEnrollmentDateTime").description("closeEnrollmentDateTime of update event response"),
                                fieldWithPath("beginEventDateTime").description("beginEventDateTime of update event response"),
                                fieldWithPath("endEventDateTime").description("endEventDateTime of update event response"),
                                fieldWithPath("location").description("location of update event response"),
                                fieldWithPath("basePrice").description("basePrice of update event response"),
                                fieldWithPath("maxPrice").description("maxPrice of update event response"),
                                fieldWithPath("limitOfEnrollment").description("limitOfEnrollment of update event response"),
                                fieldWithPath("offline").description("offline of update event response"),
                                fieldWithPath("free").description("free of update event response"),
                                fieldWithPath("manager").description("manager of update event response"),
                                fieldWithPath("eventStatus").description("eventStatus of update event response"),
                                fieldWithPath("_links.self.href").description("self of update event response"),
                                fieldWithPath("_links.profile.href").description("profile of update event response")
                        )
                    ));
    }

    @Test
    @DisplayName("입력값이 비어있는 경우 이벤트 수정 실패")
    public void updateEvent_input_empty_error() throws Exception{
        //Given
        Event event = this.generateEvent(100);
        EventDTO eventDTO = new EventDTO();

        //When
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("입력값이 잘못된 경우 이벤트 수정 실패")
    public void updateEvent_input_error() throws Exception{
        //Given
        Event event = this.generateEvent(100);
        EventDTO eventDTO = this.modelMapper.map(event, EventDTO.class);
        eventDTO.setBasePrice(20000);
        eventDTO.setMaxPrice(2000);

        //When
        this.mockMvc.perform(put("/api/events/{id}", event.getId())
                .header(HttpHeaders.AUTHORIZATION, getBearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 이벤트 수정 실패")
    public void updateEvent_notFound() throws Exception{
        //Given
        Event event = this.generateEvent(100);
        EventDTO eventDTO = this.modelMapper.map(event, EventDTO.class);

        //When
        this.mockMvc.perform(put("/api/events/32180321")
                .header(HttpHeaders.AUTHORIZATION, getBearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(eventDTO)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    private Event generateEvent(int index){
        Event event = Event.builder()
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
                .free(false)
                .offline(true)
                .eventStatus(EventStatus.DRAFT)
                .build();

        return this.eventRepository.save(event);
    }


}
