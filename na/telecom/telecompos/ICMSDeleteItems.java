package na.telecom.telecompos;

import au.com.skytechnologies.vti.VtiExitException;
import au.com.skytechnologies.vti.VtiExitLdbTable;
import au.com.skytechnologies.vti.VtiUserExit;
import au.com.skytechnologies.vti.VtiUserExitResult;
import au.com.skytechnologies.vti.VtiUserExitScreenField;
import au.com.skytechnologies.vti.VtiUserExitScreenTable;
import au.com.skytechnologies.vti.VtiUserExitScreenTableRow;

public class ICMSDeleteItems extends VtiUserExit
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

		//Screen table declarations
		VtiUserExitScreenTable PaymentScrTable = getScreenTable("PAYMENTS");

		//LDB table declaration
		VtiExitLdbTable YSPS_ICMS_INV = getLocalDatabaseTable("YSPS_ICMS_INV");

		//Variable check
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
		if (TotalDue== null)
			return new VtiUserExitResult(999, "Screen Field TOTAL_DUE does not exist");
	

		boolean blnChkSelected = false;
		int intNewItem = 0;
		double dblTotal = 0;

		//Check if any records are selected
		for(int a=0; a<PaymentScrTable.getRowCount(); a++)
		{
			VtiUserExitScreenTableRow paymentRecord = PaymentScrTable.getRow(a);

			String strChkBox = paymentRecord.getFieldValue("CHK_SEL");
			if(strChkBox.equals("X"))
			{
				blnChkSelected = true;
				a = PaymentScrTable.getRowCount(); //End the loop
			}
		}

		if(blnChkSelected==false)
			return new VtiUserExitResult(999,"Please select items to delete");


		//Loop thru the payment table and remove records
		for(int b=0; b<PaymentScrTable.getRowCount(); b++)
		{
			VtiUserExitScreenTableRow paymentRecord = PaymentScrTable.getRow(b);

			
			String strChkBox = paymentRecord.getFieldValue("CHK_SEL");

			if(strChkBox.equals("X"))
			{
			
				//Remove from table
				PaymentScrTable.deleteRow(paymentRecord);
				b-=1;
				
			}

		}

		//Renumber the item numbers
		for(int d=0; d<PaymentScrTable.getRowCount();d++)
		{
			VtiUserExitScreenTableRow paymentRecord = PaymentScrTable.getRow(d);
			
			intNewItem+=1;
			paymentRecord.setFieldValue("P_ITEM_NO", intNewItem);
					
		}
		
		//Calculate the grand total
		for(int b=0; b<PaymentScrTable.getRowCount();b++)
		{
			VtiUserExitScreenTableRow paymentRecord = PaymentScrTable.getRow(b);
			dblTotal+=paymentRecord.getDoubleFieldValue("P_PAY_AMOUNT");
		}
		
		//Update the total due amount
		TotalDue.setFieldValue(dblTotal);
		
		return new VtiUserExitResult(000,"Items deleted");
	}
}