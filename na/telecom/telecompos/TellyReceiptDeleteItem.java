package na.telecom.telecompos;


import au.com.skytechnologies.vti.VtiExitException;
import au.com.skytechnologies.vti.VtiUserExit;
import au.com.skytechnologies.vti.VtiUserExitResult;
import au.com.skytechnologies.vti.VtiUserExitScreenField;
import au.com.skytechnologies.vti.VtiUserExitScreenTable;
import au.com.skytechnologies.vti.VtiUserExitScreenTableRow;

public class TellyReceiptDeleteItem extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		// Data Declarations.     
		VtiUserExitScreenField Deposit = getScreenField("DEP");	  
		VtiUserExitScreenField ExpectedCash = getScreenField("EXP_CASH");
		VtiUserExitScreenField Discount = getScreenField("DISC");
		VtiUserExitScreenField AmountDue = getScreenField("AMOUNT_DUE");
		VtiUserExitScreenField TellyNo = getScreenField("TELLY_NO");
		VtiUserExitScreenField TellyName = getScreenField("TELLY_NAME");
		VtiUserExitScreenField TellyType = getScreenField("TELLTYPE");
		VtiUserExitScreenField CurrCust = getScreenField("CURRENT_CUST");
		VtiUserExitScreenField DueDays = getScreenField("DUE_DAYS");
		VtiUserExitScreenField Tax = getScreenField("TAX");
		VtiUserExitScreenField TaxValue = getScreenField("TAX_VALUE");

		VtiUserExitScreenTable AllocScrTable = getScreenTable("ALLOC");

		if (Deposit == null)
			return new VtiUserExitResult(999, "Screen Field DEPOSIT does not exist");
		if (DueDays== null)
			return new VtiUserExitResult(999, "Screen Field DUE_DAYS does not exist");
	
		if (CurrCust== null)
			return new VtiUserExitResult(999, "Screen Field CURRENT_CUST does not exist");

		if (ExpectedCash == null)
			return new VtiUserExitResult(999, "Screen Field EXP_CASH does not exist");    

		if (Discount== null)
			return new VtiUserExitResult(999, "Screen Field DISC does not exist");

		if (AmountDue== null)
			return new VtiUserExitResult(999, "Screen Field AMOUNT_DUE does not exist");
		if (TellyNo== null)
			return new VtiUserExitResult(999, "Screen Field TELLY_NO does not exist");
		if (TellyName== null)
			return new VtiUserExitResult(999, "Screen Field TELLY_NAME does not exist");

		if (TellyType== null)
			return new VtiUserExitResult(999, "Screen Field TELLTYPE does not exist");

		if (AllocScrTable== null)
			return new VtiUserExitResult(999, "Screen table ALLOC does not exist");
		if (Tax== null)
			return new VtiUserExitResult(999, "Screen Field TAX does not exist");
		if (TaxValue== null)
			return new VtiUserExitResult(999, "Screen Field TAX_VALUE does not exist");
		

		boolean blnItemsDeleted = false;
		double dblExpCash = ExpectedCash.getDoubleFieldValue();
		double dblAmountDue = 0;
		double dblDiscount = Discount.getDoubleFieldValue();
		double dblTotalTax = TaxValue.getDoubleFieldValue();
		double dblTax = Tax.getDoubleFieldValue();

		for(int a=0; a<AllocScrTable.getRowCount();a++)
		{
			VtiUserExitScreenTableRow allocScrTableRow = AllocScrTable.getRow(a);
			
			String strProcessed = allocScrTableRow.getFieldValue("PROCESSED");
			double dblSold = allocScrTableRow.getDoubleFieldValue("SOLD");
			
			String strCheckBox = allocScrTableRow.getFieldValue("CHK_SELECTED");
			
			double dblDiscountPrice = allocScrTableRow.getDoubleFieldValue("DISCOUNT_PRICE");
//			double dblRRP = allocScrTableRow.getDoubleFieldValue("RRP");
//			double dblTotalSoldAmt = dblSold * dblRRP;
//			double dblTotalSoldDiscAmt = dblSold * dblDiscountPrice;
//			double dblTotalDiscount = dblTotalSoldAmt - dblTotalSoldDiscAmt;
//			double dblTaxAmount = dblTotalSoldAmt * dblTax;
			
			double dblRRP = allocScrTableRow.getDoubleFieldValue("RRP");
			double dblTotalSoldAmt = dblSold * dblRRP;
			double dblTotalSoldDiscAmt = dblTotalSoldAmt -(dblTotalSoldAmt* dblDiscountPrice);
			double dblTotalDiscount = dblTotalSoldAmt - dblTotalSoldDiscAmt;
			double dblTaxAmount = dblTotalDiscount * dblTax;
		
			
			
			if(strProcessed.equals("X") && strCheckBox.equals("X"))
			{
				
				
				dblTotalTax-=dblTaxAmount;
				
				dblExpCash -= dblTotalSoldAmt; 
								
				if(dblTotalDiscount>0)
				{
					dblDiscount-=dblTotalDiscount;
				}
				
				allocScrTableRow.setDisplayOnlyFlag("RETURN", false);
				allocScrTableRow.setFieldValue("PROCESSED", "");
				allocScrTableRow.setFieldValue("CHK_SELECTED", "");
				blnItemsDeleted = true;
			}
			
		}
	

		if(blnItemsDeleted==true)
		{
			if(dblDiscount>0)
			{
				Discount.setFieldValue(dblDiscount);
			}
			else
				Discount.setFieldValue("");
			
			dblAmountDue = dblExpCash - dblDiscount;
			AmountDue.setFieldValue(dblAmountDue);
			ExpectedCash.setFieldValue(dblExpCash);
			TaxValue.setFieldValue(dblTotalTax);
			
			return new VtiUserExitResult(000,"Items successfully removed");
		}
		else
			return new VtiUserExitResult(000,"No items selected for removal");

	}
}