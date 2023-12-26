package com.example.blockchain;

import android.content.Intent;
import android.os.AsyncTask;
import com.example.blockchain.MainActivity;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
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

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

public class InteractTask extends AsyncTask<Void, Void, BigInteger> {

    private com.example.blockchain.Storage storage;
    private Web3j web3;
    private Credentials credentials;
    BigInteger gasPrice = BigInteger.valueOf(20_000_000_000L);
    BigInteger gasLimit = BigInteger.valueOf(4_300_000L);
    EthGetTransactionCount ethGetTransactionCount = null;

    public InteractTask(Web3j web3, Credentials credentials, com.example.blockchain.Storage yourContract) {
        this.storage = yourContract;
        this.web3=web3;
        this.credentials=credentials;
    }

    @Override
    protected BigInteger doInBackground(Void... voids) {
        System.out.println("Do in background start...");

        try {
            ethGetTransactionCount = web3.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        BigInteger nonce = ethGetTransactionCount.getTransactionCount();
        // Creazione di un oggetto TransactionManager utilizzando l'oggetto Web3j e le credenziali
        long chainID=1337;
        TransactionManager transactionManager = new RawTransactionManager(web3, credentials, chainID);

        // Creazione di un oggetto RawTransaction per impostare il parametro nel contratto
        RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce, // Nonce
                DefaultGasProvider.GAS_PRICE, // Gas Price
                DefaultGasProvider.GAS_LIMIT, // Gas Limit
                storage.getContractAddress(), // Indirizzo del contratto
                storage.set_monete(BigInteger.valueOf(24)).encodeFunctionCall() // Dati della transazione per impostare il parametro
        );

        try {
            // Creazione della transazione firmata
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainID, credentials);
            String hexValue = Numeric.toHexString(signedMessage);

            // Invio della transazione al nodo Ethereum
            EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).send();

            // Recupero dell'hash della transazione
            String transactionHash = ethSendTransaction.getTransactionHash();

            System.out.println("Transazione inviata con successo. Hash della transazione: " + transactionHash);
        } catch (Exception e) {
            System.err.println("Errore durante l'invio della transazione: " + e.getMessage());
        }



            /*Metodo con firma automatica
            try {
            TransactionReceipt transactionReceipt = storage.set_monete(BigInteger.valueOf(15)).send();
            // Fai qualcosa con il risultato della transazione, ad esempio mostra un messaggio di successo
            if (transactionReceipt.isStatusOK()) {
                System.out.println("Transazione completata con successo!");
            } else {
                System.out.println("Errore durante l'esecuzione della transazione");
            }
        } catch (Exception e) {
            // Gestisci eventuali eccezioni, ad esempio mostrando un messaggio di errore
            e.printStackTrace();
        }*/

        try {
            Thread.sleep(1000);
            // Chiamata a una funzione remota che restituisce una stringa
            RemoteFunctionCall<BigInteger> remoteFunctionCall = storage.get();
            return remoteFunctionCall.send();
        } catch (Exception e) {
            // Gestisci eventuali eccezioni
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(BigInteger result) {
        // Gestisci il risultato qui dopo il completamento del lavoro in background
        if (result != null) {
            System.out.println("Risultato: " + result);
        } else {
            // Gestisci eventuali errori
            System.out.println("Errore onPostExecute nell'interazione");
        }
    }
}

