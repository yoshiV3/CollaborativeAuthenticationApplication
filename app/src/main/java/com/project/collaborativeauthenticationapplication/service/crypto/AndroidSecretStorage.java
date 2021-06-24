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


    public void storeSecrets(List<BigNumber> secrets, int[] identifiers,  String applicationName) throws SecureStorageException {
        SharedPreferences.Editor editor = getEncryptedSharedPreferences().edit();
        String baseAlias                = getkeyAlias(applicationName);
        logger.logEvent(COMPONENT, "new secret", "low");
        int sequenceNumber = 0;
        for (BigNumber share: secrets){
            logger.logEvent(COMPONENT, "new secret", "low", String.valueOf(identifiers[sequenceNumber]));
            String stringBuilder = baseAlias +
                    ":" +
                    identifiers[sequenceNumber];
            editor.putString(stringBuilder, new String(share.getBigNumberAsByteArray(), StandardCharsets.ISO_8859_1));
            sequenceNumber += 1;
        }
        editor.apply();
    }

    public void storeSecrets(List<BigNumber> secrets, int[] identifiers,  String applicationName, String login) throws SecureStorageException {
        storeSecrets(secrets, identifiers, applicationName);
    }


    public void storeSecret(BigNumber share, int identifier, String applicationName) throws SecureStorageException {
        SharedPreferences.Editor editor = getEncryptedSharedPreferences().edit();
        String alias                    = getkeyAlias(applicationName) +  ":" +  String.valueOf(identifier);

        logger.logEvent(COMPONENT, "store new secret with alias", "low", alias);

        editor.putString(alias, new String(share.getBigNumberAsByteArray(), StandardCharsets.ISO_8859_1));
        editor.apply();
    }

    public void storeSecret(BigNumber share, int identifier, String applicationName, String login) throws SecureStorageException {
        storeSecret(share, identifier, applicationName);
    }

    private String getkeyAlias(String applicationName) {
        String builder = applicationName;
        return builder;
    }

    public BigNumber getSecrets(String applicationName, int identifier) throws SecureStorageException {
        String key    = getkeyAlias(applicationName) + ":" + identifier;
        String value  = getEncryptedSharedPreferences().getString(key, null);
        if (value == null){
            throw new  SecureStorageException("Keys cannot be found in the secret storage " + applicationName + " "+ String.valueOf(identifier) );
        }
        return new BigNumber(value.getBytes(StandardCharsets.ISO_8859_1));
    }


    public BigNumber getSecrets(String applicationName, String login, int identifier) throws SecureStorageException {
        return getSecrets(applicationName, identifier);
    }


    public void removeSecret(String applicationName, String login, int identifier) throws SecureStorageException {
        removeSecret(applicationName, identifier);
    }


    public void removeSecret(String applicationName, int identifier) throws SecureStorageException {
        SharedPreferences storage       = getEncryptedSharedPreferences();
        SharedPreferences.Editor editor = storage.edit();
        String alias               = getkeyAlias(applicationName)+ ":"  + identifier;
        editor.remove(alias);
        editor.apply();
    }


}
