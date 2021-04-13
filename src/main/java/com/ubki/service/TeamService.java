package com.ubki.service;

import com.ubki.dao.TeamRepository;
import com.ubki.entity.Team;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class TeamService {

    private final TeamRepository teamRepository;


    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public void saveEmployees(Set<Team> teams) {
        int size = teams.size();
        int counter = 0;
        Set<Team> temp = new LinkedHashSet<>();
        for (Team team : teams) {
            temp.add(team);
            if ((counter + 1) % 1000 == 0 || (counter + 1) == size) {
                teamRepository.saveAll(temp);
                temp.clear();
            }
            counter++;
        }
    }
}
