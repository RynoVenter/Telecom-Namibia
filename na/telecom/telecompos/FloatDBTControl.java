package na.telecom.telecompos;

import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.thread.*;
import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.ecssdk.log.*;

public class FloatDBTControl extends VtiUserExit 
{//1
	
	  public VtiUserExitResult execute() throws VtiExitException
	  {//2
		  //declare local variables
		  int sequenceID = 0;
		  String currUserId = "";
		  int matQty = 0;
		  String matNumber = "";
		  String stEanNr = "";
		  String endEanNr = "";
		  final String rowSel = "X";
		  
	    // Collect  the screen and check if it loaded correctly
		  
		VtiUserExitScreenTable floatTable = getScreenTable("PREPAID");
		 if (floatTable == null)
			return new VtiUserExitResult(999, "Screen Table PREPAID does not exist");	    
		
		 // Get other screenfields
		VtiUserExitScreenField accTime = getScreenField("ONTIME");
		//VtiUserExitScreenField type = getScreenField("TYPE_R");
		VtiUserExitScreenField cashAmnt = getScreenField("CASHAMOUNT");
		VtiUserExitScreenField userID = getScreenField("USERID");
		VtiUserExitScreenField fDate = getScreenField("ONDATE");
	
		//Verify if screenfields were loaded correctly
		// if (type == null)
		//	return new VtiUserExitResult(999, "Screen Field TYPE_R does not exist");
		 if (userID == null)
			return new VtiUserExitResult(999, "Screen Field USERID does not exist");
		 if (fDate == null)
			return new VtiUserExitResult(999, "Screen Field ONDATE does not exist");
		 if (accTime == null)
			return new VtiUserExitResult(999, "Screen Field ONTIME does not exist");
		 if (cashAmnt == null)
			return new VtiUserExitResult(999, "Screen Field CASHAMOUNT does not exist");
		
		
		//Get values from the screen fields.
		String vtiServerId = getVtiServerId();
		String currUser = userID.getFieldValue();
		String currAccTime = accTime.getFieldValue();
		//String currType = type.getFieldValue();
		double currCashAmnt = cashAmnt.getLongFieldValue();
		String currfDate = fDate.getFieldValue();

		 //Get how many rows exists in screen table, value to be used in loop iteration
		int tRowCount = floatTable.getRowCount();
        if(tRowCount == 0)
		   return new VtiUserExitResult(999, "Float table has no content.");

		
		//Get table to use
	    VtiExitLdbTable floatLdbTable = getLocalDatabaseTable("YSPS_FLOAT");
		  if (floatLdbTable == null)
            return new VtiUserExitResult(999, "LDB YSPS_FLOAT does not exist");
							

		//Run through every row and get the data to post, if it needs to post.
		
		int i = 1;  
		while(i <= tRowCount)
		{//3
			
				VtiUserExitScreenTableRow currFloatRow = floatTable.getRow(i);	
		
		if (rowSel.equals(currFloatRow.getFieldValue("SEL_P")))
		  {//4
			
			matNumber = currFloatRow.getFieldValue("ITEM_P");
			matQty = currFloatRow.getIntegerFieldValue("QTY_P");
			stEanNr = currFloatRow.getFieldValue("S_EAN_P");
			endEanNr = currFloatRow.getFieldValue("E_EAN_P");
		
			if (matNumber == null)
				return new VtiUserExitResult(999, "Row Field ITEM_P does not exist");
			if (matQty == 0)
				return new VtiUserExitResult(999, "Row Field QTY_P does not exist");
			if (stEanNr == null)
				return new VtiUserExitResult(999, "Row Field S_EAN_P does not exist");
			if (endEanNr == null)
				return new VtiUserExitResult(999, "Row Field E_EAN_P does not exist");
			
			//Run a querry to see if this material already exists for that date,
			//count how many times that date, then update the sequence field
			//with the new incremented value, even if length of array is zero, just increment and
			//add all the fields for that record into the table.
			
			
					try
					{
						// Select the required records.
						VtiExitLdbSelectCriterion[] floatSelConds =
							{//5
								new VtiExitLdbSelectCondition("MATNR",
                                  VtiExitLdbSelectCondition.EQ_OPERATOR, matNumber),
								new VtiExitLdbSelectCondition("SERVERID",
                                  VtiExitLdbSelectCondition.EQ_OPERATOR, vtiServerId),
								new VtiExitLdbSelectCondition("ONDATE",
                                  VtiExitLdbSelectCondition.EQ_OPERATOR, currfDate),
								new VtiExitLdbSelectCondition("USERID",
                                  VtiExitLdbSelectCondition.EQ_OPERATOR, currUser)
							};//5
			
					
						VtiExitLdbSelectConditionGroup floatSelCondsGrp =
							new VtiExitLdbSelectConditionGroup(floatSelConds, true);
  
						VtiExitLdbTableRow [] floatLdbRows =
							floatLdbTable.getMatchingRows(floatSelCondsGrp);
						
						sequenceID = (floatLdbRows.length)+1;
					}
					catch (VtiExitException ee)
				    {
						return new VtiUserExitResult(999, "Failed to collect matching rows row");
					}
					
		  }//4
		
		
		try
		{
		//Post row to new row in the LDB
			VtiExitLdbTableRow floatLdbTranRow = floatLdbTable.newRow();

			floatLdbTranRow.setFieldValue("SERVERID", vtiServerId);
			floatLdbTranRow.setFieldValue("USERID", currUserId);
			//floatLdbTranRow.setFieldValue("TYPE", currType);
			floatLdbTranRow.setFieldValue("MATNR", matNumber);
			floatLdbTranRow.setFieldValue("MAT_SEQ", sequenceID);
			floatLdbTranRow.setFieldValue("CASH", currCashAmnt);
			floatLdbTranRow.setFieldValue("QTY", matQty);
			floatLdbTranRow.setFieldValue("S_SERNR", stEanNr);
			floatLdbTranRow.setFieldValue("E_SERNR", endEanNr);
			floatLdbTranRow.setFieldValue("C_SERNR", stEanNr);
			floatLdbTranRow.setFieldValue("ONDATE", currfDate);
			// floatLdbTranRow.setFieldValue("ONTIME", vtiServerId);
			floatLdbTranRow.setFieldValue("TIMESTAMP", 0);

			
				floatLdbTable.saveRow(floatLdbTranRow);
		}
		catch (VtiExitException ee)
		{
			return new VtiUserExitResult(999, "Failed to save row");
		}
		i++;
		}//3
		 return new VtiUserExitResult();
	  }//2

	
	
}//1
