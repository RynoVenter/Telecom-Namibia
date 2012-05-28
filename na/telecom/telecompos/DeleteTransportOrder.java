package na.telecom.telecompos;

import java.util.*;
import java.text.*;

import au.com.skytechnologies.ecssdk.util.*;
import au.com.skytechnologies.vti.*;
import au.com.skytechnologies.ecssdk.log.*;

public class DeleteTransportOrder extends VtiUserExit
	
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		//Check screenfields
		VtiUserExitScreenTable issue = getScreenTable("TR_ISSUE");
		VtiUserExitScreenTable items = getScreenTable("TR_ITEMS");
		VtiUserExitScreenField tran = getScreenField("CURR_TRAN");
		VtiUserExitScreenField servGrp = getScreenField("C_SERVGRP");
		VtiUserExitScreenField servId = getScreenField("C_SERVID");
		VtiUserExitScreenField stoI = getScreenField("C_STO_D");
		VtiUserExitScreenField vtiRef = getScreenField("VTI_REF");
		VtiUserExitScreenField trNo = getScreenField("WA_TRNO");
		VtiUserExitScreenField tranType = getScreenField("TRAN_TYPE_D");
		VtiUserExitScreenField frmStore = getScreenField("STORE");
		VtiUserExitScreenField timeS = getScreenField("TIMESTAMP");
		VtiUserExitScreenField testF1 = getScreenField("TEST1");
		VtiUserExitScreenField testF2 = getScreenField("TEST2");
		
		if (issue == null)
			return new VtiUserExitResult(999, "The following screen element does not exist : TR_ISSUE");
		if (items == null)
			return new VtiUserExitResult(999, "The following screen element does not exist : TR_ITEMS");
		if (tran == null)
			return new VtiUserExitResult(999, "The following screen element does not exist : CURR_TRAN");
		if (servGrp == null)
			return new VtiUserExitResult(999, "The following screen element does not exist : C_SERVGRP");
		if (servId == null)
			return new VtiUserExitResult(999, "The following screen element does not exist : C_SERVID");
		if (stoI == null)
			return new VtiUserExitResult(999, "The following screen element does not exist : C_STO_I");
		if (vtiRef == null)
			return new VtiUserExitResult(999, "The following screen element does not exist : VTI_REF");
		if (trNo == null)
			return new VtiUserExitResult(999, "The following screen element does not exist : WA_TRNO");
		if (tranType == null)
			return new VtiUserExitResult(999, "The following screen element does not exist : TRAN_TYPE_I");
		if (frmStore == null)
			return new VtiUserExitResult(999, "The following screen element does not exist : TRAN_TYPE_I");

		
		//Class Declarations
		String toStore;
		int tRowCount = items.getRowCount();
		if(tRowCount == 0)
			return new VtiUserExitResult(999,"No Items to Delete");
		
		VtiUserExitScreenTableRow itemRow = items.getRow(0);
		VtiUserExitScreenTableRow issueRow;
		final String chkItem = "X";
		boolean process = false;
		int incCounter = 0;
		int incRow = 0;
		
		String nVtiNo = "";
			long nVtiRef = 0;		
				try
				{
					nVtiRef = getNextNumberFromNumberRange("YSPS_STO");
				}
				catch (VtiExitException ee)
				{
					return new VtiUserExitResult(999, "Failed to Get Order Number");
				}
        
		nVtiNo = Long.toString(nVtiRef);
					
		//Get info and Update the LDB's
		
		//Update YSPS_STO_HEADER
		//Get table to use
		VtiExitLdbTable issueHeaderLdbTable = getLocalDatabaseTable("YSPS_STO_HEADER");
			if (issueHeaderLdbTable == null)
				return new VtiUserExitResult(999, "LDB YSPS_STO_HEADER does not exist");
			
		try
		{
		// Select the required records.
			VtiExitLdbSelectCriterion[] headerSelConds =
			{
				new VtiExitLdbSelectCondition("TRANS_NO",VtiExitLdbSelectCondition.EQ_OPERATOR,tran.getFieldValue()),
				new VtiExitLdbSelectCondition("SERVERGROUP",VtiExitLdbSelectCondition.EQ_OPERATOR,servGrp.getFieldValue()),
				new VtiExitLdbSelectCondition("SERVERID",VtiExitLdbSelectCondition.EQ_OPERATOR,servId.getFieldValue()),
				new VtiExitLdbSelectCondition("PO_NUMBER",VtiExitLdbSelectCondition.EQ_OPERATOR,trNo.getFieldValue()),
			};
			
			VtiExitLdbSelectConditionGroup headerSelCondsGrp = new VtiExitLdbSelectConditionGroup(headerSelConds, true);
  			VtiExitLdbTableRow [] headerLdbRows = issueHeaderLdbTable.getMatchingRows(headerSelCondsGrp);
			headerLdbRows[0].setFieldValue("STATUS", stoI.getFieldValue());
			headerLdbRows[0].setFieldValue("VTI_REF", nVtiNo);
			headerLdbRows[0].setFieldValue("TIMESTAMP",timeS.getFieldValue());
			issueHeaderLdbTable.saveRow(headerLdbRows[0]);
		}
		catch (VtiExitException ee)
		{
			return new VtiUserExitResult(999, "Update of YSPS_STO_HEADER failed.");
		}
		
		
		//Update YSPS_STO_ITEMS	
		
		while(incRow < tRowCount)
		{
			itemRow = items.getRow(incRow);
			
			//if (chkItem.equals(itemRow.getFieldValue("IT_SEL")))
			{
				VtiExitLdbTable issueItemsLdbTable = getLocalDatabaseTable("YSPS_STO_ITEMS");
					if (issueItemsLdbTable == null)
						 return new VtiUserExitResult(999, "LDB YSPS_STO_ITEMS does not exist");
				
				try
				{
					
				// Select the required records.					
					VtiExitLdbSelectCriterion[] itemsSelConds =
					{
						new VtiExitLdbSelectCondition("TRANS_NO",VtiExitLdbSelectCondition.EQ_OPERATOR,tran.getFieldValue()),
						new VtiExitLdbSelectCondition("SERVERGROUP",VtiExitLdbSelectCondition.EQ_OPERATOR,servGrp.getFieldValue()),
						new VtiExitLdbSelectCondition("SERVERID",VtiExitLdbSelectCondition.EQ_OPERATOR,servId.getFieldValue()),
						new VtiExitLdbSelectCondition("PO_NUMBER",VtiExitLdbSelectCondition.EQ_OPERATOR,trNo.getFieldValue()),
						new VtiExitLdbSelectCondition("MATNR",VtiExitLdbSelectCondition.EQ_OPERATOR,itemRow.getFieldValue("TO_ITEM")),
					};
			
					VtiExitLdbSelectConditionGroup itemsSelCondsGrp = new VtiExitLdbSelectConditionGroup(itemsSelConds, true);
  					VtiExitLdbTableRow [] itemsLdbRows = issueItemsLdbTable.getMatchingRows(itemsSelCondsGrp);
					
					int arElCnt = itemsLdbRows.length;
					
					
					int upR = 0;
					
					while(upR<arElCnt)
					{
						itemsLdbRows[upR].setFieldValue("STATUS", stoI.getFieldValue());
						itemsLdbRows[upR].setFieldValue("VTI_REF", nVtiNo);
						itemsLdbRows[upR].setFieldValue("TIMESTAMP",timeS.getFieldValue());
						itemsLdbRows[upR].setFieldValue("TR_COMMENT",itemRow.getFieldValue("ISSUE_COMMENT"));
						itemsLdbRows[upR].setFieldValue("PO_QTY",itemRow.getFieldValue("ISSUE_QTY"));
						issueItemsLdbTable.saveRow(itemsLdbRows[upR]);
						upR++;
					}
					
				}
				catch (VtiExitException ee)
				{
					return new VtiUserExitResult(999, "Update of YSPS_STO_ITEMS failed.");
				}
				
				//Add new row to YSPS_TRAN_QUEUE
		
						String mTo = itemRow.getFieldValue("ISS_TO");
						//get the Issue To branch
						int mRow  = 0;
						int i = 0;
							while(i<issue.getRowCount())
								{
								issueRow = issue.getRow(i);
			  
									if(issueRow.getFieldValue("TO_TRNO") == mTo)
									{
										mRow = i;
									}
									i++;
								}
	
				issueRow = issue.getRow(mRow);
				VtiExitLdbTable tranQueueLdbTable = getLocalDatabaseTable("YSPS_TRAN_QUEUE");
					if (tranQueueLdbTable == null)
						return new VtiUserExitResult(999, "LDB YSPS_TRAN_QUEUE does not exist");
					
				VtiExitLdbTableRow tranQLdbTranRow = tranQueueLdbTable.newRow();
				
					String nTranNo = "";
					long order = 0;		
							try
							{
								order = getNextNumberFromNumberRange("YSPS_TRAN_NO");
							}
							catch (VtiExitException ee)
							{
								return new VtiUserExitResult(999, "Failed to Get Order Number");
							}
        
							nTranNo = Long.toString(order);

		
				tranQLdbTranRow.setFieldValue("SERVER_GROUP",servGrp.getFieldValue());
				tranQLdbTranRow.setFieldValue("SERVERID",servId.getFieldValue());
				tranQLdbTranRow.setFieldValue("FROM_PLANT",frmStore.getFieldValue());
				tranQLdbTranRow.setFieldValue("TIMESTAMP",timeS.getFieldValue());
				tranQLdbTranRow.setFieldValue("TO_STORE",issueRow.getFieldValue("TO_STORE"));
				tranQLdbTranRow.setFieldValue("TRAN_NUMBER",nTranNo);
				tranQLdbTranRow.setFieldValue("TRAN_TYPE",tranType.getFieldValue());
				tranQLdbTranRow.setFieldValue("VTI_REF",nVtiNo);
		
				tranQueueLdbTable.saveRow(tranQLdbTranRow);
				
			}
			incRow++;
		}		

		return new VtiUserExitResult();
	}
}

