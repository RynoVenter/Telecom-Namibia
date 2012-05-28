package na.telecom.telecompos;

import au.com.skytechnologies.vti.VtiExitException;
import au.com.skytechnologies.vti.VtiExitLdbTable;
import au.com.skytechnologies.vti.VtiUserExit;
import au.com.skytechnologies.vti.VtiUserExitResult;
import au.com.skytechnologies.vti.VtiUserExitScreenField;
import au.com.skytechnologies.vti.VtiUserExitScreenTable;

public class ICMSNext extends VtiUserExit
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
		VtiUserExitScreenField PayKey = getScreenField("PAYKEY"); 		
		VtiUserExitScreenField PayDep = getScreenField("PAYDEP"); 		

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
		if (PaymentScrTable== null)
			return new VtiUserExitResult(999, "Screen Table PAYMENTS does not exist");
		if (YSPS_ICMS_INV== null)
			return new VtiUserExitResult(999, "LDB YSPS_ICMS_INV does not exist");

		
		//Reset fields
		AccountNo.setFieldValue("");
		TelephoneNo.setFieldValue("");
		Name.setFieldValue("");
		CreditRating.setFieldValue("");
		ChequeBlocked.setFieldValue("");
		PayKey.setFieldValue("");
		PayDep.setDisplayOnlyFlag(false);
		
		setCursorPosition(AccountNo);
		
		return new VtiUserExitResult(000,"Please scan the next account number");

	}
}