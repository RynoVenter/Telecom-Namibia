package na.telecom.telecompos;

import au.com.skytechnologies.vti.VtiExitException;
import au.com.skytechnologies.vti.VtiUserExit;
import au.com.skytechnologies.vti.VtiUserExitResult;
import au.com.skytechnologies.vti.VtiUserExitScreenField;
import au.com.skytechnologies.vti.VtiUserExitScreenTable;
import au.com.skytechnologies.vti.VtiUserExitScreenTableRow;

public class TellyIssueSearch extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		// Data Declarations.     
		VtiUserExitScreenField Deposit = getScreenField("DEPOSIT");	  
		VtiUserExitScreenField DepositReceived = getScreenField("DEPOSIT_REC");
		VtiUserExitScreenField AmountAlloc = getScreenField("AMOUNT_ALLOC");
		VtiUserExitScreenTable QtyIssueScrTable = getScreenTable("QTY_ISS"); 
		VtiUserExitScreenTable SearchScrTable = getScreenTable("SEARCH");

		if (Deposit == null)
			return new VtiUserExitResult(999, "Screen Field DEPOSIT does not exist");

		if (DepositReceived == null)
			return new VtiUserExitResult(999, "Screen Field DEPOSIT_REC does not exist");    

		if (AmountAlloc== null)
			return new VtiUserExitResult(999, "Screen Field AMOUNT_ALLOC does not exist");

		if (QtyIssueScrTable== null)
			return new VtiUserExitResult(999, "Screen table QTY_ISS does not exist");

		if (SearchScrTable== null)
			return new VtiUserExitResult(999, "Screen table SEARCH does not exist");


		//Disable fields for material that has been issued
		for(int a=0; a<SearchScrTable.getRowCount(); a++)
		{
			VtiUserExitScreenTableRow searchTableRow = SearchScrTable.getRow(a);

			String searchMaterial = searchTableRow.getFieldValue("ITEM");
			String searchIssued  = searchTableRow.getFieldValue("ISSUED");

			//only check items without being allocated
			if(!searchIssued.equals("X"))
			{
				for(int b=0; b<QtyIssueScrTable.getRowCount(); b++)
				{
					VtiUserExitScreenTableRow qtyIssueTableRow = QtyIssueScrTable.getRow(b);
					String issueMaterial = qtyIssueTableRow.getFieldValue("A_ITEM");
					double issueQty      = qtyIssueTableRow.getDoubleFieldValue("A_QTY");

					if(issueMaterial.equals(searchMaterial))
					{
						searchTableRow.setFieldValue("ISSUED", "X");
						searchTableRow.setFieldValue("QTY_ISS", issueQty);
						searchTableRow.setDisplayOnlyFlag("QTY_ISS", true);
					}
				}
			}

		}

		return new VtiUserExitResult(000,"Please select items to allocate");

	}
}