package com.project.collaborativeauthenticationapplication.service.crypto;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;

public class AndroidSecretStorage {


    private static final String MASTER_KEY = "master_mobile_collaborative_authentication";
    private static final String STORAGE_NAME = "key_shares_collaborative_authentication";

    private static final String COMPONENT = "AndroidSecretStorage";
    private static final String ERROR_KEY = "Key error occurred";
    private static final String ERROR_CERT = "Certificate error occurred";
    private static final String ERROR_ALGORITHM = "Algorithm error occurred";
    private static final String ERROR_IO = "IO error occurred";
    private static final String EVENT_MASTER_AVAILABLE = "Master key is available for use";


    private static Logger logger = new AndroidLogger();

    private static AndroidSecretStorage instance = null;



    private final Context context;

    public AndroidSecretStorage(Context context) {
        this.context = context;
    }


    private SharedPreferences getEncryptedSharedPreferences() throws SecureStorageException {
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                MASTER_KEY,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE).build();
        try {
            MasterKey key = new MasterKey.Builder(context, MASTER_KEY).setKeyGenParameterSpec(spec).build();
            SharedPreferences storage = EncryptedSharedPreferences.create(
                    context,
                    STORAGE_NAME,
                    key,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            return storage;
        }
        catch(GeneralSecurityException e)
        {
            logger.logError(COMPONENT, ERROR_KEY, "Critical", e.toString());
            throw new SecureStorageException("General Security problem");
        } catch(
        IOException e)
        {
            logger.logError(COMPONENT, ERROR_IO, "Critical", e.toString());
            throw new SecureStorageException("IO problem during attempt to store keys");
        }

}


    public void storeSecrets(List<BigNumber> secrets, String applicationName, String login) throws SecureStorageException {
            SharedPreferences storage       = getEncryptedSharedPreferences();
            SharedPreferences.Editor editor = storage.edit();
            String baseAlias               = getkeyAlias(applicationName, login);
            int sequenceNumber = 1;
            for (BigNumber share: secrets){
                StringBuilder stringBuilder     = new StringBuilder();
                stringBuilder.append(baseAlias);
                stringBuilder.append(sequenceNumber);
                sequenceNumber += 1;
                editor.putString(stringBuilder.toString(), new String(share.getBigNumberAsByteArray(), StandardCharsets.ISO_8859_1));
            }
            editor.apply();
    }

    private String getkeyAlias(String applicationName, String login) {
        StringBuilder builder = new StringBuilder();
        builder.append(applicationName);
        builder.append(":");
        builder.append(login);
        return builder.toString();
    }


    public List<BigNumber> getSecrets(String applicationName, String login, int weight){
        return null;
    }


    public void removeSecrets(String applicationName, String login, int numberOfSecrets) throws SecureStorageException {
        SharedPreferences storage       = getEncryptedSharedPreferences();
        SharedPreferences.Editor editor = storage.edit();
        String baseAlias               = getkeyAlias(applicationName, login);
        for(int sequenceNumber =1; sequenceNumber <= numberOfSecrets; sequenceNumber++){
            StringBuilder stringBuilder     = new StringBuilder();
            stringBuilder.append(baseAlias);
            stringBuilder.append(sequenceNumber);
            editor.remove(stringBuilder.toString());
        }
        editor.apply();
    }


}
