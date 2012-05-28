package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class PaymentElementVisibility extends VtiUserExit
{
  public VtiUserExitResult execute() throws VtiExitException
  {
	
	VtiUserExitScreenField cash = getScreenField("R_CASH");
	VtiUserExitScreenField gift = getScreenField("R_GIFT_VOUCH");
	VtiUserExitScreenField dc = getScreenField("R_EFT_DEBIT");
	VtiUserExitScreenField cc = getScreenField("R_EFT_CREDIT");
	VtiUserExitScreenField pCheq = getScreenField("R_PER_CHEQUE");
	VtiUserExitScreenField bCheq = getScreenField("R_BANK_CHEQUE");
	VtiUserExitScreenField servNo = getScreenField("R_SERV_ORD");
	VtiUserExitScreenField purNo = getScreenField("R_PUR_ORD");	
	
	VtiUserExitScreenField vNo = getScreenField("VOUCHER_NO");
	VtiUserExitScreenField ccF = getScreenField("CREDITCARD");
	VtiUserExitScreenField chqNo = getScreenField("CHEQUE_NO");
	VtiUserExitScreenField accNam = getScreenField("ACC_NAME");
	VtiUserExitScreenField accNo = getScreenField("ACC_NUM");
	VtiUserExitScreenField branch = getScreenField("BRANCH");
	VtiUserExitScreenField telNum = getScreenField("TEL_NUM");
	VtiUserExitScreenField IservNo = getScreenField("SERVNO");
	VtiUserExitScreenField IpurNo = getScreenField("PURNO");
	
	
	if (vNo == null) return new VtiUserExitResult(999, "Screen Field VOUCHER_NO does not exist");
	if (ccF == null) return new VtiUserExitResult(999, "Screen Field CREDITCARD does not exist");
	if (chqNo == null) return new VtiUserExitResult(999, "Screen Field CHEQUE_NO does not exist");
	if (accNam == null) return new VtiUserExitResult(999, "Screen Field ACC_NAME does not exist");
	if (accNo == null) return new VtiUserExitResult(999, "Screen Field ACC_NUM does not exist");
	if (branch == null) return new VtiUserExitResult(999, "Screen Field BRANCH does not exist");
	if (telNum == null) return new VtiUserExitResult(999, "Screen Field TEL_NUM does not exist");
	if (IservNo == null) return new VtiUserExitResult(999, "Screen Field SERVNO does not exist");
	if (IpurNo == null) return new VtiUserExitResult(999, "Screen Field PURNO does not exist");
	
	if (cash == null) return new VtiUserExitResult(999, "Screen Field R_CASH does not exist");
	if (gift == null) return new VtiUserExitResult(999, "Screen Field R_GIFT_VOUCH does not exist");
	if (dc == null) return new VtiUserExitResult(999, "Screen Field R_EFT_DEBIT does not exist");
	if (cc == null) return new VtiUserExitResult(999, "Screen Field R_EFT_CREDIT does not exist");
	if (pCheq == null) return new VtiUserExitResult(999, "Screen Field R_PER_CHEQUE does not exist");
	if (bCheq == null) return new VtiUserExitResult(999, "Screen Field R_BANK_CHEQUE does not exist");
	if (servNo == null) return new VtiUserExitResult(999, "Screen Field R_SERV_ORD does not exist");
	if (purNo == null) return new VtiUserExitResult(999, "Screen Field R_PUR_ORD does not exist");

	String sCon = "";
	
	if(cash.getFieldValue().equals("X"))
		sCon = "Cash";
	if(gift.getFieldValue().equals("X"))
		sCon = "Voucher";
	if(dc.getFieldValue().equals("X"))
		sCon = "Debit";
	if(cc.getFieldValue().equals("X"))
		sCon = "Credit";
	if(pCheq.getFieldValue().equals("X"))
		sCon = "PCheque";
	if(bCheq.getFieldValue().equals("X"))
		sCon = "BCheque";
	if(servNo.getFieldValue().equals("X"))
		sCon = "ServOr";
	if(purNo.getFieldValue().equals("X"))
		sCon = "PurchOr";
	
	if(sCon.equals("")) return new VtiUserExitResult(999,"Please select a payment method.");
	
	if(sCon.equalsIgnoreCase("Cash"))
	{
		vNo.setDisplayOnlyFlag(true); 
		ccF.setDisplayOnlyFlag(true); 
		chqNo.setDisplayOnlyFlag(true); 
		accNam.setDisplayOnlyFlag(true); 
		accNo.setDisplayOnlyFlag(true); 
		branch.setDisplayOnlyFlag(true); 
		telNum.setDisplayOnlyFlag(true); 
		IservNo.setDisplayOnlyFlag(true); 
		IpurNo.setDisplayOnlyFlag(true); 
	}
	
	if(sCon.equalsIgnoreCase("Voucher"))
	{
		vNo.setDisplayOnlyFlag(false); 
		ccF.setDisplayOnlyFlag(true); 
		chqNo.setDisplayOnlyFlag(true); 
		accNam.setDisplayOnlyFlag(true); 
		accNo.setDisplayOnlyFlag(true); 
		branch.setDisplayOnlyFlag(true); 
		telNum.setDisplayOnlyFlag(true); 
		IservNo.setDisplayOnlyFlag(true); 
		IpurNo.setDisplayOnlyFlag(true); 
	}
	
	if(sCon.equalsIgnoreCase("Debit"))
	{
		vNo.setDisplayOnlyFlag(true); 
		ccF.setDisplayOnlyFlag(false); 
		chqNo.setDisplayOnlyFlag(true); 
		accNam.setDisplayOnlyFlag(true); 
		accNo.setDisplayOnlyFlag(true); 
		branch.setDisplayOnlyFlag(true); 
		telNum.setDisplayOnlyFlag(true); 
		IservNo.setDisplayOnlyFlag(true); 
		IpurNo.setDisplayOnlyFlag(true); 
	}
	
	if(sCon.equalsIgnoreCase("Credit"))
	{
		vNo.setDisplayOnlyFlag(true); 
		ccF.setDisplayOnlyFlag(false); 
		chqNo.setDisplayOnlyFlag(true); 
		accNam.setDisplayOnlyFlag(true); 
		accNo.setDisplayOnlyFlag(true); 
		branch.setDisplayOnlyFlag(true); 
		telNum.setDisplayOnlyFlag(true); 
		IservNo.setDisplayOnlyFlag(true); 
		IpurNo.setDisplayOnlyFlag(true); 
	}

	if(sCon.equalsIgnoreCase("PCheque"))
	{
		vNo.setDisplayOnlyFlag(true); 
		ccF.setDisplayOnlyFlag(true); 
		chqNo.setDisplayOnlyFlag(false); 
		accNam.setDisplayOnlyFlag(false); 
		accNo.setDisplayOnlyFlag(false); 
		branch.setDisplayOnlyFlag(false); 
		telNum.setDisplayOnlyFlag(false); 
		IservNo.setDisplayOnlyFlag(true); 
		IpurNo.setDisplayOnlyFlag(true); 
	}
		
	if(sCon.equalsIgnoreCase("BCheque"))
	{
		vNo.setDisplayOnlyFlag(true); 
		ccF.setDisplayOnlyFlag(true); 
		chqNo.setDisplayOnlyFlag(false); 
		accNam.setDisplayOnlyFlag(false); 
		accNo.setDisplayOnlyFlag(false); 
		branch.setDisplayOnlyFlag(false); 
		telNum.setDisplayOnlyFlag(false); 
		IservNo.setDisplayOnlyFlag(true); 
		IpurNo.setDisplayOnlyFlag(true); 
	}
		
	if(sCon.equalsIgnoreCase("ServOr"))
	{
		vNo.setDisplayOnlyFlag(true); 
		ccF.setDisplayOnlyFlag(true); 
		chqNo.setDisplayOnlyFlag(true); 
		accNam.setDisplayOnlyFlag(true); 
		accNo.setDisplayOnlyFlag(true); 
		branch.setDisplayOnlyFlag(true); 
		telNum.setDisplayOnlyFlag(true); 
		IservNo.setDisplayOnlyFlag(false); 
		IpurNo.setDisplayOnlyFlag(true); 
	}
	
	if(sCon.equalsIgnoreCase("PurchOr"))
	{
		vNo.setDisplayOnlyFlag(true); 
		ccF.setDisplayOnlyFlag(true); 
		chqNo.setDisplayOnlyFlag(true); 
		accNam.setDisplayOnlyFlag(true); 
		accNo.setDisplayOnlyFlag(true); 
		branch.setDisplayOnlyFlag(true); 
		telNum.setDisplayOnlyFlag(true); 
		IservNo.setDisplayOnlyFlag(true); 
		IpurNo.setDisplayOnlyFlag(false); 
	}
		vNo.setFieldValue("");
		ccF.setFieldValue("");
		chqNo.setFieldValue("");
		accNo.setFieldValue("");
		accNam.setFieldValue("");
		branch.setFieldValue("");
		telNum.setFieldValue("");
		
	return new VtiUserExitResult();
  }
}