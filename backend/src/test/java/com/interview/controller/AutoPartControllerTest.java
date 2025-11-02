package com.interview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.dto.AutoPartRequest;
import com.interview.dto.AutoPartResponse;
import com.interview.dto.AutoPartTotalValuePerCategoryResponse;
import com.interview.exception.GlobalExceptionHandler;
import com.interview.exception.ResourceNotFoundException;
import com.interview.service.AutoPartService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = AutoPartController.class)
@Import(GlobalExceptionHandler.class)
public class AutoPartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AutoPartService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testGetAllParts() throws Exception {
        AutoPartResponse part = AutoPartResponse.builder()
                .id(1L)
                .name("Brake Pad")
                .manufacturer("ACME")
                .price(12.5)
                .stockQuantity(10)
                .build();

        when(service.getAll()).thenReturn(Arrays.asList(part));

        mockMvc.perform(get("/api/v1/parts").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Brake Pad"));
    }

    @Test
    public void testGetPartById() throws Exception {
        AutoPartResponse part = AutoPartResponse.builder()
                .id(2L)
                .name("Oil Filter")
                .manufacturer("FilterCo")
                .price(7.99)
                .stockQuantity(5)
                .build();

        when(service.getById(2L)).thenReturn(part);

        mockMvc.perform(get("/api/v1/parts/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Oil Filter"));
    }

    @Test
    public void testGetPartsByName() throws Exception {
        AutoPartResponse part = AutoPartResponse.builder()
                .id(3L)
                .name("Air Filter")
                .manufacturer("FilterCo")
                .price(9.5)
                .stockQuantity(8)
                .build();

        when(service.getByName("Air Filter")).thenReturn(Arrays.asList(part));

        mockMvc.perform(get("/api/v1/parts/search").param("name", "Air Filter").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Air Filter"));
    }

    @Test
    public void testGetPartsPaged() throws Exception {
        AutoPartResponse part = AutoPartResponse.builder()
                .id(10L)
                .name("Paged Part")
                .manufacturer("Pager")
                .price(20.0)
                .stockQuantity(2)
                .build();

        Page<AutoPartResponse> page = new PageImpl<>(Arrays.asList(part), PageRequest.of(0, 2), 1);

        when(service.getAllPaged(0, 2)).thenReturn(page);

        mockMvc.perform(get("/api/v1/parts/paged").param("page", "0").param("size", "2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.content[0].name").value("Paged Part"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    public void testFindByExample() throws Exception {
        AutoPartRequest dto = AutoPartRequest.builder()
                .name("Filter")
                .manufacturer(null)
                .price(null)
                .stockQuantity(null)
                .build();

        AutoPartResponse part = AutoPartResponse.builder()
                .id(11L)
                .name("Air Filter")
                .manufacturer("FilterCo")
                .price(9.5)
                .stockQuantity(8)
                .build();

        when(service.findByExample(any(AutoPartRequest.class))).thenReturn(Arrays.asList(part));

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/v1/parts/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11))
                .andExpect(jsonPath("$[0].name").value("Air Filter"));
    }

    @Test
    public void testCountParts() throws Exception {
        when(service.count()).thenReturn(42L);

        mockMvc.perform(get("/api/v1/parts/count").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));
    }

    @Test
    public void testCountByExample() throws Exception {
        AutoPartRequest dto = AutoPartRequest.builder()
                .name("Filter")
                .manufacturer(null)
                .price(null)
                .stockQuantity(null)
                .build();

        when(service.countByExample(any(AutoPartRequest.class))).thenReturn(7L);

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/v1/parts/count")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("7"));
    }

    @Test
    public void testGetTotalValuePerCategory() throws Exception {
        AutoPartTotalValuePerCategoryResponse r1 = AutoPartTotalValuePerCategoryResponse.builder()
                .value(200.0)
                .category("FILTER")
                .build();

        AutoPartTotalValuePerCategoryResponse r2 = AutoPartTotalValuePerCategoryResponse.builder()
                .value(150.0)
                .category("BRAKE")
                .build();

        when(service.getTotalValuePerCategory()).thenReturn(Arrays.asList(r1, r2));

        mockMvc.perform(get("/api/v1/parts/total-value-per-category").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("FILTER"))
                .andExpect(jsonPath("$[0].value").value(200.0))
                .andExpect(jsonPath("$[1].category").value("BRAKE"))
                .andExpect(jsonPath("$[1].value").value(150.0));
    }

    @Test
    public void testCreatePart() throws Exception {
        AutoPartRequest dto = AutoPartRequest.builder()
                .name("Spark Plug")
                .manufacturer("Sparkies")
                .price(4.5)
                .stockQuantity(20)
                .build();

        AutoPartResponse saved = AutoPartResponse.builder()
                .id(4L)
                .name(dto.getName())
                .manufacturer(dto.getManufacturer())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .build();

        when(service.create(any(AutoPartRequest.class))).thenReturn(saved);

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/v1/parts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.name").value("Spark Plug"));
    }

    @Test
    public void testUpdatePart() throws Exception {
        AutoPartRequest dto = AutoPartRequest.builder()
                .name("Updated Part")
                .manufacturer("Maker")
                .price(15.0)
                .stockQuantity(3)
                .build();

        AutoPartResponse updated = AutoPartResponse.builder()
                .id(5L)
                .name(dto.getName())
                .manufacturer(dto.getManufacturer())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .build();

        when(service.update(5L, dto)).thenReturn(updated);

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/api/v1/parts/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Updated Part"));
    }

    @Test
    public void testDeletePart() throws Exception {
        doNothing().when(service).delete(6L);

        mockMvc.perform(delete("/api/v1/parts/6"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetByIdNotFound() throws Exception {
        when(service.getById(99L)).thenThrow(new ResourceNotFoundException("AutoPart not found with id 99"));

        mockMvc.perform(get("/api/v1/parts/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("AutoPart not found with id 99"));
    }
}
