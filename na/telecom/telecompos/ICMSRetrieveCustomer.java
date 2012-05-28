package na.telecom.telecompos;

import java.util.Vector;

import au.com.skytechnologies.vti.VtiExitException;
import au.com.skytechnologies.vti.VtiExitLdbSelectCondition;
import au.com.skytechnologies.vti.VtiExitLdbSelectConditionGroup;
import au.com.skytechnologies.vti.VtiExitLdbTable;
import au.com.skytechnologies.vti.VtiExitLdbTableRow;
import au.com.skytechnologies.vti.VtiUserExit;
import au.com.skytechnologies.vti.VtiUserExitResult;
import au.com.skytechnologies.vti.VtiUserExitScreenField;
import au.com.skytechnologies.vti.VtiUserExitScreenTable;
import au.com.skytechnologies.ecssdk.log.*;
import au.com.skytechnologies.vti.*;

public class ICMSRetrieveCustomer extends VtiUserExit
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
		VtiUserExitScreenField ChequeBlocked = getScreenField("CHEQUE_BLOCKED");
		VtiUserExitScreenField PayKey = getScreenField("PAYKEY");
		VtiUserExitScreenField PayAmount = getScreenField("PAYMENT_AMOUNT");

		//Screen table declarations
		VtiUserExitScreenTable PaymentScrTable = getScreenTable("PAYMENTS");

		//LDB table declaration
		VtiExitLdbTable YSPS_ICMS_INV = getLocalDatabaseTable("YSPS_ICMS_INV");
		VtiExitLdbTable YSPS_ICMS_ACC = getLocalDatabaseTable("YSPS_ICMS_ACC");
    	VtiExitLdbTable VTI_VALUE_LIST = getLocalDatabaseTable("VTI_VALUE_LIST");
		
		if (AccountNo == null) return new VtiUserExitResult(999, "Screen Field ACCOUNT_NO does not exist");
		if (Name == null) return new VtiUserExitResult(999, "Screen Field NAME does not exist");    
		if (TelephoneNo== null) return new VtiUserExitResult(999, "Screen Field TELEPHONE_NO does not exist");
		if (CreditRating== null) return new VtiUserExitResult(999, "Screen Field CRRATE does not exist");
		if (PaymentType== null) return new VtiUserExitResult(999, "Screen Field PAYMENT_TYPE does not exist");
		if (PayDep== null) return new VtiUserExitResult(999, "Screen Field PAYDEP does not exist");
		if (InvNo== null) return new VtiUserExitResult(999, "Screen Field Invoice_no does not exist");
		if (PaymentAmt== null) return new VtiUserExitResult(999, "Screen Field PAYMENT_AMOUNT does not exist");
		if (CompCode== null) return new VtiUserExitResult(999, "Screen Field COMP_CODE does not exist");
		if (CompDesc== null) return new VtiUserExitResult(999, "Screen Field COMP_DESC does not exist");
		if (TotalDue== null) return new VtiUserExitResult(999, "Screen Field TOTAL_DUE does not exist");
		if (ChequeBlocked== null) return new VtiUserExitResult(999, "Screen Field CHEQUE_BLOCKED does not exist");
		if (PaymentScrTable== null) return new VtiUserExitResult(999, "Screen Table PAYMENTS does not exist");
		if (YSPS_ICMS_INV== null) return new VtiUserExitResult(999, "LDB YSPS_ICMS_INV does not exist");
		if (YSPS_ICMS_ACC== null) return new VtiUserExitResult(999, "LDB YSPS_ICMS_ACC does not exist");		 
		
		String strAccountNo = AccountNo.getFieldValue();
		String strName = Name.getFieldValue();
		String strTelNo = TelephoneNo.getFieldValue();
		String strCreditRating = CreditRating.getFieldValue();
		String strPayDep = PayDep.getFieldValue();
		String strChequeBlocked = ChequeBlocked.getFieldValue();
		String strCompCode = CompCode.getFieldValue();
		String strCompDesc = CompDesc.getFieldValue();
		
// Remove TEL prefix if account barcode was scanned
   String formAccNo = strAccountNo.substring(0,3);
     final String tel = "TEL";                                             
        if(formAccNo.equalsIgnoreCase(tel))
          {
            formAccNo = strAccountNo.substring(3,strAccountNo.length());
			strAccountNo = formAccNo;
            AccountNo.setFieldValue(formAccNo);			
          }
	
		if(AccountNo.getFieldValue().length() != 10)
		 return new VtiUserExitResult(999,"Account must be equal to 10 characters");
		
		if(!AccountNo.getFieldValue().startsWith("0"))
			return new VtiUserExitResult(999,"Account must start with a 0");
		
		//Check input
		if(strAccountNo.equals("") && strTelNo.equals(""))
		{
			setCursorPosition(AccountNo);
			return new VtiUserExitResult(999,"Please enter the account or telephone number");
		}
		
		// A vector to dinamically populate relevant select conditions
		Vector vSelectCond = new Vector();
		
		if (!strAccountNo.trim().equals(""))
			vSelectCond.addElement (new VtiExitLdbSelectCondition("ACCOUNT_NO", VtiExitLdbSelectCondition.EQ_OPERATOR, strAccountNo));
		//No longer use telephone number a s database search condition due to duplicate tel no.
		//if (!strTelNo.trim().equals(""))
			//vSelectCond.addElement (new VtiExitLdbSelectCondition("TELEPHONE_NO", VtiExitLdbSelectCondition.EQ_OPERATOR, strTelNo));

		vSelectCond.addElement (new VtiExitLdbSelectCondition("DEL_IND", VtiExitLdbSelectCondition.NE_OPERATOR, "X"));

		VtiExitLdbSelectCondition [] customerSelectConditions = new VtiExitLdbSelectCondition[vSelectCond.size()];
		
		vSelectCond.copyInto(customerSelectConditions);

		VtiExitLdbSelectConditionGroup customerCondsGrp = new VtiExitLdbSelectConditionGroup(customerSelectConditions, true);
		
		VtiExitLdbTableRow[] customerLdbRows = YSPS_ICMS_ACC.getMatchingRows(customerCondsGrp);
		    
		for(int i=0; i<customerLdbRows.length; i++ )
		{
			strAccountNo = customerLdbRows[i].getFieldValue("ACCOUNT_NO");
			strChequeBlocked = customerLdbRows[i].getFieldValue("CHEQUE_BLOCKED");
			strCompCode = customerLdbRows[i].getFieldValue("COMPANY_CODE");
			strCompDesc = customerLdbRows[i].getFieldValue("COMPANY_DESC");
			strCreditRating = customerLdbRows[i].getFieldValue("CREDIT_RATING");
			strName = customerLdbRows[i].getFieldValue("DESCRIPTION");
			strTelNo = customerLdbRows[i].getFieldValue("TELEPHONE_NO");
			
			AccountNo.setFieldValue(strAccountNo);
			ChequeBlocked.setFieldValue(strChequeBlocked);
			CompCode.setFieldValue(strAccountNo);
			CompDesc.setFieldValue(strAccountNo);
			CreditRating.setFieldValue(strAccountNo);
			Name.setFieldValue(strName);
			TelephoneNo.setFieldValue(strTelNo);
		}

		//if could not find in LDB, check account algorithm
		if(customerLdbRows.length==0)
		{
			if(strAccountNo.equals(""))
			{
         		setCursorPosition(AccountNo);		
				return new VtiUserExitResult(999, "Customer account is not valid");
			}
			
			//Check algorithm of the account number
			if(ICMSAccountValidation.CheckAlgorithm(strAccountNo)==true)
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
				return new VtiUserExitResult(999, "Customer account number is not valid");
			}
		}
		
        // Check if cheque blocked then only diplay relevant types
	    if(strChequeBlocked.equalsIgnoreCase("C"))
			{
		//Get payment type value list
		  VtiExitLdbSelectCriterion[] valueListConds =
		    {
		    new VtiExitLdbSelectCondition("ID",
		                                  VtiExitLdbSelectCondition.EQ_OPERATOR, "YSPS_PAYMENT"),
		    new VtiExitLdbSelectCondition("DELETE_IND",
		                                  VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		    };
		        
		    VtiExitLdbSelectConditionGroup valueListCondsGrp =
		        new VtiExitLdbSelectConditionGroup(valueListConds, true);
		  
		    VtiExitLdbTableRow[] valueListLdbRows =
		        VTI_VALUE_LIST.getMatchingRows(valueListCondsGrp);
	
			PaymentType.clearPossibleValues();
			for(int i=0; i<valueListLdbRows.length; i++)
			{
				String strValue = valueListLdbRows[i].getFieldValue("DATA");
				if(!strValue.equals("CHEQUE"))
					PaymentType.addPossibleValue(strValue);
			}
		      
			}
	       if(strChequeBlocked.equalsIgnoreCase("F"))
			{
		      PayKey.setFieldValue("BD");
			  PayDep.setDisplayOnlyFlag(true);
	    	//Get payment type value list
		    VtiExitLdbSelectCriterion[] valueListConds =
		    {
		    new VtiExitLdbSelectCondition("ID",
		                                  VtiExitLdbSelectCondition.EQ_OPERATOR, "YSPS_PAYTYPE"),
		    new VtiExitLdbSelectCondition("DELETE_IND",
		                                  VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		    };
		        
		    VtiExitLdbSelectConditionGroup valueListCondsGrp =
		        new VtiExitLdbSelectConditionGroup(valueListConds, true);
		  
		    VtiExitLdbTableRow[] valueListLdbRows =
		        VTI_VALUE_LIST.getMatchingRows(valueListCondsGrp);	
			
			PayDep.clearPossibleValues();
			for(int i=0; i<valueListLdbRows.length; i++)
			{
				String strValue = valueListLdbRows[i].getFieldValue("DATA");
				if(!strValue.equals("DEPOSIT"))
					PaymentType.addPossibleValue(strValue);
				if(!strValue.equals("DEBIT"))
					PaymentType.addPossibleValue(strValue);				
			}

			}		   
		setCursorPosition(PayAmount);
		return new VtiUserExitResult(000,"Enter an Amount");
	}
}