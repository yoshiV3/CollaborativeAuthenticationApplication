package com.project.collaborativeauthenticationapplication.service.key.application;

import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;

import java.util.ArrayList;
import java.util.List;

public interface LocalKeyPartHandler {



    void receiveLocalKeyPartsFromLocalSource(List<List<BigNumber>> keyParts);






}
