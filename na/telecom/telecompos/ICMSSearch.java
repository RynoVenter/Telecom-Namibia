package na.telecom.telecompos;

import au.com.skytechnologies.vti.VtiExitException;
import au.com.skytechnologies.vti.VtiExitLdbTable;
import au.com.skytechnologies.vti.VtiUserExit;
import au.com.skytechnologies.vti.VtiUserExitResult;
import au.com.skytechnologies.vti.VtiUserExitScreenField;
import au.com.skytechnologies.vti.VtiUserExitScreenTable;
import au.com.skytechnologies.vti.VtiUserExitScreenTableRow;

public class ICMSSearch extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		// Data Declarations.     
		VtiUserExitScreenField AccountNo = getScreenField("ACCOUNT_NO");	  
		VtiUserExitScreenField Name = getScreenField("NAME");
		VtiUserExitScreenField TelephoneNo = getScreenField("TELEPHONE_NO");
		VtiUserExitScreenField CreditRating = getScreenField("CRRATE");
		VtiUserExitScreenField PaymentType = getScreenField("PAY_TYPE"); 

		//Screen table declarations
		VtiUserExitScreenTable OutstandingInvScrTable = getScreenTable("OUTSTANDING_INV");
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
		if (OutstandingInvScrTable== null)
			return new VtiUserExitResult(999, "Screen Table OUTSTANDING_INV does not exist");
		if (PaymentScrTable== null)
			return new VtiUserExitResult(999, "Screen Table PAYMENTS does not exist");
		if (YSPS_ICMS_INV== null)
			return new VtiUserExitResult(999, "LDB YSPS_ICMS_INV does not exist");

		//Populate the screen table
		for(int a=0; a<OutstandingInvScrTable.getRowCount(); a++)
		{
			VtiUserExitScreenTableRow OutstandingTableRow = OutstandingInvScrTable.getRow(a);

			String strInvoiceNo = OutstandingTableRow.getFieldValue("INVOICE_NO");
			String strPayDep = OutstandingTableRow.getFieldValue("PAYDEP");
			double dblInvoiceAmt = OutstandingTableRow.getDoubleFieldValue("INVOICE_AMT");
			double dblPaymentAmt = OutstandingTableRow.getDoubleFieldValue("PAYMENT_AMOUNT");
			boolean blnFound = false;

			//Check if this entry exists in the Payment table
			for(int b=0; b<PaymentScrTable.getRowCount();b++)
			{
				VtiUserExitScreenTableRow paymentRecord = PaymentScrTable.getRow(b);
				String strInvoiceNoPayment = paymentRecord.getFieldValue("P_INVOICE_NO");

				if(strInvoiceNoPayment.equals(strInvoiceNo))
				{
					blnFound = true;
					b=PaymentScrTable.getRowCount(); //end the loop
					dblPaymentAmt = paymentRecord.getDoubleFieldValue("P_PAY_AMOUNT");
					strPayDep = paymentRecord.getFieldValue("P_PAY_DEP");
				}

			}

			if(blnFound == true)
			{
				OutstandingTableRow.setFieldValue("PAYMENT_AMOUNT", dblPaymentAmt);
				OutstandingTableRow.setFieldValue("PROCESSED", "X");
				OutstandingTableRow.setFieldValue("PAYDEP", strPayDep);
				OutstandingTableRow.setDisplayOnlyFlag("PAYMENT_AMOUNT", true);
				OutstandingTableRow.setDisplayOnlyFlag("PAYDEP", true);

			}
			else
			{
				if(dblPaymentAmt==0)
				{
					OutstandingTableRow.setFieldValue("PAYMENT_AMOUNT", dblInvoiceAmt);
				}
			}

		}

		if(OutstandingInvScrTable.getRowCount()>0)
		{
			AccountNo.setDisplayOnlyFlag(true);
			TelephoneNo.setDisplayOnlyFlag(true);
		}
		
		return new VtiUserExitResult(000,"Outstanding invoices retrieved");

	}
}