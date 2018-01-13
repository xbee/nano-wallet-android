package co.nano.nanowallet;

import java.math.BigDecimal;
import java.math.BigInteger;
/**
 * Created by admin on 12/22/2017.
 */




public class NanoWallet {
    public static final BigInteger baseOfDivider = new BigInteger("10");
    public static final BigInteger xrbDivider = baseOfDivider.pow(30);
    public static final BigInteger nanoDivider = baseOfDivider.pow(24);

    String accountAddress;
    String representativeAddress;
    String frontierBlock;
    String openBlock;
    BigDecimal accountBalance;
    Integer blockCount;

    String seed;
    Integer seedIndex;
    String privateKey;

    public BigDecimal getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
    }
}
