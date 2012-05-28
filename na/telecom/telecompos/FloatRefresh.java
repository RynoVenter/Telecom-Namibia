package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.ecssdk.log.*;

public class FloatRefresh extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		//Screen Field Declarations
		VtiUserExitScreenField scFUser = getScreenField("USERIDT");
		VtiUserExitScreenField scFDate = getScreenField("DATE");
		VtiUserExitScreenField scFFName = getScreenField("FIRST_NAME");
		VtiUserExitScreenField scFLName = getScreenField("LAST_NAME");
		VtiUserExitScreenField scFLDate = getScreenField("LAST_DATE");
		VtiUserExitScreenField scFLTime = getScreenField("LAST_TIME");
		VtiUserExitScreenField scFCashAmount = getScreenField("CASHAMOUNT");
		VtiUserExitScreenField wrkFCards = getScreenField("C_CARDS");
		VtiUserExitScreenField wrkTp = getScreenField("TYPE_P");
		
		VtiUserExitScreenTable scrTPrePaid = getScreenTable("PREPAID");
		
		//Screen Field Validation
		if (scFUser == null) return new VtiUserExitResult(999, "Screen Field USERIDT does not exist");
		if (scFDate == null) return new VtiUserExitResult(999, "Screen Field DATE does not exist");
		if (scFFName == null) return new VtiUserExitResult(999, "Screen Field FIRST_NAME does not exist");
		if (scFLName == null) return new VtiUserExitResult(999, "Screen Field LAST_NAME does not exist");
		if (scFLDate == null) return new VtiUserExitResult(999, "Screen Field LAST_DATE does not exist");
		if (scFLTime == null) return new VtiUserExitResult(999, "Screen Field LAST_TIME does not exist");
		if (scFCashAmount == null) return new VtiUserExitResult(999, "Screen Field CASHAMOUNT does not exist");
		if (wrkFCards == null) return new VtiUserExitResult(999, "Screen Field C_CARDS does not exist");
		
		if (scrTPrePaid == null) return new VtiUserExitResult(999, "Screen Field PREPAID does not exist");

		//LDB Declaration
		VtiExitLdbTable materialLdbTable = getLocalDatabaseTable("YSPS_MATERIAL");
		VtiExitLdbTable floatLdbTable = getLocalDatabaseTable("YSPS_FLOAT");
  
		if (materialLdbTable == null) return new VtiUserExitResult(999, "LDB table YSPS_MATERIAL not found");
		if (floatLdbTable == null) return new VtiUserExitResult(999, "LDB table YSPS_FLOAT not found");
		
		//Attribute & Constant Declaration
		VtiUserExitScreenTableRow itemRow;

		//Prepair screen table to be filled with new values
		scrTPrePaid.clear();
		
		//Table Query
		
		
		//Material Query & sreen table population
		VtiExitLdbSelectCriterion[] materialSelConds =
			{
				new VtiExitLdbSelectCondition("MATKL", VtiExitLdbSelectCondition.EQ_OPERATOR, wrkFCards.getFieldValue()),
			};
        
		 VtiExitLdbSelectConditionGroup materialSelCondGrp = new VtiExitLdbSelectConditionGroup(materialSelConds, true);
  
		 VtiExitLdbTableRow[] materialLdbRows = materialLdbTable.getMatchingRows(materialSelCondGrp);
		 
		 for(int matI = 0;matI < materialLdbRows.length;matI++)
		 {
			 
			 itemRow = scrTPrePaid.getNewRow();
			 itemRow.setFieldValue("DESC_P",materialLdbRows[matI].getFieldValue("MAT_DESC"));
			 itemRow.setFieldValue("ITEM_P",materialLdbRows[matI].getFieldValue("MATERIAL"));
			 itemRow.setFieldValue("QTY_P",materialLdbRows[matI].getFieldValue("TAKEON_QTY"));
			 scrTPrePaid.appendRow(itemRow);
		 }
		
		 //Float Query & screen table update with float allocation for the user
		 for(int pupI = 0;pupI < scrTPrePaid.getRowCount();pupI++)
		 {
			 itemRow = scrTPrePaid.getRow(pupI);
			 
			 VtiExitLdbSelectCriterion[] floatSelConds =
				{
					new VtiExitLdbSelectCondition("MATNR", VtiExitLdbSelectCondition.EQ_OPERATOR, itemRow.getFieldValue("ITEM_P")),
					new VtiExitLdbSelectCondition("ONDATE", VtiExitLdbSelectCondition.EQ_OPERATOR, scFDate.getFieldValue()),
					new VtiExitLdbSelectCondition("SERVERID", VtiExitLdbSelectCondition.EQ_OPERATOR, getVtiServerId()),
					new VtiExitLdbSelectCondition("TYPE", VtiExitLdbSelectCondition.EQ_OPERATOR, wrkTp.getFieldValue()),
					new VtiExitLdbSelectCondition("USERID", VtiExitLdbSelectCondition.EQ_OPERATOR, scFUser.getFieldValue()),
				};
        
			 VtiExitLdbSelectConditionGroup floatSelCondGrp = new VtiExitLdbSelectConditionGroup(floatSelConds, true);
  
			 VtiExitLdbTableRow[] floatLdbRows = floatLdbTable.getMatchingRows(floatSelCondGrp);
		 
			 if(floatLdbRows.length > 0)
			 {
				 itemRow.setFieldValue("QTY_P",floatLdbRows[0].getFieldValue("QTY"));
				 itemRow.setFieldValue("SEL_P","X");
				 scFCashAmount.setDoubleFieldValue(floatLdbRows[0].getDoubleFieldValue("CASH"));
			 }
		 }

		return new VtiUserExitResult();
	}
}
