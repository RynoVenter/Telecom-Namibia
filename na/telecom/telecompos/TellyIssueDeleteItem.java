package na.telecom.telecompos;

import au.com.skytechnologies.vti.VtiExitException;
import au.com.skytechnologies.vti.VtiUserExit;
import au.com.skytechnologies.vti.VtiUserExitResult;
import au.com.skytechnologies.vti.VtiUserExitScreenField;
import au.com.skytechnologies.vti.VtiUserExitScreenTable;
import au.com.skytechnologies.vti.VtiUserExitScreenTableRow;

public class TellyIssueDeleteItem extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		// Data Declarations.     
		VtiUserExitScreenField Deposit = getScreenField("DEPOSIT");	  
		VtiUserExitScreenField TaxField= getScreenField("TAX");
		VtiUserExitScreenField DepositReceived = getScreenField("DEPOSIT_REC");
		VtiUserExitScreenField AmountAlloc = getScreenField("AMOUNT_ALLOC");
		VtiUserExitScreenTable QtyIssueScrTable = getScreenTable("QTY_ISS"); 
		VtiUserExitScreenTable SearchScrTable = getScreenTable("SEARCH");

		if (Deposit == null)
			return new VtiUserExitResult(999, "Screen Field DEPOSIT does not exist");
		if (TaxField == null)
			return new VtiUserExitResult(999, "Screen Field TAX does not exist");

		if (DepositReceived == null)
			return new VtiUserExitResult(999, "Screen Field DEPOSIT_REC does not exist");    

		if (AmountAlloc== null)
			return new VtiUserExitResult(999, "Screen Field AMOUNT_ALLOC does not exist");

		if (QtyIssueScrTable== null)
			return new VtiUserExitResult(999, "Screen table QTY_ISS does not exist");

		if (SearchScrTable== null)
			return new VtiUserExitResult(999, "Screen table SEARCH does not exist");

		boolean blnChkFound = false;
		double  amtAlloc = AmountAlloc.getDoubleFieldValue();
		boolean blnItemDelete = false;
		 

		//Check if any item is checked for deletion
		for(int a=0; a<QtyIssueScrTable.getRowCount(); a++)
		{
			VtiUserExitScreenTableRow qtyIssueRow = QtyIssueScrTable.getRow(a);

			String issueChkBox = qtyIssueRow.getFieldValue("CHK_ALLOCATED");
			String issueMaterial = qtyIssueRow.getFieldValue("A_ITEM");
			double issueAmt    = qtyIssueRow.getDoubleFieldValue("A_AMOUNT");
			double subTaxAmount =qtyIssueRow.getDoubleFieldValue("TAX_AMOUNT");

			//If item is check for deletion
			if(issueChkBox.equals("X"))
			{
				blnChkFound = true;
				//look in search table to enable the issue qty field
				for(int b=0; b<SearchScrTable.getRowCount(); b++)
				{
					VtiUserExitScreenTableRow searchTableRow = SearchScrTable.getRow(b);
					String searchMaterial = searchTableRow.getFieldValue("ITEM");


					if(issueMaterial.equals(searchMaterial))
					{
						b=SearchScrTable.getRowCount();

						searchTableRow.setDisplayOnlyFlag("QTY_ISS", false);
						searchTableRow.setFieldValue("ISSUED", "");
						searchTableRow.setFieldValue("QTY_ISS", "");

						//Recalculate the amount allocated
						//amtAlloc -= issueAmt+subTaxAmount;
						amtAlloc -= issueAmt;
						AmountAlloc.setDoubleFieldValue(amtAlloc);

						blnItemDelete = true;
					}
				}

				//Remove from allocated list
				if(blnItemDelete==true)
				{
					QtyIssueScrTable.deleteRow(qtyIssueRow);
					blnItemDelete = false;
					a-=1;
				}
			}
		}

		if(blnChkFound == false)
			return new VtiUserExitResult(000,"Please select an item to remove");
		else
			return new VtiUserExitResult(000,"Item successfully removed");

	}
}