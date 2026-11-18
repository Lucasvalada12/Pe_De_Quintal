package br.edu.aula.pedequintal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/itens";   // a "home" do sistema é a lista de itens (W2)
    }
}
