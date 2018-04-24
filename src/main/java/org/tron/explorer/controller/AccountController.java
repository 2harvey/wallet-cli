package org.tron.explorer.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.tron.api.GrpcAPI.AccountList;
import org.tron.common.utils.Base58;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.TransactionUtils;
import org.tron.explorer.domain.AccountVo;
import org.tron.explorer.domain.Transfer;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.Account;
import org.tron.walletserver.WalletClient;
import org.tron.protos.Protocol.Transaction;

import java.util.Optional;


@RestController
public class AccountController {

  protected final Log log = LogFactory.getLog(getClass());

  @ModelAttribute
  AccountVo setAccountVo() {
    return new AccountVo();
  }

  @PostMapping("/queryAccount")
  public byte[] queryAccount(String address) {
    try {
      if (address == null) {
        return null;
      }
      byte[] baAddress = WalletClient.decodeFromBase58Check(address);
      if (baAddress == null) {
        return null;
      }
      Account account = WalletClient.queryAccount(baAddress);
      return account.toByteArray();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  @GetMapping("/accountList")
  public byte[] getAcountList() {
    try {
      Optional<AccountList> result = WalletClient.listAccounts();
      if (result.isPresent()) {
        AccountList accountList = result.get();
        return accountList.toByteArray();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @GetMapping("/updateAccount")
  public ModelAndView updateAccount() {
    return new ModelAndView("updateAccount");
  }


  @PostMapping("/updateAccountToView")
  public byte[] updateAccountToView(@ModelAttribute AccountVo account) {
    String address = account.getAddress();
    String accountName = account.getName();

    try {
      if (address == null || accountName == null) {
        return null;
      }
      byte[] addressBytes = WalletClient.decodeFromBase58Check(address);
      byte[] nameBytes = accountName.getBytes();
      if (addressBytes == null) {
        return null;
      }

      Transaction transaction = WalletClient.updateAccountTransaction(addressBytes, nameBytes);
      transaction = TransactionUtils.setTimestamp(transaction);
      return transaction.toByteArray();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}