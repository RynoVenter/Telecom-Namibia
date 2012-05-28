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

public class TellyReceiptEnter extends VtiUserExit
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
		VtiUserExitScreenTable AllocScrTable = getScreenTable("ALLOC");
		
		VtiExitLdbTable YSPS_TELLY_CUST = getLocalDatabaseTable("YSPS_TELLY_CUST");


		if (Deposit == null)
			return new VtiUserExitResult(999, "Screen Field DEPOSIT does not exist");

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

		if (YSPS_TELLY_CUST== null)
			return new VtiUserExitResult(999, "LDB table YSPS_TELLY_CUST does not exist");

		String strTellyNo = TellyNo.getFieldValue();
		String strTellyName = TellyName.getFieldValue();
		String strTellyType = TellyType.getFieldValue();
	
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
				return new VtiUserExitResult(999,"More than 1 customers available. Please select a customer");

			}
			else if(customerLdbRows.length==1)
			{
				TellyName.setFieldValue(customerLdbRows[0].getFieldValue("NAME1"));
				TellyNo.setFieldValue(customerLdbRows[0].getFieldValue("KUNNR"));
				
			}
			else if(customerLdbRows.length==0)
				return new VtiUserExitResult(999,"No customers found");
		}
		

		
		
		return new VtiUserExitResult(000,"");

	}
}