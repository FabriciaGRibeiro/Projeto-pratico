package com.example.Projeto.pratico.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

// Habilita suporte a Pageable nos controllers (@PageableDefault) e
// garante que Page<T> seja serializado como um objeto JSON com campos
// content, totalElements, totalPages etc. em vez de um formato interno
// do Spring que pode variar entre versões.
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class WebConfig {}
