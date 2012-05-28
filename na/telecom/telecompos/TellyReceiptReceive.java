package na.telecom.telecompos;


import au.com.skytechnologies.vti.VtiExitException;
import au.com.skytechnologies.vti.VtiUserExit;
import au.com.skytechnologies.vti.VtiUserExitResult;
import au.com.skytechnologies.vti.VtiUserExitScreenField;
import au.com.skytechnologies.vti.VtiUserExitScreenTable;
import au.com.skytechnologies.vti.VtiUserExitScreenTableRow;

public class TellyReceiptReceive extends VtiUserExit
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
		if (Tax== null)
			return new VtiUserExitResult(999, "Screen Field TAX does not exist");
		if (TaxValue== null)
			return new VtiUserExitResult(999, "Screen Field TAX_VALUE does not exist");
		
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

		boolean blnItemsReceived = false;
		double dblExpCash = ExpectedCash.getDoubleFieldValue();
		
		float dblTotalTax = 0;
		double dblAmountDue = 0;
		double dblDiscount = Discount.getDoubleFieldValue();
		double dblTax = Tax.getDoubleFieldValue();
		
		for(int a=0; a<AllocScrTable.getRowCount();a++)
		{
			VtiUserExitScreenTableRow allocScrTableRow = AllocScrTable.getRow(a);
			
			String strProcessed = allocScrTableRow.getFieldValue("PROCESSED");
			int intQty = allocScrTableRow.getIntegerFieldValue("QTY");
			int intSold = allocScrTableRow.getIntegerFieldValue("SOLD");
			int intReturned = allocScrTableRow.getIntegerFieldValue("RETURN");
			double dblDiscountPrice = allocScrTableRow.getDoubleFieldValue("DISCOUNT_PRICE");
			int intTotalReceived = 0;
			int intAutoSalesQty = 0;
			
			//Auto-calculate sales quantity
			if (intReturned>0)
			{
				intAutoSalesQty = intReturned - intQty;
				if(intAutoSalesQty<0)
				{
					intAutoSalesQty *=  -1;
				}
			}
			else
			{
				//if there is not sold and return quantity, default to open qty 
				if(intSold==0)
					intSold = intQty;
				
				intAutoSalesQty = intSold;
			}
			
			intTotalReceived = intAutoSalesQty + intReturned;
			
			double dblRRP = allocScrTableRow.getDoubleFieldValue("RRP");
			 
			double dblTotalSoldAmt = intAutoSalesQty * dblRRP;
			double dblTotalSoldAmtInclTax = dblTotalSoldAmt + (dblTotalSoldAmt * dblTax);
			
			double dblTotalSoldDiscAmt = intAutoSalesQty * dblDiscountPrice;
			double dblTotalDiscount = dblTotalSoldAmt - dblTotalSoldDiscAmt;
			float dblTaxAmount = (float)dblTotalSoldDiscAmt * (float)dblTax;
			
			System.out.println("RRP = " + dblRRP);
			System.out.println("Sold in RRP = " + dblTotalSoldAmt);
			System.out.println("Discount price = " + dblDiscountPrice);
			System.out.println("Sold on discount price = " + dblTotalSoldDiscAmt);
			System.out.println("Total Discount " + dblTotalDiscount);
			System.out.println("Tax rate = " + dblTax);
			System.out.println("Total tax = " + dblTaxAmount);
			
			dblTotalTax += dblTaxAmount;
			System.out.println("Floor tax value " + Math.floor(dblTaxAmount));
			if(strProcessed.equals("") && intTotalReceived>0)
			{
				if(intTotalReceived>intQty)
				{
					
					//Before exit, check if anything is received
					if(blnItemsReceived==true)
					{
						dblAmountDue = dblExpCash - dblDiscount;
						AmountDue.setFieldValue(dblAmountDue);
						ExpectedCash.setFieldValue(dblExpCash);
						
						if(dblDiscount>0)
						{
							Discount.setFieldValue(dblDiscount);
						}
						else
							Discount.setFieldValue("");
					}
					
					return new VtiUserExitResult(000,"Received quantity is more than issue quantity");

				}
				
				dblExpCash += dblTotalSoldAmt; 
			
				if(dblTotalDiscount>0)
				{
					dblDiscount+=dblTotalDiscount;
				}
				
				allocScrTableRow.setDisplayOnlyFlag("SOLD", true);
				allocScrTableRow.setDisplayOnlyFlag("RETURN", true);
				allocScrTableRow.setFieldValue("PROCESSED", "X");
				allocScrTableRow.setFieldValue("CHK_SELECTED", "");
				allocScrTableRow.setFieldValue("SOLD", intAutoSalesQty);
				blnItemsReceived = true;
			}
			
		}
	

		if(blnItemsReceived==true)
		{
			dblAmountDue = dblExpCash - dblDiscount + dblTotalTax;
			TaxValue.setFieldValue(dblTotalTax);
			AmountDue.setFieldValue(dblAmountDue);
			ExpectedCash.setFieldValue(dblExpCash);
			
			if(dblDiscount>0)
			{
				Discount.setFieldValue(dblDiscount);
			}
			else
				Discount.setFieldValue("");
			
			return new VtiUserExitResult(000,"Items successfully added");
		}
		else
			return new VtiUserExitResult(000,"No items selected for receipt");

	}
}