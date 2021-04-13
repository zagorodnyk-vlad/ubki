package com.ubki.controller;

import com.ubki.service.TeamConnectionService;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

@RestController
public class TeamController {
    private final TeamConnectionService teamConnectionService;

    public TeamController(TeamConnectionService teamConnectionService) {
        this.teamConnectionService = teamConnectionService;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/save")
    public void save(@RequestParam String source) throws ParserConfigurationException, SAXException, IOException {
        teamConnectionService.saveProgress(source);
    }
}
