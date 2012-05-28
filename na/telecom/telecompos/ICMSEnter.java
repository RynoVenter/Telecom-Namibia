package na.telecom.telecompos;

import java.util.Vector;

import au.com.skytechnologies.vti.VtiExitException;
import au.com.skytechnologies.vti.VtiExitLdbSelectCondition;
import au.com.skytechnologies.vti.VtiExitLdbSelectConditionGroup;
import au.com.skytechnologies.vti.VtiExitLdbSelectCriterion;
import au.com.skytechnologies.vti.VtiExitLdbTable;
import au.com.skytechnologies.vti.VtiExitLdbTableRow;
import au.com.skytechnologies.vti.VtiUserExit;
import au.com.skytechnologies.vti.VtiUserExitResult;
import au.com.skytechnologies.vti.VtiUserExitScreenField;
import au.com.skytechnologies.vti.VtiUserExitScreenTable;


public class ICMSEnter extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		// Data Declarations.     
		VtiUserExitScreenField AccountNo = getScreenField("ACCOUNT_NO");	  
		VtiUserExitScreenField Name = getScreenField("NAME");
		VtiUserExitScreenField TelephoneNo = getScreenField("TELEPHONE_NO");
		VtiUserExitScreenField CreditRating = getScreenField("CRRATE");
		VtiUserExitScreenField PaymentType = getScreenField("PAY_TYPE"); 
		VtiUserExitScreenField ChequeBlocked = getScreenField("CHEQUE_BLOCKED");

		//Screen table declarations
		VtiUserExitScreenTable PaymentScrTable = getScreenTable("PAYMENTS");

		//LDB table declaration
		VtiExitLdbTable YSPS_ICMS_INV = getLocalDatabaseTable("YSPS_ICMS_INV");
		VtiExitLdbTable VTI_VALUE_LIST = getLocalDatabaseTable("VTI_VALUE_LIST");

		if (AccountNo == null)
			return new VtiUserExitResult(999, "Screen Field ACCOUNT_NO does not exist");
		if (Name == null)
			return new VtiUserExitResult(999, "Screen Field NAME does not exist");    
		if (TelephoneNo== null)
			return new VtiUserExitResult(999, "Screen Field TELEPHONE_NO does not exist");
		if (CreditRating== null)
			return new VtiUserExitResult(999, "Screen Field CRRATE does not exist");
		if (ChequeBlocked== null)
			return new VtiUserExitResult(999, "Screen Field CHEQUE_BLOCKED does not exist");
		if (PaymentType== null)
			return new VtiUserExitResult(999, "Screen Field PAYMENT_TYPE does not exist");
		if (PaymentScrTable== null)
			return new VtiUserExitResult(999, "Screen Table PAYMENTS does not exist");
		if (YSPS_ICMS_INV== null)
			return new VtiUserExitResult(999, "LDB YSPS_ICMS_INV does not exist");
		if (VTI_VALUE_LIST== null)
			return new VtiUserExitResult(999, "LDB VTI_VALUE_LIST does not exist");

		
		String strAccountNo = AccountNo.getFieldValue();
		String strTelNo = TelephoneNo.getFieldValue();
		String strChequeBlocked = ChequeBlocked.getFieldValue();
		
		//Check input
		if(strAccountNo.equals("") && strTelNo.equals(""))
		{
			setCursorPosition(AccountNo);
			return new VtiUserExitResult(999,"Please enter the account or telephone number");
		}
		
		//Check account algorithm
		VtiUserExitScreenField CompCode = getScreenField("COMP_CODE");
		VtiUserExitScreenField CompDesc= getScreenField("COMP_DESC");
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
				if(AccountNo.getFieldValue().length() != 10)
						return new VtiUserExitResult(999,"Account is not 10 characters long");
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
		//Get payment type value list
		  VtiExitLdbSelectCriterion[] valueListConds =
		    {
				new VtiExitLdbSelectCondition("ID",VtiExitLdbSelectCondition.EQ_OPERATOR, "YSPS_PAYMENT"),
					new VtiExitLdbSelectCondition("DELETE_IND",VtiExitLdbSelectCondition.NE_OPERATOR, "X")
		    };
		        
		    VtiExitLdbSelectConditionGroup valueListCondsGrp = new VtiExitLdbSelectConditionGroup(valueListConds, true);
		  
		    VtiExitLdbTableRow[] valueListLdbRows = VTI_VALUE_LIST.getMatchingRows(valueListCondsGrp);

			if(strChequeBlocked.equals("C") || (strChequeBlocked.equals("F")))
		{
			PaymentType.clearPossibleValues();
			for(int ii=0; ii<valueListLdbRows.length; ii++)
			{
				String strValue = valueListLdbRows[ii].getFieldValue("DATA");
				if(!strValue.equals("CHEQUE"))
					PaymentType.addPossibleValue(strValue);
			}
			
		}
		else
		{
			PaymentType.clearPossibleValues();
			for(int e=0; e<valueListLdbRows.length; e++)
			{
				String strValue = valueListLdbRows[e].getFieldValue("DATA");
				PaymentType.addPossibleValue(strValue);
			}
		}
		return new VtiUserExitResult(000,"");
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