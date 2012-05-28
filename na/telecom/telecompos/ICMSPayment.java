package na.telecom.telecompos;

import java.util.Date;
import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;
import au.com.skytechnologies.ecssdk.util.DateFormatter;
import au.com.skytechnologies.vti.VtiExitException;
import au.com.skytechnologies.vti.VtiExitLdbTable;
import au.com.skytechnologies.vti.VtiExitLdbTableRow;
import au.com.skytechnologies.vti.VtiUserExit;
import au.com.skytechnologies.vti.VtiUserExitResult;
import au.com.skytechnologies.vti.VtiUserExitScreenField;
import au.com.skytechnologies.vti.VtiUserExitScreenTable;
import au.com.skytechnologies.vti.VtiUserExitScreenTableRow;

public class ICMSPayment extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		// Data Declarations.     
		VtiUserExitScreenField AccountNo = getScreenField("ACCOUNT_NO");	  
		VtiUserExitScreenField Name = getScreenField("NAME");
		VtiUserExitScreenField TelephoneNo = getScreenField("TELEPHONE_NO");
		VtiUserExitScreenField CreditRating = getScreenField("CRRATE");
		VtiUserExitScreenField PaymentType = getScreenField("PAY_TYPE"); 
		VtiUserExitScreenField TotalDue = getScreenField("TOTAL_DUE");
		VtiUserExitScreenField OrderNo = getScreenField("ORDER_NO");
		VtiUserExitScreenField UserId = getScreenField("USERID");
		
		//Screen table declarations
		VtiUserExitScreenTable PaymentScrTable = getScreenTable("PAYMENTS");

		//LDB table declaration
		VtiExitLdbTable YSPS_ICMS_INV = getLocalDatabaseTable("YSPS_ICMS_INV");

		if (AccountNo == null)
			return new VtiUserExitResult(999, "Screen Field ACCOUNT_NO does not exist");
		if (Name == null)
			return new VtiUserExitResult(999, "Screen Field NAME does not exist");    
		if (TelephoneNo== null)
			return new VtiUserExitResult(999, "Screen Field TELEPHONE_NO does not exist");
		if (CreditRating== null)
			return new VtiUserExitResult(999, "Screen Field CRRATE does not exist");
		if (PaymentType== null)
			return new VtiUserExitResult(999, "Screen Field PAYMENT_TYPE does not exist");
		if (TotalDue== null)
			return new VtiUserExitResult(999, "Screen Field TOTAL_DUE does not exist");
		if (PaymentScrTable== null)
			return new VtiUserExitResult(999, "Screen Table PAYMENTS does not exist");
		if (YSPS_ICMS_INV== null)
			return new VtiUserExitResult(999, "LDB YSPS_ICMS_INV does not exist");
		if (OrderNo== null)
			return new VtiUserExitResult(999, "Screen Field ORDER_NO does not exist");
		if (UserId== null)
			return new VtiUserExitResult(999, "Screen Field USER_ID does not exist");

		String strOrderNo = OrderNo.getFieldValue();
		long lgOrderNo = 0;
		String strServerGroup = getServerGroup();
		String strServerID = getServerId();
		String strUserID = UserId.getFieldValue();
		String strPayType = PaymentType.getFieldValue();
		Date currNow = new Date();
		int currDate = Integer.parseInt(DateFormatter.format("yyyyMMdd", currNow));
		String currTime = DateFormatter.format("HHmmss", currNow).toString();

		//Check ff no records exist for payment
		if(PaymentScrTable.getRowCount()==0)
			return new VtiUserExitResult(999,"There are no items for payment");
			
		//Get next number range object 
		if(strOrderNo.equals(""))
		{
			lgOrderNo = getNextNumberFromNumberRange("YSPS_ORDER");
			OrderNo.setFieldValue(lgOrderNo);
			strOrderNo = String.valueOf(lgOrderNo);
		}
		
		for(int a=0; a<PaymentScrTable.getRowCount(); a++)
		{
			VtiUserExitScreenTableRow paymentRecord = PaymentScrTable.getRow(a);
			String strInvoiceNo = paymentRecord.getFieldValue("P_INVOICE_NO");
			String strAccountNo = paymentRecord.getFieldValue("P_ACCOUNT_NO");
			String strTelephoneNo = paymentRecord.getFieldValue("P_TELEPHONE_NO");
			String strPaymentDeposit = paymentRecord.getFieldValue("P_PAY_DEP");
			double dblPaymentAmount = paymentRecord.getDoubleFieldValue("P_PAY_AMOUNT");
			String strItemNo = paymentRecord.getFieldValue("P_ITEM_NO");
			String strDescription = paymentRecord.getFieldValue("P_DESCRIPTION");
			String strCompCode = paymentRecord.getFieldValue("P_COMP_CODE");
			String strCompDesc = paymentRecord.getFieldValue("P_COMP_DESC");
			String strPosLocation = paymentRecord.getFieldValue("P_PAYLOC");
			String strPaymentKey = paymentRecord.getFieldValue("P_PAYKEY");
			VtiExitLdbTableRow ICMSLDBTableRow = YSPS_ICMS_INV.newRow();
			
			ICMSLDBTableRow.setFieldValue("SERVER_GROUP", strServerGroup);
			ICMSLDBTableRow.setFieldValue("SERVER_ID", strServerID);
			ICMSLDBTableRow.setFieldValue("REFERENCE_NO", strOrderNo);
			ICMSLDBTableRow.setFieldValue("ITEM_NO", strItemNo);
			
			ICMSLDBTableRow.setFieldValue("ACCOUNT_NO", strAccountNo);
			ICMSLDBTableRow.setFieldValue("TELEPHONE_NO", strTelephoneNo);
			ICMSLDBTableRow.setFieldValue("DESCRIPTION", strDescription);
			ICMSLDBTableRow.setFieldValue("INVOICE_NO", strInvoiceNo);
			ICMSLDBTableRow.setFieldValue("COMPANY_CODE", strCompCode);
			ICMSLDBTableRow.setFieldValue("COMPANY_DESC", strCompDesc);
			
			ICMSLDBTableRow.setFieldValue("PAYMENT_AMT", dblPaymentAmount);
			ICMSLDBTableRow.setFieldValue("PAYMENT_DATE", currDate);
			ICMSLDBTableRow.setFieldValue("PAYMENT_TIME", currTime);
			ICMSLDBTableRow.setFieldValue("PAYMENT_TYPE",strPayType);
			ICMSLDBTableRow.setFieldValue("PAYMENT_DEPOSIT", strPaymentDeposit);
			
			ICMSLDBTableRow.setFieldValue("SKYPOS_LOCATION", strPosLocation);
			ICMSLDBTableRow.setFieldValue("SKYPOS_RECEIPT", strOrderNo);
			ICMSLDBTableRow.setFieldValue("PAY_TYPE_CODE", strPaymentKey);
			
			ICMSLDBTableRow.setFieldValue("USER_ID", strUserID);
			ICMSLDBTableRow.setFieldValue("TIMESTAMP", "BLANK");
			
			YSPS_ICMS_INV.saveRow(ICMSLDBTableRow);
		}
			
		return new VtiUserExitResult(000,"");
	}

}