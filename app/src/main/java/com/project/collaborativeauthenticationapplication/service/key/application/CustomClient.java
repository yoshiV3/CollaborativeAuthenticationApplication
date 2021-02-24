package com.project.collaborativeauthenticationapplication.service.key.application;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.IllegalNumberOfTokensException;
import com.project.collaborativeauthenticationapplication.service.IllegalUseOfClosedTokenException;
import com.project.collaborativeauthenticationapplication.service.Participant;
import com.project.collaborativeauthenticationapplication.service.ServiceStateException;
import com.project.collaborativeauthenticationapplication.service.controller.CustomAuthenticationServicePool;
import com.project.collaborativeauthenticationapplication.service.controller.CustomServiceMonitor;
import com.project.collaborativeauthenticationapplication.service.key.KeyPresenter;
import com.project.collaborativeauthenticationapplication.service.key.KeyToken;
import com.project.collaborativeauthenticationapplication.service.key.user.DistributedKeyGenerationActivity;
import com.project.collaborativeauthenticationapplication.service.network.CustomCommunication;
import com.project.collaborativeauthenticationapplication.service.network.UnreachableParticipantException;

import java.util.ArrayList;
import java.util.List;

public class CustomClient implements Client {


    public static final int STATE_CLOSED      = 1;
    public static final int STATE_START       = 2;
    public static final int STATE_DETAILS     = 3;
    public static final int STATE_SELECT      = 4;
    public static final int STATE_FINISHED    = 5;
    public static final int STATE_ERROR       = 6;
    public static final int STATE_BAD_INP_SEL = 7;
    public static final int STATE_SESSION     = 8;
    public static final int STATE_INVITATION  = 9 ;


    private KeyPresenter presenter;

    public CustomClient(KeyPresenter presenter)
    {
        this.presenter = presenter;
    }


    private String[] details = {"", ""};



    private  int state = STATE_CLOSED;


    private KeyToken token = null;


    private ArrayList<Participant> selection;


    @Override
    public void open() {
        if (CustomServiceMonitor.getInstance().isServiceEnabled()) {
            try {
                token = CustomAuthenticationServicePool.getInstance().getNewKeyToken();
                state = STATE_START;
                presenter.SignalClientInNewState(state, STATE_CLOSED);

            } catch (IllegalNumberOfTokensException | ServiceStateException e) {
                presenter.setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, e.getMessage());
                state = STATE_ERROR;
                presenter.SignalClientInNewState(state, STATE_CLOSED);
            }
        }
        else
        {
            presenter.setMessage(DistributedKeyGenerationActivity.KEY_ERROR_MESSAGES, "Disabled");

        }
    }

    @Override
    public void close() {
        int previousState = state;
        state = STATE_FINISHED;
        if (token != null)
        {
            token.close();
            token = null;
        }
        presenter.SignalClientInNewState(state, previousState);
    }

    @Override
    public void submitLoginDetails(String login, String application) {
        if (state != STATE_START)
        {
            throw new IllegalStateException("Presenter should not call this method during this state");
        }
        if (token != null && !token.isClosed())
        {
            details[0] = login;
            details[1] = application;
            state = STATE_DETAILS;
        }
        else
        {
            state = STATE_ERROR;
        }
        presenter.SignalClientInNewState(state, STATE_START);
    }

    @Override
    public int getState() {
        return  state;
    }


    @Override
    public List<Participant> getOptions() {
        return CustomCommunication.getInstance().getReachableParticipants();
    }

    @Override
    public void submitSelection(List<Participant> selection) {
        if (!(state == STATE_DETAILS || state == STATE_BAD_INP_SEL))
        {
            throw new IllegalStateException("Presenter should not call this method during this state");
        }
        if (token != null && !token.isClosed())
        {
            if (isWellFormedInput(selection))
            {
                    this.selection =  new ArrayList<>(selection);
                    state = STATE_SELECT;
            }
            else
            {
                state = STATE_BAD_INP_SEL;
            }
        }
        else
        {
            state = STATE_ERROR;
        }
        presenter.SignalClientInNewState(state, STATE_DETAILS);
    }


    @Override
    public void run() {
        if (state != STATE_SELECT)
        {
            throw new IllegalStateException("Cannot run during this state");
        }
        KeyGenerationSessionGenerator            generator         = new CustomKeyGenerationSessionGenerator();
        KeyGenerationDistributedInvitationSender invitationSender  = new CustomKeyGenerationDistributedInvitationSender();
        try {
            generateSession(generator, invitationSender);
            sendInvitations( invitationSender, generator);
        } catch (IllegalUseOfClosedTokenException | UnreachableParticipantException e) {
            int previousState = state;
            state  = STATE_ERROR;
            presenter.SignalClientInNewState(state, previousState);
        }
    }

    private void generateSession(KeyGenerationSessionGenerator generator, KeyGenerationDistributedInvitationSender invitationSender) throws IllegalUseOfClosedTokenException {
        generator.generateSession(selection, token);
        state = STATE_SESSION;
        presenter.SignalClientInNewState(state, STATE_SELECT);
    }


    private void sendInvitations(KeyGenerationDistributedInvitationSender sender, KeyGenerationSessionGenerator generator) throws IllegalUseOfClosedTokenException, UnreachableParticipantException {
        generator.giveKeyGenerationSessionTo(sender);
        sender.sendInvitations(token);
        state = STATE_INVITATION;
        presenter.SignalClientInNewState(state, STATE_SESSION);
    }

    private boolean isWellFormedInput(List<Participant> selection) {
        boolean inputCorrect;
        inputCorrect= selection.size()>=1;

        int totalWeight = 0;
        for (Participant participant : selection)
        {
            totalWeight = totalWeight + participant.getWeight();
        }

        inputCorrect = inputCorrect && (totalWeight>=2);
        return  inputCorrect;
    }

    @Override
    protected void finalize() throws Throwable {
        if (state != STATE_FINISHED)
        {
            Logger logger = new AndroidLogger();
            logger.logError("CLIENT", "not properly managed states", "CRITICAL");
            close();
        }
        super.finalize();
    }
}
