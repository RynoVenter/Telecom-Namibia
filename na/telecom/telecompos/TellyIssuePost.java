package na.telecom.telecompos;

import java.util.Date;

import au.com.skytechnologies.ecssdk.thread.StableThread;
import au.com.skytechnologies.ecssdk.util.DateFormatter;
import au.com.skytechnologies.vti.VtiExitException;
import au.com.skytechnologies.vti.VtiExitLdbRequest;
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

public class TellyIssuePost extends VtiUserExit
{
	public VtiUserExitResult execute() throws VtiExitException
	{
		// Data Declarations.     
		VtiUserExitScreenField Customer = getScreenField("TELLY_NO");
		VtiUserExitScreenField Deposit = getScreenField("DEPOSIT");	  
		VtiUserExitScreenField DepositReceived = getScreenField("DEPOSIT_REC");
		VtiUserExitScreenField AmountAlloc = getScreenField("AMOUNT_ALLOC");
		VtiUserExitScreenField AmountIssued = getScreenField("AMOUNT_ISSUED");
		VtiUserExitScreenField DefaultPlant = getScreenField("DEF_PLANT");
		VtiUserExitScreenField FromStore = getScreenField("FROM_STORE");
		VtiUserExitScreenField TellyWalkStore = getScreenField("TELEWALK_STORE");
		VtiUserExitScreenField TellyPointStore = getScreenField("TELEPOINT_STORE");
		VtiUserExitScreenField UserID = getScreenField("USERID");
		VtiUserExitScreenField TellyType = getScreenField("TELLTYPE");
		VtiUserExitScreenField TellyName = getScreenField("TELLY_NAME");
		VtiUserExitScreenField ItemS = getScreenField("ITEM_S");
		VtiUserExitScreenField DescS = getScreenField("DESC_S");
		VtiUserExitScreenField LastIssDate = getScreenField("LAST_ISS_DATE");
		
		VtiUserExitScreenTable QtyIssueScrTable = getScreenTable("QTY_ISS"); 
		VtiUserExitScreenTable SearchScrTable = getScreenTable("SEARCH"); 

		VtiExitLdbTable YSPS_TRAN_QUEUE = getLocalDatabaseTable("YSPS_TRAN_QUEUE");
		VtiExitLdbTable YSPS_MVMT_HEADER = getLocalDatabaseTable("YSPS_MVMT_HEADER");
		VtiExitLdbTable YSPS_MVMT_ITEMS = getLocalDatabaseTable("YSPS_MVMT_ITEMS");
		VtiExitLdbTable YSPS_TELLY_CUST = getLocalDatabaseTable("YSPS_TELLY_CUST");
		VtiExitLdbTable YSPS_TELLY_DEP = getLocalDatabaseTable("YSPS_TELLY_DEP");

		if (Deposit == null)
			return new VtiUserExitResult(999, "Screen Field DEPOSIT does not exist");
		if (LastIssDate== null)
			return new VtiUserExitResult(999, "Screen Field LAST_ISS_DATE does not exist");
		if (DepositReceived == null)
			return new VtiUserExitResult(999, "Screen Field DEPOSIT_REC does not exist");    
		if (AmountAlloc== null)
			return new VtiUserExitResult(999, "Screen Field AMOUNT_ALLOC does not exist");
		if (DefaultPlant== null)
			return new VtiUserExitResult(999, "Screen Field DEF_PLANT does not exist");
		if (FromStore== null)
			return new VtiUserExitResult(999, "Screen Field FROM_STORE does not exist");
		if (TellyWalkStore== null)
			return new VtiUserExitResult(999, "Screen Field TELEWALK_STORE does not exist");
		if (TellyPointStore== null)
			return new VtiUserExitResult(999, "Screen Field TELEPOINT_STORE does not exist");
		if (TellyName== null)
			return new VtiUserExitResult(999, "Screen Field TELLY_NAME does not exist");
		if (ItemS== null)
			return new VtiUserExitResult(999, "Screen Field ITEM_S does not exist");
		if (DescS== null)
			return new VtiUserExitResult(999, "Screen Field DESC_S does not exist");

		if (QtyIssueScrTable== null)
			return new VtiUserExitResult(999, "Screen table QTY_ISS does not exist");
		if (SearchScrTable== null)
			return new VtiUserExitResult(999, "Screen table SEARCH does not exist");

		if (YSPS_MVMT_HEADER== null)
			return new VtiUserExitResult(999, "LDB table YSPS_MVMT_HEADER does not exist");
		if (YSPS_TRAN_QUEUE== null)
			return new VtiUserExitResult(999, "LDB table YSPS_TRAN_QUEUE does not exist");
		if (YSPS_MVMT_ITEMS== null)
			return new VtiUserExitResult(999, "LDB table YSPS_MVMT_ITEMS does not exist");
		if (YSPS_TELLY_CUST== null)
			return new VtiUserExitResult(999, "LDB table YSPS_TELLY_CUST does not exist");
		if (YSPS_TELLY_DEP== null)
			return new VtiUserExitResult(999, "LDB table YSPS_TELLY_DEP does not exist");

		if (Customer== null)
			return new VtiUserExitResult(999, "Screen table TELLY_NO does not exist");
		if (UserID== null)
			return new VtiUserExitResult(999, "Screen table USERID does not exist");


		Date currNow = new Date();
		int currDate = Integer.parseInt(DateFormatter.format("yyyyMMdd", currNow));
		int currTime = Integer.parseInt(DateFormatter.format("HHmmss", currNow));
	
		double dblAmountAlloc      	= AmountAlloc.getDoubleFieldValue();
		double dblAmountIssued     	= AmountIssued.getDoubleFieldValue();
		double dblDeposit     		= Deposit.getDoubleFieldValue();
		long   longTranNo	  		= getNextNumberFromNumberRange("YSPS_TRAN_NO");
		long   longIssNo			= getNextNumberFromNumberRange("YSPS_ORDER");
		String strServerGroup 		= getServerGroup();
		String strServerID 			= getServerId();
		String strTranType 			= "TELLY ISSUE";
		String strPlantFr 			= DefaultPlant.getFieldValue();
		String strStoreFr 			= FromStore.getFieldValue();
		String strTellyWalkStore 	= TellyWalkStore.getFieldValue();
		String strTellyPointStore 	= TellyPointStore.getFieldValue();
		String strCustomer			= Customer.getFieldValue();
		double dblTotalAlloc		= 0;
		String strUserID			= UserID.getFieldValue();
		String strTellyType         = TellyType.getFieldValue();
		String strCurrTm = new String();
		int actualQtyCount =0;
		int intItemNo = 0;
		int intTotalItems = 0;
		
		if(currTime<100000)
		{
			strCurrTm = String.valueOf(currTime);
			actualQtyCount = strCurrTm.length();

			while (actualQtyCount <6)
			{
				strCurrTm = "0" + strCurrTm ;
				actualQtyCount = strCurrTm.length();
			}

			
		}
		else
		{
			strCurrTm = String.valueOf(currTime);
		}
		


		
		//Update deposit table
		VtiExitLdbSelectCriterion[] depositSelCond =
		{
				new VtiExitLdbSelectCondition("SERVER_GROUP",
						VtiExitLdbSelectCondition.EQ_OPERATOR, strServerGroup),
					new VtiExitLdbSelectCondition("KUNNR",
						VtiExitLdbSelectCondition.EQ_OPERATOR, strCustomer),
							new VtiExitLdbSelectCondition("DEL_IND",
								VtiExitLdbSelectCondition.NE_OPERATOR, "X"),
								

		};

		VtiExitLdbSelectConditionGroup depositSelCondGrp =
			new VtiExitLdbSelectConditionGroup(depositSelCond, true);

	
		VtiExitLdbTableRow[] depositLdbRows =
			YSPS_TELLY_DEP.getMatchingRows(depositSelCondGrp);

		//if record exists, modify value
		for(int a=0; a<depositLdbRows.length; a++)
		{
			//Assign total allocated
			dblTotalAlloc = dblAmountAlloc + dblAmountIssued;
			depositLdbRows[a].setDoubleFieldValue("AMOUNT_ALLOCATED", dblTotalAlloc);
			depositLdbRows[a].setFieldValue("LAST_ISS_DATE", currDate);
			depositLdbRows[a].setFieldValue("TIMESTAMP", "");
			YSPS_TELLY_DEP.saveRow(depositLdbRows[a]);
		}
		
		//If no deposit data found, insert new record
		if(depositLdbRows.length==0)
		{
			dblTotalAlloc = dblAmountAlloc + dblAmountIssued;

			VtiExitLdbTableRow newDepositRecord = YSPS_TELLY_DEP.newRow();
			newDepositRecord.setFieldValue("SERVER_GROUP", strServerGroup);
			newDepositRecord.setFieldValue("KUNNR", strCustomer);
			newDepositRecord.setFieldValue("INITIAL_DEPOSIT", dblDeposit);
			newDepositRecord.setFieldValue("DEPOSIT_DATE", currDate);
			newDepositRecord.setFieldValue("AMOUNT_ALLOCATED", dblTotalAlloc);
			newDepositRecord.setFieldValue("LAST_ISS_DATE", currDate);
			newDepositRecord.setFieldValue("TIMESTAMP","");
			
			YSPS_TELLY_DEP.saveRow(newDepositRecord);
			
		}
		
		
				
		//Create movement header
		VtiExitLdbTableRow moveHeaderLdbRow = YSPS_MVMT_HEADER.newRow();
		
		moveHeaderLdbRow.setFieldValue("SERVERGROUP",strServerGroup);
		moveHeaderLdbRow.setFieldValue("SERVERID",strServerID);
		moveHeaderLdbRow.setFieldValue("TRANS_NO",longIssNo);
		moveHeaderLdbRow.setFieldValue("TRAN_TYPE",strTranType);
		moveHeaderLdbRow.setFieldValue("KUNNR",strCustomer);
		moveHeaderLdbRow.setFieldValue("USERID",strUserID);
		moveHeaderLdbRow.setFieldValue("CRE_DATE",currDate);
		moveHeaderLdbRow.setFieldValue("CRE_TIME",strCurrTm);
		
		YSPS_MVMT_HEADER.saveRow(moveHeaderLdbRow);
		
		
		//Create movement items
		for(int b=0; b<QtyIssueScrTable.getRowCount(); b++)
		{
			
			VtiUserExitScreenTableRow QtyIssueScrRow =QtyIssueScrTable.getRow(b);
			
			String strMaterial = QtyIssueScrRow.getFieldValue("A_ITEM");
			String strDesc = QtyIssueScrRow.getFieldValue("A_DESC");
			double dblQty = QtyIssueScrRow.getDoubleFieldValue("A_QTY");
			double dblAmt = QtyIssueScrRow.getDoubleFieldValue("A_AMOUNT");
			double dblTaxAmount = QtyIssueScrRow.getDoubleFieldValue("TAX_AMOUNT");
			double dblTotalAmount = dblAmt + dblTaxAmount;
			intItemNo+=1;
			
			VtiExitLdbTableRow moveItemLdbRows = YSPS_MVMT_ITEMS.newRow();	
			
			moveItemLdbRows.setFieldValue("SERVERGROUP",strServerGroup);
			moveItemLdbRows.setFieldValue("SERVERID",strServerID);
			moveItemLdbRows.setFieldValue("TRANS_NO",longIssNo);
			moveItemLdbRows.setFieldValue("ITEM_NO",intItemNo);
			moveItemLdbRows.setFieldValue("MATNR",strMaterial);
			moveItemLdbRows.setFieldValue("MAT_DESC",strDesc);
			moveItemLdbRows.setFieldValue("MENGE",dblQty);
			moveItemLdbRows.setFieldValue("DMBTR",dblTotalAmount);
			moveItemLdbRows.setFieldValue("PLANTFR",strPlantFr);
			moveItemLdbRows.setFieldValue("STOREFR",strStoreFr);
			
			moveItemLdbRows.setFieldValue("PLANTTO",strPlantFr);
			
			if(strTellyType.equals("TELLY WALKER"))
				moveItemLdbRows.setFieldValue("STORETO",strTellyWalkStore);
			else if(strTellyType.equals("TELLY POINT"))
				moveItemLdbRows.setFieldValue("STORETO",strTellyPointStore);
			
			moveItemLdbRows.setFieldValue("CRE_DATE",currDate);
			
			YSPS_MVMT_ITEMS.saveRow(moveItemLdbRows);
			intTotalItems += 1;
			
		}
		
		//Create transaction queue 
		
		VtiExitLdbTableRow tranQueueLDBRow = YSPS_TRAN_QUEUE.newRow();
	
		tranQueueLDBRow.setFieldValue("SERVERID",strServerID);
		tranQueueLDBRow.setFieldValue("TRAN_NUMBER",longTranNo);
		tranQueueLDBRow.setFieldValue("TRAN_TYPE",strTranType);
		tranQueueLDBRow.setFieldValue("VTI_REF",longIssNo);
		tranQueueLDBRow.setFieldValue("NO_ITEMS",intTotalItems);
		tranQueueLDBRow.setFieldValue("SERVER_GROUP",strServerGroup);

		YSPS_TRAN_QUEUE.saveRow(tranQueueLDBRow);
		
		//Clear all fields
		Customer.setFieldValue("");
		Deposit.setFieldValue("");	  
		DepositReceived.setFieldValue("");
		AmountAlloc.setFieldValue("");
		AmountIssued.setFieldValue("");
		TellyName.setFieldValue("");
		ItemS.setFieldValue("");
		DescS.setFieldValue("");
		LastIssDate.setFieldValue("");
		
		QtyIssueScrTable.clear();
		SearchScrTable.clear();
		setCursorPosition(Customer);
		DepositReceived.setDisplayOnlyFlag(true);
		
		// Trigger the uploads to SAP, if a connection is available.
	    String hostName = getHostInterfaceName();
	    boolean hostConnected = isHostInterfaceConnected(hostName);
	        
	    if (hostConnected)
	    { 
		    VtiExitLdbRequest ldbReqUploadMvmtHeader = 
		    	new VtiExitLdbRequest(YSPS_MVMT_HEADER,VtiExitLdbRequest.UPLOAD);
		    VtiExitLdbRequest ldbReqUploadMvmtItems = 
		    	new VtiExitLdbRequest(YSPS_MVMT_ITEMS,VtiExitLdbRequest.UPLOAD);
		    VtiExitLdbRequest ldbReqUploadTellyDep = 
		    	new VtiExitLdbRequest(YSPS_TELLY_DEP,VtiExitLdbRequest.UPLOAD);
		    VtiExitLdbRequest ldbReqUploadTranQueue = 
		    	new VtiExitLdbRequest(YSPS_TRAN_QUEUE,VtiExitLdbRequest.UPLOAD);
		 
	    	ldbReqUploadMvmtHeader.submit(false);
	    	ldbReqUploadMvmtItems.submit(false);
	    	ldbReqUploadTellyDep.submit(false);
	    	StableThread.snoozeCurrentThread(2000);
	    	ldbReqUploadTranQueue.submit(false);
	     
	    }
	    
		

		
		return new VtiUserExitResult(000,"Successfully posted");

	}
}