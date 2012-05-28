package na.telecom.telecompos;

import java.util.Vector;

import au.com.skytechnologies.vti.VtiExitException;
import au.com.skytechnologies.vti.VtiExitLdbTable;
import au.com.skytechnologies.vti.VtiUserExit;
import au.com.skytechnologies.vti.VtiUserExitResult;
import au.com.skytechnologies.vti.VtiUserExitScreenField;
import au.com.skytechnologies.vti.VtiUserExitScreenTable;
import au.com.skytechnologies.vti.VtiUserExitScreenTableRow;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class ICMSAddItems extends VtiUserExit
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
		VtiUserExitScreenField PayDep = getScreenField("PAYDEP");
		VtiUserExitScreenField InvNo = getScreenField("INVOICE_NO");
		VtiUserExitScreenField CompCode = getScreenField("COMP_CODE");
		VtiUserExitScreenField CompDesc= getScreenField("COMP_DESC");
		VtiUserExitScreenField PaymentAmt = getScreenField("PAYMENT_AMOUNT");
		VtiUserExitScreenField PaymentLocation = getScreenField("PAYLOC");
		VtiUserExitScreenField PaymentKey = getScreenField("PAYKEY");
    	VtiUserExitScreenField user = getScreenField("USERID");	

		//Screen table declarations
		VtiUserExitScreenTable PaymentScrTable = getScreenTable("PAYMENTS");

		//LDB table declaration
		VtiExitLdbTable YSPS_ICMS_INV = getLocalDatabaseTable("YSPS_ICMS_INV");
		VtiExitLdbTable YSPS_LOGON = getLocalDatabaseTable("YSPS_LOGON");

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
		if (PayDep== null)
			return new VtiUserExitResult(999, "Screen Field PAYDEP does not exist");
		if (InvNo== null)
			return new VtiUserExitResult(999, "Screen Field Invoice_no does not exist");
		if (PaymentAmt== null)
			return new VtiUserExitResult(999, "Screen Field PAYMENT_AMOUNT does not exist");
		if (CompCode== null)
			return new VtiUserExitResult(999, "Screen Field COMP_CODE does not exist");
		if (CompDesc== null)
			return new VtiUserExitResult(999, "Screen Field COMP_DESC does not exist");
		if (TotalDue== null)
			return new VtiUserExitResult(999, "Screen Field TOTAL_DUE does not exist");
		if (PaymentLocation== null)
			return new VtiUserExitResult(999, "Screen Field PAYLOC does not exist");
		if (PaymentKey== null)
			return new VtiUserExitResult(999, "Screen Field PAYKEY does not exist");
		
		if (PaymentScrTable== null)
			return new VtiUserExitResult(999, "Screen Table PAYMENTS does not exist");
		if (YSPS_ICMS_INV== null)
			return new VtiUserExitResult(999, "LDB YSPS_ICMS_INV does not exist");

		int lastPaymentRow = 0; 
		String strAccountNo = AccountNo.getFieldValue();
		String strName = Name.getFieldValue();
		String strTelNo = TelephoneNo.getFieldValue();
		String strCreditRating = CreditRating.getFieldValue();
		String strPayDep = PayDep.getFieldValue();
		String strInvoiceNo = InvNo.getFieldValue();
		String strCompCode = CompCode.getFieldValue();
		String strCompDesc = CompDesc.getFieldValue();
		String strPayLocation = PaymentLocation.getFieldValue();
		String strPayKey = PaymentKey.getFieldValue();
		String authlevel = "SUPERVISOR";
		
		double dblPayAmount = PaymentAmt.getDoubleFieldValue();
		boolean blnProcessed = false;
		boolean blnDuplicate = false;
		boolean blnPaymode = false;
		double dblTotal = 0;

		//Validate that the account was entered correctly and that there are no problems with it.
		VtiUserExitScreenField ChequeBlocked = getScreenField("CHEQUE_BLOCKED");
		VtiUserExitScreenField test = getScreenField("TEST");
		
		if (AccountNo == null) return new VtiUserExitResult(999, "Screen Field ACCOUNT_NO does not exist");
		if (Name == null) return new VtiUserExitResult(999, "Screen Field NAME does not exist");
		if (TelephoneNo == null) return new VtiUserExitResult(999, "Screen Field TELEPHONE_NO does not exist");
		if (CompCode == null) return new VtiUserExitResult(999, "Screen Field COMP_CODE does not exist");
		if (CompDesc == null) return new VtiUserExitResult(999, "Screen Field COMP_DESC does not exist");
		if (ChequeBlocked == null) return new VtiUserExitResult(999, "Screen Field CHEQUE_BLOCKED does not exist");
		if (CreditRating == null) return new VtiUserExitResult(999, "Screen Field CRRATE does not exist");
		
		
		VtiExitLdbTable YSPS_ICMS_ACC = getLocalDatabaseTable("YSPS_ICMS_ACC");
		if (YSPS_ICMS_ACC == null) return new VtiUserExitResult(999, "Local database table YSPS_ICMS_INV does not exist");
		
		String strChequeBlocked = ChequeBlocked.getFieldValue();
		boolean validAcc = false;
		boolean dbAcc = false;
		int i = 0;

		// Check if AccountNo has length
			if(strAccountNo.equals(""))
			{
         		setCursorPosition(AccountNo);		
					return new VtiUserExitResult(999, "Customer account is not valid");
			}
		// Remove TEL prefix if account barcode was scanned
		String formAccNo = strAccountNo.substring(0,3);
		final String tel = "TEL";                                             
			if(formAccNo.equalsIgnoreCase(tel))
				{
					formAccNo = strAccountNo.substring(3,strAccountNo.length());
					strAccountNo = formAccNo;
					AccountNo.setFieldValue(formAccNo);			
				}
			// Check length of AccountNo : must be = 10
				if(AccountNo.getFieldValue().length() != 10)
						return new VtiUserExitResult(999,"Account needs to be 10 characters long.");
			// Check if AccountNo starts with a 0
				if(!AccountNo.getFieldValue().startsWith("0"))
						return new VtiUserExitResult(999,"Account must start with a 0");		
		validAcc = ICMSAccountValidation.CheckAlgorithm(strAccountNo);

		
		
		
		VtiExitLdbSelectCriterion[] customerSelectConditions =
				{
					new VtiExitLdbSelectCondition("ACCOUNT_NO", VtiExitLdbSelectCondition.EQ_OPERATOR, strAccountNo),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X"),
				};				

		VtiExitLdbSelectConditionGroup customerCondsGrp = new VtiExitLdbSelectConditionGroup(customerSelectConditions, true);
		
		VtiExitLdbTableRow[] customerLdbRows = YSPS_ICMS_ACC.getMatchingRows(customerCondsGrp);
		
		
		
		
		
		i = customerLdbRows.length;
			
		test.setFieldValue(i + " " + validAcc + " " + strAccountNo);
		if(customerLdbRows.length > 0)
		{		
			
				test.setFieldValue(i + " " + validAcc + " " + strAccountNo);

				dbAcc = true;
				strAccountNo = customerLdbRows[0].getFieldValue("ACCOUNT_NO");
				strChequeBlocked = customerLdbRows[0].getFieldValue("CHEQUE_BLOCKED");
				strCompCode = customerLdbRows[0].getFieldValue("COMPANY_CODE");
				strCompDesc = customerLdbRows[0].getFieldValue("COMPANY_DESC");
				strCreditRating = customerLdbRows[0].getFieldValue("CREDIT_RATING");
				strName = customerLdbRows[0].getFieldValue("DESCRIPTION");
				strTelNo = customerLdbRows[0].getFieldValue("TELEPHONE_NO");
			
				AccountNo.setFieldValue(strAccountNo);
				//ChequeBlocked.setFieldValue(strChequeBlocked);
				CompCode.setFieldValue(strAccountNo);
				CompDesc.setFieldValue(strAccountNo);
				CreditRating.setFieldValue(strAccountNo);
				Name.setFieldValue(strName);
				TelephoneNo.setFieldValue(strTelNo);		
		}
				
				
				
		if(dbAcc == false)
			{
			//Check algorithm of the account number
			
			if(validAcc==true)
			{
				Name.setFieldValue("NEW CUSTOMER");
				ChequeBlocked.setFieldValue("");
				TelephoneNo.setFieldValue("");
				CompCode.setFieldValue("TEL");
				CompDesc.setFieldValue("TELECOM NAMIBIA");
			}
			else
			{
			    setCursorPosition(AccountNo);
				AccountNo.setFieldValue("");
					return new VtiUserExitResult(999, "Customer account number is not valid");
			}
		}
		
		
		if(AccountNo.getFieldValue().equals(""))
			return new VtiUserExitResult(999, "Invalid account no.");
		
		//Get last record
		if(PaymentScrTable.getRowCount()>0)
			lastPaymentRow = PaymentScrTable.getRowCount();

		//Get required values
		if(strPayDep.equals(""))
		{
			setCursorPosition(PayDep);
			return new VtiUserExitResult(999, "Please select Payment,Deposit or Credit");
		}

//      Make sure that a supervisor is using the DEBIT
		if(strPayDep.equals("DEBIT"))
		{
          // Check if the current user is a supervisor.
          VtiExitLdbSelectCriterion[] logonSelConds =
          {
           new VtiExitLdbSelectCondition("USERID",
                                  VtiExitLdbSelectCondition.EQ_OPERATOR,user.getFieldValue()),		
           new VtiExitLdbSelectCondition("AUTHLEVEL",
                                  VtiExitLdbSelectCondition.EQ_OPERATOR,authlevel),
		  };
        
          VtiExitLdbSelectConditionGroup logonSelCondGrp =
          new VtiExitLdbSelectConditionGroup(logonSelConds, true);
  
          VtiExitLdbTableRow[] logonLdbRows =
            YSPS_LOGON.getMatchingRows(logonSelCondGrp);

            VtiExitLdbTableRow logonLdbRow = null;
    
             // Send error message if not found
            if (logonLdbRows.length == 0)   
	         {	
               return new VtiUserExitResult(999, "Only Supervisors can Debit an account!");    
	         }				
		 }	
/*		if(strInvoiceNo.equals(""))
		{
			setCursorPosition(InvNo);
			return new VtiUserExitResult(999, "Please enter the invoice number");
		}
*/		
//      Add validation to check if payment location and payment type has been populated
		if(strPayLocation.equals(""))
		{
		  setCursorPosition(PaymentLocation);
			return new VtiUserExitResult(999, "Please select a payment location");
		}

		if(strPayKey.equals(""))
		{
		  setCursorPosition(PaymentKey);			
			return new VtiUserExitResult(999, "No Payment Key has been specified");
		}
	
		if(dblPayAmount==0)
		{
			setCursorPosition(PaymentAmt);
			return new VtiUserExitResult(999, "Please enter the payment amount");
		}
		
		
		
		//Check if entry exist in the screen table
		for(int ii=0; ii<PaymentScrTable.getRowCount();ii++)
		{
			VtiUserExitScreenTableRow paymentRow = PaymentScrTable.getRow(ii);
			String strPInvoiceNo = paymentRow.getFieldValue("P_INVOICE_NO");
			String strPAccountNo = paymentRow.getFieldValue("P_ACCOUNT_NO");
			String strPTelephoneNo = paymentRow.getFieldValue("P_TELEPHONE_NO");
			
			if(strPInvoiceNo.equals(strInvoiceNo)&&
					strPAccountNo.equals(strAccountNo)&&
					strPTelephoneNo.equals(strTelNo))
			{
				blnDuplicate = true;
				ii = PaymentScrTable.getRowCount();
			}
			
			
		}

		if (blnDuplicate==true)
			return new VtiUserExitResult(999, "Duplicate entries found");
		
		
		//Check if the 'Credit' pay mode is selected and there isn't any other payment mode in the table
		if(strPayDep.equals("DEBIT"))
		{
			for(int iii=0; iii<PaymentScrTable.getRowCount();iii++)
			{
				VtiUserExitScreenTableRow paymentRow = PaymentScrTable.getRow(iii);
				String strPPayDep = paymentRow.getFieldValue("P_PAY_DEP");
				if(!strPPayDep.equals(strPayDep))
				{
					blnPaymode = true;
					iii = PaymentScrTable.getRowCount();
				}
				
				
			}
		}
		else
		{
			for(int e=0; e<PaymentScrTable.getRowCount();e++)
			{
				VtiUserExitScreenTableRow paymentRow = PaymentScrTable.getRow(e);
				String strPPayDep = paymentRow.getFieldValue("P_PAY_DEP");
				if(strPPayDep.equals("CREDIT"))
				{
					blnPaymode = true;
					e = PaymentScrTable.getRowCount();
				}
				
				
			}
		}
		
		
		if(blnPaymode == true)
			return new VtiUserExitResult(999, "Debit payment mode is not allowed to mixed with Payment or Deposit");
		
		
		//Create new entry into payment table
		lastPaymentRow += 1;
		VtiUserExitScreenTableRow paymentRecord = PaymentScrTable.getNewRow();
		paymentRecord.setFieldValue("P_ITEM_NO", lastPaymentRow);
		paymentRecord.setFieldValue("P_INVOICE_NO", strInvoiceNo);
		paymentRecord.setFieldValue("P_PAY_DEP", strPayDep);
		paymentRecord.setFieldValue("P_PAY_AMOUNT", dblPayAmount);
		paymentRecord.setFieldValue("P_ACCOUNT_NO", strAccountNo);
		paymentRecord.setFieldValue("P_TELEPHONE_NO", strTelNo);
		paymentRecord.setFieldValue("P_CREDIT_RATING", strCreditRating);
		paymentRecord.setFieldValue("P_DESCRIPTION", strName);
		paymentRecord.setFieldValue("P_COMP_CODE", strCompCode);
		paymentRecord.setFieldValue("P_COMP_DESC", strCompDesc);
		paymentRecord.setFieldValue("P_PAYLOC", strPayLocation);
		paymentRecord.setFieldValue("P_PAYKEY", strPayKey);
		
		PaymentScrTable.appendRow(paymentRecord);

		blnProcessed = true;
		
		//Calculate the grand total
		for(int b=0; b<PaymentScrTable.getRowCount();b++)
		{
			VtiUserExitScreenTableRow paymentRecord2 = PaymentScrTable.getRow(b);
			dblTotal+=paymentRecord2.getDoubleFieldValue("P_PAY_AMOUNT");
		}

		//Update the total due amount
		TotalDue.setFieldValue(dblTotal);

		if(blnProcessed==true)
		{
			InvNo.setFieldValue("");
			PaymentAmt.setFieldValue("");
			setCursorPosition(InvNo);
			return new VtiUserExitResult(000,"Transaction added");
			
		}
		else
			return new VtiUserExitResult(000,"No Transactions selected for adding");

	}
	
	//Class Methods
	public VtiUserExitResult validateAccount() throws VtiExitException
	{
		VtiUserExitScreenField AccountNo = getScreenField("ACCOUNT_NO");
		VtiUserExitScreenField Name = getScreenField("NAME");
		VtiUserExitScreenField TelephoneNo = getScreenField("TELEPHONE_NO");
		VtiUserExitScreenField CompCode = getScreenField("COMP_CODE");
		VtiUserExitScreenField CompDesc= getScreenField("COMP_DESC");
		VtiUserExitScreenField ChequeBlocked = getScreenField("CHEQUE_BLOCKED");
		VtiUserExitScreenField CreditRating = getScreenField("CRRATE");
		VtiUserExitScreenField test = getScreenField("TEST");
		
		if (AccountNo == null) return new VtiUserExitResult(999, "Screen Field ACCOUNT_NO does not exist");
		if (Name == null) return new VtiUserExitResult(999, "Screen Field NAME does not exist");
		if (TelephoneNo == null) return new VtiUserExitResult(999, "Screen Field TELEPHONE_NO does not exist");
		if (CompCode == null) return new VtiUserExitResult(999, "Screen Field COMP_CODE does not exist");
		if (CompDesc == null) return new VtiUserExitResult(999, "Screen Field COMP_DESC does not exist");
		if (ChequeBlocked == null) return new VtiUserExitResult(999, "Screen Field CHEQUE_BLOCKED does not exist");
		if (CreditRating == null) return new VtiUserExitResult(999, "Screen Field CRRATE does not exist");
		
		
		VtiExitLdbTable YSPS_ICMS_ACC = getLocalDatabaseTable("YSPS_ICMS_ACC");
		if (YSPS_ICMS_ACC == null) return new VtiUserExitResult(999, "Local database table YSPS_ICMS_INV does not exist");
		
		String strAccountNo = AccountNo.getFieldValue();
		String strTelNo = TelephoneNo.getFieldValue();
		String strChequeBlocked = ChequeBlocked.getFieldValue();
		String strCompCode = CompCode.getFieldValue();
		String strCompDesc = CompDesc.getFieldValue();
		String strCreditRating = CreditRating.getFieldValue();
		String strName = Name.getFieldValue();
		boolean validAcc = false;
		boolean dbAcc = false;
		int i = 0;

		// Check if AccountNo has length
			if(strAccountNo.equals(""))
			{
         		setCursorPosition(AccountNo);		
					return new VtiUserExitResult(999, "Customer account is not valid");
			}
		// Remove TEL prefix if account barcode was scanned
		String formAccNo = strAccountNo.substring(0,3);
		final String tel = "TEL";                                             
			if(formAccNo.equalsIgnoreCase(tel))
				{
					formAccNo = strAccountNo.substring(3,strAccountNo.length());
					strAccountNo = formAccNo;
					AccountNo.setFieldValue(formAccNo);			
				}
			// Check length of AccountNo : must be  < 11
				if(AccountNo.getFieldValue().length() > 10)
						return new VtiUserExitResult(999,"Account cannot be longer than 10 character");
			// Check if AccountNo starts with a 0
				if(!AccountNo.getFieldValue().startsWith("0"))
						return new VtiUserExitResult(999,"Account must start with a 0");		
		validAcc = ICMSAccountValidation.CheckAlgorithm(strAccountNo);

		
		
		
		VtiExitLdbSelectCriterion[] customerSelectConditions =
				{
					new VtiExitLdbSelectCondition("ACCOUNT_NO", VtiExitLdbSelectCondition.EQ_OPERATOR, strAccountNo),
						new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X"),
				};				

		VtiExitLdbSelectConditionGroup customerCondsGrp = new VtiExitLdbSelectConditionGroup(customerSelectConditions, true);
		
		VtiExitLdbTableRow[] customerLdbRows = YSPS_ICMS_ACC.getMatchingRows(customerCondsGrp);
		
		
		
		
		
		i = customerLdbRows.length;
			
		test.setFieldValue(i + " " + validAcc + " " + strAccountNo);
		if(customerLdbRows.length > 0)
		{		
			
				test.setFieldValue(i + " " + validAcc + " " + strAccountNo);

				dbAcc = true;
				strAccountNo = customerLdbRows[0].getFieldValue("ACCOUNT_NO");
				strChequeBlocked = customerLdbRows[0].getFieldValue("CHEQUE_BLOCKED");
				strCompCode = customerLdbRows[0].getFieldValue("COMPANY_CODE");
				strCompDesc = customerLdbRows[0].getFieldValue("COMPANY_DESC");
				strCreditRating = customerLdbRows[0].getFieldValue("CREDIT_RATING");
				strName = customerLdbRows[0].getFieldValue("DESCRIPTION");
				strTelNo = customerLdbRows[0].getFieldValue("TELEPHONE_NO");
			
				AccountNo.setFieldValue(strAccountNo);
				//ChequeBlocked.setFieldValue(strChequeBlocked);
				CompCode.setFieldValue(strAccountNo);
				CompDesc.setFieldValue(strAccountNo);
				CreditRating.setFieldValue(strAccountNo);
				Name.setFieldValue(strName);
				TelephoneNo.setFieldValue(strTelNo);		
		}
				
				
				
		if(dbAcc == false)
			{
			//Check algorithm of the account number
			
			if(validAcc==true)
			{
				Name.setFieldValue("NEW CUSTOMER");
				ChequeBlocked.setFieldValue("");
				TelephoneNo.setFieldValue("");
				CompCode.setFieldValue("TEL");
				CompDesc.setFieldValue("TELECOM NAMIBIA");
			}
			else
			{
			    setCursorPosition(AccountNo);
				AccountNo.setFieldValue("");
					return new VtiUserExitResult(999, "Customer account number is not valid");
			}
		}
		return new VtiUserExitResult();

	}
}