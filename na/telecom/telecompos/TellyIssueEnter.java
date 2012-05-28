package na.telecom.telecompos;

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

public class TellyIssueEnter extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		// Data Declarations.     
		VtiUserExitScreenField Deposit = getScreenField("DEPOSIT");	  
		VtiUserExitScreenField DepositReceived = getScreenField("DEPOSIT_REC");
		VtiUserExitScreenField AmountIssued = getScreenField("AMOUNT_ISSUED");
		VtiUserExitScreenField AmountAlloc = getScreenField("AMOUNT_ALLOC");
		VtiUserExitScreenField TellyNo = getScreenField("TELLY_NO");
		VtiUserExitScreenField TellyName = getScreenField("TELLY_NAME");
		VtiUserExitScreenField TellyType = getScreenField("TELLTYPE");
		VtiUserExitScreenTable QtyIssueScrTable = getScreenTable("QTY_ISS"); 
		VtiUserExitScreenTable SearchScrTable = getScreenTable("SEARCH");
		VtiUserExitScreenField DescSearch = getScreenField("DESC_S");

		VtiExitLdbTable YSPS_TELLY_CUST = getLocalDatabaseTable("YSPS_TELLY_CUST");


		if (Deposit == null)
			return new VtiUserExitResult(999, "Screen Field DEPOSIT does not exist");

		if (DepositReceived == null)
			return new VtiUserExitResult(999, "Screen Field DEPOSIT_REC does not exist");    

		if (AmountAlloc== null)
			return new VtiUserExitResult(999, "Screen Field AMOUNT_ALLOC does not exist");

		if (AmountIssued== null)
			return new VtiUserExitResult(999, "Screen Field AMOUNT_ISSUED does not exist");
		if (TellyNo== null)
			return new VtiUserExitResult(999, "Screen Field TELLY_NO does not exist");
		if (TellyName== null)
			return new VtiUserExitResult(999, "Screen Field TELLY_NAME does not exist");
		
		if (TellyType== null)
			return new VtiUserExitResult(999, "Screen Field TELLTYPE does not exist");
		if (DescSearch== null)
			return new VtiUserExitResult(999, "Screen Field DESC_S does not exist");

		if (QtyIssueScrTable== null)
			return new VtiUserExitResult(999, "Screen table QTY_ISS does not exist");

		if (SearchScrTable== null)
			return new VtiUserExitResult(999, "Screen table SEARCH does not exist");

		if (YSPS_TELLY_CUST== null)
			return new VtiUserExitResult(999, "LDB table YSPS_TELLY_CUST does not exist");

		String strTellyNo = TellyNo.getFieldValue();
		String strTellyName = TellyName.getFieldValue();
		String strTellyType = TellyType.getFieldValue();
		double dblDeposit = Deposit.getDoubleFieldValue();
		double dblDepositReceived = DepositReceived.getDoubleFieldValue();
		double dblAmountIssued = AmountIssued.getDoubleFieldValue();
		double dblAmountAlloc  = AmountAlloc.getDoubleFieldValue();
		double dblTotalAlloc = 0;
		
		
		
		if(DepositReceived.getFieldValue().equals(""))
			DepositReceived.setDisplayOnlyFlag(true);
		
		//Check input
		if(strTellyType.equals(""))
		{
			setCursorPosition(TellyType);
			return new VtiUserExitResult(999,"Please select a Telly Type");
		}
		
		if(strTellyNo.equals("") && strTellyName.equals(""))
		{
			
			setCursorPosition(TellyNo);
			return new VtiUserExitResult(999,"Please select a customer");
		}

		if(strTellyNo.equals("") && !strTellyName.equals(""))
		{
			
			VtiExitLdbSelectCriterion[] customerSelCond =
			{
					new VtiExitLdbSelectCondition("NAME1",
							VtiExitLdbSelectCondition.CS_OPERATOR, strTellyName),
							new VtiExitLdbSelectCondition("TELLY_TYPE",
									VtiExitLdbSelectCondition.EQ_OPERATOR, strTellyType),
									

			};

			VtiExitLdbSelectConditionGroup customerSelCondGrp =
				new VtiExitLdbSelectConditionGroup(customerSelCond, true);

		
			VtiExitLdbTableRow[] customerLdbRows =
				YSPS_TELLY_CUST.getMatchingRows(customerSelCondGrp);

			//if more than 1 record, open search customer screen
			if(customerLdbRows.length>1)
			{
//				VtiUserExitHeaderInfo sessionHeader = getHeaderInfo();
//				sessionHeader.setNextFunctionId("YSPS_TY_CUS_SRC");
				return new VtiUserExitResult(000,"More than 1 customers available. Please select a customer");

			}
			else if(customerLdbRows.length==1)
			{
				TellyName.setFieldValue(customerLdbRows[0].getFieldValue("NAME1"));
				TellyNo.setFieldValue(customerLdbRows[0].getFieldValue("KUNNR"));
				
			}
			else if(customerLdbRows.length==0)
				return new VtiUserExitResult(000,"No customers found");
		}
		
		if(dblDeposit==0 && dblDepositReceived==0)
		{   
			DepositReceived.setDisplayOnlyFlag(false);
			setCursorPosition(DepositReceived);
			return new VtiUserExitResult(999,"Enter the customer's deposit");
		}
		
		if(dblDepositReceived==0 && dblDeposit != 0)
		{
			DepositReceived.setDisplayOnlyFlag(true);
		}
		
		//If the deposit is received from customer, 
		//show the deposit in the deposit received
		if(dblDeposit==0 && dblDepositReceived>0)
		{
			dblDeposit = dblDepositReceived;
		Deposit.setFieldValue(dblDeposit);
		}
		
		dblTotalAlloc = dblAmountAlloc + dblAmountIssued;
		
		if(dblTotalAlloc>dblDeposit)
		{
			setCursorPosition(TellyNo);
			return new VtiUserExitResult(999,"Amount allocated is more than deposit amount");	
		}
		setCursorPosition(DescSearch);
		
		return new VtiUserExitResult(000,"Please select items to allocate");

	}
}