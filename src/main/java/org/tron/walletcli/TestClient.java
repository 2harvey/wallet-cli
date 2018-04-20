package org.tron.walletcli;

import com.beust.jcommander.JCommander;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.api.GrpcAPI.*;
import org.tron.common.utils.Base58;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.protos.Contract;
import org.tron.protos.Contract.AssetIssueContract;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.BlockHeader;
import org.tron.protos.Protocol.BlockHeader.raw;
import org.tron.protos.Protocol.Witness;
import org.tron.walletserver.WalletClient;

import java.util.*;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

public class TestClient {

  private static final Logger logger = LoggerFactory.getLogger("TestClient");
  private Client client = new Client();

  private void registerWallet(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("RegisterWallet need 1 parameter like following: ");
      System.out.println("RegisterWallet Password");
      return;
    }
    String password = parameters[0];

    if (client.registerWallet(password)) {
      logger.info("Register a wallet and store it successful !!");
    } else {
      logger.info("Register wallet failed !!");
    }
  }

  private void importWallet(String[] parameters) {
    if (parameters == null || parameters.length != 2) {
      System.out.println("ImportWallet need 2 parameter like following: ");
      System.out.println("ImportWallet Password PriKey");
      System.out.println("PriKey need Hex string format.");
      return;
    }
    String password = parameters[0];
    String priKey = parameters[1];

    if (client.importWallet(password, priKey)) {
      logger.info("Import a wallet and store it successful !!");
    } else {
      logger.info("Import a wallet failed !!");
    }
  }

  private void importwalletByBase64(String[] parameters) {
    if (parameters == null || parameters.length != 2) {
      System.out.println("ImportwalletByBase64 need 2 parameter like following: ");
      System.out.println("ImportwalletByBase64 Password PriKey");
      System.out.println("PriKey need base64 string format.");
      return;
    }
    String password = parameters[0];
    String priKey64 = parameters[1];
    Decoder decoder = Base64.getDecoder();
    String priKey = ByteArray.toHexString(decoder.decode(priKey64));

    if (client.importWallet(password, priKey)) {
      logger.info("Import a wallet and store it successful !!");
    } else {
      logger.info("Import a wallet failed !!");
    }
  }

  private void changePassword(String[] parameters) {
    if (parameters == null || parameters.length != 2) {
      System.out.println("ChangePassword need 2 parameter like following: ");
      System.out.println("ChangePassword OldPassword NewPassword ");
      return;
    }
    String oldPassword = parameters[0];
    String newPassword = parameters[1];

    if (client.changePassword(oldPassword, newPassword)) {
      logger.info("ChangePassword successful !!");
    } else {
      logger.info("ChangePassword failed !!");
    }
  }

  private void login(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("Login need 1 parameter like following: ");
      System.out.println("Login Password ");
      return;
    }
    String password = parameters[0];

    boolean result = client.login(password);
    if (result) {
      logger.info("Login successful !!!");
    } else {
      logger.info("Login failed !!!");
    }
  }

  private void logout() {
    client.logout();
    logger.info("Logout successful !!!");
  }

  private void backupWallet(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("BackupWallet need 1 parameter like following: ");
      System.out.println("BackupWallet Password ");
      return;
    }
    String password = parameters[0];

    String priKey = client.backupWallet(password);
    if (priKey != null) {
      logger.info("Backup a wallet successful !!");
      logger.info("priKey = " + priKey);
    }
  }

  private void backupWallet2Base64(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("BackupWallet2Base64 need 1 parameter like following: ");
      System.out.println("BackupWallet2Base64 Password ");
      return;
    }
    String password = parameters[0];
    String priKey = client.backupWallet(password);
    Encoder encoder = Base64.getEncoder();
    String priKey64 = encoder.encodeToString(ByteArray.fromHexString(priKey));
    if (priKey != null) {
      logger.info("Backup a wallet successful !!");
      logger.info("priKey = " + priKey64);
    }
  }

  private void getAddress() {
    String address = client.getAddress();
    if (address != null) {
      logger.info("GetAddress successful !!");
      logger.info("address = " + address);
    }
  }

  private void getBalance() {
    Account account = client.queryAccount();
    if (account == null) {
      logger.info("Get Balance failed !!!!");

    } else {
      long balance = account.getBalance();
      logger.info("Balance = " + balance);
    }
  }

  private void getAccount(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("GetAccount need 1 parameter like following: ");
      System.out.println("GetAccount Address ");
      return;
    }
    String address = parameters[0];
    byte[] addressBytes = WalletClient.decodeFromBase58Check(address);
    if (addressBytes == null) {
      return;
    }

    Account account = WalletClient.queryAccount(addressBytes);
    if (account == null) {
      logger.info("Get Account failed !!!!");
    } else {
      logger.info("\n" + Utils.printAccount(account));
    }
  }

  private void getAssetIssueByAccount(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("GetAssetIssueByAccount need 1 parameter like following: ");
      System.out.println("GetAssetIssueByAccount Address ");
      return;
    }
    String address = parameters[0];
    byte[] addressBytes = WalletClient.decodeFromBase58Check(address);
    if (addressBytes == null) {
      return;
    }

    Optional<AssetIssueList> result = WalletClient.getAssetIssueByAccount(addressBytes);
    if (result.isPresent()) {
      AssetIssueList assetIssueList = result.get();
      List<AssetIssueContract> list = assetIssueList.getAssetIssueList();
      for (int i = 0; i < list.size(); i++) {
        AssetIssueContract assetIssueContract = list.get(i);
        logger.info(Utils.printAssetIssueList(assetIssueList));
      }
    } else {
      logger.info("GetAssetIssueByAccount " + " failed !!");
    }
  }

  private void getAssetIssueByName(String[] parameters) {
    if (parameters == null || parameters.length != 1) {
      System.out.println("GetAssetIssueByName need 1 parameter like following: ");
      System.out.println("GetAssetIssueByName AssetName ");
      return;
    }
    String assetName = parameters[0];

    AssetIssueContract assetIssueContract = WalletClient.getAssetIssueByName(assetName);
    if (assetIssueContract != null) {
      logger.info("\n" + Utils.printAssetIssue(assetIssueContract));
    } else {
      logger.info("GetAssetIssueByName " + " failed !!");
    }
  }

  private void sendCoin(String[] parameters) {
    if (parameters == null || parameters.length != 3) {
      System.out.println("SendCoin need 3 parameter like following: ");
      System.out.println("SendCoin Password ToAddress Amount");
      return;
    }
    String password = parameters[0];
    String toAddress = parameters[1];
    String amountStr = parameters[2];
    long amount = new Long(amountStr);

    boolean result = client.sendCoin(password, toAddress, amount);
    if (result) {
      logger.info("Send " + amount + " drop to " + toAddress + " successful !!");
    } else {
      logger.info("Send " + amount + " drop to " + toAddress + " failed !!");
    }
  }

  private void testTransaction(String[] parameters) {
    if (parameters == null || (parameters.length != 4 && parameters.length != 5)) {
      System.out.println("testTransaction need 4 or 5 parameter like following: ");
      System.out.println("testTransaction Password ToAddress assertName times");
      System.out.println("testTransaction Password ToAddress assertName times interval");
      System.out.println("If needn't transferAsset, assertName input null");
      return;
    }
    String password = parameters[0];
    String toAddress = parameters[1];
    String assertName = parameters[2];
    String loopTime = parameters[3];
    int intervalInt = 0;//s
    if (parameters.length == 5) {
      String interval = parameters[4];
      intervalInt = Integer.parseInt(interval);//s
    }
    intervalInt *= 500; //ms
    long times = new Long(loopTime);

    for (int i = 1; i <= times; i++) {
      long amount = i;
      boolean result = client.sendCoin(password, toAddress, amount);
      if (result) {
        logger.info("Send " + amount + " drop to " + toAddress + " successful !!");
        if (intervalInt > 0) {
          try {
            Thread.sleep(intervalInt);
          } catch (Exception e) {
            e.printStackTrace();
            break;
          }
        }
      } else {
        logger.info("Send " + amount + " drop to " + toAddress + " failed !!");
        break;
      }

      if (!"null".equalsIgnoreCase(assertName)) {
        result = client.transferAsset(password, toAddress, assertName, amount);
        if (result) {
          logger
              .info("transferAsset " + amount + assertName + " to " + toAddress + " successful !!");
          if (intervalInt > 0) {
            try {
              Thread.sleep(intervalInt);
            } catch (Exception e) {
              e.printStackTrace();
              break;
            }
          }
        } else {
          logger.info("transferAsset " + amount + assertName + " to " + toAddress + " failed !!");
          break;
        }
      }
    }

  }

  private void transferAsset(String[] parameters) {
    if (parameters == null || parameters.length != 4) {
      System.out.println("TransferAsset need 4 parameter like following: ");
      System.out.println("TransferAsset Password ToAddress AssertName Amount");
      return;
    }
    String password = parameters[0];
    String toAddress = parameters[1];
    String assertName = parameters[2];
    String amountStr = parameters[3];
    long amount = new Long(amountStr);

    boolean result = client.transferAsset(password, toAddress, assertName, amount);
    if (result) {
      logger.info("TransferAsset " + amount + " to " + toAddress + " successful !!");
    } else {
      logger.info("TransferAsset " + amount + " to " + toAddress + " failed !!");
    }
  }

  private void participateAssetIssue(String[] parameters) {
    if (parameters == null || parameters.length != 4) {
      System.out.println("ParticipateAssetIssue need 4 parameter like following: ");
      System.out.println("ParticipateAssetIssue Password ToAddress AssertName Amount");
      return;
    }
    String password = parameters[0];
    String toAddress = parameters[1];
    String assertName = parameters[2];
    String amountStr = parameters[3];
    long amount = new Integer(amountStr);

    boolean result = client.participateAssetIssue(password, toAddress, assertName, amount);
    if (result) {
      logger.info("ParticipateAssetIssue " + assertName + " " + amount + " from " + toAddress
          + " successful !!");
    } else {
      logger.info("ParticipateAssetIssue " + assertName + " " + amount + " from " + toAddress
          + " failed !!");
    }
  }

  private void assetIssue(String[] parameters) {
    if (parameters == null || parameters.length != 10) {
      System.out.println("AssetIssue need 10 parameter like following: ");
      System.out.println(
          "AssetIssue Password AssetName TotalSupply TrxNum AssetNum StartDate EndDate DecayRatio Description Url");
      System.out
          .println("TrxNum and AssetNum represents the conversion ratio of the tron to the asset.");
      System.out.println("The StartDate and EndDate format should look like 2018-3-1 2018-3-21 .");
      return;
    }

    String password = parameters[0];
    String name = parameters[1];
    String totalSupplyStr = parameters[2];
    String trxNumStr = parameters[3];
    String icoNumStr = parameters[4];
    String startYyyyMmDd = parameters[5];
    String endYyyyMmDd = parameters[6];
    String decayRatioStr = parameters[7];
    String description = parameters[8];
    String url = parameters[9];
    long totalSupply = new Long(totalSupplyStr);
    int trxNum = new Integer(trxNumStr);
    int icoNum = new Integer(icoNumStr);
    Date startDate = Utils.strToDateLong(startYyyyMmDd);
    Date endDate = Utils.strToDateLong(endYyyyMmDd);
    long startTime = startDate.getTime();
    long endTime = endDate.getTime();
    int decayRatio = new Integer(decayRatioStr);

    boolean result = client
        .assetIssue(password, name, totalSupply, trxNum, icoNum, startTime, endTime, decayRatio, 0,
            description, url);
    if (result) {
      logger.info("AssetIssue " + name + " successful !!");
    } else {
      logger.info("AssetIssue " + name + " failed !!");
    }
  }

  private void createWitness(String[] parameters) {
    if (parameters == null || parameters.length != 2) {
      System.out.println("CreateWitness need 2 parameter like following: ");
      System.out.println("CreateWitness Password Url");
      return;
    }

    String password = parameters[0];
    String url = parameters[1];

    boolean result = client.createWitness(password, url);
    if (result) {
      logger.info("CreateWitness " + " successful !!");
    } else {
      logger.info("CreateWitness " + " failed !!");
    }
  }

  private void listAccounts() {
    Optional<AccountList> result = client.listAccounts();
    if (result.isPresent()) {
      AccountList accountList = result.get();
      logger.info(Utils.printAccountList(accountList));
    } else {
      logger.info("List accounts " + " failed !!");
    }
  }

  private void listWitnesses() {
    Optional<WitnessList> result = client.listWitnesses();
    if (result.isPresent()) {
      WitnessList witnessList = result.get();
      logger.info(Utils.printWitnessList(witnessList));
    } else {
      logger.info("List witnesses " + " failed !!");
    }
  }

  private void getAssetIssueList() {
    Optional<AssetIssueList> result = client.getAssetIssueList();
    if (result.isPresent()) {
      AssetIssueList assetIssueList = result.get();
      logger.info(Utils.printAssetIssueList(assetIssueList));
    } else {
      logger.info("GetAssetIssueList " + " failed !!");
    }
  }

  private void listNodes() {
    Optional<NodeList> result = client.listNodes();
    if (result.isPresent()) {
      NodeList nodeList = result.get();
      List<Node> list = nodeList.getNodesList();
      for (int i = 0; i < list.size(); i++) {
        Node node = list.get(i);
        logger.info("IP::" + ByteArray.toStr(node.getAddress().getHost().toByteArray()));
        logger.info("Port::" + node.getAddress().getPort());
      }
    } else {
      logger.info("GetAssetIssueList " + " failed !!");
    }
  }

  private void GetBlock(String[] parameters) {
    long blockNum = -1;

    if (parameters == null || parameters.length == 0) {
      System.out.println("Get current block !!!!");
    } else {
      if (parameters.length != 1) {
        System.out.println("Get block too many paramters !!!");
        System.out.println("You can get current block like:");
        System.out.println("Getblock");
        System.out.println("Or get block by number like:");
        System.out.println("Getblock BlockNum");
      }
      blockNum = Long.parseLong(parameters[0]);
    }
    Block block = client.GetBlock(blockNum);
    if (block == null) {
      logger.info("No block for num : " + blockNum);
      return;
    }
    int transactionCount = block.getTransactionsCount();
    BlockHeader header = block.getBlockHeader();
    raw data = header.getRawData();
    ByteString witnessAddress = data.getWitnessAddress();
    long witnessID = data.getWitnessId();
    ByteString parentHash = data.getParentHash();
    ByteString txTrieRoot = data.getTxTrieRoot();
    long blockNum1 = data.getNumber();

    logger.info("Block num is : " + blockNum1);
    logger.info("witnessID is : " + witnessID);
    logger.info("TransactionCount is : " + transactionCount);
    logger.info("ParentHash is : " + ByteArray.toHexString(parentHash.toByteArray()));
    logger.info("TxTrieRoot is : " + ByteArray.toHexString(txTrieRoot.toByteArray()));
    logger.info("WitnessAddress is : " + WalletClient.encode58Check(witnessAddress.toByteArray()));
  }

  private void voteWitness(String[] parameters) {
    if (parameters == null || parameters.length < 3 || (parameters.length & 1) != 1) {
      System.out.println("Use VoteWitness command you need like: ");
      System.out.println("VoteWitness Password Address0 Count0 ... AddressN CountN");
      return;
    }

    String password = parameters[0];
    HashMap<String, String> witness = new HashMap<String, String>();
    for (int i = 1; i < parameters.length; i += 2) {
      String address = parameters[i];
      String countStr = parameters[i + 1];
      witness.put(address, countStr);
    }

    boolean result = client.voteWitness(password, witness);
    if (result) {
      logger.info("VoteWitness " + " successful !!");
    } else {
      logger.info("VoteWitness " + " failed !!");
    }
  }

  private void getTotalTransaction() {
    try {
      NumberMessage totalTransition = client.getTotalTransaction();
      logger.info("The num of total transactions is : " + totalTransition.getNum());

    } catch (Exception e) {
      logger.info("GetTotalTransaction " + " failed !!");
    }
  }

  /*
   * Query the contract by the address # by Jorge
   */
  private void getContract(String[] parameters) {
    if (parameters == null ||
            parameters.length != 1) {
      System.out.println("GetContract: invalid arguments!");
      return;
    }

    byte[] addressBytes = WalletClient.decodeFromBase58Check(parameters[0]);
    if (addressBytes == null) {
      System.out.println("GetContract: invalid address!");
      return;
    }

    Contract.ContractCreationContract contractCreationContract = WalletClient.getContract(addressBytes);
    if (contractCreationContract != null) {
      System.out.println("contract :" +
              "\tcreater:" + contractCreationContract.getOwnerAddress().toByteArray() +
              "\tcontract address:" + contractCreationContract.getContractAddress().toByteArray());
    } else {
      System.out.println("query contract failed!");
    }
  }

  /*
   * Create contract # by Jorge
   */
  /*
   pragma solidity ^0.4.0;
contract Ballot {

    address chairperson;
    mapping(address => uint) voters;

    function Ballot() public {
        chairperson = msg.sender;
        voters[chairperson] = 1;
    }

    function set(uint value) public {
        voters[msg.sender] = value;
    }

    function get(address account) public constant returns (uint) {
        return voters[account];
    }
}
   */
  private void createContract(String[] parameters) {
    /*
    if (parameters == null ||
            parameters.length != 4) {
      System.out.println("Create contract invalid arguments");
      return;
    }*/
    /*
     * Contract Address: 27WU3s5gexfL6KMQJ26MohXzvzntvAH1mQd
     * priv_key: T428PuByxZF4OvK0uYYHNIHoDstMsbD8xBak/Nfx5ak=
     * priv_key:4F8DBC3EE072C591783AF2B4B986073481E80ECB4CB1B0FCC416A4FCD7F1E5A9
     */
    String AddrBase58 = "27WU3s5gexfL6KMQJ26MohXzvzntvAH1mQd";

    String password = parameters[0];
    byte[] contractAddress = WalletClient.decodeFromBase58Check(AddrBase58); //parameters[1];
    String ABI; //= parameters[2];
    String codeStr; // = parameters[3];

    ABI = "[{\"constant\":false,\"inputs\":[{\"name\":\"value\",\"type\":\"uint256\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}," +
            "{\"constant\":true,\"inputs\":[{\"name\":\"account\",\"type\":\"address\"}],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"}," +
            "{\"inputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]";
    codeStr = "6060604052341561000f57600080fd5b336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060018060008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000208190555061017d806100c36000396000f30060606040526004361061004c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806360fe47b114610051578063c2bc2efc14610074575b600080fd5b341561005c57600080fd5b61007260048080359060200190919050506100c1565b005b341561007f57600080fd5b6100ab600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610108565b6040518082815260200191505060405180910390f35b80600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000208190555050565b6000600160008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205490509190505600a165627a7a72305820cb0570c138c8993c75fd222a49d1b054956ac21367bd9c0e5cf8d94628769d8d0029";

    boolean result = client.createContract(password, contractAddress, ABI, codeStr);
    if (result) {
      System.out.println("Deploy the contract successfully");
    } else {
      System.out.println("Deploy the contract failed");
    }

  }

  /*
   * Call contract # by Jorge
   */
  private void callContract(String[] parameters) {
    if (parameters == null ||
            parameters.length != 4) {
      System.out.println("Call contract invalid arguments");
      return;
    }

    String password = parameters[0];
    String contractAddr = parameters[1];
    String callValue = parameters[2];
    String func = parameters[3];

    boolean result = client.callContract(password, contractAddr,
            callValue, func);
    if (result) {
      System.out.println("Call the contract successfully");
    } else {
      System.out.println("Call the contract failed");
    }
  }


  private void help() {
    System.out.println("You can enter the following command: ");

    System.out.println("RegisterWallet");
    System.out.println("ImportWallet");
    System.out.println("ImportwalletByBase64");
    System.out.println("ChangePassword");
    System.out.println("Login");
    System.out.println("Logout");
    System.out.println("BackupWallet");
    System.out.println("BackupWallet2Base64");
    System.out.println("Getaddress");
    System.out.println("GetBalance");
    System.out.println("GetAccount");
    System.out.println("GetAssetissueByAccount");
    System.out.println("GetAssetIssueByName");
    System.out.println("SendCoin");
    System.out.println("TransferAsset");
    System.out.println("ParticipateAssetissue");
    System.out.println("Assetissue");
    System.out.println("CreateWitness");
    System.out.println("VoteWitness");
    System.out.println("Listaccounts");
    System.out.println("Listwitnesses");
    System.out.println("Listassetissue");
    System.out.println("listNodes");
    System.out.println("Getblock");
    System.out.println("getTotalTransaction");
    System.out.println("Exit or Quit");

    System.out.println("Input any one of then, you will get more tips.");
  }

  private void run() {
    Scanner in = new Scanner(System.in);
    while (true) {
      try {
        String cmdLine = in.nextLine().trim();
        String[] cmdArray = cmdLine.split("\\s+");
        // split on trim() string will always return at the minimum: [""]
        String cmd = cmdArray[0];
        if ("".equals(cmd)) {
          continue;
        }
        String[] parameters = Arrays.copyOfRange(cmdArray, 1, cmdArray.length);
        String cmdLowerCase = cmd.toLowerCase();

        switch (cmdLowerCase) {
          case "help": {
            help();
            break;
          }

          /*
           * # Jorge
           */
          case "getcontract": {
            getContract(parameters);
            return;
            //break;
          }
          case "cc":
          case "createcontract": {
            // login(parameters);
            createContract(parameters);
            return;
            //break;
          }
          case "callcontract": {
            callContract(parameters);
            return;
            //break;
          }

          case "registerwallet": {
            registerWallet(parameters);
            break;
          }
          case "importwallet": {
            importWallet(parameters);
            break;
          }
          case "importwalletbybase64": {
            importwalletByBase64(parameters);
            break;
          }
          case "changepassword": {
            changePassword(parameters);
            break;
          }
          case "login": {
            login(parameters);
            break;
          }
          case "logout": {
            logout();
            break;
          }
          case "backupwallet": {
            backupWallet(parameters);
            break;
          }
          case "backupwallet2base64": {
            backupWallet2Base64(parameters);
            break;
          }
          case "getaddress": {
            getAddress();
            break;
          }
          case "getbalance": {
            getBalance();
            break;
          }
          case "getaccount": {
            getAccount(parameters);
            break;
          }
          case "getassetissuebyaccount": {
            getAssetIssueByAccount(parameters);
            break;
          }
          case "getassetissuebyname": {
            getAssetIssueByName(parameters);
            break;
          }
          case "sendcoin": {
            sendCoin(parameters);
            break;
          }
          case "testtransaction": {
            testTransaction(parameters);
            break;
          }
          case "transferasset": {
            transferAsset(parameters);
            break;
          }
          case "participateassetissue": {
            participateAssetIssue(parameters);
            break;
          }
          case "assetissue": {
            assetIssue(parameters);
            break;
          }
          case "createwitness": {
            createWitness(parameters);
            break;
          }
          case "votewitness": {
            voteWitness(parameters);
            break;
          }
          case "listaccounts": {
            listAccounts();
            break;
          }
          case "listwitnesses": {
            listWitnesses();
            break;
          }
          case "listassetissue": {
            getAssetIssueList();
            break;
          }
          case "listnodes": {
            listNodes();
            break;
          }
          case "getblock": {
            GetBlock(parameters);
            break;
          }
          case "testTransaction": {
            testTransaction(parameters);
            break;
          }
          case "gettotaltransaction": {
            getTotalTransaction();
            break;
          }
          case "exit":
          case "quit": {
            System.out.println("Exit !!!");
            return;
          }
          default: {
            System.out.println("Invalid cmd: " + cmd);
            help();

          }
        }
      } catch (Exception e) {
        logger.error(e.getMessage());
      }
    }

  }

  public static void main(String[] args) {
    TestClient cli = new TestClient();

    JCommander.newBuilder()
        .addObject(cli)
        .build()
        .parse(args);

    cli.run();
  }
}