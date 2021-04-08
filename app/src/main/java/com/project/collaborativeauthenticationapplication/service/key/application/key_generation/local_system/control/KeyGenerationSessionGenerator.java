package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control;


import com.project.collaborativeauthenticationapplication.service.Participant;


import java.util.List;

public interface KeyGenerationSessionGenerator {

    KeyGenerationSession generateSession(List<Participant> participants, int threshold, String applicationName, String login);

}
