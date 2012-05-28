package na.telecom.telecompos;

import java.text.DecimalFormat;

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
import au.com.skytechnologies.vti.VtiUserExitScreenTableRow;

public class TellyIssueAllocate extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		// Data Declarations.     
		VtiUserExitScreenField Deposit = getScreenField("DEPOSIT");	  
		VtiUserExitScreenField DepositReceived = getScreenField("DEPOSIT_REC");
		VtiUserExitScreenField AmountAlloc = getScreenField("AMOUNT_ALLOC");
		VtiUserExitScreenField AmountIssued = getScreenField("AMOUNT_ISSUED");
		VtiUserExitScreenField TaxField= getScreenField("TAX");
		VtiUserExitScreenTable QtyIssueScrTable = getScreenTable("QTY_ISS"); 
		VtiUserExitScreenTable SearchScrTable = getScreenTable("SEARCH");
		VtiExitLdbTable YSPS_GEN_CONFIG = getLocalDatabaseTable("YSPS_GENERAL_CONFIGURATION");

		if (Deposit == null)
			return new VtiUserExitResult(999, "Screen Field DEPOSIT does not exist");
		if (TaxField == null)
			return new VtiUserExitResult(999, "Screen Field TAX does not exist");
		if (DepositReceived == null)
			return new VtiUserExitResult(999, "Screen Field DEPOSIT_REC does not exist");    

		if (AmountAlloc== null)
			return new VtiUserExitResult(999, "Screen Field AMOUNT_ALLOC does not exist");
		if (AmountIssued== null)
			return new VtiUserExitResult(999, "Screen Field AMOUNT_ISSUED does not exist");

		if (QtyIssueScrTable== null)
			return new VtiUserExitResult(999, "Screen table QTY_ISS does not exist");

		if (SearchScrTable== null)
			return new VtiUserExitResult(999, "Screen table SEARCH does not exist");
		if (YSPS_GEN_CONFIG== null)
			return new VtiUserExitResult(999, "LDB table YSPS_GENERAL_CONFIGURATION does not exist");

		double dblAmountAlloc = AmountAlloc.getDoubleFieldValue();
		boolean blnAllocated = false;
		double dblDeposit = Deposit.getDoubleFieldValue();
		double dblAmountIssued = AmountIssued.getDoubleFieldValue();
		double dblTotalAlloc = dblAmountAlloc + dblAmountIssued; 
		double dblTaxRate = TaxField.getDoubleFieldValue(); 
		DecimalFormat df1 = new DecimalFormat("######0.00");
		//Total allocated
		

//		Get tax value
		VtiExitLdbSelectCriterion[] taxCondSelCond =
		{
				new VtiExitLdbSelectCondition("SERVERID",
						VtiExitLdbSelectCondition.EQ_OPERATOR,""),
						new VtiExitLdbSelectCondition("OBJECT",
								VtiExitLdbSelectCondition.EQ_OPERATOR,"SALES ORDER"),
								new VtiExitLdbSelectCondition("TYPE_CODE",
										VtiExitLdbSelectCondition.EQ_OPERATOR, "VAT"),
										new VtiExitLdbSelectCondition("DEL_IND",
												VtiExitLdbSelectCondition.NE_OPERATOR, "X"),

		};

		VtiExitLdbSelectConditionGroup taxCondSelCondGrp =
			new VtiExitLdbSelectConditionGroup(taxCondSelCond, true);


		VtiExitLdbTableRow[] taxCondLDBRow =
			YSPS_GEN_CONFIG.getMatchingRows(taxCondSelCondGrp);

		if(taxCondLDBRow.length==0)
			return new VtiUserExitResult(999,"Sales VAT value not found in general configuration");

		dblTaxRate = taxCondLDBRow[0].getDoubleFieldValue("ATTRIBUTE1");
		
		TaxField.setFieldValue(dblTaxRate);
		
		
		//check for issue quantity
		for(int a=0; a<SearchScrTable.getRowCount(); a++)
		{
			VtiUserExitScreenTableRow searchTableRow = SearchScrTable.getRow(a);
			
			int searchQtyIssue = searchTableRow.getIntegerFieldValue("QTY_ISS");
			double searchRRP = searchTableRow.getDoubleFieldValue("RRP");
			double subAmountAlloc = 0;
			String searchMaterial = searchTableRow.getFieldValue("ITEM");
			String searchMaterialDesc = searchTableRow.getFieldValue("DESC");
			String searchIssued  = searchTableRow.getFieldValue("ISSUED");
			int searchTakeOnQty = searchTableRow.getIntegerFieldValue("QTY");
			double subTaxAmount = 0;
			
			//if issue quantity is more than 0, check if the record exist in qty issue table
			if(searchQtyIssue> 0 && searchIssued.equals(""))
			{
				if(searchQtyIssue > searchTakeOnQty)
					return new VtiUserExitResult(999,"Issue quantity is more than Take On quantity");
				
				subAmountAlloc = searchQtyIssue * searchRRP;
				subTaxAmount = subAmountAlloc * dblTaxRate;
				
			
				
				//dblTotalAlloc += (subAmountAlloc+subTaxAmount); 
				//dblAmountAlloc+= (subAmountAlloc+subTaxAmount);
				dblTotalAlloc += (subAmountAlloc); 
				dblAmountAlloc+= (subAmountAlloc);
				
				if(dblTotalAlloc>dblDeposit)
				{
					return new VtiUserExitResult(999,"Amount allocated is more than deposit amount");

				}
				
				AmountAlloc.setDoubleFieldValue(dblAmountAlloc);
				
				VtiUserExitScreenTableRow qtyIssueTableRow = QtyIssueScrTable.getNewRow();
				qtyIssueTableRow.setFieldValue("A_ITEM", searchMaterial);
				qtyIssueTableRow.setFieldValue("A_DESC", searchMaterialDesc);
				qtyIssueTableRow.setFieldValue("A_QTY", String.valueOf(searchQtyIssue));
				qtyIssueTableRow.setFieldValue("A_AMOUNT", df1.format(subAmountAlloc));
				qtyIssueTableRow.setFieldValue("TAX_AMOUNT", df1.format(subTaxAmount));
				
				QtyIssueScrTable.appendRow(qtyIssueTableRow);
				
				
				searchTableRow.setFieldValue("ISSUED", "X");
				searchTableRow.setDisplayOnlyFlag("QTY_ISS", true);
				blnAllocated = true;
			}

		}

		if(blnAllocated == true)
		{
			return new VtiUserExitResult(000,"Items were successfully allocated");
		}
		else
		{
			return new VtiUserExitResult(000,"There were no items to allocate");
		}
		
	}
}