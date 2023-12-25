package com.example.blockchain;

import android.content.Intent;
import android.os.AsyncTask;
import com.example.blockchain.MainActivity;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.ChainId;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Numeric;

import com.example.blockchain.Storage;
import java.math.BigInteger;

public class DeployContractTask extends AsyncTask<Void, Void, String> {
    MainActivity activity;
    Web3j web3;
    Credentials credentials;

    public DeployContractTask(MainActivity activity, Web3j web3, Credentials credentials) {
        this.activity=activity;
        this.web3=web3;
        this.credentials=credentials;
    }

    @Override
    protected String doInBackground(Void... voids) {
        System.out.println("Do in background start...");
        int chainId=1337;
        try {
            // Definisci il gas provider
            BigInteger gasPrice = BigInteger.valueOf(20_000_000_000L);
            BigInteger gasLimit = BigInteger.valueOf(4_300_000L);
            ContractGasProvider gasProvider = new StaticGasProvider(gasPrice,gasLimit);


            // Crea il RawTransactionManager
            TransactionManager transactionManager = new RawTransactionManager(web3, credentials,chainId);

            // Effettua il deploy del contratto
            Storage myContract = Storage.deploy(
                    web3,
                    transactionManager,
                    gasProvider // Sostituisci con i parametri del costruttore del tuo contratto
            ).send();

            // Restituisci l'indirizzo del contratto

            return myContract.getContractAddress();

            // Restituisci l'indirizzo del contratto
            //return storage.getContractAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String contractAddress) {
        System.out.println("Indirizzo contratto: "+contractAddress);
        if (contractAddress != null) {
            activity.set_contractAddress(contractAddress);
        } else {
            System.out.println("Errore nell'indirizzo");
        }
    }
}


