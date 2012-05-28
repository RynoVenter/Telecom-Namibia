package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class ConsolidatedBankDeposit  extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		
		//Screen Field Declarations
		VtiUserExitScreenField scFDate = getScreenField("FR_DATE2");
		VtiUserExitScreenField wrkServGrp = getScreenField("SERVERGRP");
		
		VtiUserExitScreenTable scrTBankDep = getScreenTable("TB_BANKDEP");
		
		//Screen Field Validation
		if (scFDate == null) return new VtiUserExitResult(999, "Screen Field FR_DATE2 does not exist");
		if (wrkServGrp == null) return new VtiUserExitResult(999, "Screen Field SERVERGRP does not exist");
		
		if (scrTBankDep == null) return new VtiUserExitResult(999, "Screen Field TB_BANKDEP does not exist");

		//LDB Declaration
		VtiExitLdbTable ldbLogonLdbTable = getLocalDatabaseTable("YSPS_LOGON");
		VtiExitLdbTable ldbTillChkLdbTable = getLocalDatabaseTable("YSPS_TILL_CHEQUE");
		VtiExitLdbTable ldbTillCshLdbTable = getLocalDatabaseTable("YSPS_TILL_CASH");
  
		if (ldbLogonLdbTable == null) return new VtiUserExitResult(999, "LDB table YSPS_LOGON not found");
		if (ldbTillChkLdbTable == null) return new VtiUserExitResult(999, "LDB table YSPS_TILL_CHEQUE not found");
		if (ldbTillCshLdbTable == null) return new VtiUserExitResult(999, "LDB table YSPS_TILL_CASH not found");
		
		//Attribute & Constant Declaration
		double totCheq = 0;
		
		scrTBankDep.clear();
		
	//LDB Query
		
		//Till Cash Query
		VtiExitLdbSelectCriterion[] cashSelConds =
			{
				new VtiExitLdbSelectCondition("TILL_DATE", VtiExitLdbSelectCondition.EQ_OPERATOR, scFDate.getFieldValue()),
				 new VtiExitLdbSelectCondition("SERVERGRP", VtiExitLdbSelectCondition.EQ_OPERATOR, wrkServGrp.getFieldValue())
			};
        
		 VtiExitLdbSelectConditionGroup cashSelCondGrp = 	new VtiExitLdbSelectConditionGroup(cashSelConds, true);
  
		 VtiExitLdbTableRow[] cashLdbRows = ldbTillCshLdbTable.getMatchingRows(cashSelCondGrp);
		 
		 //End Query
		 if(cashLdbRows.length == 0)
			return new VtiUserExitResult(999,"No recon for the chosen date.");
		 //Get Values and show on screen
		 for(int curRow = 0; curRow < cashLdbRows.length; curRow++)
		 {
			     VtiUserExitScreenTableRow newScreenRow = scrTBankDep.getNewRow();
				 
				 	newScreenRow.setFieldValue("SEL_USER",cashLdbRows[curRow].getFieldValue("INCLUDEPRINT"));
					newScreenRow.setFieldValue("USERID",cashLdbRows[curRow].getFieldValue("USERID"));
					newScreenRow.setFieldValue("SERVER_ID",cashLdbRows[curRow].getFieldValue("SERVER_ID"));
					
					//Till USER Query
					VtiExitLdbSelectCriterion[] logSelConds =
						{
							new VtiExitLdbSelectCondition("USERID",VtiExitLdbSelectCondition.EQ_OPERATOR, cashLdbRows[curRow].getFieldValue("USERID")),
						};
        
						VtiExitLdbSelectConditionGroup logSelCondGrp = new VtiExitLdbSelectConditionGroup(logSelConds, true);
  
						VtiExitLdbTableRow[] logLdbRows = ldbLogonLdbTable.getMatchingRows(logSelCondGrp);
					//End Query

					newScreenRow.setFieldValue("FIRSTNAME",logLdbRows[0].getFieldValue("FIRST_NAME"));
					newScreenRow.setFieldValue("LASTNAME",logLdbRows[0].getFieldValue("LAST_NAME"));
					
					//Till Cheque Query
					VtiExitLdbSelectCriterion[] cheqSelConds =
						{
							new VtiExitLdbSelectCondition("TILL_DATE",	VtiExitLdbSelectCondition.EQ_OPERATOR, scFDate.getFieldValue()),
							new VtiExitLdbSelectCondition("SERVERGRP",VtiExitLdbSelectCondition.EQ_OPERATOR, getServerGroup())
						};
        
					VtiExitLdbSelectConditionGroup cheqSelCondGrp = 	new VtiExitLdbSelectConditionGroup(cheqSelConds, true);
  
					VtiExitLdbTableRow[] cheqLdbRows = ldbTillChkLdbTable.getMatchingRows(cheqSelCondGrp);
					//Sum cheque Totals
								for(int curChq = 0; curChq < cheqLdbRows.length; curChq++)
								{
									totCheq += cheqLdbRows[curChq].getDoubleFieldValue("AMOUNT");
								}
								 
					newScreenRow.setFieldValue("TOT_CHEQUES",totCheq);
					newScreenRow.setFieldValue("TOT_CASH",cashLdbRows[curRow].getFieldValue("TOTAL_CAPTURED"));
					newScreenRow.setFieldValue("FLOAT",cashLdbRows[curRow].getFieldValue("CASH_FLOAT"));
					scrTBankDep.appendRow(newScreenRow);
		 }
		 
		return new VtiUserExitResult(999,"Recon done.");
	}
}
