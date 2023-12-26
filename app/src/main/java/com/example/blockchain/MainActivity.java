package com.example.blockchain;

import org.web3j.contracts.token.ERC20BasicInterface;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.io.File;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.util.concurrent.ExecutionException;
import androidx.appcompat.app.AppCompatActivity;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.rlp.RlpEncoder;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.io.IOException;
import java.math.BigInteger;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.blockchain.Storage;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    Web3j web3 = Web3j.build(new HttpService("http://10.0.2.2:8545"));
    private Button bottone, bottone2, bottone3, bottone4;
    String fileName="";
    String pass="prova";
    Credentials credentials=null;
    EthGetTransactionCount ethGetTransactionCount = null;
    String contractAddress;
    BigInteger gasPrice = BigInteger.valueOf(20_000_000_000L);
    BigInteger gasLimit = BigInteger.valueOf(4_300_000L);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupBouncyCastle();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottone=(Button) findViewById(R.id.button);
        bottone.setOnClickListener(this);

        bottone2=(Button) findViewById(R.id.button2);
        bottone2.setOnClickListener(this);

        bottone3=(Button) findViewById(R.id.button3);
        bottone3.setOnClickListener(this);

        bottone4=(Button) findViewById(R.id.button4);
        bottone4.setOnClickListener(this);

        try {
            EthBlockNumber blockNumber = web3.ethBlockNumber().sendAsync().get();
            System.out.println("current block number : " + blockNumber.getBlockNumber());
        } catch (InterruptedException | ExecutionException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button:
                createWallet();
                break;

            case R.id.button2:
                query();
                    break;

            case R.id.button3:
                interactContract();
                break;

            case R.id.button4:
                retrieveWallet();
                break;
        }
    }

    private void retrieveWallet() {
        String downloadFolderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        // Creare un oggetto File per la cartella di download
        File downloadFolder = new File(downloadFolderPath);
        // Ottenere un array di file presenti nella cartella di download
        File[] files = downloadFolder.listFiles();

        if(files!=null && files.length >0){
            try {
                credentials = WalletUtils.loadCredentials(pass,"/storage/emulated/0/Download/"+files[0].getName());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CipherException e) {
                e.printStackTrace();
            }
            System.out.println(fileName);
            System.out.println("Indirizzo: "+credentials.getAddress());
            System.out.println("Indirizzo contratto: "+contractAddress);
        }
    }

    public void set_contractAddress(String s){
        contractAddress=s;
    }

    private void interactContract() {
        int chainId=1337;

        try {
            ethGetTransactionCount = web3.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }



        TransactionManager transactionManager = new RawTransactionManager(web3, credentials,chainId);
        Storage storage = Storage.load(contractAddress,web3,transactionManager,new DefaultGasProvider());

        InteractTask interactTask = new InteractTask(web3,credentials,storage);
        interactTask.execute();
    }

    public void createWallet(){
        try {
            fileName=WalletUtils.generateNewWalletFile(pass, new File("/storage/emulated/0/Download/"));
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void query() {

        //BigInteger value= BigInteger.valueOf(0);
        //RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce,BigInteger.valueOf(0),BigInteger.valueOf(0),"0x3ebE6Bc2378C6E6EB181156DD28C672Eb8ef4656",value);
        //String ABI="Storage.abi";
        //String BIN="Storage.bin";
        new DeployContractTask(this,web3,credentials).execute();
        contractAddress = getIntent().getStringExtra("CONTRACT_ADDRESS");
        //System.out.println("Indirizzo contratto: "+contractAddress);
    }

    private void setupBouncyCastle() {
        final Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (provider == null) {
            // Web3j will set up the provider lazily when it's first used.
            return;
        }
        if (provider.getClass().equals(BouncyCastleProvider.class)) {
            // BC with same package name, shouldn't happen in real life.
            return;
        }
        // Android registers its own BC provider. As it might be outdated and might not include
        // all needed ciphers, we substitute it with a known BC bundled in the app.
        // Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
        // of that it's possible to have another BC implementation loaded in VM.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }
}
