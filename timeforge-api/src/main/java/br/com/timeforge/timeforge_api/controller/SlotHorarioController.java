package br.com.timeforge.timeforge_api.controller;

import br.com.timeforge.timeforge_api.repository.SlotHorarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
class SlotHorarioController {
  private final SlotHorarioService service;
}
