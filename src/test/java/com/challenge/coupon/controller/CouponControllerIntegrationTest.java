package com.challenge.coupon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.challenge.coupon.dto.CouponRequest;
import com.challenge.coupon.dto.CouponResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CouponControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final LocalDate FUTURE = LocalDate.now().plusDays(7);

    @Test
    @DisplayName("POST /coupon creates coupon and returns 201 with sanitized code")
    void createReturns201AndSanitizedCode() throws Exception {
        CouponRequest request = new CouponRequest(
                "XY-Z-99-A",
                "Integration test coupon",
                new BigDecimal("2.5"),
                FUTURE,
                false
        );

        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("XYZ99A"))
                .andExpect(jsonPath("$.description").value("Integration test coupon"))
                .andExpect(jsonPath("$.discountValue").value(2.5))
                .andExpect(jsonPath("$.published").value(false))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("POST /coupon returns 400 when discount less than 0.5")
    void createReturns400WhenInvalidDiscount() throws Exception {
        CouponRequest request = new CouponRequest(
                "ABC123",
                "Desc",
                new BigDecimal("0.3"),
                FUTURE,
                false
        );

        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /coupon returns 422 when expiration date is in the past")
    void createReturns422WhenExpirationPast() throws Exception {
        CouponRequest request = new CouponRequest(
                "ABC123",
                "Desc",
                new BigDecimal("1.0"),
                LocalDate.now().minusDays(1),
                false
        );

        mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("DELETE /coupon/{id} returns 204 when coupon exists")
    void deleteReturns204() throws Exception {
        CouponRequest request = new CouponRequest(
                "DEL123",
                "To delete",
                new BigDecimal("1.0"),
                FUTURE,
                false
        );

        String createBody = mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CouponResponse created = objectMapper.readValue(createBody, CouponResponse.class);

        mockMvc.perform(delete("/coupon/" + created.id()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /coupon/{id} returns 404 when id not found")
    void deleteNotFoundReturns404() throws Exception {
        mockMvc.perform(delete("/coupon/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /coupon/{id} returns 200 and coupon detail when exists")
    void getByIdReturns200AndBody() throws Exception {
        CouponRequest request = new CouponRequest(
                "GET123",
                "For GET test",
                new BigDecimal("1.0"),
                FUTURE,
                true
        );

        String createBody = mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CouponResponse created = objectMapper.readValue(createBody, CouponResponse.class);

        mockMvc.perform(get("/coupon/" + created.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.id().toString()))
                .andExpect(jsonPath("$.code").value("GET123"))
                .andExpect(jsonPath("$.description").value("For GET test"))
                .andExpect(jsonPath("$.discountValue").value(1.0))
                .andExpect(jsonPath("$.published").value(true));
    }

    @Test
    @DisplayName("GET /coupon/{id} returns 404 when id not found")
    void getByIdNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/coupon/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Coupon not found"));
    }

    @Test
    @DisplayName("GET /coupon/{id} returns 404 when coupon was deleted")
    void getByIdDeletedReturns404() throws Exception {
        CouponRequest request = new CouponRequest(
                "DELGET",
                "Create then delete",
                new BigDecimal("1.0"),
                FUTURE,
                false
        );

        String createBody = mockMvc.perform(post("/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CouponResponse created = objectMapper.readValue(createBody, CouponResponse.class);

        mockMvc.perform(delete("/coupon/" + created.id()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/coupon/" + created.id()))
                .andExpect(status().isNotFound());
    }
}