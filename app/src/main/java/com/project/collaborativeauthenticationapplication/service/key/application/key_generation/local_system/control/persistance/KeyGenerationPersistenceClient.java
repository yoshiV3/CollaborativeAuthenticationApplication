package com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.persistance;

import android.content.Context;

import com.project.collaborativeauthenticationapplication.service.crypto.AndroidSecretStorage;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;
import com.project.collaborativeauthenticationapplication.service.general.FeedbackRequester;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.distributed_system.KeyGenerationCoordinator;
import com.project.collaborativeauthenticationapplication.service.key.application.key_generation.local_system.control.protocol.KeyGenerationSession;

import java.util.List;

public interface KeyGenerationPersistenceClient {

    void checkCredentials(FeedbackRequester requester, String applicationName);

    void open(Context context);

    void persist( List<BigNumber> shares, Point publicKey, KeyGenerationSession session, KeyGenerationCoordinator coordinator);

    void rollback();

    void confirm();
}
